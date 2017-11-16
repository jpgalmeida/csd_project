package client;

import java.net.URI;
import java.util.HashMap;
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

import resources.Entry;


public class ClientInterface {

	private static Client client;
	private static URI serverURI;
	private static WebTarget target;
	
	public static void main(String[] args) {
		String serverURL=args[0];
		
		//Server connection
		serverURI = UriBuilder.fromUri(serverURL).port(11100).build();
		client = ClientBuilder.newBuilder().hostnameVerifier(new InsecureHostnameVerifier())
				.build();
		target = client.target( serverURI );

		System.out.println("Client ready!");
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
				result = putSet(key, valuesParsed);
				
				if(result == 204)
					System.out.println("> Success");
				else
					System.out.println("> Failed!");

				break;

			case "gs":
				key = sc.next();

				byte[] fields = getSet( key);
				
				if(fields != null)
					System.out.println(new String(fields));
				else
					System.out.println("> Failed!");

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

				if(result == 204)
					System.out.println("> Success");
				else
					System.out.println("> Failed!");

				break;

			case "we":
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
				key = sc.next();
				pos = sc.nextInt();

				String elementRead = readElement(key, pos);

				if(!elementRead.equals(""))
					System.out.println("> Read:" +elementRead);
				else
					System.out.println("> Failed");

				break;

			case "ie":
				key = sc.next();
				String element = sc.next();

				boolean res = isElement(key, element);

				if(res)
					System.out.println("> " +element+" is element");
				else
					System.out.println("> " +element+" isn't element");

				break;

			case "sum":
				pos = sc.nextInt();
				String key1 = sc.next();
				String key2 = sc.next();

				int sum_res = Sum(pos, key1, key2);

				System.out.println("> Sum is: "+sum_res);
				break;

			case "mult":
				pos = sc.nextInt();
				key1 = sc.next();
				key2 = sc.next();
				
				
				int mult_res = Mult(pos, key1, key2);
				
				System.out.println("> Mult is: "+mult_res);
				
				break;

			case "sumall":
				pos = sc.nextInt();

				int sum_all = SumAll( pos);
				break;

			case "multall":
				pos = sc.nextInt();

				int mult_all = MultAll( pos);

				break;
				
			}
			
		}

	}
	

	private static int MultAll(int pos) {


		return 0;
	}

	private static int SumAll(int pos) {


		return 0;
	}

	private static int Mult( int pos, String key1, String key2) {

		int response = target.path("/entries/mult/"+key1+"/"+key2+"/"+pos)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<Integer>(){});

		return response;
	}


	private static int Sum(int pos, String key1, String key2) {

		int response = target.path("/entries/sum/"+key1+"/"+key2+"/"+pos)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<Integer>(){});

		return response;
	}



	private static boolean isElement(String key, String element) {

		boolean response = target.path("/entries/ie/"+key+"/"+element)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<Boolean>(){});

		return response;
	}

	private static String readElement(String key, int pos) {

		String response = target.path("/entries/"+key+"/"+pos)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<String>(){});

		return response;
	}

	public static int writeElement(String key, String new_element, int pos) {

		Response response = target.path("/entries/"+key+"/"+pos)
				.request()
				.put(Entity.entity(new_element, MediaType.APPLICATION_JSON));

		return response.getStatus();
	}

	public static int putSet(String key, HashMap<String, String> values){
		
		Entry entry = new Entry(key, values);
		
		Response response = target.path("/entries/ps/"+key)
				.request()
				.post( Entity.entity(entry, MediaType.APPLICATION_JSON));

	
		return response.getStatus();

	}

	public static int removeSet(String key) {

		Response response = target.path("/entries/rs/"+key)
				.request()
				.delete(new GenericType<Response>() {});

		return response.getStatus();

	}

	public static int addElement(String element){

		Response response = target.path("/entries/adde/"+element)
				.request()
				.post( Entity.entity(element, MediaType.APPLICATION_JSON));


		return response.getStatus();

	}

	public static byte[] getSet(String key){

		byte[] response = target.path("/entries/"+key)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<byte[]>() {});
		
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
