package httpd.v4;

import httpd.v4.server.HTTPServer;
import httpd.v4.server.RequestContext;
import httpd.v4.server.ResponseContext;
import httpd.v4.server.ServiceWorker;


public class HelloService extends ServiceWorker {

  public HelloService(HTTPServer server, String uri) {
    super(server, uri);
  }

  public void doRequest(RequestContext request, ResponseContext response) {
    response.body.println("Hello! Welcome to the server.");
  }
}
