package services;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.CalcEngine;


@WebServlet("/calc")
public class CalcService extends HttpServlet {
	private static final long serialVersionUID = 1L;

  public CalcService() {
    super();
  }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	  response.setContentType("text/plain");
	  
	  PrintStream out = new PrintStream(response.getOutputStream(), true);
	  Map<String, String[]> params = request.getParameterMap();

	  if (!params.containsKey("a") ||
        !params.containsKey("b") ||
        !params.containsKey("op")) {
	    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
	  }

    String op = request.getParameter("op").toLowerCase();
    String a  = request.getParameter("a");
    String b  = request.getParameter("b");
    
    CalcEngine engine = CalcEngine.getInstance();
    String responseText;

    try {
      responseText = engine.compute(op, a, b);
      if (responseText == null) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      }      
    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      responseText = e.toString();
    }

    out.print(responseText);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
