package httpd.v4.server;

import java.util.Date;
import java.util.Map;


public class RootWorker extends Worker {

  private static RootWorker singleton = null;

  protected final Map<String, ServiceWorker> services;
  
  private RootWorker(HTTPServer server, Map<String, ServiceWorker> services) {
    super(server);
    this.services = services;
  }

  protected void doRequest(RequestContext request, ResponseContext response) {
    
    boolean found = false;

    response.headers.put("Server", "Java HTTP Server : 1.0");
    response.headers.put("Date", new Date());
    response.headers.put("Content-Type", "text/plain");

    try {
      request.processRequest(response);
      server.insertLogEntry("Request Received:", request.toString());
      if (response.getStatus() == 200) {
        for (String uri : services.keySet()) {
          if (request.resource.equals(uri)) {
            services.get(uri).doRequest(request, response);
            found = true;
            break;
          }
        }
      }
    } catch (Exception e) {
      server.insertLogEntry("Handler Exception:", e.getMessage());
      response.setStatus(500);
    }

    if (response.getStatus() == 200 && !found) response.setStatus(404);
    if (response.getStatus() != 200 && response.getResponseText().isEmpty()) {
      response.body.println(response.getStatusText());
    }

    response.headers.put("Content-Length", response.getResponseText().getBytes().length);
    response.send(!request.method.equals("HEAD"));
  }

  public static RootWorker getInstance(HTTPServer server, Map<String, ServiceWorker> servlets) {
    if (singleton == null) {
      singleton = new RootWorker(server, servlets);
    }
    return singleton;
  }
}
