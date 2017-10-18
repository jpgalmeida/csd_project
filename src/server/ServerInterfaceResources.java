package server;

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

	public String rendezVousUri;
	private Jedis jedis;
	
	public ServerInterfaceResources(String rendezVousUri){
		this.rendezVousUri = rendezVousUri;

		//Connecting to Redis server on localhost 
		jedis=new Jedis("172.18.0.2", 6379);

		//check whether server is running or not 
		System.out.println("Redis server is running: "+jedis.ping()); 

//		jedis.set("foo", "bar");
//		System.out.println("Value for foo: "+jedis.get("foo"));


	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String Entrys(){
		System.out.println("hello");
		return "hello";

	}

	@POST
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void register( @PathParam("id") String id, @QueryParam("secret") String secret, Entry Entry) {

		System.out.println("Received new Entry!");


	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void update(@PathParam("id") String id, Entry Entry) {


	}

	@DELETE
	@Path("/{id}")
	public void unregister(@PathParam("id") String id, @QueryParam("secret") String secret){


	}



}

