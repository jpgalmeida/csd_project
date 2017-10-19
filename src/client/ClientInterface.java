package client;

import java.io.Console;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import server.Entry;

public class ClientInterface {

	private static Client client;
	private static URI serverURI;
	private static WebTarget target;
	
	public static void main(String[] args) {
		int port = 8080;
		URI myURI = null;
		String serverURL=args[0];
		
		try {
			myURI = UriBuilder.fromUri("https://"+InetAddress.getLocalHost().getHostAddress()+"/").port(port).build();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		//Server connection
		client = ClientBuilder.newBuilder().hostnameVerifier(new InsecureHostnameVerifier())
				.build();
		serverURI = UriBuilder.fromUri(serverURL).build();
		target = client.target( serverURI );
		
		
		
		System.out.println("Client ready!");

		Console console = System.console();

		Scanner sc = new Scanner(System.in);

		String key, value;
		int result;
		
		while (true) {

			String cmd = sc.next();

			switch (cmd) {

			case "ps": 
				key = sc.next();
				value = sc.nextLine();

				HashMap<String, String> valuesParsed = parseValuesToMap(value);
				result = registerEntry(key, valuesParsed);

				if(result == 204)
					System.out.println("> Success");
				else
					System.out.println("> Failed!");


				break;

			case "gs":
				key = sc.next();

				List<String> fields = getEntry( key);
				
				
				if(fields != null)
					System.out.println(fields.toString());
				else
					System.out.println("> Failed!");

				System.out.println();
				break;

			case "adde":
				key = sc.next();
				
				result = addElement(key);
				
				if(result == 204)
					System.out.println("> Success");
				else
					System.out.println("> Failed!");
								
				
				break;

			case "rs":
				key = sc.next();
				
				result = removeSet(key);
				
				if(result != 200 && result != 204 )
					System.out.println("> Failed");
				else 
					System.out.println("> Success");
				
				break;

			case "we":
				System.out.println("> write element");
				key = sc.next();
				String new_element = sc.next();
				int pos = sc.nextInt();
				
				result = writeElement(key, new_element, pos);
				
				if(result == 204 || result == 200 )
					System.out.println("> Success");
				else 
					System.out.println("> Failed!");

				break;

			case "re":
				//System.out.println("> read element");
				key = sc.next();
				pos = sc.nextInt();
				
				String elementRead = readElement(key, pos);
				
				if(!elementRead.equals(""))
					System.out.println("> Read:" +elementRead);
				else
					System.out.println("> Failed");
				
				break;

			case "ie":
				//System.out.println("> is element");
				key = sc.next();
				String element = sc.next();

				boolean res = isElement(key, element);
				if(res)
					System.out.println("> " +element+" is element");
				else
					System.out.println("> " +element+" isn't element");
				
				break;

			case "sum":
				System.out.println("> Sum");
				pos = sc.nextInt();
				String key1 = sc.next();
				String key2 = sc.next();
				
				int sum_res = Sum(key1, key2, pos);

				break;

			case "mult":
				System.out.println("> Mult");
				pos = sc.nextInt();
				key1 = sc.next();
				key2 = sc.next();

				int mult_res = Mult( key1, key2, pos);
				
				break;
				
			case "sumall":
				System.out.println("> SumAll");
				pos = sc.nextInt();
				
				int sum_all = SumAll( pos);
				break;
				
			case "multall":
				System.out.println("> MultAll");
				pos = sc.nextInt();
				
				int mult_all = MultAll( pos);
				
				break;

			}
				
		}

	}
	
	
	
	
	/* --------------------------------------------------------------
	 * ---------------------- NOT IN USE YET ------------------------ */
	private static int MultAll(int pos) {
		
		
		return 0;
	}
	
	private static int SumAll(int pos) {
		
		
		return 0;
	}
	
	private static int Mult(String key1, String key2, int pos) {
		
		
		return 0;
	}
	
	
	private static int Sum(String key1, String key2, int pos) {
		
		
		return 0;
	}
	
	/* ------------------------------------------------------------ 
	 * ------------------------------------------------------------ */

	
	
	
	private static boolean isElement(String key, String element) {
		
		
		// GET request
		boolean response = target.path("/entries/ie/"+key+"/"+element)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<Boolean>(){});
		
		return response;
	}

	private static String readElement(String key, int pos) {
		
		
		// GET request
		String response = target.path("/entries/"+key+"/"+pos)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<String>(){});
		
		return response;
	}

	public static int writeElement(String key, String new_element, int pos) {
		
		
		
		// PUT request
		Response response = target.path("/entries/"+key+"/"+pos)
				.request()
				.put(Entity.entity(new_element, MediaType.APPLICATION_JSON));
		
		return response.getStatus();
	}

	public static int registerEntry(String key, HashMap<String, String> values){

		Entry entry = new Entry(key, values);

		//POST Request
		Response response = target.path("/entries/ps/"+key)
				.request()
				.post( Entity.entity(entry, MediaType.APPLICATION_JSON));


		return response.getStatus();

	}
	
	public static int removeSet(String key) {
				
		Response response = target.path("/entries/rs/"+key)
				.request()
				.delete();
		
		return response.getStatus();
		
	}

	public static int addElement(String element){

		
		//POST Request
		Response response = target.path("/entries/adde/"+element)
				.request()
				.post( Entity.entity(element, MediaType.APPLICATION_JSON));


		return response.getStatus();

	}
	
	public static List<String> getEntry(String key){

		//GET Request
//		Response response = target.path("/entries/"+key)
//				.request().get();
		
		List<String> response = target.path("/entries/"+key)
								.request()
								.accept(MediaType.APPLICATION_JSON)
								.get(new GenericType<List<String>>() {});
				
		return response;
	}


	static public class InsecureHostnameVerifier implements HostnameVerifier {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}

	private static HashMap<String, String> parseValuesToMap(String values) {
		HashMap<String, String> hm = new HashMap<String, String>();
		String [] parts = values.split(" ");

		for( int i = 1 ; i < parts.length-1 ; i = i+2)
			hm.put(parts[i], parts[i+1]);

		return hm;
	}

}
