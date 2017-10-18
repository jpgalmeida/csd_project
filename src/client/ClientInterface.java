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
		URI myURI=null;
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
				result = registerEntry(serverURI,  myURI, key, valuesParsed);

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
				System.out.println("> add element");
				key = "";

				break;

			case "rs":
				key = sc.next();

			

				break;

			case "we":
				System.out.println("> write element");
				key = "";
				Object new_element = null;
				int pos = 0;

				break;

			case "re":
				System.out.println("> read element");
				key = "";
				pos = 0;

				break;

			case "ie":
				System.out.println("> is element");
				key = "";
				String element = "";

				break;

			case "sum":
				System.out.println("> Sum");
				pos = 0;
				String key1 = "";
				String key2 = "";

				break;

			case "mult":
				System.out.println("> Mult");
				pos = 0;
				key1 = "";
				key2 = "";

				break;

			}
		}

	}


	public static int registerEntry(String serverURL, URI myURI, String key, HashMap<String, String> values){

		Client client = ClientBuilder.newBuilder().hostnameVerifier(new InsecureHostnameVerifier())
				.build();
		URI serverURI = UriBuilder.fromUri(serverURL).build();
		WebTarget target = client.target( serverURI );

		Entry entry = new Entry(key, values);

		//POST Request
		Response response = target.path("/entries/"+key)
				.request()
				.post( Entity.entity(entry, MediaType.APPLICATION_JSON));


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
