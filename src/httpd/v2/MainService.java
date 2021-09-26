package httpd.v2;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


public class MainService extends HTTPServer {

  public MainService(Socket client) {
    super(client);
  }

  protected void doRequest(RequestContext req, ResponseContext res) throws Exception {
    switch (req.uri) {
      case "/":
        res.out.println("Hello! Welcome to the server.");
        break;

      case "/calc":
        if (req.qs.containsKey("op") &&
            req.qs.containsKey("a") && 
            req.qs.containsKey("b")) {
          String op = req.qs.get("op");
          double a = Double.parseDouble(req.qs.get("a"));
          double b = Double.parseDouble(req.qs.get("b"));
          
          switch (op) {
            case "add":      res.out.println(a + b); break;
            case "subtract": res.out.println(a - b); break;
            case "multiply": res.out.println(a * b); break;
            case "divide":   res.out.println(a / b); break;
            case "exponent": res.out.println(Math.pow(a, b)); break;
            default:
              res.setStatus(400);
          }
        } else {
          res.setStatus(400);          
        }
        break;

      case "/add":
      case "/subtract":
      case "/multiply":
      case "/divide":
      case "/exponent":
        res.setStatus(301);
        res.headers.put("Location", "/calc?op=" + req.uri.substring(1) + "&" + req.toQueryString(req.qs));
        break;

      case "/students":
        if (req.qs.containsKey("major") && 
            req.qs.containsKey("gpa")) {
          String url   = "jdbc:derby://localhost:64413/EECS";
          String query = "SELECT * FROM Roumani.Sis "
                       + "WHERE major = ? "
                       + "AND gpa >= ?";

          String major = req.qs.get("major");
          double gpa   = Double.parseDouble(req.qs.get("gpa"));

          try (Connection connection = DriverManager.getConnection(url)) {
            log.printf("Connected to database: %s\n", connection.getMetaData().getURL());

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
              
              res.headers.put("Content-Type", "application/json");
              res.out.println(jsonRoot);
            }
          } catch (SQLException e) {
            log.println(e);
            res.setStatus(500);
          } finally {
            log.println("Disconnected from database.");
          }
        } else {
          res.setStatus(400);          
        }
        break;

      default:
        res.setStatus(404);
    }
  }

  /// Main

  public static void main(String[] args) throws Exception {
    int port = 0;
    InetAddress host = InetAddress.getLocalHost(); // .getLoopbackAddress();
    try (ServerSocket server = new ServerSocket(port, 0, host)) {
      log.printf("Server listening on %s:%d\n", server.getInetAddress(), server.getLocalPort());
      while (true) {
        (new MainService(server.accept())).start();
      }
    }
  }
}
