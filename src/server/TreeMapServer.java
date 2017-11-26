package server;

// These are the classes which receive requests from clients
import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultRecoverable;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
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
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import homolib.*;


public class TreeMapServer extends DefaultRecoverable {

	private static String configHome = "/home/csd/config/";
	private Jedis jedis;
	private List<String> fields;
	private JedisPool pool;
	private static boolean bizantinemode = false;

	public TreeMapServer(int id, String serverUri){

		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(1024);
		config.setMaxWaitMillis(30000);
		config.setMaxIdle(512);

		config.setMinIdle(1);
		config.setTestOnBorrow(true);
		config.setTestOnReturn(true);
		config.setTestWhileIdle(true);
		config.setNumTestsPerEvictionRun(10);
		config.setTimeBetweenEvictionRunsMillis(60000);
		pool = new JedisPool(config, serverUri, 6379);

		jedis=pool.getResource();


		new ServiceReplica(id, configHome, this, this, null, null);
		//Connecting to Redis server on localhost 


		//check whether server is running or not 
		System.out.println("Redis server is running: "+jedis.ping()); 
		fields=new ArrayList<String>();
		fields.add("nome");
		fields.add("idade");
		fields.add("morada");
		fields.add("telefone");
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: HashMapServer <server id>");
			System.exit(0);
		}

		new TreeMapServer(Integer.parseInt(args[0]), args[1]);

		if(args.length>2)
			bizantinemode = true;
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

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(out);

		Jedis jedis2=pool.getResource();


		int reqType;
		try {
			reqType = dis.readInt();
			if (reqType == RequestType.PUTSET) {
				System.out.println("> RECEIVED PUTSET");
				
				String id = dis.readUTF();
				
				int size = dis.readInt();
				
				String key = "";
				HashMap<byte[], byte[]> att = new HashMap<byte[], byte[]>();
				
				try {

					for(int i=0;i<size;i++) {
						
						key = dis.readUTF();
						
						if(!fields.contains(key)) {
							throw new Exception();
						}
						
						int valueSize = dis.readInt();
						
						byte[] value = new byte[valueSize];
						dis.read(value, 0, valueSize);
						
						if(bizantinemode)
							att.put(key.getBytes(), "bizantineValue".getBytes());
						else
							att.put(key.getBytes(), value);
					}
				}catch(Exception e) {
					System.out.println("Problem");
				}
				
				Map<byte[], byte[]> attributes=null;
				try{
					jedis2.hmset(id.getBytes(), att);
					attributes = jedis2.hgetAll(key.getBytes());
				}catch(Exception e) {
					System.out.println("Cast problem");
				}
				
				
				//verification if added
				String toWrite = "true";
				if(attributes!=null) {
					for (Map.Entry<byte[], byte[]> e : attributes.entrySet()){
						if(!att.get(e.getKey()).equals(e.getValue())){
							toWrite="false";
							break;
						}

					}
				}

				dos.writeUTF(toWrite);

				return out.toByteArray();

			} else if (reqType == RequestType.REMOVESET) {
				String key = dis.readUTF();

				String att = jedis.hget(key,fields.get(0));

				if(att!=null)
					dos.writeUTF("true");
				else
					dos.writeUTF("false");

				jedis.del(key.getBytes());


				return out.toByteArray();
			}else if (reqType == RequestType.WRITEELEMENT) {
				String key = dis.readUTF();
				int pos = dis.readInt();
				String new_element = dis.readUTF();

				if(pos < 0 || pos >= fields.size()) {
					dos.writeUTF("false");
					return out.toByteArray();
				}

				String field = fields.get(pos);
				System.out.println(jedis.hset(key, field, new_element));

				dos.writeUTF("true");
				return out.toByteArray();
			}else if (reqType == RequestType.ADDE) {
				String key = dis.readUTF();

				if(fields.contains(key))
					dos.writeUTF("false");
				else {
					fields.add(key);
					dos.writeUTF("true");
				}
				return out.toByteArray();
			}
			else if (reqType == RequestType.GETSET) {
				System.out.println("> RECEIVED GETSET");
				String key = dis.readUTF();
				
				Map<byte[], byte[]> att=null;
				try{

					att = jedis.hgetAll(key.getBytes());
					
				}catch(Exception e) {
					e.printStackTrace();
				}

				if(bizantinemode && att != null) {
					dos.write("123".getBytes());
					dos.write(",".getBytes());
					dos.write("bizantinevalue".getBytes());
					dos.write(",".getBytes());
					System.out.println("bizantine Sending"+dos.toString());
				}
				else if(att!=null) {
					dos.writeInt(att.entrySet().size());
					for (Map.Entry<byte[], byte[]> e : att.entrySet()){
						dos.writeInt(e.getKey().length);
						dos.write(e.getKey());
						dos.writeInt(e.getValue().length);
						dos.write(e.getValue());
					}
				}
				else {
					dos.write("".getBytes());
				}

				return out.toByteArray();

			}else if (reqType == RequestType.ISELEMENT) {
				
				if(bizantinemode)
					return "bizantineValue".getBytes();
				
				String key = dis.readUTF();
				String element = dis.readUTF();

				for (String current_field : fields) {

					Object result = jedis.hget(key, current_field);

					if(result!=null)
						if(result.toString().equalsIgnoreCase(element)) {
							System.out.println(true);
							return "true".getBytes();
						}

				}
				System.out.println(false);
				return "false".getBytes();
				
			} if (reqType == RequestType.READELEMENT) {
				if(bizantinemode)
					return "bizantineValue".getBytes();
				
				
				String key = dis.readUTF();
				int pos = dis.readInt();
				
				String field = fields.get(pos);
				String result = "";
				result = jedis.hget(key, field);
				System.out.println(result);

				if(result != null)
					return result.getBytes();
				return null;
				
			} else if (reqType == RequestType.MULT) {
				
				if(bizantinemode)
					return "-1".getBytes();
				
				String key1 = dis.readUTF();
				String key2 = dis.readUTF();
				int pos = dis.readInt();

				System.out.println("Mult "+key1+ " "+key2+" "+pos);
				String field = fields.get(pos);

				String val1 = jedis.hget(key1, field);
				String val2 = jedis.hget(key2, field);

				int mult = Integer.valueOf(val1) * Integer.valueOf(val2);
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
				outputStream.write(mult);


				return outputStream.toByteArray();

			} else {
				System.out.println("Unknown request type: " + reqType);
				return "".getBytes();
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
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(out);

		try {
			reqType = dis.readInt();
			
			if (reqType == RequestType.SUM) {
				
				if(bizantinemode)
					return "-1".getBytes();
				
				String key1 = dis.readUTF();
				String key2 = dis.readUTF();
				int pos = dis.readInt();
				String encKey = dis.readUTF();
				
				String field = fields.get(pos);

				String val1 = jedis.hget(key1, field);
				String val2 = jedis.hget(key2, field);

				
				BigInteger val1BigInt = new BigInteger(val1.getBytes());
				BigInteger val2BigInt = new BigInteger(val2.getBytes());
				
				BigInteger encKeyBigInt = new BigInteger(encKey);
				
				
				byte[] result = HomoAdd.sum(val1BigInt, val2BigInt, encKeyBigInt).toByteArray();
				int resultSize = result.length;
				
				dos.writeInt(resultSize);
				dos.write(result);


				return out.toByteArray();

			}  else {
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
			System.out.println("=======INSTALLING SNAPSHOT=======");
			// write new map
			if(map.size()>0)
				System.out.println("=======I HAVE THINGS TO WRITE=======");

			for(String key: map.keySet()) {
				jedis.hmset(key, map.get(key));
				System.out.println("=======ELEMENT ADDED=======");
			}

			in.close();
			bis.close();
		} catch (ClassNotFoundException e) {
			//			System.out.print("Coudn't find Map: " + e.getMessage());
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
			System.out.println("=======GETTING SNAPSHOT=======");
			for( String key : l) {
				System.out.println("=======GETTING VALUE=======");
				Map<String,String> val = jedis.hgetAll(key);
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