package server;

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
	
	public ServerInterfaceResources(String serverUri, String redisUri){
		this.serverUri = serverUri;

		//Connecting to Redis server on localhost 
		jedis=new Jedis(serverUri, 6379);
		
		//check whether server is running or not 
		System.out.println("Redis server is running: "+jedis.ping()); 


	}

	@GET
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> Entries(@PathParam("id") String id){
		System.out.println("Received GET Request!");
				
		//tera de se ver como e com os outros campos
		return jedis.hmget(id, "nome", "idade");
	}

	@POST
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void register( @PathParam("id") String id, Entry entry) {
		System.out.println("Received POST Request!");
	
		System.out.println(jedis.hmset(id, entry.getAttributes()));
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void update(@PathParam("id") String id, Entry entry) {


	}

	@DELETE
	@Path("/{id}")
	public void unregister(@PathParam("id") String id, @QueryParam("secret") String secret){


	}



}

