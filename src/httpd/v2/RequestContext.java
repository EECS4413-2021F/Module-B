package httpd.v2;

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

  public final InputStream input;
  public final Scanner req;

  public String method;
  public String uri;
  public String version;
  public String body;

  public Map<String, String> headers;
  public Map<String, String> qs;

  public RequestContext(InputStream input) {
    this.input = input;
    this.req   = new Scanner(input);
  }

  public void processRequest(ResponseContext response) {
    String request = req.nextLine();
    StringTokenizer parse = new StringTokenizer(request);

    this.method  = parse.nextToken().toUpperCase();
    String uri   = parse.nextToken();
    this.version = parse.nextToken().toUpperCase();

    String[] uriComponents = getURIComponents(uri);

    this.uri     = uriComponents[0];
    this.headers = getHeaders(req);
    this.body    = "";

    try {
      this.qs = getQueryStrings(uriComponents[1]);
    } catch (Exception e) {
      response.setStatus(400);
    }

    if ("POST".equals(this.method)) {
      getRequestBody(req);
    }

    if (!this.method.equals("GET")  &&
        !this.method.equals("POST") &&
        !this.method.equals("HEAD")) {
      response.setStatus(501);
    } else if (!"HTTP/1.1".equals(this.version)) {
      response.setStatus(505);
    }
  }

  public String[] getURIComponents(String uri) {
    if (!uri.contains("?")) {
      return new String[]{ uri, "" };
    } else {
      return uri.split("\\?", 2);
    }
  }

  public Map<String, String> getHeaders(Scanner req) {
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

  public Map<String, String> getQueryStrings(String qs) throws Exception {
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

  public String toQueryString(Map<String, String> qs) throws Exception {
    List<String> params = new ArrayList<>();
    for (String key : qs.keySet()) {
      params.add(key + "=" + URLEncoder.encode(qs.get(key), "UTF-8"));
    }
    return String.join("&", params);
  }

  public String getRequestBody(Scanner req) {
    String buffer;
    String body = "";

    while (req.hasNextLine()) {
      buffer = req.nextLine();
      if (buffer.isEmpty()) break;
      body += buffer;
    }
    return body;
  }
}
