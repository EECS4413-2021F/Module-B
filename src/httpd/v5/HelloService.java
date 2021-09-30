package httpd.v5;

import httpd.v5.server.HTTPServer;
import httpd.v5.server.RequestContext;
import httpd.v5.server.ResponseContext;
import httpd.v5.server.ServiceWorker;


public class HelloService extends ServiceWorker {

  public HelloService(HTTPServer server, String uri) {
    super(server, uri);
  }

  public void doRequest(RequestContext request, ResponseContext response) {
    response.body.println("Hello! Welcome to the server.");
  }
}
