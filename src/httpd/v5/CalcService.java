package httpd.v5;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;

import httpd.v5.server.HTTPServer;
import httpd.v5.server.RequestContext;
import httpd.v5.server.ResponseContext;
import httpd.v5.server.ServiceWorker;
import model.CalcEngine;


public class CalcService extends ServiceWorker {

  public CalcService(HTTPServer server, String uri) {
    super(server, uri);
  }

  public void doRequest(RequestContext request, ResponseContext response) {
    Map<String, String> qs = request.parameters;
    PrintWriter body = response.body;

    if (Arrays.stream("op,a,b".split(",")).allMatch(k -> qs.containsKey(k))) {
      CalcEngine engine = CalcEngine.getInstance();
      String responseText;
      
      responseText = engine.compute(qs.get("op"), qs.get("a"), qs.get("b"));
      if (responseText == null) {
        response.setStatus(400);
      }
      body.println(responseText);
    } else {
      response.setStatus(400);
    }
  }
}
