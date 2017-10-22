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
                
               
                
            
            
            }
        }
    }

    
    
}