package httpd.v5.server;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;


/**
 * This class handles the outgoing response to the client and all of the logic
 * involved. It exposes an interface for the services to form and send the
 * response.
 */
public class ResponseContext {

  private static final Map<Integer, String> httpResponseCodes = new HashMap<>();

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

  public final HTTPServer server;
  public final OutputStream output;
  public final Map<String, Object> headers = new HashMap<>();
  public final PrintWriter body;
  public final PrintStream out;

  private int status;
  private StringWriter bout;

  public ResponseContext(HTTPServer server, OutputStream output) throws Exception {
    this.setStatus(200);

    this.server = server;
    this.output = output;
    this.bout   = new StringWriter();
    this.body   = new PrintWriter(bout);
    this.out    = new PrintStream(output);
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
    return bout.toString();
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
      StringWriter bout = this.bout;
      PrintWriter  body = this.body;
    ) {
      sendHeaders(head);
      out.print(hout.toString());
      if (hasBody) out.print(bout.toString());
      out.flush();
      server.insertLogEntry("Response Sent:", hout.toString().split("\n")[0]);
    } catch (Exception e) {
      server.insertLogEntry("Response Exception:", e.getMessage());
    }
  }
}
