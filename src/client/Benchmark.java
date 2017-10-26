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


public class Benchmark {

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

		if(args.length < 2)
			System.out.println("Specify Benchmark to use!");

		String key, value;

		long timeInit = System.currentTimeMillis();
		
		//Benchmark1: 100 PutSet
		if(args[1].equals("1")) {
			value = " nome dummy idade 100";

			HashMap<String, String> valuesParsed = parseValuesToMap(value);
			for(int i = 0;i<100;i++) {
				key = Integer.toString(i);

				//System.out.println("adding "+valuesParsed.toString());
				registerEntry(key, valuesParsed);
			}
		} 
		
		//Benchmark2: 100 GetSet
		else if(args[1].equals("2")) {
			for(int i = 0;i<100;i++) {
				key = Integer.toString(i);

				getEntry( key);

			}

		}
		
		//Benchmark3: 50 PutSet, 50 GetSet alternated
		else if(args[1].equals("3")) {
			value = " nome dummy idade 100";
			HashMap<String, String> valuesParsed = parseValuesToMap(value);
			for(int i = 0;i<50;i++) {
				key = Integer.toString(i);
				
				registerEntry(key, valuesParsed);
				getEntry( key);
			}
		}
		
		//Benchmark4: All operations: alternated, distribution discussed (without sums or multiplications)
		else if(args[1].equals("4")) {
			value = " nome dummy idade 100";
			HashMap<String, String> valuesParsed = parseValuesToMap(value);
			for(int i = 0;i<100;i++) {
				key = Integer.toString(i);

				registerEntry(key, valuesParsed);
				getEntry( key);
				
				//continuar
			}
		}

		long totalTime = (System.currentTimeMillis() - timeInit);

		System.out.println("Time: "+totalTime+" ms");
	}


	private static int MultAll(int pos) {


		return 0;
	}

	private static int SumAll(int pos) {


		return 0;
	}

	private static int Mult(String key1, String key2, int pos) {
		// GET request
		int response = target.path("/entries/mult/"+key1+"/"+key2+"/"+pos)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<Integer>(){});

		return response;
	}


	private static int Sum(String key1, String key2, int pos) {

		// GET request
		int response = target.path("/entries/sum/"+key1+"/"+key2+"/"+pos)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<Integer>(){});

		return response;
	}



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

		//falta alterar isto
		return response.getStatus();
	}

	public static int registerEntry(String key, HashMap<String, String> values){

		Entry entry = new Entry(key, values);
		//System.out.println("Register entry "+entry.getkey());
		//POST Request
		Response response = target.path("/entries/ps/"+key)
				.request()
				.post( Entity.entity(entry, MediaType.APPLICATION_JSON));


		return response.getStatus();

	}

	public static String removeSet(String key) {

		String response = target.path("/entries/rs/"+key)
				.request()
				.delete(new GenericType<String>() {});

		return response;

	}

	public static int addElement(String element){


		//POST Request
		Response response = target.path("/entries/adde/"+element)
				.request()
				.post( Entity.entity(element, MediaType.APPLICATION_JSON));


		return response.getStatus();

	}

	public static byte[] getEntry(String key){

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
