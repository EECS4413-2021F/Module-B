package httpd.v5;

import java.util.Map;

import httpd.v5.server.HTTPServer;
import httpd.v5.server.RequestContext;
import httpd.v5.server.ResponseContext;
import httpd.v5.server.ServiceWorker;
import model.StudentsDAO;


public class StudentsService extends ServiceWorker {
  public static final String dbURL = "jdbc:derby://localhost:64413/EECS";
  public static final String query = "SELECT * FROM Roumani.Sis "
                                   + "WHERE major = ? "
                                   + "AND gpa >= ?";  

  public StudentsService(HTTPServer server, String uri) {
    super(server, uri);
  }

  public void doRequest(RequestContext request, ResponseContext response) {
    Map<String, String> qs = request.parameters;

    if (!qs.containsKey("major") || !qs.containsKey("gpa") || !qs.containsKey("format")) {
      response.setStatus(400);      
    } else {
      String major  = qs.get("major");
      String gpa    = qs.get("gpa");
      String format = qs.get("format").toLowerCase();

      if (!format.equals("xml") && !format.equals("json")) {
        response.setStatus(400);
        return;
      }

      StudentsDAO dao = StudentsDAO.getInstance();
      String responseText;

      try {    
        responseText = dao.doQuery(major, gpa, format);
        if (responseText == null) {
          response.setStatus(502);
        } else {
          response.headers.put("Content-Type", "application/" + format);
        }
      } catch (Exception e) {
        response.setStatus(400);
        responseText = e.getMessage();
      }
    }
  }
}
