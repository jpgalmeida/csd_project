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


public class TreeMapServer extends DefaultRecoverable {

	private static String configHome = "/home/csd/config/";
	private Jedis jedis;
	private List<String> fields;
	private String serverUri;
	private JedisPool pool;


	public TreeMapServer(int id, String serverUri){

		this.serverUri = serverUri;
		//		JedisPool pool = new JedisPool(new JedisPoolConfig(), serverUri);
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
		//		jedis=new Jedis(serverUri, 6379);


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

//		try{
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
							att.put(key, value);
						}
					}catch(Exception e) {
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

					//				if(jedis!=null)
					//					jedis.close();


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
//					ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
//					outputStream.write(RequestType.GETSET);
//					appExecuteUnordered(outputStream.toByteArray(), msgCtx);
//					return "".getBytes();
//				}
					String key = dis.readUTF();
					//String readValue = table.get(key);

					Map<String, String> att=null;
					try{
						
						att = jedis.hgetAll(key);
						System.out.println(att.toString());
					}catch(Exception e) {
						e.printStackTrace();
					}
					
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
					if(att!=null) {	
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

					//				if(jedis!=null)
					//					jedis.close();


					return outputStream.toByteArray();

				}
				else {
					System.out.println("Unknown request type: " + reqType);
					return "".getBytes();
				}
			} catch (IOException e) {
				System.out.println("Exception reading data in the replica: " + e.getMessage());
				e.printStackTrace();
//				if(jedis!=null)
//					jedis.close();
				return null;
			}
//		}finally {
//			jedis.close();
//		}

	}

	@Override
	public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
		ByteArrayInputStream in = new ByteArrayInputStream(command);
		DataInputStream dis = new DataInputStream(in);
		int reqType;

//		try{
//			jedis=pool.getResource();



			try {
				reqType = dis.readInt();
//				if (reqType == RequestType.GETSET) {
//					String key = dis.readUTF();
//					//String readValue = table.get(key);
//
//					Map<String, String> att=null;
//					try{
//						att = jedis.hgetAll(key);
//						System.out.println(att.toString());
//					}catch(Exception e) {
//
//						e.printStackTrace();
//					}
//
//					
//					ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
//					if(att!=null) {	
//						for (Map.Entry<String, String> e : att.entrySet()){
//							outputStream.write(e.getKey().getBytes(StandardCharsets.UTF_8));
//							outputStream.write(",".getBytes());
//							outputStream.write(e.getValue().getBytes(StandardCharsets.UTF_8));
//							outputStream.write(",".getBytes());
//						}
//					}
//					else {
//						outputStream.write("".getBytes());
//					}
//
//					//				if(jedis!=null)
//					//					jedis.close();
//
//
//					return outputStream.toByteArray();
//
//				} 
				if (reqType == RequestType.READELEMENT) {
					String key = dis.readUTF();
					int pos = dis.readInt();

					String field = fields.get(pos);

					String result = "";
					result = jedis.hget(key, field);
					System.out.println(result);

					return result.getBytes();
				}else if (reqType == RequestType.SUM) {
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

				} else if (reqType == RequestType.MULT) {
					String key1 = dis.readUTF();
					String key2 = dis.readUTF();
					int pos = dis.readInt();


					String field = fields.get(pos);

					String val1 = jedis.hget(key1, field);
					String val2 = jedis.hget(key2, field);

					int mult = Integer.valueOf(val1) * Integer.valueOf(val2);
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
					outputStream.write(mult);


					return outputStream.toByteArray();

				}else if (reqType == RequestType.ISELEMENT) {
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
//		} finally {
//			jedis.close();
//		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void installSnapshot(byte[] state) {
		ByteArrayInputStream bis = new ByteArrayInputStream(state);
//		jedis=pool.getResource();
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
				//System.out.println(map.get(key));
				//jedis.append(key, "");
				jedis.hmset(key, map.get(key));
				System.out.println("=======ELEMENT ADDED=======");
			}

			in.close();
			bis.close();
//			jedis.close();
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
//			jedis=pool.getResource();

			Set<String> l = jedis.keys("*");
			Map<String, Map<String,String>> map = new HashMap<String, Map<String,String>>(); 
			System.out.println("=======GETTING SNAPSHOT=======");
			for( String key : l) {
				System.out.println("=======GETTING VALUE=======");
				Map<String,String> val = jedis.hgetAll(key);
				//System.out.println(val);
				map.put(key,val);
			}

			out.writeObject(map);
			out.flush();
			out.close();
			bos.close();
//			jedis.close();
			return bos.toByteArray();
		} catch (IOException e) {
			System.out.println("Exception when trying to take a + "
					+ "snapshot of the application state" + e.getMessage());
			e.printStackTrace();
			return new byte[0];
		}
	}
}