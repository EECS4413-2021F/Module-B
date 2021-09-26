package httpd.v1;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


/**
 * HTTP Server example v1.3
 * 
 * Implements a number of endpoints over HTTP.
 * The root endpoint simply returns a plaintext message
 * "Hello! Welcome to the server.". The /calc endpoint
 * does simple arithmetic with the given query parameters.
 * The various arithmetic operations can be requested
 * directly and will redirect to /calc. The /students
 * endpoint queries the SIS table in the Derby database
 * and returns the students in the given major with a GPA
 * equal or greater than the value given.
 * 
 * Supports the following request:
 * 
 *    GET /
 *    GET /students?major=<major>&gpa=<gpa>
 *    GET /calc?op=<op>&a=<number>&b=<number>
 *    GET /<op>?a=<number>&b=<number>
 *
 * where <op> is one of:
 *    - add
 *    - subtract
 *    - multiply
 *    - divide
 *    - exponent
 *
 * **Important Note:**
 * 
 * This is the third iteration of this implementation.
 * In iteration one, we took the business logic out of 
 * the `run` method and put it in a dedicated `doRequest`
 * method. In this version, we take that method entirely
 * out of the class, into the subclass MainService.
 * We made this class abstract and left the `doRequest`
 * method signature as an abstract method. We adjusted
 * the modifiers on the class variables to protected
 * so the subclass can access them.
 * 
 */
public abstract class HTTPServer3 extends Thread {
  
  protected static final PrintStream log = System.out;
  protected static final Map<Integer, String> httpResponseCodes = new HashMap<>();
  
  static {
    httpResponseCodes.put(100, "HTTP CONTINUE");
    httpResponseCodes.put(101, "SWITCHING PROTOCOLS");
    httpResponseCodes.put(200, "OK");
    httpResponseCodes.put(201, "CREATED");
    httpResponseCodes.put(202, "ACCEPTED");
    httpResponseCodes.put(203, "NON AUTHORITATIVE INFORMATION");
    httpResponseCodes.put(204, "NO CONTENT");
    httpResponseCodes.put(205, "RESET CONTENT");
    httpResponseCodes.put(206, "PARTIAL CONTENT");
    httpResponseCodes.put(300, "MULTIPLE CHOICES");
    httpResponseCodes.put(301, "MOVED PERMANENTLY");
    httpResponseCodes.put(302, "MOVED TEMPORARILY");
    httpResponseCodes.put(303, "SEE OTHER");
    httpResponseCodes.put(304, "NOT MODIFIED");
    httpResponseCodes.put(305, "USE PROXY");
    httpResponseCodes.put(400, "BAD REQUEST");
    httpResponseCodes.put(401, "UNAUTHORIZED");
    httpResponseCodes.put(402, "PAYMENT REQUIRED");
    httpResponseCodes.put(403, "FORBIDDEN");
    httpResponseCodes.put(404, "NOT FOUND");
    httpResponseCodes.put(405, "METHOD NOT ALLOWED");
    httpResponseCodes.put(406, "NOT ACCEPTABLE");
    httpResponseCodes.put(407, "PROXY AUTHENTICATION REQUIRED");
    httpResponseCodes.put(408, "REQUEST TIME OUT");
    httpResponseCodes.put(409, "CONFLICT");
    httpResponseCodes.put(410, "GONE");
    httpResponseCodes.put(411, "LENGTH REQUIRED");
    httpResponseCodes.put(412, "PRECONDITION FAILED");
    httpResponseCodes.put(413, "REQUEST ENTITY TOO LARGE");
    httpResponseCodes.put(414, "REQUEST URI TOO LARGE");
    httpResponseCodes.put(415, "UNSUPPORTED MEDIA TYPE");
    httpResponseCodes.put(500, "INTERNAL SERVER ERROR");
    httpResponseCodes.put(501, "NOT IMPLEMENTED");
    httpResponseCodes.put(502, "BAD GATEWAY");
    httpResponseCodes.put(503, "SERVICE UNAVAILABLE");
    httpResponseCodes.put(504, "GATEWAY TIME OUT");
    httpResponseCodes.put(505, "HTTP VERSION NOT SUPPORTED");
  }
  
  protected Socket client;
  
  public HTTPServer3(Socket client) {
    this.client = client;
  }

  /// Request

  protected String[] getURIComponents(String uri) {
    if (!uri.contains("?")) {
      return new String[]{ uri, "" };
    } else {
      return uri.split("\\?", 2);
    }
  }
  
  protected Map<String, String> getHeaders(Scanner req) {
    Map<String, String> headers = new HashMap<>();

    String buffer;
    String[] pair;

    while (req.hasNextLine()) {
      buffer = req.nextLine();
      if (buffer.isEmpty()) break;
      pair = buffer.split(":\\s+", 2);
      if (pair.length != 2) continue; // malformed
      headers.put(pair[0], pair[1]);
    }
    return headers;
  }

  protected Map<String, String> getQueryStrings(String qs) throws Exception {
    Map<String, String> queries = new HashMap<>();
    String[] fields = qs.split("&");
    
    for (String field : fields) {
      String[] pairs = field.split("=", 2);
      if (pairs.length == 2) {
        queries.put(pairs[0], URLDecoder.decode(pairs[1], "UTF-8"));
      }
    }

    return queries;
  }

  protected String toQueryString(Map<String, String> qs) throws Exception {
    List<String> params = new ArrayList<>();   
    for (String key : qs.keySet()) {
      params.add(key + "=" + URLEncoder.encode(qs.get(key), "UTF-8"));
    }
    return String.join("&", params);
  }

  protected String getRequestBody(Scanner req) {
    String buffer;
    String body = "";

    while (req.hasNextLine()) {
      buffer = req.nextLine();
      if (buffer.isEmpty()) break;
      body += buffer;
    }
    return body;
  }

  /// Response

  protected void sendHeaders(PrintStream res, int code, Map<String, Object> headers) {
    res.printf("HTTP/1.1 %d %s\n", code, httpResponseCodes.get(code));
    headers.entrySet().stream().forEach(e -> {
      res.printf("%s: %s\n", e.getKey(), e.getValue().toString());
    });
    res.println();
  }

  /// Handling Requests

  protected abstract void doRequest(Request req, Response res) throws Exception;

  public void run() {
    final String clientAddress = String.format("%s:%d", client.getInetAddress(), client.getPort());
    log.printf("Connected to %s\n", clientAddress);

    try (
      Socket client   = this.client; // Makes sure that client is closed at end of try-statement.
      Scanner req     = new Scanner(client.getInputStream());
      PrintStream res = new PrintStream(client.getOutputStream(), true);
    ) {
      String request = req.nextLine();
      String method, uri, version;

      try (Scanner parse = new Scanner(request)) {
        method   = parse.next().toUpperCase();
        uri      = parse.next();
        version  = parse.next().toUpperCase(); 
      }

      Map<String, String> reqHeaders = getHeaders(req);
      Map<String, Object> resHeaders = new HashMap<>();

      Response httpResponse = new Response(200, resHeaders);

      resHeaders.put("Server", "Java HTTP Server : 1.0");
      resHeaders.put("Date", new Date());
      resHeaders.put("Content-Type", "text/plain");
      
      try {
        if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
          httpResponse.setStatus(501);
        } else if (!version.equals("HTTP/1.1")) {
          httpResponse.setStatus(505);
        } else {
          String[] components = getURIComponents(uri);
          uri = components[0];
          Map<String, String> qs = getQueryStrings(components[1]);
          String body = null;

          if (method.equals("POST")) {
            body = getRequestBody(req);
          }

          Request httpRequest = new Request(method, uri, reqHeaders, qs, body);

          doRequest(httpRequest, httpResponse);
        }
      } catch (Exception err) {       
        log.println(err.getMessage());
        err.printStackTrace(log);
        httpResponse.setStatus(500);
      }

      int status = httpResponse.getStatus();
      StringWriter response = httpResponse.response;

      if (status != 200 && response.toString().length() == 0) {
        httpResponse.out.println(httpResponseCodes.get(status));
      }

      String responseText = response.toString();
      resHeaders.put("Content-Length", responseText.getBytes().length);      
      sendHeaders(res, status, resHeaders);

      if (!method.equals("HEAD")) {
        res.println(responseText);
      }

      res.flush(); // flush character output stream buffer      
    } catch (Exception e) {
      log.println(e);
    } finally {
      log.printf("Disconnected from %s\n", clientAddress);
    }
  }

  class Request {
    final String method;
    final String uri;
    final Map<String, String> headers;
    final Map<String, String> qs;
    final String body;
  
    public Request(
      String method, 
      String uri, 
      Map<String, String> headers, 
      Map<String, String> qs, 
      String body
    ) {
      this.method  = method;
      this.uri     = uri;
      this.headers = headers;
      this.qs      = qs;
      this.body    = body;
    }
  }
  
  class Response {
    private int status;
    final Map<String, Object> headers;
    final StringWriter response = new StringWriter();
    final PrintWriter  out      = new PrintWriter(response); 
  
    public Response(int status, Map<String, Object> headers) {
      this.status  = status;
      this.headers = headers;
    }
    public int getStatus() {
      return status;
    }
    public void setStatus(int status) {
      this.status = status;
    }
  }
}
