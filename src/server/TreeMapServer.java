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
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
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
	private BigInteger nsquare, modulus, exponent;
	private HomoOpeInt ope;

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
		fields.add("salario");
		fields.add("golos");
		keysInit();
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
				HashMap<String, String> att = new HashMap<String, String>();

				try {

					for(int i=0;i<size;i++) {

						key = dis.readUTF();

						if(!fields.contains(key)) {
							throw new Exception();
						}

						String value = dis.readUTF();

						if(bizantinemode)
							att.put(key, "bizantineValue");
						else
							att.put(key, value);
					}
				}catch(Exception e) {
					System.out.println("Problem");
				}

				Map<String, String> attributes=null;
				try{
					jedis2.hmset(id, att);
					attributes = jedis2.hgetAll(key);
				}catch(Exception e) {
					System.out.println("Cast problem");
				}


				//verification if added
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

				return out.toByteArray();

			} else if (reqType == RequestType.REMOVESET) {
				System.out.println("> RECEIVED REMOVESET");

				String key = dis.readUTF();

				String att = jedis.hget(key,fields.get(0));

				if(att!=null)
					dos.writeUTF("true");
				else
					dos.writeUTF("false");

				jedis.del(key);


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

			}else if (reqType == RequestType.GETSET) {
				System.out.println("> RECEIVED GETSET");
				String key = dis.readUTF();

				Map<String, String> att=null;
				try{

					att = jedis.hgetAll(key);

				}catch(Exception e) {
					e.printStackTrace();
				}

				if(bizantinemode && att != null) {
					dos.writeUTF("123");
					dos.writeUTF(",");
					dos.writeUTF("bizantinevalue");
					dos.writeUTF(",");
					System.out.println("bizantine Sending"+dos.toString());
				}
				else if(att!=null) {
					dos.writeInt(att.entrySet().size());
					for (Map.Entry<String, String> e : att.entrySet()){

						dos.writeUTF(e.getKey());
						dos.writeUTF(e.getValue());
					}
				}else {
					dos.writeUTF("");
				}


				return out.toByteArray();
			}else if (reqType == RequestType.SBT) {
				System.out.println("> RECEIVED SBT");

				int pos = dis.readInt();
				String val = dis.readUTF();

				String field = fields.get(pos);

				Set<String> l = jedis.keys("*");
				String result = "";

//				long l1 = Long.parseLong((String) HelpSerial.fromString(val));
				long l1 = (long) HelpSerial.fromString(val);
				
				
				try{

					for( String entryKey : l ) {

						String toSearch = jedis.hget(entryKey,field);

						long l2 = (long) HelpSerial.fromString(toSearch);

						if(ope.compare(l2, l1))
							result += entryKey+",";

					}
					System.out.println(result);
					dos.writeUTF(result);

				}catch(Exception e) {
					e.printStackTrace();
				}
				return out.toByteArray();

			}else if (reqType == RequestType.SLT) {
				System.out.println("> RECEIVED SLT");

				int pos = dis.readInt();
				String val = dis.readUTF();

				String field = fields.get(pos);

				Set<String> l = jedis.keys("*");
				String result = "";

//				long l1 = Long.parseLong((String) HelpSerial.fromString(val));
				long l1 = (long) HelpSerial.fromString(val);
				
				
				try{

					for( String entryKey : l ) {

						String toSearch = jedis.hget(entryKey,field);

						long l2 = (long) HelpSerial.fromString(toSearch);
						if(ope.compare(l1, l2))
							result += entryKey+",";

					}
					System.out.println(result);
					dos.writeUTF(result);

				}catch(Exception e) {
					e.printStackTrace();
				}
				return out.toByteArray();

			

		} else {
			System.out.println("Unknown request type ordered: " + reqType);
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

			String field = fields.get(pos);

			String val1 = jedis.hget(key1, field);
			String val2 = jedis.hget(key2, field);


			BigInteger val1BigInt = (BigInteger)HelpSerial.fromString(val1);
			BigInteger val2BigInt = (BigInteger)HelpSerial.fromString(val2);

			String resultBI = HelpSerial.toString(HomoAdd.sum(val1BigInt, val2BigInt, nsquare));


			dos.writeUTF(resultBI);


			return out.toByteArray();

		}else if (reqType == RequestType.MULT) {

			System.out.println(">RECEIVED MULT");

			if(bizantinemode)
				return "-1".getBytes();

			String key1 = dis.readUTF();
			String key2 = dis.readUTF();
			int pos = dis.readInt();

			System.out.println("Mult "+key1+ " "+key2+" "+pos);
			String field = fields.get(pos);

			String val1 = jedis.hget(key1, field);
			String val2 = jedis.hget(key2, field);

			BigInteger val1BigInt = (BigInteger)HelpSerial.fromString(val1);
			BigInteger val2BigInt = (BigInteger)HelpSerial.fromString(val2);


			RSAPublicKeySpec pkSpec = new RSAPublicKeySpec(modulus, exponent);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			RSAPublicKey generatedPublic = (RSAPublicKey) kf.generatePublic(pkSpec);

			String resultBI = HelpSerial.toString(HomoMult.multiply(val1BigInt, val2BigInt, generatedPublic));


			dos.writeUTF(resultBI);


			return out.toByteArray();
		}else if (reqType == RequestType.SEQ) {
			System.out.println("> RECEIVED SEQ");

			int pos = dis.readInt();
			String val = dis.readUTF();

			String field = fields.get(pos);

			Set<String> l = jedis.keys("*");
			Map<String, String> entry = null;
			String result = "";

			try{

				for( String entryKey : l ) {

					String toSearch = jedis.hget(entryKey,field);

					if(HomoSearch.pesquisa(val, toSearch))
						result += entryKey+",";

				}
				System.out.println(result);
				dos.writeUTF(result);

			}catch(Exception e) {
				e.printStackTrace();
			}
			return out.toByteArray();

		}else {
			System.out.println("Unknown request type unordered: " + reqType);
			return null;
		}
	} catch (IOException e) {
		System.out.println("Exception reading data in the replica: " + e.getMessage());
		e.printStackTrace();
		return null;
	} catch (NoSuchAlgorithmException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return null;
	} catch (InvalidKeySpecException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return null;
	}
}

public void keysInit() {
	nsquare = new BigInteger("450114259778886752345994972925686146945175719516761522713198032574212830403533700183623261468021445078621760277779263141968015314983806128821058275802755545379938157291541399835009995569683895334890113014285060082790243552087685381383203593754939202348592721888034967877276307286156255678855763399447355959224711916549581546885085076426020478521602260395624180860696575344834409471583415670097862455826930115321719679903616216507036425955946871540061083041188261888011387921155914785756600965822823820335396455565877600925391487234095866282893990501955926369072534595727702891613849272847282083068819208706653784963946902373565121574954362690827541488261200963131888214781747205025305288855145274621870061838362489615190239392460517201952524600831191354242747305690851938059864987581083260076537564172705633914487255514251288518489875766184202330155222520855879090477482527774846732519751497906986646845790885449414100375356594455653634608031360028117692359146525986187077013576268060089986639104363108940356956657085556867213560550690477282013323735081036608396242294777772679166416552518895073108745808291336395838454225511647635991804381856099567271039293748901550303296912200005568012909142450882237697100362114845207902918991561");

	modulus = new BigInteger("130487224226764029103379992274406128521227926260527674247729988501680805927872808994651616026679464842579366924718518730473975925824458708424193507742886054685776005837909295920529709252527342539911219764516786824960672308893646723687457895142977908483514986464012078223985582254092525512451181114852568055393");

	exponent = new BigInteger("65537");

	ope = new HomoOpeInt(3447202209197703099L);
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