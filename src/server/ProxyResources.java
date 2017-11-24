package server;

import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
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
	
	
	public ProxyResources(String serverUri) {
		this.serverUri = serverUri;
		
		//Server connection
		serverURI = UriBuilder.fromUri(serverUri).port(11100).build();
		client = ClientBuilder.newBuilder().hostnameVerifier(new InsecureHostnameVerifier())
				.build();
		target = client.target( serverURI );
	
		
		//Homomorphic Init
		pk = HomoAdd.generateKey();

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
	@Path("/ps/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void putSet( @PathParam("id") String id, Entry entry) {
		System.out.println("Received Put Set Request");
		putSetImplementation(id, entry.getAttributes());

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
	public int sum(@PathParam("id1") String id1, @PathParam("id2") String id2, @PathParam("pos") int pos) {

		return sumImplementation(id1, id2, pos);

	}

	@GET
	@Path("/mult/{id1}/{id2}/{pos}")
	@Produces(MediaType.APPLICATION_JSON)
	public int mult(@PathParam("id1") String id1, @PathParam("id2") String id2, @PathParam("pos") int pos) {

		return multImplementation(id1, id2, pos);
	}


	public int sumImplementation(String key1, String key2, int pos) {
		
		
		int response = target.path("/entries/sum/"+key1+"/"+key2+"/"+pos+"/"+pk.getNsquare().toString())
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<Integer>(){});
		
		BigInteger bi = new BigInteger(String.valueOf(response));
		
		
		response = homoSumDecryption(bi).intValue();
		return response;
	}

	public int multImplementation(String key1, String key2, int pos) {
		return 0;
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

	public byte[] getSetImplementation(Object key) {
		
		byte[] response = target.path("/entries/"+key)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<byte[]>() {});

		String res = new String(response, StandardCharsets.UTF_8);
		String[] parsedString = res.split(" ");
		String encryptedAge = "";
		for(int i = 0; i<parsedString.length;i++ ) {
			if(parsedString[i].equals("idade")) {
				encryptedAge = parsedString[i+1];
				System.out.println("FOUND AGE");
				
				break;
			}
		}
		
		BigInteger ageBigInt = homoSumDecryption(new BigInteger(encryptedAge));
		System.out.println("DECAGE "+ageBigInt);
		res.replaceAll(encryptedAge, ageBigInt.toString());
		
		return res.getBytes();
	}


	public Response putSetImplementation(String id, Map<String, String> attributes) {

		Entry entry = new Entry(id, attributes);

		Map<String, String> hm = entry.getAttributes();
		
		String age = hm.get("idade");
		byte[] ageEncrypted = homoSumEncryption(age);
		hm.remove("idade");
		hm.put("idade", new String(ageEncrypted, StandardCharsets.UTF_8));
		entry.setAttributes(hm);
		
		Response response = target.path("/entries/ps/"+id)
				.request()
				.post( Entity.entity(entry, MediaType.APPLICATION_JSON));
		
		return response;
	}
	
	public String homoSearch(String name) {
		
		SecretKey key = homolib.HomoSearch.generateKey();
		
		return HomoSearch.encrypt(key, name);
		
	}
	
	public byte[] homoSumEncryption(String n1) {
		
		BigInteger big1 = new BigInteger(n1);

		try {
			return HomoAdd.encrypt(big1, pk).toByteArray();
		} catch (Exception e) {
			System.out.println("> Erro Sum encryption!");
		}
		return null;
		
	}
	
	public BigInteger homoSumDecryption(BigInteger big3Code) {
		
		try {
			return HomoAdd.decrypt(big3Code, pk);
		} catch (Exception e) {
			System.out.println("> Erro Sum decryption!");
		}
		
		//se for necessario retornar -1 ou assim
		return null;
	}



}

