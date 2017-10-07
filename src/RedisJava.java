import redis.clients.jedis.*;

public class RedisJava { 
	   public static void main(String[] args) { 
	      //Connecting to Redis server on localhost 
		  Jedis jedis=new Jedis("172.17.0.2", 6379); 
		  //Jedis j = new Jedis(host, port, ssl)

		  
	      //check whether server is running or not 
	      
	      System.out.println("Server is running: "+jedis.ping()); 
	      
	      jedis.set("foo", "bar");
	      System.out.println("Value for foo: "+jedis.get("foo"));
	      
	      
	   }
	   
	   
	} 