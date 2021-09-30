package httpd.v3;

import httpd.v3.server.RequestContext;
import httpd.v3.server.ResponseContext;
import httpd.v3.server.Service;


public class CalcService implements Service {
  public void doRequest(RequestContext request, ResponseContext response) {
    if (request.qs.containsKey("op") &&
        request.qs.containsKey("a") && 
        request.qs.containsKey("b")) {
      String op = request.qs.get("op");
      double a = Double.parseDouble(request.qs.get("a"));
      double b = Double.parseDouble(request.qs.get("b"));

      switch (op) {
        case "add":      response.out.println(a + b); break;
        case "subtract": response.out.println(a - b); break;
        case "multiply": response.out.println(a * b); break;
        case "divide":   response.out.println(a / b); break;
        case "exponent": response.out.println(Math.pow(a, b)); break;
        default:
          response.setStatus(400);
      }
    } else {
      response.setStatus(400);          
    }
  }
}
