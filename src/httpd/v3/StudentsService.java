package httpd.v3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import httpd.v3.server.HTTPServer;
import httpd.v3.server.RequestContext;
import httpd.v3.server.ResponseContext;
import httpd.v3.server.ServiceWorker;


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

    if (!qs.containsKey("major") || !qs.containsKey("gpa")) {
      response.setStatus(400);      
    } else {
      String major;
      double gpa;

      try {
        major = qs.get("major");
        gpa   = Double.parseDouble(qs.get("gpa"));
      } catch (Exception e) {
        response.setStatus(400);
        return;
      }

      try (Connection connection = DriverManager.getConnection(dbURL)) {
        server.insertLogEntry("Database Connection Established:", connection.getMetaData().getURL());

        try (PreparedStatement statement = connection.prepareStatement(query)) {
          statement.setString(1, major);
          statement.setDouble(2, gpa);
   
          JsonObject jsonRoot = new JsonObject();
          JsonArray  students = new JsonArray(); 

          try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
              JsonObject student = new JsonObject();

              student.addProperty("id",           rs.getInt("id"));
              student.addProperty("surname",      rs.getString("surname"));
              student.addProperty("givenName",    rs.getString("givenname"));
              student.addProperty("gpa",          rs.getDouble("gpa"));
              student.addProperty("yearAdmitted", rs.getInt("yearadmitted"));

              students.add(student);
            }
          }

          jsonRoot.add("students", students);
          
          response.headers.put("Content-Type", "application/json");
          response.body.println(jsonRoot);          
        }
      } catch (SQLException e) {
        server.insertLogEntry("Database Exception:", e.getMessage());
        response.setStatus(500);
      } finally {
        server.insertLogEntry("Database Connection Ended:", dbURL);
      }
    }
  }
}
