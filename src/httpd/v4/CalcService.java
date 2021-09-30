package httpd.v4;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;

import httpd.v4.server.HTTPServer;
import httpd.v4.server.RequestContext;
import httpd.v4.server.ResponseContext;
import httpd.v4.server.ServiceWorker;


public class CalcService extends ServiceWorker {

  public CalcService(HTTPServer server, String uri) {
    super(server, uri);
  }

  public void doRequest(RequestContext request, ResponseContext response) {
    Map<String, String> qs = request.parameters;
    PrintWriter body = response.body;

    if (Arrays.stream("op,a,b".split(",")).allMatch(k -> qs.containsKey(k))) {
      double a, b;
      try {
        a = Double.parseDouble(qs.get("a"));
        b = Double.parseDouble(qs.get("b"));
      } catch (Exception e) {
        response.setStatus(400);
        return;
      }
      switch (qs.get("op")) {
        case "add":      body.println(a + b); break;
        case "subtract": body.println(a - b); break;
        case "multiply": body.println(a * b); break;
        case "divide":   body.println(a / b); break;
        case "exponent": body.println(Math.pow(a, b)); break;
        default:
          response.setStatus(400);
      }
    } else {
      response.setStatus(400);
    }
  }
}
