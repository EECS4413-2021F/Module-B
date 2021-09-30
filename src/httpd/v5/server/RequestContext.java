package httpd.v5.server;

import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;


/**
 * This class handles the incoming request and all of the logic involved. It
 * exposes an interface for the services to query aspects of the request.
 */
public class RequestContext {

  private static List<String> methods = new ArrayList<>();

  static {
    methods.add("GET");
    methods.add("HEAD");
    methods.add("POST");
  }

  public final HTTPServer server;
  public final InputStream input;
  public final Scanner in;

  public String method;
  public String resource;
  public String version;
  public Map<String, String> headers;
  public Map<String, String> parameters;
  public String body;

  public RequestContext(HTTPServer server, InputStream input) {
    this.server = server;
    this.input  = input;
    this.in     = new Scanner(input);
  }

  public void processRequest(ResponseContext response) {
    String request = in.nextLine();
    StringTokenizer parse = new StringTokenizer(request);

    this.method  = parse.nextToken().toUpperCase();
    String uri   = parse.nextToken();
    this.version = parse.nextToken().toUpperCase();

    String[] uriComponents = parseURIComponents(uri);

    this.resource   = uriComponents[0];
    this.headers    = parseHeaders(in);
    this.body       = "";

    try {
      this.parameters = parseQueryStrings(uriComponents[1]);
    } catch (Exception e) {
      response.setStatus(400);
    }

    if ("POST".equals(this.method)) {
      readRequestBody(in);
    }

    if (!methods.contains(this.method)) {
      response.setStatus(501);
    } else if (!"HTTP/1.1".equals(this.version)) {
      response.setStatus(505);
    }
  }

  public String getParameters() {
    try {
      return toQueryString(this.parameters);
    } catch (Exception e) {
      return "";
    }
  }

  public String toString() {
    String qs = getParameters();

    return this.method + " "
         + this.resource
         + (qs.isEmpty() ? "" : "?" + qs);
  }

  private String[] parseURIComponents(String uri) {
    if (!uri.contains("?")) {
      return new String[]{ uri, "" };
    } else {
      return uri.split("\\?", 2);
    }
  }

  private Map<String, String> parseHeaders(Scanner request) {
    Map<String, String> headers = new HashMap<>();

    String buffer;
    String[] pair;

    while (request.hasNextLine()) {
      buffer = request.nextLine();
      if (buffer.isEmpty()) break;
      pair = buffer.split(":\\s+", 2);
      if (pair.length != 2) continue; // malformed
      headers.put(pair[0], pair[1]);
    }
    return headers;
  }

  private Map<String, String> parseQueryStrings(String qs) throws Exception {
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

  private String toQueryString(Map<String, String> qs) throws Exception {
    List<String> params = new ArrayList<>();
    for (String key : qs.keySet()) {
      params.add(key + "=" + URLEncoder.encode(qs.get(key), "UTF-8"));
    }
    return String.join("&", params);
  }

  private String readRequestBody(Scanner request) {
    String buffer;
    String body = "";

    while (request.hasNextLine()) {
      buffer = request.nextLine();
      if (buffer.isEmpty()) break;
      body += buffer;
    }
    return body;
  }
}
