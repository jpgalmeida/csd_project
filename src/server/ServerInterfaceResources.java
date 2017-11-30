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
import javax.ws.rs.core.Response;

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
	public byte[] getSet(@PathParam("id") String id){
		System.out.println("Received Get Set Request");
		return getSetImplementation(id);
	}

	@POST
	@Path("/ps")
	@Consumes(MediaType.APPLICATION_JSON)
	public void putSet( Entry entry) {
		System.out.println("Received Put Set Request");
		putSetImplementation(entry.getkey(), entry.getAttributes());

	}

	@POST
	@Path("/adde/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addElement( @PathParam("id") String id) {
		System.out.println("Received Add Element Request");
		return addElementImplementation(id);
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
		System.out.println("MULT");
		return multImplementation(id1, id2, pos);
	}

	@GET
	@Path("/seq/{pos}/{val}")
	@Produces(MediaType.APPLICATION_JSON)
	public byte[] searchEq(@PathParam("pos") int pos, @PathParam("val") String val) {
		System.out.println("Received Seq Request");
		return searchEqImplementation(pos, val);
	}
	
	
	public byte[] searchEqImplementation(int pos, String val) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(out);
		
		try {
			dos.writeInt(RequestType.SEQ);
			dos.writeInt(pos);
			dos.writeUTF(val);
			
			ByteArrayInputStream in = new ByteArrayInputStream(clientProxy.invokeUnordered(out.toByteArray()));
			DataInputStream dis = new DataInputStream(in);
			
			String entries = dis.readUTF();
			
			ByteArrayOutputStream out2 = new ByteArrayOutputStream();
			DataOutputStream dos2 = new DataOutputStream(out2);
			
			dos2.writeUTF(entries);
			
			return out2.toByteArray();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		
		
		
		return null;
	}

	public byte[] sumImplementation(String key1, String key2, int pos) {
		System.out.println("Received Sum Request");
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(out);
			dos.writeInt(RequestType.SUM);
			dos.writeUTF(key1);
			dos.writeUTF(key2);
			dos.writeInt(pos);
			
			ByteArrayInputStream in = new ByteArrayInputStream(clientProxy.invokeUnordered(out.toByteArray()));
			
			DataInputStream dis = new DataInputStream(in);
			
			String res = dis.readUTF();
			
			ByteArrayOutputStream out2 = new ByteArrayOutputStream();
			DataOutputStream dos2 = new DataOutputStream(out2);
			
			dos2.writeUTF(res);
			
			return out2.toByteArray();
		}
		catch(IOException e) {
			e.printStackTrace();
			return "0".getBytes();
		}
	}

	public byte[] multImplementation(String key1, String key2, int pos) {
		System.out.println("Received Mult Request");
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(out);
			dos.writeInt(RequestType.MULT);
			dos.writeUTF(key1);
			dos.writeUTF(key2);
			dos.writeInt(pos);
			
			ByteArrayInputStream in = new ByteArrayInputStream(clientProxy.invokeUnordered(out.toByteArray()));
			
			DataInputStream dis = new DataInputStream(in);
			
			String res = dis.readUTF();
			
			ByteArrayOutputStream out2 = new ByteArrayOutputStream();
			DataOutputStream dos2 = new DataOutputStream(out2);
			
			dos2.writeUTF(res);
			
			return out2.toByteArray();
		}
		catch(IOException e) {
			e.printStackTrace();
			return "0".getBytes();
		}
	}
	
	public Response addElementImplementation(String id) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(out);
			dos.writeInt(RequestType.ADDE);
			dos.writeUTF(id);
			
			
			ByteArrayInputStream in = new ByteArrayInputStream(clientProxy.invokeOrdered(out.toByteArray()));
			DataInputStream dis = new DataInputStream(in);
			String res = dis.readUTF();

			if(res.equals("true"))
				return Response.status(204).build();
			else
				return Response.status(404).build();
		}
		catch(IOException e) {
			return Response.status(404).build();
		}
	}

	public Response removeSetImplementation(Object key) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(out);
			dos.writeInt(RequestType.REMOVESET);
			dos.writeUTF(String.valueOf(key));

			
			ByteArrayInputStream in = new ByteArrayInputStream(clientProxy.invokeOrdered(out.toByteArray()));
			DataInputStream dis = new DataInputStream(in);
			String res = dis.readUTF();
			System.out.println(res);
			if(res.equals("true"))
				return Response.status(204).build();
			else
				return Response.status(404).build();
		}
		catch (IOException e) {
			System.out.println("Exception removing entry from the hashmap: "+e.getMessage());
			return null;
		}
	}


	public byte[] getSetImplementation(String key) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(out);
			dos.writeInt(RequestType.GETSET);
			
			dos.writeUTF(key);
			byte[] reply = clientProxy.invokeOrdered(out.toByteArray());
			
			return reply;
		} catch (IOException ioe) {
			System.out.println("Exception getting value from the hashmap: " + ioe.getMessage());
			return "".getBytes();
		}
	}


	public Response putSetImplementation(String id, Map<String, String> attributes) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(out);
		try {
			dos.writeInt(RequestType.PUTSET);
			dos.writeUTF(id);
			dos.writeInt(attributes.entrySet().size());
			
			for (Map.Entry<String, String> e : attributes.entrySet()){
				dos.writeUTF(e.getKey());
				dos.writeUTF(e.getValue());
			}

			//validation
			ByteArrayInputStream in = new ByteArrayInputStream(clientProxy.invokeOrdered(out.toByteArray()));
			DataInputStream dis = new DataInputStream(in);
			if(dis.readUTF().equals("true"))
				return Response.status(204).build();
			else
				return Response.status(404).build();
		} catch (IOException ioe) {
			System.out.println("Exception putting value into hashmap: " + ioe.getMessage());
			return null;
		}
	}



}

