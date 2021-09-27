package services;

import java.io.PrintStream;
import java.net.URL;
import java.util.Scanner;


/**
 * A simple HTTP client. 
 *
 * Connects to and prints the world's first webpage 
 * created by Tim-Berners Lee at CERN in 1990.
 *
 */
public class HTTPClient {
  public static void main(String[] args) throws Exception {
    PrintStream out = System.out;
    
    try (Scanner in = new Scanner((new URL("http://info.cern.ch/hypertext/WWW/TheProject.html")).openStream())) {
      while (in.hasNextLine()) {
        out.println(in.nextLine());
      }
    }
  }
}
