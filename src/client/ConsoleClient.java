package client;

import java.io.Console;
import java.util.Scanner;
import java.util.Set;

import redis.clients.jedis.*;
import java.util.Collections; 
import java.util.HashMap;

public class ConsoleClient {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: ConsoleClient <client id>");
            System.exit(0);
        }

        MapClient client = new MapClient(Integer.parseInt(args[0]));
        
        Console console = System.console();

        Scanner sc = new Scanner(System.in);

        HashMap<String, HashMap> newmap = new HashMap<String,HashMap>();

        while (true) {
//            System.out.println("Select an option:");
//            System.out.println("1. ADD A KEY AND VALUE TO THE MAP");

            String cmd = sc.next();
            
            switch (cmd) {
                case "1":
                    System.out.println("Putting value in the map");
                    String key = console.readLine("Enter the key:");
                    String value = console.readLine("Enter the value:");
                    
                    String result = client.put(key, value);

                    break;
                case "2":
                    System.out.println("Reading value from the map");
                    key = console.readLine("Enter the key:");
                    result = client.get(key);
                    if(result!=null)
                        System.out.println("Value read: " + result);
                    else 
                        System.out.println("Value not found!");
                    break;
                case "3":
                    System.out.println("Removing value in the map");
                    key = console.readLine("Enter the key:");
                    result = client.remove(key);
                    System.out.println("Value removed: " + result);
                    break;
                case "4":
                    System.out.println("Getting the map size");
                    int size = client.size();
                    System.out.println("Map size: " + size);
                    break;
                
                case "ps": 
                    key = sc.next();
                    value = sc.nextLine();
                    //newmap.put(key, parseValuesToMap(value));
                    
                    System.out.println("> Success");
                    
                    String[] valuesParsed = parseValuesToArray(value);
                    //String hmToString = newmap.toString();
                    //result = client.puthm(key, valuesParsed, 2);
                    result="Error!";
                    
                    for(int i =0;i<4;i+=2) {
                    	System.out.println("values ot send "+valuesParsed[i]+" "+valuesParsed[i+1]);
                    	result = client.put(valuesParsed[i], valuesParsed[i+1]);
                    }
                    
                    System.out.println("RESULT FROM SV: "+result);
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
