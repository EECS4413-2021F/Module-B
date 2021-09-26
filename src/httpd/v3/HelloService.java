package httpd.v3;

import httpd.v3.server.HTTPServer;
import httpd.v3.server.RequestContext;
import httpd.v3.server.ResponseContext;
import httpd.v3.server.ServiceWorker;


public class HelloService extends ServiceWorker {

  public HelloService(HTTPServer server, String uri) {
    super(server, uri);
  }

  public void doRequest(RequestContext request, ResponseContext response) {
    response.body.println("Hello! Welcome to the server.");
  }
}
