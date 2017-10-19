package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import redis.clients.jedis.Jedis;

/**
 * Implementacao do servidor em REST 
 */
@Path("/entries")
public class ServerInterfaceResources {

	public String serverUri;
	private Jedis jedis;
	List<String> fields;
	
	public ServerInterfaceResources(String serverUri, String redisUri){
		this.serverUri = serverUri;

		//Connecting to Redis server on localhost 
		jedis=new Jedis(serverUri, 6379);
		
		//check whether server is running or not 
		System.out.println("Redis server is running: "+jedis.ping()); 

		fields=new ArrayList<String>();
		fields.add("nome");
		fields.add("idade");
	}

	@GET
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> Entries(@PathParam("id") String id){
		System.out.println("Received GET Request!");

		String[] arr = fields.toArray(new String[fields.size()]);
		return jedis.hmget(id, arr);
	}

	@POST
	@Path("/ps/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void putSet( @PathParam("id") String id, Entry entry) {
		System.out.println("Received POST Request!");
		System.out.println(jedis.hmset(id, entry.getAttributes()));
		
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
		String result = jedis.hget(id, field);
		
		System.out.println(result);
		return result;
	}
	
	@GET
	@Path("/ie/{id}/{element}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean isElement(@PathParam("id") String id, @PathParam("element") String element) {
		System.out.println("Received GET IsElement Request!");
		
		for (String current_field : fields) {
			
			Object result = jedis.hget(id, current_field);
			
			if(result.toString().equalsIgnoreCase(element)) {
				System.out.println(true);
				return true;
			}
		}
		
		System.out.println(false);
		return false;
	}

	@PUT
	@Path("/{id}/{pos}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void writeElement(@PathParam("id") String id, @PathParam("pos") int pos, String new_element) {
		String field = fields.get(pos);
		System.out.println(jedis.hset(id, field, new_element));
	}

	@DELETE
	@Path("/rs/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void removeSet(@PathParam("id") String id){
		System.out.println("Received rs request!");
		System.out.println(jedis.del(id));
	}
	
}

