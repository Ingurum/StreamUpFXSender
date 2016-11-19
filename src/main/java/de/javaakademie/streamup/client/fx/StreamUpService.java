package de.javaakademie.streamup.client.fx;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

/**
 * Service for testing the connection and sending the video chunks.
 * 
 * @author Guido.Oelmann
 */
public class StreamUpService {

	private Client client;

	public StreamUpService() {
		client = ClientBuilder.newClient();
	}
	
	/**
	 * Test the connection to the StreamUp-Server.
	 * 
	 * @param server server address
	 * @return true, if connection available
	 */
	public boolean testConnection(String server) {
		if (server.trim().length() == 0) {
			return false;
		}
		try {
			final URLConnection connection = new URL(server).openConnection();
			connection.connect();
			return true;
		} catch (final MalformedURLException e) {
			// bad url
			return false;
		} catch (final IOException e) {
			// url unavailable
			return false;
		}
	}

	/**
	 * Send the video chunk to the StreamUp-Server.
	 * 
	 * @param server the StreamUp-Server
	 * @param chunk the video chunk
	 */
	public void sendChunk(String server, Path chunk) {
		WebTarget webTarget = client.target(server);
		File file = chunk.toFile();
		webTarget.request().post(Entity.entity(file, MediaType.APPLICATION_OCTET_STREAM), Long.class);
	}

}
