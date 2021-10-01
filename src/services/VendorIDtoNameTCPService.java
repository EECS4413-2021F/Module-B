package services;

import java.io.File;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class VendorIDtoNameTCPService extends Thread {

	public static PrintStream Log = System.out;
	private Socket client;
	
	public VendorIDtoNameTCPService(Socket client) { 
	  this.client = client;
	}

	public void run() {
		Log.printf("Connected to %s:%d\n", client.getInetAddress(), client.getPort());

    try (
      Socket client = this.client; 
  		Scanner req = new Scanner(client.getInputStream()); 
  		PrintStream res = new PrintStream(client.getOutputStream(), true);
    ) {
    	String response;
	    String request = req.nextLine();
	    
	    // Implement: 
	    // Given id, find the corresponding name in the database
	    // HR.vendor database (derby) has id, name
	    
	    // Check input is a number (cause id can only be number)
	    if (request.matches("[\\s\\S]*")) {
	    	
	    	// Connect to database
	    	String dbURL = "jdbc:derby://localhost:64413/EECS"; // Network-based
	    	Connection connection = DriverManager.getConnection(dbURL);
	    	connection.setSchema("HR");
	    	
	    	// SQL statement
  			String query = "SELECT name FROM vendor WHERE id=?";
  			PreparedStatement statement = connection.prepareStatement(query);
  			statement.setString(1, request);
  			ResultSet rs = statement.executeQuery();

  			// Print out id and name if found else print id not found and empty name. 
  			String name;
  			if (rs.next()) {
  			  name = rs.getString("name");				
  			} else {
  			  name = "not found";
  			}
			response = name;
	    }	else { // If not number say I don't understand
	    	response = "Do not understand: " + request;
	    }
	    res.println(response);
    } catch (Exception e) {
        Log.print(e);
    } finally {
      Log.printf("Disconnected from %s:%d\n", client.getInetAddress(), client.getPort());
    }
	}

	public static void main(String[] args) throws Exception {
		int port = 0;
    InetAddress host = InetAddress.getLocalHost();
    File serverLocator = new File("/tmp/" + VendorIDtoNameTCPService.class.getName());

    serverLocator.deleteOnExit();
    if (!serverLocator.exists()) {
      serverLocator.createNewFile();
    }

    try (ServerSocket server = new ServerSocket(port, 0, host)) {
    	Log.printf("Server listening on %s:%d\n", server.getInetAddress(), server.getLocalPort());

    	// Write this service's Host and Port address to a file, so that
    	// the VendorsEngine can automatically retrieve it.
    	try (PrintStream out = new PrintStream(serverLocator, "UTF-8")) {
    	  out.printf("%s:%d\n", server.getInetAddress().getHostAddress(), server.getLocalPort());
    	}

    	while (true) {
    		Socket client = server.accept();
    		(new VendorIDtoNameTCPService(client)).start();
    	}
    }
	}
}