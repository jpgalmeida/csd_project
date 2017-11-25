//package client;
//
//import java.net.URI;
//import java.nio.charset.StandardCharsets;
//import java.util.HashMap;
//import java.util.Random;
//import java.util.Scanner;
//
//import javax.net.ssl.HostnameVerifier;
//import javax.net.ssl.SSLSession;
//import javax.ws.rs.client.Client;
//import javax.ws.rs.client.ClientBuilder;
//import javax.ws.rs.client.Entity;
//import javax.ws.rs.client.WebTarget;
//import javax.ws.rs.core.GenericType;
//import javax.ws.rs.core.MediaType;
//import javax.ws.rs.core.Response;
//import javax.ws.rs.core.UriBuilder;
//
//import resources.Entry;
//
//
//public class Benchmark {
//
//	private static Client client;
//	private static URI serverURI;
//	private static WebTarget target;
//
//	private static String command = "";
//
//	public static void main(String[] args) throws InterruptedException {
//		String serverURL=args[0];
//
//		serverURI = UriBuilder.fromUri(serverURL).port(11100).build();
//		client = ClientBuilder.newBuilder().hostnameVerifier(new InsecureHostnameVerifier())
//				.build();
//		target = client.target( serverURI );
//
//		System.out.println("Client ready!");
//
//		if(args.length < 3) {
//			System.out.println("Usage: <benchmark number> <number of threads>");
//			System.exit(1);
//		}
//
//		command = args[1];
//		int servers = Integer.valueOf(args[2]);
//
//		//		if(command.equals("2"))
//		//			BenchmarkInitRequest();	
//
//		for(int i = 0; i < servers; i++)
//			(new Thread(new Tester())).start();
//
//		
//	}
//
//
//
//
//
//	private static void BenchmarkInitRequest() {
//		String value = " nome dummy000000 idade 100 morada RandomStreet telefone 9158128912";
//		for( int i = 0; i < 100 ; i++)
//			registerEntry(String.valueOf(i), parseValuesToMap(value));
//
//	}
//
//	private static int MultAll(int pos) {
//		return 0;
//	}
//
//	private static int SumAll(int pos) {
//		return 0;
//	}
//
//	private static int Mult(int pos, String key1, String key2) {
//		int response = target.path("/entries/mult/"+key1+"/"+key2+"/"+pos)
//				.request()
//				.accept(MediaType.APPLICATION_JSON)
//				.get(new GenericType<Integer>(){});
//		
//		System.out.println("MULT "+response);
//		return response;
//	}
//
//
//	private static int Sum(int pos, String key1, String key2) {
//		int response = target.path("/entries/sum/"+key1+"/"+key2+"/"+pos)
//				.request()
//				.accept(MediaType.APPLICATION_JSON)
//				.get(new GenericType<Integer>(){});
//
//		System.out.println("SUM"+response);
//		return response;
//	}
//
//
//
//	private static boolean isElement(String key, String element) {
//		boolean response = target.path("/entries/ie/"+key+"/"+element)
//				.request()
//				.accept(MediaType.APPLICATION_JSON)
//				.get(new GenericType<Boolean>(){});
//
//		System.out.println("IE "+response);
//		return response;
//	}
//
//	private static String readElement(String key, int pos) {
//		String response = target.path("/entries/"+key+"/"+pos)
//				.request()
//				.accept(MediaType.APPLICATION_JSON)
//				.get(new GenericType<String>(){});
//
//		System.out.println("READEL "+response);
//		return response;
//	}
//
//	public static int writeElement(String key, String new_element, int pos) {
//		Response response = target.path("/entries/"+key+"/"+pos)
//				.request()
//				.put(Entity.entity(new_element, MediaType.APPLICATION_JSON));
//
//		System.out.println("WRITEEL "+response.toString());
//		return response.getStatus();
//	}
//
//	public static int registerEntry(String key, HashMap<byte[], byte[]> values){
//		Entry entry = new Entry(key.getBytes(), values);
//
//		Response response = target.path("/entries/ps/"+key)
//				.request()
//				.post( Entity.entity(entry, MediaType.APPLICATION_JSON));
//
//		System.out.println("REGENTRY "+response);
//		return response.getStatus();
//
//	}
//
//	public static String removeSet(String key) {
//		Response response = target.path("/entries/rs/"+key)
//				.request()
//				.delete(new GenericType<Response>() {});
//
//		System.out.println("WRITEEL "+response.toString());
//		return String.valueOf(response.getStatus());
//	}
//
//	public static int addElement(String element){
//
//		Response response = target.path("/entries/adde/"+element)
//				.request()
//				.post( Entity.entity(element, MediaType.APPLICATION_JSON));
//
//		System.out.println("WRITEEL "+response.toString());
//		return response.getStatus();
//	}
//
//	public static byte[] getEntry(String key){
//		byte[] response=null;
//
//		try {
//			response = target.path("/entries/"+key)
//					.request()
//					.accept(MediaType.APPLICATION_JSON)
//					.get(new GenericType<byte[]>() {});
//			System.out.println(new String(response, StandardCharsets.UTF_8));
//		}catch(Exception e) {
////			System.out.println("Excepcao");
//		}
//
//		return response;
//	}
//
//
//	static public class InsecureHostnameVerifier implements HostnameVerifier {
//		@Override
//		public boolean verify(String hostname, SSLSession session) {
//			return true;
//		}
//	}
//
//	private static HashMap<byte[], byte[]> parseValuesToMap(String values) {
//		HashMap<byte[], byte[]> hm = new HashMap<byte[], byte[]>();
//		String [] parts = values.split(" ");
//
//		for( int i = 1 ; i < parts.length-1 ; i = i+2)
//			hm.put(parts[i].getBytes(), parts[i+1].getBytes());
//
//		return hm;
//	}
//
//
//	static class Tester implements Runnable {
//
//		@Override
//		public void run() {
//
//			long timeInit = System.currentTimeMillis();
//			String key, value;
//			long threadId = Thread.currentThread().getId();
//
//			System.out.println("Started Thread #"+threadId);
//
//			//Benchmark1: 100 PutSet
//			if(command.equals("1")) {
//				value = " nome dummy111111 idade 100 morada RandomStreet telefone 9158128912";
//
//				HashMap<String, String> valuesParsed = parseValuesToMap(value);
//				for(int i = 0;i<100;i++) {
//					key = Integer.toString(i);
//					registerEntry(key, valuesParsed);
//				}
//			} 
//
//			//Benchmark2: 100 GetSet
//			else if(command.equals("2")) {
//				for(int i = 0;i<100;i++) {
//					key = Integer.toString(i);
//					getEntry( key);
//				}
//
//
//			}
//
//			//Benchmark3: 50 PutSet, 50 GetSet alternated
//			else if(command.equals("3")) {
//				value = " nome dummy33333333 idade 100 morada RandomStreet telefone 9158128912";
//				HashMap<String, String> valuesParsed = parseValuesToMap(value);
//				for(int i = 0;i<50;i++) {
//					key = Integer.toString(i);
//
//					registerEntry(key, valuesParsed);
//					
//					getEntry( key);
//
//				}
//			}
//
//			//Benchmark4: All operations: alternated, distribution discussed (without sums or multiplications)
//			else if(command.equals("4")) {
//				value = " nome dummy4444444 idade 100 morada RandomStreet telefone 9158128912";
//				HashMap<String, String> valuesParsed = parseValuesToMap(value);
//
//				for(int i = 0;i<15;i++) {
//					key = Integer.toString(i);
//
//					registerEntry(key, valuesParsed);
//					getEntry( key);
//					isElement(key, "100");
//					readElement(key, 0);
//					writeElement(key, "10000", 1);
//					addElement("NewElement");
//					removeSet(key);
//
//				}
//			}
//
//			//Benchmark5: 
//			else if(command.equals("5")) {
//
//				value = " nome dummy5555555 idade 100 morada RandomStreet telefone 9158128912";
//				HashMap<String, String> valuesParsed = parseValuesToMap(value);
//
//				for(int i = 0;i<15;i++) {
//					key = Integer.toString(i);
//
//					registerEntry(key, valuesParsed);
//					getEntry( key);
//					isElement(key, "100");
//					readElement(key, 0);
//					writeElement(key, "10000", 1);
//					addElement("NewElement");
//					
//					if(i>0 && i<11) {
//						String key2 = Integer.toString(i-1);
//						Mult(1, key2, key);
//					}
//					
//				}
//				
//				for(int i = 0;i<15;i++) {
//					key = Integer.toString(i);
//					removeSet(key);
//				}
//
//			}
//			
//			//Benchmark6: 
//					else if(command.equals("6")) {
//
//						value = " nome dummy5555555 idade 100 morada RandomStreet telefone 9158128912";
//						HashMap<String, String> valuesParsed = parseValuesToMap(value);
//
//						for(int i = 0;i<15;i++) {
//							key = Integer.toString(i);
//
//							registerEntry(key, valuesParsed);
//							getEntry( key);
//							isElement(key, "100");
//							readElement(key, 0);
//							writeElement(key, "10000", 1);
//							addElement("NewElement");
//							
//							if(i>0 && i<11) {
//								String key2 = Integer.toString(i-1);
//								Sum(1, key2, key);
//							}
//							
//						}
//						
//						for(int i = 0;i<15;i++) {
//							key = Integer.toString(i);
//							removeSet(key);
//						}
//
//					}
//
//			long totalTime = (System.currentTimeMillis() - timeInit);
//			System.out.println("Thread #"+threadId+" Time: "+totalTime+" ms");
//
//		}
//
//	}
//
//}
//
//
