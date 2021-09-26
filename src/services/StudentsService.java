package services;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.StudentsDAO;


@WebServlet("/students")
public class StudentsService extends HttpServlet {
	private static final long serialVersionUID = 1L;

  public StudentsService() {
    super();
  }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	  response.setContentType("text/plain");

	  PrintStream out = new PrintStream(response.getOutputStream(), true);
    Map<String, String[]> params = request.getParameterMap();

	  if (!params.containsKey("major") ||
	      !params.containsKey("gpa")   ||
	      !params.containsKey("format")) {
	    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	    return;
	  }

    String major  = request.getParameter("major");
    String gpa    = request.getParameter("gpa");
    String format = request.getParameter("format").toLowerCase();

    if (!format.equals("xml") && !format.equals("json")) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    StudentsDAO dao = StudentsDAO.getInstance();
    String responseText;

    try {    
      responseText = dao.doQuery(major, gpa, format);
      if (responseText == null) {
        response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
      } else {
        response.setContentType("application/" + format);
      }
    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
      responseText = e.getMessage();
    }

    out.print(responseText);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
