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

	public static void main(String[] args) {
		int port = 8080;
		URI myURI = null;
		String serverURI=args[0];
		try {
			myURI = UriBuilder.fromUri("https://"+InetAddress.getLocalHost().getHostAddress()+"/").port(port).build();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
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
				result = registerEntry(serverURI, key, valuesParsed);

				if(result == 204)
					System.out.println("> Success");
				else
					System.out.println("> Failed!");


				break;

			case "gs":
				key = sc.next();

				List<String> fields = getEntry(serverURI,  myURI, key);
				
				
				if(fields != null)
					System.out.println(fields.toString());
				else
					System.out.println("> Failed!");

				System.out.println();
				break;

			case "adde":
				key = sc.next();
				
				result = addElement(serverURI, key);
				
				if(result == 204)
					System.out.println("> Success");
				else
					System.out.println("> Failed!");
								
				
				break;

			case "rs":
				key = sc.next();
				
				result = removeSet(serverURI, key);
				
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
				
				result = writeElement(serverURI, key, new_element, pos);
				
				if(result == 204 || result == 200 )
					System.out.println("> Success");
				else 
					System.out.println("> Failed!");

				break;

			case "re":
				System.out.println("> read element");
				key = sc.next();
				pos = sc.nextInt();
				
				result = readElement(serverURI, key, pos);
				
				if(result == 200)
					System.out.println("> Success");
				else
					System.out.println("> Failed");
				
				break;

			case "ie":
				System.out.println("> is element");
				key = sc.next();
				String element = sc.next();

				Object res = isElement(serverURI, key, element);
				
				System.out.println(res);
				
				break;

			case "sum":
				System.out.println("> Sum");
				pos = sc.nextInt();
				String key1 = sc.next();
				String key2 = sc.next();
				
				

				break;

			case "mult":
				System.out.println("> Mult");
				pos = sc.nextInt();
				key1 = sc.next();
				key2 = sc.next();

				break;

			}
		}

	}

	private static Object isElement(String serverURL, String key, String element) {
		Client client = ClientBuilder.newBuilder().hostnameVerifier(new InsecureHostnameVerifier())
				.build();
		URI serverURI = UriBuilder.fromUri(serverURL).build();
		WebTarget target = client.target( serverURI );
		
		// GET request
		Response response = target.path("/entries/ie/"+key+"/"+element)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get();
		
		return response;
	}

	private static int readElement(String serverURL, String key, int pos) {
		Client client = ClientBuilder.newBuilder().hostnameVerifier(new InsecureHostnameVerifier())
				.build();
		URI serverURI = UriBuilder.fromUri(serverURL).build();
		WebTarget target = client.target( serverURI );
		
		// GET request
		Response response = target.path("/entries/"+key+"/"+pos)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get();
		
		return response.getStatus();
	}

	public static int writeElement( String serverURL, String key, String new_element, int pos) {
		
		Client client = ClientBuilder.newBuilder().hostnameVerifier(new InsecureHostnameVerifier())
				.build();
		URI serverURI = UriBuilder.fromUri(serverURL).build();
		WebTarget target = client.target( serverURI );
		
		// PUT request
		Response response = target.path("/entries/"+key+"/"+pos)
				.request()
				.put(Entity.entity(new_element, MediaType.APPLICATION_JSON));
		
		return response.getStatus();
	}

	public static int registerEntry(String serverURL, String key, HashMap<String, String> values){

		Client client = ClientBuilder.newBuilder().hostnameVerifier(new InsecureHostnameVerifier())
				.build();
		URI serverURI = UriBuilder.fromUri(serverURL).build();
		WebTarget target = client.target( serverURI );

		Entry entry = new Entry(key, values);

		//POST Request
		Response response = target.path("/entries/ps/"+key)
				.request()
				.post( Entity.entity(entry, MediaType.APPLICATION_JSON));


		return response.getStatus();

	}
	
	public static int removeSet(String serverURL, String key) {
		
		Client client = ClientBuilder.newBuilder().hostnameVerifier(new InsecureHostnameVerifier())
				.build();
		URI serverURI = UriBuilder.fromUri(serverURL).build();
		WebTarget target = client.target( serverURI );
		
		Response response = target.path("/entries/rs/"+key)
				.request()
				.delete();
		
		return response.getStatus();
		
	}

	public static int addElement(String serverURL, String element){

		Client client = ClientBuilder.newBuilder().hostnameVerifier(new InsecureHostnameVerifier())
				.build();
		URI serverURI = UriBuilder.fromUri(serverURL).build();
		WebTarget target = client.target( serverURI );

		//POST Request
		Response response = target.path("/entries/adde/"+element)
				.request()
				.post( Entity.entity(element, MediaType.APPLICATION_JSON));


		return response.getStatus();

	}
	
	public static List<String> getEntry(String serverURL, URI myURI, String key){

		Client client = ClientBuilder.newBuilder().hostnameVerifier(new InsecureHostnameVerifier())
				.build();
		URI serverURI = UriBuilder.fromUri(serverURL).build();
		WebTarget target = client.target( serverURI );

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
