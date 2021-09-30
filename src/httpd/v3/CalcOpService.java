package httpd.v3;

import httpd.v3.server.RequestContext;
import httpd.v3.server.ResponseContext;
import httpd.v3.server.Service;


public class CalcOpService implements Service {
  public void doRequest(RequestContext request, ResponseContext response) throws Exception {
    response.setStatus(301);
    response.headers.put("Location", "/calc?op=" + request.uri.substring(1) + "&" + request.toQueryString(request.qs));
  }
}
