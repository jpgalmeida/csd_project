package server;

// These are the classes which receive requests from clients
import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultRecoverable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.exceptions.JedisDataException;
import resources.RequestType;

// Classes that need to be declared to implement this
// replicated Map
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class TreeMapServer extends DefaultRecoverable {

	Map<String, String> table;
	private static String configHome = "/home/csd/config/";
	private Jedis jedis;
	List<String> fields;

	public TreeMapServer(int id, String serverUri){

		table = new TreeMap<>();
		jedis=new Jedis(serverUri, 6379);
		
		new ServiceReplica(id, configHome, this, this, null, null);
		//Connecting to Redis server on localhost 

		
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
			if (reqType == RequestType.PUTSET) {
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


			} else if (reqType == RequestType.REMOVESET) {
				String key = dis.readUTF();
				
				jedis.del(key.getBytes());
				
				
				return key.getBytes();
			}else if (reqType == RequestType.WRITEELEMENT) {
				String key = dis.readUTF();
				int pos = dis.readInt();
				String new_element = dis.readUTF();
				
				if(pos < 0 || pos >= fields.size())
					return "false".getBytes();
				
				String field = fields.get(pos);
				
				System.out.println(jedis.hset(key, field, new_element));
				
				return "true".getBytes();
			}else {
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
			if (reqType == RequestType.GETSET) {
				String key = dis.readUTF();
				//String readValue = table.get(key);

				Map<String, String> att = jedis.hgetAll(key);
				
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
				
				for (Map.Entry<String, String> e : att.entrySet()){
					outputStream.write(e.getKey().getBytes(StandardCharsets.UTF_8));
					outputStream.write(",".getBytes());
					outputStream.write(e.getValue().getBytes(StandardCharsets.UTF_8));
					outputStream.write(",".getBytes());
				}

				return outputStream.toByteArray();
				
			} else if (reqType == RequestType.READELEMENT) {
				String key = dis.readUTF();
				int pos = dis.readInt();
				
				String field = fields.get(pos);
				
				String result = "";
				result = jedis.hget(key, field);
				System.out.println(result);
				
				return result.getBytes();
			} else if (reqType == RequestType.ISELEMENT) {
				String key = dis.readUTF();
				String element = dis.readUTF();
				
				for (String current_field : fields) {
				
					Object result = jedis.hget(key, current_field);
					
					if(result.toString().equalsIgnoreCase(element)) {
						System.out.println(true);
						return "true".getBytes();
					}
			
				}
				System.out.println(false);
				return "false".getBytes();
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
	@SuppressWarnings("unchecked")
	public void installSnapshot(byte[] state) {
		ByteArrayInputStream bis = new ByteArrayInputStream(state);
		try {
			ObjectInput in = new ObjectInputStream(bis);
			
			Map<String,Map<String,String>> map = (Map<String,Map<String,String>>) in.readObject();
			Set<String> l = jedis.keys("*");
			
			// delete previous map
			for( String key : l )
				jedis.del(key);
			
			// write new map
			for(String key: map.keySet()) {
				System.out.println(map.get(key));
				jedis.append(key, "");
				jedis.hmset(key, map.get(key));
			}
			
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
			
			Set<String> l = jedis.keys("*");
			Map<String, Map<String,String>> map = new HashMap<String, Map<String,String>>(); 

			for( String key : l) {
				Map<String,String> val = jedis.hgetAll(key);
				System.out.println(val);
				map.put(key,val);
			}
			
			out.writeObject(map);
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
