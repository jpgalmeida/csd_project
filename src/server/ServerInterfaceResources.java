package server;

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
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import bftsmart.tom.ServiceProxy;
import resources.RequestType;


/**
 * Implementacao do servidor em REST 
 */
@Path("/entries")
public class ServerInterfaceResources {

	public String serverUri;
	List<String> fields;
	ServiceProxy clientProxy = null;
    private static String configHome = "/home/csd/config/";
	
	
	public ServerInterfaceResources(String serverUri, int clientId) {
		this.serverUri = serverUri;

		fields=new ArrayList<String>();
		fields.add("nome");
		fields.add("idade");
		
		clientProxy = new ServiceProxy(clientId, configHome);
		
	}

	@GET
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public byte[] Entries(@PathParam("id") String id){
		System.out.println("Received GET Request!");
		
		String[] arr = fields.toArray(new String[fields.size()]);
		//return jedis.hmget(id, arr);
		byte[] res = get(id);
		String a = new String(res, StandardCharsets.UTF_8);
		System.out.println(a);
		return res;
	}

	@POST
	@Path("/ps/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void putSet( @PathParam("id") String id, Entry entry) {
		System.out.println("Received POST Request!");
		//System.out.println(jedis.hmset(id, entry.getAttributes()));
		
		put(id, entry.getAttributes());
		
	}
	
	@POST
	@Path("/adde/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void addElement( @PathParam("id") String id, String element) {
		System.out.println("Received adde Request!");
		fields.add(id);
	}
	
	@GET
	@Path("/{id}/{pos}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String readElement(@PathParam("id") String id, @PathParam("pos") int pos) {
		System.out.println("Received Read Element Request!");
		String field = fields.get(pos);
		
		//TODO client crasha quando hget retorna nil
		//result = jedis.hget(id, field);
		byte[] res = getElement(id,field);
		String a = new String(res, StandardCharsets.UTF_8);
		System.out.println(a);
		return a;
	}
	
	
	@GET
	@Path("/ie/{id}/{element}")
	@Consumes(MediaType.APPLICATION_JSON)
	public boolean isElement(@PathParam("id") String id, @PathParam("element") String element) {
		System.out.println("Received GET IsElement Request!");
		
		return checkElement(id,element);
//		for (String current_field : fields) {
//			
//			Object result = jedis.hget(id, current_field);
//			
//			if(result.toString().equalsIgnoreCase(element)) {
//				System.out.println(true);
//				return true;
//			}
		
		//}
	}

	@PUT
	@Path("/{id}/{pos}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void writeElement(@PathParam("id") String id, @PathParam("pos") int pos, String new_element) {
		String field = fields.get(pos);
		putElement(id, field, new_element);
		//System.out.println(jedis.hset(id, field, new_element));
	}

	@DELETE
	@Path("/rs/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void removeSet(@PathParam("id") String id){
		System.out.println("Received rs request!");
		remove(id);
		//System.out.println(jedis.del(id));
	}
	
	@GET
	@Path("/sum/{id1}/{id2}/{pos}")
	@Produces(MediaType.APPLICATION_JSON)
	public int sum(@PathParam("id1") String id1, @PathParam("id2") String id2, @PathParam("pos") int pos) {
		System.out.println("Received Sum Request!");
		
		String field = fields.get(pos);
		
//		int val1 = Integer.valueOf(jedis.hget(id1, field));
//		int val2 = Integer.valueOf(jedis.hget(id2, field));
		return sumOperation(id1, id2, pos);
		
//		return val1+val2;

	}
	
	@GET
	@Path("/mult/{id1}/{id2}/{pos}")
	@Produces(MediaType.APPLICATION_JSON)
	public int mult(@PathParam("id1") String id1, @PathParam("id2") String id2, @PathParam("pos") int pos) {
		System.out.println("Received Sum Request!");
		
		String field = fields.get(pos);
		
//		int val1 = Integer.valueOf(jedis.hget(id1, field));
//		int val2 = Integer.valueOf(jedis.hget(id2, field));
//		
//		return val1*val2;
		return 0;
	}
	
//	@GET
//	@Path("/sumAll/{pos}")
//	@Produces(MediaType.APPLICATION_JSON)
//	public int sumAll(@PathParam("pos") int pos) {
//		System.out.println("Received Sum Request!");
//		
//		String field = fields.get(pos);
//		
////		int val1 = Integer.valueOf(jedis.hget(id1, field));
////		int val2 = Integer.valueOf(jedis.hget(id2, field));
//		
//		return 0;
//	}

	public int sumOperation(Object key1, Object key2, Object pos) {
		try {
			//ByteArrayOutputStream out = new ByteArrayOutputStream();
			//DataOutputStream dos = new DataOutputStream(out);
			//dos.writeIn;
			return 0;
		}
		catch(IOException e) {
			return 0;
		}
	}
	
	public String putElement(Object key, Object pos, Object element) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(out);
			dos.writeInt(RequestType.WRITEELEMENT);
			dos.writeUTF(String.valueOf(pos));
			dos.writeUTF(String.valueOf(element));
			byte[] reply = clientProxy.invokeOrdered(out.toByteArray());
			if(reply != null)
				return new String(reply);
				
			return null;
		}
		catch(IOException e ) {
			System.out.println("Exception writing element to the hashmap: "+e.getMessage());
			return null;
		}
	}
	
	public boolean checkElement(Object key, Object element) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(out);
			dos.writeInt(RequestType.ISELEMENT);
			dos.writeUTF(String.valueOf(key));
			dos.writeUTF(String.valueOf(element));
			
			byte[] reply = clientProxy.invokeUnordered(out.toByteArray());
			if(reply == null)
				return false;
			
			return Boolean.valueOf(String.valueOf(reply));
		}
		catch (IOException e) {
			System.out.println("Exception checking existance of element in the hashmap: "+e.getMessage());
			return false;
		}
	}
	
	public String remove(Object key) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(out);
			dos.writeInt(RequestType.REMOVESET);
			dos.writeUTF(String.valueOf(key));
			
			byte[] reply = clientProxy.invokeOrdered(out.toByteArray());
			if(reply == null)
				return null;
			
			return new String(reply);
		}
		catch (IOException e) {
			System.out.println("Exception removing entry from the hashmap: "+e.getMessage());
			return null;
		}
	}
	
	public byte[] getElement(Object key, String pos) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(out);
			dos.writeInt(RequestType.READELEMENT);
			dos.writeUTF(String.valueOf(key));
			dos.writeUTF(pos);
			byte[] reply = clientProxy.invokeUnordered(out.toByteArray());
			
			return reply;
		}
		catch(IOException ioe) {
			System.out.println("Exception getting element from the hashmap: "+ioe.getMessage());
			return null;
		}
	}
	
    public byte[] get(Object key) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(out);
            dos.writeInt(RequestType.GET);
            dos.writeUTF(String.valueOf(key));
            byte[] reply = clientProxy.invokeUnordered(out.toByteArray());
            
            
            if(reply == null)
                return null;
            
            return reply;
        } catch (IOException ioe) {
            System.out.println("Exception getting value from the hashmap: " + ioe.getMessage());
            return null;
        }
    }
	
	
    public String put(String id, Map<String, String> attributes) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(out);
        try {
            dos.writeInt(RequestType.PUT);
            dos.writeUTF(id);
            dos.writeInt(attributes.entrySet().size());
            
            for (Map.Entry<String, String> e : attributes.entrySet()){
    		    System.out.println(e.getKey() + "/" + e.getValue());
    		    dos.writeUTF(e.getKey());
                dos.writeUTF(e.getValue());
    		}
            
            byte[] reply = clientProxy.invokeOrdered(out.toByteArray());
            
            if (reply != null) {
                String previousValue = new String(reply);
                return previousValue;
            }
            return null;
        } catch (IOException ioe) {
            System.out.println("Exception putting value into hashmap: " + ioe.getMessage());
            return null;
        }
    }
	
	
	
}

