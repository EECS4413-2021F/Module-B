package httpd.v3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import httpd.v3.server.HTTPServer;
import httpd.v3.server.RequestContext;
import httpd.v3.server.ResponseContext;
import httpd.v3.server.Service;


public class StudentsService implements Service {
  public static final String dbURL = "jdbc:derby://localhost:64413/EECS";
  public static final String query = "SELECT * FROM Roumani.Sis "
                                   + "WHERE major = ? "
                                   + "AND gpa >= ?";  

  public void doRequest(RequestContext request, ResponseContext response) throws Exception {
    if (request.qs.containsKey("major") && 
        request.qs.containsKey("gpa")) {

      String major = request.qs.get("major");
      double gpa   = Double.parseDouble(request.qs.get("gpa"));

      try (Connection connection = DriverManager.getConnection(dbURL)) {
        HTTPServer.log.printf("Connected to database: %s\n", connection.getMetaData().getURL());

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
          response.out.println(jsonRoot);
        }
      } catch (SQLException e) {
        HTTPServer.log.println(e);
        response.setStatus(500);
      } finally {
        HTTPServer.log.println("Disconnected from database.");
      }
    } else {
      response.setStatus(400);          
    }
  }
}
