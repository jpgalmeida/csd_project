package server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import resources.Entry;
import resources.RequestType;


/**
 * Implementacao do servidor em REST 
 */
@Path("/entries")
public class ServerInterfaceResources {

	public String serverUri;
	ServiceProxy clientProxy = null;
    private static String configHome = "/home/csd/config/";
	
	
	public ServerInterfaceResources(String serverUri, int clientId) {
		this.serverUri = serverUri;
		
		clientProxy = new ServiceProxy(clientId, configHome);
		
	}

	@GET
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public byte[] Entries(@PathParam("id") String id){
		System.out.println("Received GET Request!");
		
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
	}
	
	@GET
	@Path("/{id}/{pos}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String readElement(@PathParam("id") String id, @PathParam("pos") int pos) {
		System.out.println("Received Read Element Request!");

		//result = jedis.hget(id, field);
		byte[] res = getElement(id,pos);
		String a = new String(res, StandardCharsets.UTF_8);
		return a;
	}
	
	
	@GET
	@Path("/ie/{id}/{element}")
	@Consumes(MediaType.APPLICATION_JSON)
	public boolean isElement(@PathParam("id") String id, @PathParam("element") String element) {
		System.out.println("Received GET IsElement Request!");
		
		return checkElement(id,element);
	}

	@PUT
	@Path("/{id}/{pos}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void writeElement(@PathParam("id") String id, @PathParam("pos") int pos, String new_element) {
		putElement(id, pos, new_element);
	}

	@DELETE
	@Path("/rs/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void removeSet(@PathParam("id") String id){
		System.out.println("Received rs request!");
		remove(id);
	}
	
	@GET
	@Path("/sum/{id1}/{id2}/{pos}")
	@Produces(MediaType.APPLICATION_JSON)
	public int sum(@PathParam("id1") String id1, @PathParam("id2") String id2, @PathParam("pos") int pos) {
		System.out.println("Received Sum Request!");
		
		return sumOperation(id1, id2, pos);

	}
	
	@GET
	@Path("/mult/{id1}/{id2}/{pos}")
	@Produces(MediaType.APPLICATION_JSON)
	public int mult(@PathParam("id1") String id1, @PathParam("id2") String id2, @PathParam("pos") int pos) {
		System.out.println("Received Mult Request!");
		
		return multOperation(id1, id2, pos);
	}
	
	
	public int sumOperation(String key1, String key2, int pos) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(out);
			dos.writeInt(RequestType.SUM);
			dos.writeUTF(key1);
			dos.writeUTF(key2);
			dos.writeInt(pos);
			
			ByteArrayInputStream in = new ByteArrayInputStream(clientProxy.invokeUnordered(out.toByteArray()));
			DataInputStream dis = new DataInputStream(in);
			int res = dis.read();
			
			return res;
		}
		catch(IOException e) {
			return 0;
		}
	}
	
	public int multOperation(String key1, String key2, int pos) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(out);
			dos.writeInt(RequestType.MULT);
			dos.writeUTF(key1);
			dos.writeUTF(key2);
			dos.writeInt(pos);
			
			ByteArrayInputStream in = new ByteArrayInputStream(clientProxy.invokeUnordered(out.toByteArray()));
			DataInputStream dis = new DataInputStream(in);
			int res = dis.read();
			
			return res;
		}
		catch(IOException e) {
			return 0;
		}
	}
	
	public int putElement(String key, int pos, String element) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(out);
			dos.writeInt(RequestType.WRITEELEMENT);
			dos.writeUTF(key);
			dos.writeInt(pos);
			dos.writeUTF(element);
			byte[] reply = clientProxy.invokeOrdered(out.toByteArray());
			
			System.out.println(new String(reply));
			String res = new String(reply);
			
			if(res.equals("true"))
				return 200;
				
			return 404;
		}
		catch(IOException e ) {
			System.out.println("Exception writing element to the hashmap: "+e.getMessage());
			return 404;
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
			
			boolean res = new String(reply).equals("true");
			
			return res;
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
	
	public byte[] getElement(Object key, int pos) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(out);
			dos.writeInt(RequestType.READELEMENT);
			dos.writeUTF(String.valueOf(key));
			dos.writeInt(pos);
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
            dos.writeInt(RequestType.GETSET);
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
            dos.writeInt(RequestType.PUTSET);
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

