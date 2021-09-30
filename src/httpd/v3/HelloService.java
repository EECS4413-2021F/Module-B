package httpd.v3;

import httpd.v3.server.RequestContext;
import httpd.v3.server.ResponseContext;
import httpd.v3.server.Service;


public class HelloService implements Service {
  public void doRequest(RequestContext request, ResponseContext response) {
    response.out.println("Hello! Welcome to the server.");
  }
}
