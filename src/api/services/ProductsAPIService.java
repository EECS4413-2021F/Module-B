package api.services;

import java.io.IOException;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import api.model.APIRequest;
import api.model.APIResponse;
import api.model.Product;
import api.model.ProductFilter;
import api.model.Products;
import api.model.ProductsDAO;


@WebServlet(
  name = "Products",
  urlPatterns = {
    "/products", 
    "/product/*"
  }
)
public class ProductsAPIService extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String
	    REQUEST_CANNOT_PARSE = "Cannot parse request as a Product or Products object"
	  ;

	@Override
	public void init(ServletConfig config) throws ServletException {
		try {
      config.getServletContext().setAttribute("ProductsDAO", ProductsDAO.getInstance());
    } catch (NamingException | SQLException e) {
      throw new ServletException(e);
    }
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException
	{
	  setRequestID(request);

	  if (request.getRequestURI().startsWith(request.getContextPath() + "/product/") && 
	      request.getPathInfo().length() > 1) {
	    doGetOne(request, response);
	  } else {
	    doGetMany(request, response);
	  }
	}

	@Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
	  doPut(request, response);
  }

  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    setRequestID(request);
    
    if (request.getRequestURI().startsWith(request.getContextPath() + "/product/") && 
        request.getPathInfo().length() > 1) {
      doPutOne(request, response);
    } else {
      doPutMany(request, response);
    }
  }

  @Override
  protected void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    setRequestID(request);

    if (request.getRequestURI().startsWith(request.getContextPath() + "/product/") && 
        request.getPathInfo().length() > 1) {
      doDeleteOne(request, response);
    }
  }
  

  /// Helpers
  
  private void setRequestID(HttpServletRequest request) {
    HttpSession session = request.getSession(true);  
    if (session.getAttribute("rid") == null) session.setAttribute("rid", 0);
    int i = (int)session.getAttribute("rid");
    session.setAttribute("rid", i + 1);
  }

  /// Handlers

  protected void doGetOne(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    ServletContext sc = request.getServletContext();
    ProductsDAO dao   = (ProductsDAO)sc.getAttribute("ProductsDAO");
    APIResponse res   = new APIResponse(request, response);
    String pid        = request.getPathInfo().substring(1);

    try {
      res.serializeGetOne(dao.getProductById(pid));
    } catch (Exception e) {
      e.printStackTrace();
      if (e instanceof SQLException) {
        response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
      } else {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
      res.serializeException(e);
    }
  }

  protected void doGetMany(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    ServletContext sc   = request.getServletContext();
    HttpSession session = request.getSession(true);
    APIResponse res     = new APIResponse(request, response);
    ProductsDAO dao     = (ProductsDAO)sc.getAttribute("ProductsDAO");
    String chain        = request.getParameter("chain");

    ProductFilter filter = chain == null || chain.equals("false")
        ? new ProductFilter()
        : (session.getAttribute("filter") == null
            ? new ProductFilter()
            : (ProductFilter)session.getAttribute("filter")
        );

    try {
      filter.populate(request.getParameterMap());
      session.setAttribute("filter", filter);
      res.serializeGetMany(dao.getProducts(filter));
    } catch (Exception e) {
      e.printStackTrace();
      if (e instanceof SQLException) {
        response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
      } else {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
      res.serializeException(e);
    }
  }

  protected void doPutMany(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    ServletContext sc = request.getServletContext();
    APIRequest req    = APIRequest.getInstance();
    APIResponse res   = new APIResponse(request, response);
    ProductsDAO dao   = (ProductsDAO)sc.getAttribute("ProductsDAO");

    boolean update = "true".equals(request.getParameter("update"));

    try {
      JsonElement json = req.getRequestBody(request);

      if (req.isProducts(json, update)) {
        Products products = req.getProductsFromJson(json, update);
        Products changed  = new Products();

        for (Product p : products.getProducts()) {
          if (update) {
            dao.updateProduct(p);
          } else {
            dao.addProduct(p);
          }
          changed.add(dao.getProductById(p.getId()));
        }
        if (update) {
          res.serializeUpdateMany(changed);
        } else {
          res.serializeCreateMany(changed);
        }
      } else if (req.isProduct(json, update)) {
        Product product = req.getProductFromJson(json, update);
        if (update) {
          dao.updateProduct(product);
          res.serializeUpdateOne(dao.getProductById(product.getId()));
        } else {
          dao.addProduct(product);
          res.serializeCreateOne(dao.getProductById(product.getId()));
        }        
      } else {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        JsonObject output = res.serializeException(REQUEST_CANNOT_PARSE, false);
        output.add("failed_request", json);
        res.endResponse(output);
      }
    } catch (Exception e) {
      e.printStackTrace();
      if (e instanceof SQLException) {
        response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
      } else if (e instanceof RuntimeException) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      } else {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
      res.serializeException(e);
    }
  }

  protected void doPutOne(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    ServletContext sc = request.getServletContext();
    APIRequest req    = APIRequest.getInstance();
    APIResponse res   = new APIResponse(request, response);
    ProductsDAO dao   = (ProductsDAO)sc.getAttribute("ProductsDAO");
    String pid        = request.getPathInfo().substring(1);

    try {
      JsonElement json = req.getRequestBody(request);

      if (req.isProduct(json, true)) {
        Product product = req.getProductFromJson(json, true);
        dao.updateProduct(product, pid);
        res.serializeUpdateOne(dao.getProductById(pid));
      } else {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        JsonObject output = res.serializeException(REQUEST_CANNOT_PARSE, false);
        output.add("failed_request", json);
        res.endResponse(output);
      }
    } catch (Exception e) {
      e.printStackTrace();
      if (e instanceof SQLException) {
        response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
      } else if (e instanceof RuntimeException) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      } else {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
      res.serializeException(e);
    }
  }

  protected void doDeleteOne(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {   
    ServletContext sc = request.getServletContext();
    ProductsDAO dao   = (ProductsDAO)sc.getAttribute("ProductsDAO");
    APIResponse res   = new APIResponse(request, response);
    String pid        = request.getPathInfo().substring(1);

    try {
      Product product = dao.getProductById(pid);
      dao.deleteProduct(pid);
      res.serializeDeleteOne(dao.getProductById(pid), product, pid);
    } catch (Exception e) {
      e.printStackTrace();
      if (e instanceof SQLException) {
        response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
      } else {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
      res.serializeException(e);
    }
  }
}
