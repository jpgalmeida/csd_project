package client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Random;
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

	private static String command = "";

	public static void main(String[] args) throws InterruptedException {
		String serverURL = args[0];

		serverURI = UriBuilder.fromUri(serverURL).port(11100).build();
		client = ClientBuilder.newBuilder().hostnameVerifier(new InsecureHostnameVerifier()).build();
		target = client.target(serverURI);

		System.out.println("Client ready!");

		if (args.length < 2) {
			System.out.println("Usage: <benchmark number>");
			System.exit(1);
		}

		command = args[1];

		Tester(command);
	
	}

	private static void BenchmarkInitRequest() {
		String value = " nome qwe idade 10 salario 20 golos 30";
		for (int i = 0; i < 100; i++)
			putSet(String.valueOf(i), parseValuesToMap(value));

	}


	private static String searchEq( int pos, String val) {
		String response = target.path("/entries/seq/"+pos+"/"+val)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<String>(){});


		return response;
	}


	private static String searchBt( int pos, String val) {
		String response = target.path("/entries/sbt/"+pos+"/"+val)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<String>(){});

		return response;
	}

	private static String searchLt( int pos, String val) {
		String response = target.path("/entries/slt/"+pos+"/"+val)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<String>(){});

		return response;
	}


	private static int Mult( int pos, String key1, String key2) {

		byte[] response = target.path("/entries/mult/"+key1+"/"+key2+"/"+pos)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<byte[]>(){});

		ByteArrayInputStream in = new ByteArrayInputStream(response);
		DataInputStream res = new DataInputStream(in);

		int result = 0;
		try {
			result = res.readInt();
		} catch (IOException e) {
		}

		return result;
	}


	private static String Sum(int pos, String key1, String key2) {

		byte[] response = target.path("/entries/sum/"+key1+"/"+key2+"/"+pos)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<byte[]>(){});

		ByteArrayInputStream in = new ByteArrayInputStream(response);
		DataInputStream res = new DataInputStream(in);

		String result ="";
		try {
			result = res.readUTF();
		} catch (IOException e) {
		}

		return result;
	}



	private static String isElement(String key, String element) {
		String response = "false";
		
		try {
			response = target.path("/entries/ie/"+key+"/"+element)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<String>(){});
		}catch(Exception e) {
			
		}
		
		return response;
	}

	private static String readElement(String key, int pos) {
		String response = "";
		try {
			response = target.path("/entries/"+key+"/"+pos)
					.request()
					.accept(MediaType.APPLICATION_JSON)
					.get(new GenericType<String>(){});
		}catch(Exception e) {
			
		}
		
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

		Response response = target.path("/entries/ps")
				.request()
				.post( Entity.entity(entry,MediaType.APPLICATION_JSON));

		return response.getStatus();

	}

	public static byte[] getSet(String key){
		byte[] response = "".getBytes();

		try {
			response = target.path("/entries/"+key)
					.request()
					.accept(MediaType.APPLICATION_JSON)
					.get(new GenericType<byte[]>() {});

		} catch(Exception e) {

		}

		if(response.length < 32)
			return response;

		ByteArrayInputStream in = new ByteArrayInputStream(response);
		DataInputStream res = new DataInputStream(in);


		String finalResult="";
		String k="";
		String v="";

		try {
			int attSize = res.readInt();
			for(int i = 0; i < attSize; i++) {
				k = res.readUTF();
				finalResult+=k;
				finalResult+=",";
				v = res.readUTF();
				finalResult+=v;
				finalResult+=",";
			}

		} catch (IOException e) {
		}

		return finalResult.getBytes();
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

	private static void Tester(String command) {


			long timeInit = System.currentTimeMillis();
			String key, value;
			int numOps = 0;

			// Benchmark1: 100 PutSet
			if (command.equals("1")) {
				value = " nome qwe idade 10 salario 20 golos 30";

				HashMap<String, String> valuesParsed = parseValuesToMap(value);
				for (int i = 0; i < 100; i++) {
					key = Integer.toString(i);
					putSet(key, valuesParsed);
				}
				
				numOps = 100;
			}

			// Benchmark2: 100 GetSet
			else if (command.equals("2")) {
				for (int i = 0; i < 100; i++) {
					key = Integer.toString(i);
					getSet(key);
				}
				
				numOps = 100;

			}

			// Benchmark3: 50 PutSet, 50 GetSet alternated
			else if (command.equals("3")) {
				value = " nome qwe idade 10 salario 20 golos 30";
				HashMap<String, String> valuesParsed = parseValuesToMap(value);
				for (int i = 0; i < 50; i++) {
					key = Integer.toString(i);

					putSet(key, valuesParsed);
					getSet(key);
				}
				
				numOps = 100;
			}

			// Benchmark4: All operations: alternated, distribution discussed
			// (without sums or multiplications)
			else if (command.equals("4")) {
				value = " nome qwe idade 10 salario 20 golos 30";
				HashMap<String, String> valuesParsed = parseValuesToMap(value);

				for (int i = 0; i < 15; i++) {
					key = Integer.toString(i);

					putSet(key, valuesParsed);
					getSet(key);
					isElement(key, "100");
					readElement(key, 0);
					writeElement(key, "10000", 1);
					addElement("NewElement");
					removeSet(key);

					numOps += 7;
				}
			}

			// Benchmark5:
			else if (command.equals("5")) {

				value = " nome qwe idade 10 salario 20 golos 30";
				HashMap<String, String> valuesParsed = parseValuesToMap(value);

				for (int i = 0; i < 15; i++) {
					key = Integer.toString(i);

					putSet(key, valuesParsed);
					getSet(key);
					isElement(key, "100");
					readElement(key, 0);
					writeElement(key, "10000", 1);
					addElement("NewElement");

					numOps += 6;
					
					if (i > 0 && i < 11) {
						String key2 = Integer.toString(i - 1);
						Mult(1, key2, key);
						numOps++;
					}

				}

				for (int i = 0; i < 15; i++) {
					key = Integer.toString(i);
					removeSet(key);
					numOps++;
				}

			}

			// Benchmark6:
			else if (command.equals("6")) {

				value = " nome qwe idade 10 salario 20 golos 30";
				HashMap<String, String> valuesParsed = parseValuesToMap(value);

				for (int i = 0; i < 15; i++) {
					key = Integer.toString(i);

					putSet(key, valuesParsed);
					getSet(key);
					isElement(key, "100");
					readElement(key, 0);
					writeElement(key, "10000", 1);
					addElement("NewElement");

					numOps += 6;
					
					if (i > 0 && i < 11) {
						String key2 = Integer.toString(i - 1);
						Sum(1, key2, key);
						numOps++;
					}

				}

				for (int i = 0; i < 15; i++) {
					key = Integer.toString(i);
					removeSet(key);
					numOps++;
				}

				//Benchmark 7 (E1)	
			} else if (command.equals("7")) {

				int pos = 0;
				String val = "10";

				for (int i = 0; i < 33; i++) {
					key = Integer.toString(i);

					searchEq(pos, val);
					searchBt(pos+3 , val);
					searchLt(pos+3, val);
					
					numOps += 3;
					
				}

				//Benchmark 8 (E2)	
			} else if (command.equals("8")) {

				int pos = 0;
				String val = "10";

				value = " nome qwe idade 10 salario 20 golos 30";
				HashMap<String, String> valuesParsed = parseValuesToMap(value);

				for (int i = 0; i < 10; i++) {
					key = Integer.toString(i);

					putSet(key, valuesParsed);
					getSet(key);
					isElement(key, "100");
					readElement(key, 0);
					writeElement(key, "10000", 1);
					addElement("NewElement");
					searchEq(pos, val);
					searchBt(pos+3 , val);
					searchLt(pos+3, val);

					numOps += 9;
					
					if (i > 0 && i < 11) {
						String key2 = Integer.toString(i - 1);
						Sum(1, key2, key);
						
						numOps++;
					}

				}

				for (int i = 0; i < 10; i++) {
					key = Integer.toString(i);
					removeSet(key);
					numOps++;
				}

				//Benchmark 9 (E3)	
			}else if (command.equals("9")) {

				int pos = 0;
				String val = "10";

				for (int i = 0; i < 50; i++) {
					key = Integer.toString(i);

					//Encripted Search 
					searchBt(pos+3 , val);
					searchLt(pos+3, val);
					
					numOps += 2;
				}
			}

			long totalTime = (System.currentTimeMillis() - timeInit);
			
			System.out.println("Time: " + totalTime + " ms");
			System.out.println(numOps/(totalTime/1000) + " ops/s");

		

	}

}
