package server;

// These are the classes which receive requests from clients
import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultRecoverable;
import redis.clients.jedis.Jedis;
import resources.RequestType;

// Classes that need to be declared to implement this
// replicated Map
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TreeMapServer extends DefaultRecoverable {

	Map<String, String> table;
	private static String configHome = "/home/csd/config/";
	private Jedis jedis;
	List<String> fields;

	public TreeMapServer(int id, String serverUri){
		table = new TreeMap<>();
		
		new ServiceReplica(id, configHome, this, this, null, null);
		//Connecting to Redis server on localhost 
		jedis=new Jedis(serverUri, 6379);
		
		//check whether server is running or not 
		System.out.println("Redis server is running: "+jedis.ping()); 
		fields=new ArrayList<String>();
		fields.add("nome");
		fields.add("idade");
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: HashMapServer <server id>");
			System.exit(0);
		}
		
		new TreeMapServer(Integer.parseInt(args[0]), args[1]);


	}

	@Override
	public byte[][] appExecuteBatch(byte[][] command, MessageContext[] mcs) {

		byte[][] replies = new byte[command.length][];
		for (int i = 0; i < command.length; i++) {
			replies[i] = executeSingle(command[i], mcs[i]);
		}

		return replies;
	}

	private byte[] executeSingle(byte[] command, MessageContext msgCtx) {
		ByteArrayInputStream in = new ByteArrayInputStream(command);
		DataInputStream dis = new DataInputStream(in);
		int reqType;
		try {
			reqType = dis.readInt();
			if (reqType == RequestType.PUT) {
				String id = dis.readUTF();
				int size = dis.readInt();

				HashMap<String, String> att = new HashMap<String, String>();
				try {
					
				
					for(int i=0;i<size;i++) {
						String key = dis.readUTF();
						if(!fields.contains(key)) {
							throw new Exception();
						}
						String value = dis.readUTF();
						att.put(key, value);
						System.out.println("added "+key+": "+value);
					}
				}catch(Exception e) {
					System.out.println("Please add element first!");
				}

				String res = jedis.hmset(id, att);

				return res.getBytes();


			} else if (reqType == RequestType.REMOVE) {
				String key = dis.readUTF();
				String removedValue = table.remove(key);
				byte[] resultBytes = null;
				if (removedValue != null) {
					resultBytes = removedValue.getBytes();
				}
				return resultBytes;
			} else {
				System.out.println("Unknown request type: " + reqType);
				return null;
			}
		} catch (IOException e) {
			System.out.println("Exception reading data in the replica: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
		ByteArrayInputStream in = new ByteArrayInputStream(command);
		DataInputStream dis = new DataInputStream(in);
		int reqType;
		try {
			reqType = dis.readInt();
			if (reqType == RequestType.GET) {
				String key = dis.readUTF();
				//String readValue = table.get(key);

				Map<String, String> att = jedis.hgetAll(key);
				byte[] resultBytes = null;
				
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
				
				for (Map.Entry<String, String> e : att.entrySet()){
					outputStream.write(e.getKey().getBytes(StandardCharsets.UTF_8));
					outputStream.write(",".getBytes());
					outputStream.write(e.getValue().getBytes(StandardCharsets.UTF_8));
					outputStream.write(",".getBytes());
				}

				return outputStream.toByteArray();
				
			} else if (reqType == RequestType.SIZE) {
				int size = table.size();

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(out);
				dos.writeInt(size);
				byte[] sizeInBytes = out.toByteArray();

				return sizeInBytes;
			} else {
				System.out.println("Unknown request type: " + reqType);
				return null;
			}
		} catch (IOException e) {
			System.out.println("Exception reading data in the replica: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void installSnapshot(byte[] state) {
		ByteArrayInputStream bis = new ByteArrayInputStream(state);
		try {
			ObjectInput in = new ObjectInputStream(bis);
			table = (Map<String, String>) in.readObject();
			in.close();
			bis.close();
		} catch (ClassNotFoundException e) {
			System.out.print("Coudn't find Map: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.print("Exception installing the application state: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public byte[] getSnapshot() {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			out.writeObject(table);
			out.flush();
			out.close();
			bos.close();
			return bos.toByteArray();
		} catch (IOException e) {
			System.out.println("Exception when trying to take a + "
					+ "snapshot of the application state" + e.getMessage());
			e.printStackTrace();
			return new byte[0];
		}
	}
}
