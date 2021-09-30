package services;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet({ "/add", "/subtract", "/multiply", "/divide", "/exponent" })
public class CalcOpService extends HttpServlet {
	private static final long serialVersionUID = 1L;

  public CalcOpService() {
    super();
  }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	  File requestPath = new File(request.getRequestURI());

	  response.sendRedirect(request.getContextPath() + "/calc?op=" + requestPath.getName() + "&" + request.getQueryString());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
