package httpd.v2;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * This class handles the outgoing response to the client and all of the logic
 * involved. It exposes an interface for the services to form and send the
 * response.
 */
public class ResponseContext {

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

  private int status;

  public final OutputStream output;
  public final Map<String, Object> headers = new HashMap<>();
  public final StringWriter response = new StringWriter();
  public final PrintWriter  out      = new PrintWriter(response);
  public final PrintStream  res;

  public ResponseContext(OutputStream output) {
    this.output = output;
    this.res    = new PrintStream(output, true);

    this.setStatus(200);
    this.defaultHeaders();
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getStatusText() {
    return httpResponseCodes.get(getStatus());
  }

  public String getResponseText() {
    return response.toString();
  }

  public void defaultHeaders() {
    headers.put("Server", "Java HTTP Server : 1.0");
    headers.put("Date", new Date());
    headers.put("Content-Type", "text/plain");
  }

  protected void sendHeaders(PrintWriter head) {
    head.printf("HTTP/1.1 %d %s\n", getStatus(), getStatusText());
    headers.entrySet().stream().forEach(e ->
        head.printf("%s: %s\n", e.getKey(), e.getValue().toString()));
    head.println();
  }

  public void send(boolean hasBody) {
    try (
      StringWriter hout = new StringWriter();
      PrintWriter  head = new PrintWriter(hout);
      StringWriter bout = this.response;
      PrintWriter  body = this.out;
    ) {
      sendHeaders(head);
      res.print(hout.toString());
      if (hasBody) res.print(bout.toString());
      res.flush();
    } catch (Exception e) {
      HTTPServer.log.println(e.getMessage());
    }
  }
}
