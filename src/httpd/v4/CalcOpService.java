package httpd.v4;

import httpd.v4.server.HTTPServer;
import httpd.v4.server.RequestContext;
import httpd.v4.server.ResponseContext;
import httpd.v4.server.ServiceWorker;


public class CalcOpService extends ServiceWorker {

  public CalcOpService(HTTPServer server, String uri) {
    super(server, uri);
  }

  public void doRequest(RequestContext request, ResponseContext response) {
    response.setStatus(301);
    response.headers.put("Location", "/calc?op=" + request.resource.substring(1) + "&" + request.getParameters());
  }
}
