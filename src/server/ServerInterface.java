package server;

import java.net.InetAddress;
import java.net.URI;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import server.ServerInterfaceResources;

public class ServerInterface {
	private static ServerInterfaceResources resources;

	public static void main(String[] args) throws Exception {
		int port =11100;
		URI baseUri = UriBuilder.fromUri("https://"+InetAddress.getLocalHost().getHostAddress()+"/").port(port).build();
		
		ResourceConfig config = new ResourceConfig();
		
		resources = new ServerInterfaceResources(baseUri.toString(), Integer.valueOf(args[0])); 
		config.register( resources );

		JdkHttpServerFactory.createHttpServer(baseUri, config,SSLContext.getDefault());
		System.err.println("REST Server ready @ " + baseUri);

	}

}
