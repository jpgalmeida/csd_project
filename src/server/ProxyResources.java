package server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import client.ClientInterface.InsecureHostnameVerifier;
import homolib.*;
import resources.Entry;
import resources.KeySaver;


/**
 * Implementacao do servidor em REST 
 */
@Path("/entries")
public class ProxyResources {

	public String serverUri;


	private static Client client;
	private static URI serverURI;
	private static WebTarget target;
	private PaillierKey pk;
	private KeySaver ks;
	private KeyPair keyPair;
	private RSAPublicKey publicKey;
	private RSAPrivateKey privateKey;
	private String pkSer;


	public ProxyResources(String serverUri) {
		this.serverUri = serverUri;

		//Server connection
		serverURI = UriBuilder.fromUri(serverUri).port(11100).build();
		client = ClientBuilder.newBuilder().hostnameVerifier(new InsecureHostnameVerifier())
				.build();
		target = client.target( serverURI );


		//Homomorphic Init
		pk = HomoAdd.generateKey();
		keyPair = HomoMult.generateKey();
		publicKey = (RSAPublicKey) keyPair.getPublic();
		privateKey = (RSAPrivateKey) keyPair.getPrivate();
		
		pkSer = HelpSerial.toString(pk.getNsquare());d

	}

	@POST
	@Path("/ps")
	@Consumes(MediaType.APPLICATION_JSON)
	public void putSet( Entry entry) {
		System.out.println("Received Put Set Request");
		putSetImplementation(entry.getkey(), entry.getAttributes());

	}


	@GET
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public byte[] getSet(@PathParam("id") String id){
		System.out.println("Received Get Set Request");
		return getSetImplementation(id);
	}


	@POST
	@Path("/adde/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addElement( @PathParam("id") String id) {
		System.out.println("Received Add Element Request");
		return addElementImplementation(id);
	}

	@GET
	@Path("/{id}/{pos}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String readElement(@PathParam("id") String id, @PathParam("pos") int pos) {
		System.out.println("Received Read Element Request");

		byte[] res = readElementImplementation(id,pos);
		String a = new String(res, StandardCharsets.UTF_8);
		return a;
	}


	@GET
	@Path("/ie/{id}/{element}")
	@Consumes(MediaType.APPLICATION_JSON)
	public boolean isElement(@PathParam("id") String id, @PathParam("element") String element) {
		System.out.println("Received Is Element Request");
		return isElementImplementation(id,element);
	}

	@PUT
	@Path("/{id}/{pos}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response writeElement(@PathParam("id") String id, @PathParam("pos") int pos, String new_element) {
		System.out.println("Received Write Element Request");
		return writeElementImplementation(id, pos, new_element);
	}

	@DELETE
	@Path("/rs/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response removeSet(@PathParam("id") String id){
		System.out.println("Received Remove Set Request");
		return removeSetImplementation(id);
	}

	@GET
	@Path("/sum/{id1}/{id2}/{pos}")
	@Produces(MediaType.APPLICATION_JSON)
	public byte[] sum(@PathParam("id1") String id1, @PathParam("id2") String id2, @PathParam("pos") int pos) {
		System.out.println("Received Sum Request");
		return sumImplementation(id1, id2, pos);

	}

	@GET
	@Path("/mult/{id1}/{id2}/{pos}")
	@Produces(MediaType.APPLICATION_JSON)
	public byte[] mult(@PathParam("id1") String id1, @PathParam("id2") String id2, @PathParam("pos") int pos) {
		System.out.println("Received Mult Request");
		return multImplementation(id1, id2, pos);
	}


	public byte[] sumImplementation(String key1, String key2, int pos) {

		byte[] response = target.path("/entries/sum/"+key1+"/"+key2+"/"+pos+"/"+pk.getNsquare())
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<byte[]>(){});

		ByteArrayInputStream in = new ByteArrayInputStream(response);
		DataInputStream res = new DataInputStream(in);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(out);
		
		byte[] result = null;
		byte[] resultDecrypted = null;
		try {
			int size = res.readInt();
			result = new byte[size];
			
			res.read(result, 0, size);
			
			if (result[0] == 0) {
			    byte[] tmp = new byte[result.length - 1];
			    System.arraycopy(result, 1, tmp, 0, tmp.length);
			    result = tmp;
			}
			
			BigInteger resultDec = homoSumDecryption(result);
			resultDecrypted = resultDec.toByteArray();
			
			
			dos.writeUTF(new String(resultDecrypted));
			System.out.println("RES: " + resultDec.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(response==null)
			System.out.println("decription nao deu");

		return out.toByteArray();
		
	}

	public byte[] multImplementation(String key1, String key2, int pos) {
		
		BigInteger modulus = publicKey.getModulus();
		//String modString = HelpSerial.toString(modulus);
		BigInteger pubExp = publicKey.getPublicExponent();
		//String expString = HelpSerial.toString(pubExp);
		byte[] response = target.path("/entries/mult/"+key1+"/"+key2+"/"+pos+"/"+modulus+"/"+pubExp)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<byte[]>(){});

		ByteArrayInputStream in = new ByteArrayInputStream(response);
		DataInputStream res = new DataInputStream(in);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(out);
		
		byte[] result = null;
		byte[] resultDecrypted = null;
		try {
			int size = res.readInt();
			result = new byte[size];
			
			res.read(result, 0, size);
			
			if (result[0] == 0) {
				System.out.println("Entri");
			    byte[] tmp = new byte[result.length - 1];
			    System.arraycopy(result, 1, tmp, 0, tmp.length);
			    result = tmp;
			}
			
			BigInteger resultDec = homoMultDecryption(result);
			resultDecrypted = resultDec.toByteArray();
			
			
			
			dos.writeUTF(new String(resultDecrypted));
			System.out.println("RES: " + resultDec.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(response==null)
			System.out.println("decription nao deu");

		return out.toByteArray();
		
	}

	public Response addElementImplementation(String id) {
		return null;
	}

	public Response writeElementImplementation(String key, int pos, String element) {
		return null;
	}

	public boolean isElementImplementation(Object key, Object element) {
		return false;
	}

	public Response removeSetImplementation(Object key) {
		return null;
	}

	public byte[] readElementImplementation(Object key, int pos) {
		return null;
	}

	public byte[] getSetImplementation(String key) {

		byte[] response = target.path("/entries/"+key)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<byte[]>() {});

		ByteArrayInputStream in = new ByteArrayInputStream(response);
		DataInputStream res = new DataInputStream(in);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(out);
		
		int attSize;
		try {
			attSize = res.readInt();
			dos.writeInt(attSize);
			
			for(int i = 0; i < attSize; i++) {
				int keySize = res.readInt();
				byte[] keyRead = new byte[keySize];
				res.read(keyRead, 0, keySize);
				
				int valueSize = res.readInt();
				byte[] valueRead = new byte[valueSize];
				res.read(valueRead, 0, valueSize);
				
				String keyString = new String(keyRead, StandardCharsets.UTF_8);
				
				if(keyString.equals("idade")) {
					
					BigInteger ageBigInt = homoSumDecryption(valueRead);
					
					valueRead = ageBigInt.toByteArray();
					if (valueRead[0] == 0) {
					    byte[] tmp = new byte[valueRead.length - 1];
					    System.arraycopy(valueRead, 1, tmp, 0, tmp.length);
					    valueRead = tmp;
					}
					
				}
				String k = new String(keyRead, StandardCharsets.UTF_8);
				String v = new String(valueRead, StandardCharsets.UTF_8);
				
				dos.writeUTF(k);
				dos.writeUTF(v);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return out.toByteArray();
	}


	public Response putSetImplementation(String id, Map<String, byte[]> attributes) {

		Entry entry = new Entry(id, attributes);

		Map<String, byte[]> hm = entry.getAttributes();

		byte[] age = hm.get("idade");
		
		byte[] ageEncrypted = homoSumEncryption(age);
		
		hm.remove("idade");
		hm.put("idade", ageEncrypted);
		entry.setAttributes(hm);
		
		
		byte[] salary = hm.get("salary");
		
		byte[] salaryEncrypted = homoMultEncryption(salary);
		
		hm.remove("salary");
		hm.put("salary", salaryEncrypted);
		entry.setAttributes(hm);

		Response response = target.path("/entries/ps/")
				.request()
				.post( Entity.entity(entry, MediaType.APPLICATION_JSON));


		return response;
	}

	public String homoSearch(String name) {

		SecretKey key = homolib.HomoSearch.generateKey();

		return HomoSearch.encrypt(key, name);

	}

	public byte[] homoSumEncryption(byte[] n1) {
		
		BigInteger big1 = new BigInteger(n1);
		
		try {
			return HomoAdd.encrypt(big1, pk).toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("> Erro Sum encryption!");
		}
		return null;

	}

	public BigInteger homoSumDecryption(byte[] n1) {
		
		BigInteger big1 = new BigInteger(n1);
		
		try {
			return HomoAdd.decrypt(big1, pk);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("> Erro Sum decryption!");
		}

		//se for necessario retornar -1 ou assim
		return new BigInteger("0");
	}
	
	public byte[] homoMultEncryption(byte[] n1) {
		
		BigInteger big1 = new BigInteger(n1);
		
		try {
			return HomoMult.encrypt(publicKey, big1).toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("> Erro Sum encryption!");
		}
		return null;

	}

	public BigInteger homoMultDecryption(byte[] n1) {
		
		BigInteger big1 = new BigInteger(n1);
		
		try {
			return HomoMult.decrypt(privateKey, big1);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("> Erro Sum decryption!");
		}

		//se for necessario retornar -1 ou assim
		return new BigInteger("0");
	}
	

}

