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
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Random;

public class TreeMapServer extends DefaultRecoverable {

	private static String configHome = "/home/csd/config/";
	private Jedis jedis;
	private List<String> fields;
	private JedisPool pool;
	private static boolean bizantine = false;
	private static String bizantineValue = "";
	private static int bizantineCertainty = 0; // how likely a fault is to happen

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

		if(args.length>2) {
			bizantine = true;
			bizantineValue = args[2];
			bizantineCertainty = Integer.parseInt(args[3]);	// value from 0 to 100
		}
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
				String id = dis.readUTF();
				int size = dis.readInt();
				String key="";
				HashMap<String, String> att = new HashMap<String, String>();
				try {

					for(int i=0;i<size;i++) {
						key = dis.readUTF();
						if(!fields.contains(key)) {
							throw new Exception();
						}
						String value = dis.readUTF();
						if(bizantine) {
							// decide if adding wrong value
							att.put(key, bizantineValue);
						}
						else {
							att.put(key, value);							
						}

					}
					
				} catch(Exception e) {
					System.out.println("Please add element first!");
				}

				Map<String, String> attributes=null;
				try{
					jedis2.hmset(id, att);
					attributes = jedis2.hgetAll(key);
				}catch(Exception e) {
					System.out.println("Cast problem");
				}

				String toWrite = "true";
				if(attributes!=null) {
					for (Map.Entry<String, String> e : attributes.entrySet()){
						if(!att.get(e.getKey()).equals(e.getValue())){
							toWrite="false";
							break;
						}
					}
				}

				dos.writeUTF(toWrite);
				out.toByteArray();

			} else if (reqType == RequestType.REMOVESET) {
				String key = dis.readUTF();
				String att = jedis.hget(key,fields.get(0));

				if(att!=null)
					dos.writeUTF("true");
				else
					dos.writeUTF("false");
				
				// check exception
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

				String key = dis.readUTF();

				Map<String, String> att=null;
				try{

					att = jedis.hgetAll(key);
					
				}catch(Exception e) {
					e.printStackTrace();
				}

				ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
				if(checkBizantineFault() && att != null) {
					outputStream.write("123".getBytes());
					outputStream.write(",".getBytes());
					outputStream.write("bizantinevalue".getBytes());
					outputStream.write(",".getBytes());
					System.out.println("bizantine Sending"+outputStream.toString());
				}
				else if(att!=null) {	
					for (Map.Entry<String, String> e : att.entrySet()){
						outputStream.write(e.getKey().getBytes(StandardCharsets.UTF_8));
						outputStream.write(",".getBytes());
						outputStream.write(e.getValue().getBytes(StandardCharsets.UTF_8));
						outputStream.write(",".getBytes());
					}
				}
				else {
					outputStream.write("".getBytes());
				}

				return outputStream.toByteArray();

			}else if (reqType == RequestType.ISELEMENT) {
				
				if(checkBizantineFault())
					return bizantineValue.getBytes();
				
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
				
			} if (reqType == RequestType.READELEMENT) {
				if(checkBizantineFault())
					return bizantineValue.getBytes();
				
				String key = dis.readUTF();
				int pos = dis.readInt();
				
				String field = fields.get(pos);
				String result = "";
				result = jedis.hget(key, field);
				System.out.println(result);

				return result.getBytes();
				
			} else if (reqType == RequestType.MULT) {
				
				if(checkBizantineFault())
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


		try {
			reqType = dis.readInt();
			
			if (reqType == RequestType.SUM) {
				
				if(checkBizantineFault())
					return "-1".getBytes();
				
				String key1 = dis.readUTF();
				String key2 = dis.readUTF();
				int pos = dis.readInt();


				String field = fields.get(pos);

				String val1 = jedis.hget(key1, field);
				String val2 = jedis.hget(key2, field);

				int sum = Integer.valueOf(val1) + Integer.valueOf(val2);
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
				outputStream.write(sum);


				return outputStream.toByteArray();

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
	
	private boolean checkBizantineFault() {
		if( !bizantine ) return false;
		
		Random rand = new Random();
		int n = rand.nextInt(100);
		
		if(n < bizantineCertainty)
			return true;
		
		return false;
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