package services;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.VendorsEngine;


@WebServlet("/idToNameConcat")
public class VendorIDtoNamesService extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
  public VendorIDtoNamesService() {
    super();
  }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// We want to return a comma delimited list of the searched up names
		VendorsEngine engine = VendorsEngine.getInstance();
		Writer out = response.getWriter();
		HttpSession session = request.getSession(true);
		
		// Set the response type
		response.setContentType("text/plain");

		// If session doesn't have name, set it to empty string	
		if (session.getAttribute("name") == null) {
			session.setAttribute("name", "");
		}

		String concat_name = (String) session.getAttribute("name");	
		Map<String, String[]> parameters = request.getParameterMap();
		String resp;

		// If given id
		if (parameters.containsKey("id")) {
			// Get name from id
			String id = request.getParameter("id");
			String name = engine.runIDtoName(id);		
				
			// Do not append to comma separated 'list' if id not found
			if (name.startsWith("not found") || 
					name.startsWith("Do not understand: ") || 
					name.startsWith("Failed to complete connection with ID") ||
					name.startsWith("Failed to get ID service's host and port addresses")) {

				resp = name + "\n" + "List of names: " + concat_name;
			} else {
				// Add to list of names if found
				String new_name;
				if (concat_name.equals("")) {
					new_name = name; 
				} else {
					new_name = concat_name + ", " + name; 					
				}
				session.setAttribute("name", new_name);
				resp = "List of names: " + new_name;
				out.write(resp);
			}
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
