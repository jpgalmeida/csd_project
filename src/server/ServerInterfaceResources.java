package server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
		return getSetImplementation(id);
	}

	@POST
	@Path("/ps")
	@Consumes(MediaType.APPLICATION_JSON)
	public void putSet( Entry entry) {
		putSetImplementation(entry.getkey(), entry.getAttributes());

	}

	@POST
	@Path("/adde/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addElement( @PathParam("id") String id) {
		return addElementImplementation(id);
	}

	@DELETE
	@Path("/rs/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response removeSet(@PathParam("id") String id){
		return removeSetImplementation(id);
	}

	@GET
	@Path("/sum/{id1}/{id2}/{pos}")
	@Produces(MediaType.APPLICATION_JSON)
	public byte[] sum(@PathParam("id1") String id1, @PathParam("id2") String id2, @PathParam("pos") int pos) {
		return sumImplementation(id1, id2, pos);

	}

	@GET
	@Path("/mult/{id1}/{id2}/{pos}")
	@Produces(MediaType.APPLICATION_JSON)
	public byte[] mult(@PathParam("id1") String id1, @PathParam("id2") String id2, @PathParam("pos") int pos) {
		return multImplementation(id1, id2, pos);
	}

	@POST
	@Path("/seq/{pos}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchEq(@PathParam("pos") int pos, String val) {
		return searchEqImplementation(pos, val);
	}
	
	@POST
	@Path("/sbt/{pos}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchBt(@PathParam("pos") int pos, String val) {
		return searchBtImplementation(pos, val);
	}
	
	@POST
	@Path("/slt/{pos}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchLt(@PathParam("pos") int pos, String val) {
		return searchLtImplementation(pos, val);
	}
	
	public Response searchLtImplementation(int pos, String val) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(out);
		
		try {
			dos.writeInt(RequestType.SLT);
			dos.writeInt(pos);
			dos.writeUTF(val);
			
			ByteArrayInputStream in = new ByteArrayInputStream(clientProxy.invokeOrdered(out.toByteArray()));
			DataInputStream dis = new DataInputStream(in);
			
			String entries = dis.readUTF();

			ByteArrayOutputStream out2 = new ByteArrayOutputStream();
			DataOutputStream dos2 = new DataOutputStream(out2);
			
			dos2.writeUTF(entries);
			
			Response resp = Response.ok(entries, MediaType.APPLICATION_JSON).build();
			
			return resp;

		} catch (IOException e) {
		}
		
		
		
		
		return null;
	}
	
	public Response searchBtImplementation(int pos, String val) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(out);
		
		try {
			dos.writeInt(RequestType.SBT);
			dos.writeInt(pos);
			dos.writeUTF(val);
			
			ByteArrayInputStream in = new ByteArrayInputStream(clientProxy.invokeOrdered(out.toByteArray()));
			DataInputStream dis = new DataInputStream(in);
			
			String entries = dis.readUTF();

			ByteArrayOutputStream out2 = new ByteArrayOutputStream();
			DataOutputStream dos2 = new DataOutputStream(out2);
			
			dos2.writeUTF(entries);
			
			Response resp = Response.ok(entries, MediaType.APPLICATION_JSON).build();
			
			return resp;

		} catch (IOException e) {
		}
		
		
		
		
		return null;
	}
	
	
	public Response searchEqImplementation(int pos, String val) {
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
			
			Response resp = Response.ok(entries, MediaType.APPLICATION_JSON).build();
			

			return resp;
			
		} catch (IOException e) {
		}
		
		
		
		
		return null;
	}

	public byte[] sumImplementation(String key1, String key2, int pos) {

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
			return "0".getBytes();
		}
	}

	public byte[] multImplementation(String key1, String key2, int pos) {

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

			if(res.equals("true"))
				return Response.status(204).build();
			else
				return Response.status(404).build();
		}
		catch (IOException e) {

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
			return null;
		}
	}



}

