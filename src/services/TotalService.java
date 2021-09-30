package services;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.CalcEngine;


@WebServlet("/total")
public class TotalService extends HttpServlet {
  private static final long serialVersionUID = 1L;

  public TotalService() {
    super();
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    PrintStream out = new PrintStream(response.getOutputStream());
    HttpSession session = request.getSession(true);

    if (session.getAttribute("total") == null) {
      session.setAttribute("total", "0");
    }

    Map<String, String[]> parameters = request.getParameterMap();

    if (parameters.containsKey("a")) {
      String a          = request.getParameter("a");
      String total      = (String)session.getAttribute("total");
      CalcEngine engine = CalcEngine.getInstance();
      String newTotal   = engine.compute("add", a, total);

      session.setAttribute("total", newTotal);
      out.println(newTotal);
    } else {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }
}
