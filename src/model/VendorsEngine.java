package model;

import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;


public class VendorsEngine {
  private static VendorsEngine engine = null;
  private static PrintStream log = System.out;
	
	private VendorsEngine() {	}
	
	public static VendorsEngine getInstance() {
		if (engine == null) engine = new VendorsEngine();
		return engine;
	}
	
	public String runIDtoName(String id, String idHost, int idPort) {
		// Host and Port from running VendorIDtoNameTCPService.java	  

		// Try to connect to it
		try (Socket idService = new Socket(idHost, idPort); 
				PrintStream req   = new PrintStream(idService.getOutputStream(), true); 
				Scanner res       = new Scanner(idService.getInputStream());
		) {
			log.printf("Connected to %s:%d\n", idService.getInetAddress(), idService.getPort());
		
			// Send request and get response 
			req.printf("%s \n", id);
			String name =  res.nextLine();
			return name;
		} catch (Exception e) {
			log.println(e);
			return "Failed to complete connection with ID";
		} finally {
			log.printf("Disconnected from ID %s:%d\n", idHost, idPort);
		}
	}
}
