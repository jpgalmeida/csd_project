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

	public int multImplementation(String key1, String key2, int pos) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(out);
			dos.writeInt(RequestType.MULT);
			dos.writeUTF(key1);
			dos.writeUTF(key2);
			dos.writeInt(pos);
			System.out.println("Mult "+key1+ " "+key2+" "+pos);
			ByteArrayInputStream in = new ByteArrayInputStream(clientProxy.invokeUnordered(out.toByteArray()));
			DataInputStream dis = new DataInputStream(in);
			int res = dis.read();

			return res;
		}
		catch(IOException e) {
			return 0;
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

	public Response writeElementImplementation(String key, int pos, String element) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(out);
			dos.writeInt(RequestType.WRITEELEMENT);
			dos.writeUTF(key);
			dos.writeInt(pos);
			dos.writeUTF(element);
			
			ByteArrayInputStream in = new ByteArrayInputStream(clientProxy.invokeOrdered(out.toByteArray()));
			DataInputStream dis = new DataInputStream(in);
			String res = dis.readUTF();

			if(res.equals("true"))
				return Response.status(204).build();
			else
				return Response.status(404).build();
		}
		catch(IOException e ) {
			System.out.println("Exception writing element to the hashmap: "+e.getMessage());
			return Response.status(404).build();
		}
	}

	public boolean isElementImplementation(Object key, Object element) {
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

	public byte[] readElementImplementation(Object key, int pos) {
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

	public byte[] getSetImplementation(Object key) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(out);
			dos.writeInt(RequestType.GETSET);
			dos.writeUTF(String.valueOf(key));
//			byte[] reply = clientProxy.invokeUnordered(out.toByteArray());
			byte[] reply = clientProxy.invokeOrdered(out.toByteArray());
			System.out.println(new String(reply, StandardCharsets.UTF_8));
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

