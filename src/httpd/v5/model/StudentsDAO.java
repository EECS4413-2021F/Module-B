package httpd.v5.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.google.gson.Gson;


public class StudentsDAO {

  private static StudentsDAO singleton = null;

  public static final String dbURL = "jdbc:derby://localhost:64413/EECS";
  public static final String query = "SELECT * FROM Roumani.Sis "
                                   + "WHERE major = ? "
                                   + "AND gpa >= ?";  
  
  private StudentsDAO() { }

  public Students runQuery(String major, String gpaStr) throws SQLException {
    double gpa = Double.parseDouble(gpaStr);
 
    Students students = new Students();
    
    try (Connection connection = DriverManager.getConnection(dbURL)) {
      try (PreparedStatement statement = connection.prepareStatement(query)) {
        statement.setString(1, major);
        statement.setDouble(2, gpa);
 
        try (ResultSet rs = statement.executeQuery()) {
          while (rs.next()) {
            Student student = new Student();

            student.setId(rs.getInt("id"));
            student.setSurname(rs.getString("surname"));
            student.setGivenName(rs.getString("givenname"));
            student.setGpa(rs.getDouble("gpa"));
            student.setYearAdmitted(rs.getInt("yearadmitted"));

            students.add(student);
          }
        }
      }
    }

    return students;
  }

  public String doQuery(String major, String gpaStr, String format) throws Exception {
    if (format.toLowerCase().equals("xml")) {
      return doQueryAsXML(major, gpaStr);
    } else {
      return doQueryAsJson(major, gpaStr);
    }
  }
  
  public String doQueryAsJson(String major, String gpaStr) throws SQLException {
    return (new Gson()).toJson(runQuery(major, gpaStr));
  }

  public String doQueryAsXML(String major, String gpaStr) throws SQLException, JAXBException, IOException {
    Students students = runQuery(major, gpaStr);

    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      JAXBContext context = JAXBContext.newInstance(Students.class);
      Marshaller m = context.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      m.marshal(students, baos);
      return baos.toString();
    }
  }

  public static StudentsDAO getInstance() {
    if (singleton == null) {
      singleton = new StudentsDAO();
    }
    return singleton;
  }
}
