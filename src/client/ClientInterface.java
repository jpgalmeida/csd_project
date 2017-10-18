package client;

import java.io.Console;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
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
		
		//registerEntry(serverURI,  myURI);
		
		Console console = System.console();

        Scanner sc = new Scanner(System.in);

        HashMap<String, HashMap> newmap = new HashMap<String,HashMap>();
        
        String key, value;
        
        while (true) {

            String cmd = sc.next();
            
            switch (cmd) {
                                
                case "ps": 
                    key = sc.next();
                    value = sc.nextLine();
                    
                    HashMap valuesParsed = parseValuesToMap(value);
                    int result = registerEntry(serverURI,  myURI, key, valuesParsed);
                    
                    if(result == 204)
                    		System.out.println("> Success");
                    else
                    	System.out.println("> Failed!");
                    
                    
                    break;
                
                case "gs":
                    key = sc.next();

                    HashMap auxMap = newmap.get(key);
                    
                    System.out.print("> Set values: ");
                    if(auxMap!=null){
	                    for (Object name: auxMap.keySet()){
	                        String keyAux =name.toString();
	                        String valueAux = auxMap.get(name).toString(); 
	                        
	                        System.out.print(keyAux + " " + valueAux+" ");  
	                    }
                    }
                    else
                    	System.out.println("No key associated!");
                    System.out.println();
                    break;
                    
                case "adde":
                    System.out.println("> add element");
                    key = "";
                    
                    break;
                
                case "rs":
                	key = sc.next();
                	
                	Object response = newmap.remove(key);
                	
                	if(response==null)
                		System.out.println("> Remove failed!");
                	else
                		System.out.println("> Sucessfully removed object: "+ key);
                    
                    
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
	
	
	public static int registerEntry(String rendezvousURL, URI myURI, String key, HashMap values){
		
		Client client = ClientBuilder.newBuilder().hostnameVerifier(new InsecureHostnameVerifier())
				.build();
		URI rendezvousURI = UriBuilder.fromUri(rendezvousURL).build();
		WebTarget target = client.target( rendezvousURI );

//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("Nome", "Joao");
		
		//String key = "123";
		
		
		Entry Entry = new Entry(key, values);

		//GET Request
//		Response response = target.path("/entries/")
//				.request().get();
		
		//POST Request
		Response response = target.path("/entries/"+key)
				.request()
				.post( Entity.entity(Entry, MediaType.APPLICATION_JSON));
		
				
		return response.getStatus();

	}
	
	static public class InsecureHostnameVerifier implements HostnameVerifier {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}
	
	private static HashMap parseValuesToMap(String values) {
    	HashMap hm = new HashMap();
    	String [] parts = values.split(" ");
    	
    	for( int i = 1 ; i < parts.length-1 ; i = i+2)
    		hm.put(parts[i], parts[i+1]);
    	
    	return hm;
    }
    
    private static String[] parseValuesToArray(String values) {
    	
    	String [] parts = values.split(" ");
    	String[] buf = new String[parts.length-1];
    	
    	System.out.println("parts len "+parts.length);
    	
    	for( int i = 1 ; i < parts.length ; i++)
    		buf[i-1] = parts[i];
    	
//    	for(int i = 0;i<buf.length;i++)
//    		System.out.println(buf[i]);
    	
    	return buf;
    }
}
