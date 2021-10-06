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
    "/product/*",
    "/v1/products",   // We should version our RESTful APIs.
    "/v1/product/*"   // This is version 1.
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

	  if ((request.getRequestURI().startsWith(request.getContextPath() + "/product/") || 
	       request.getRequestURI().startsWith(request.getContextPath() + "/v1/product/")) &&
	       request.getPathInfo().length() > 1) {
	    doGetOne(request, response);   // GET /product/<id>
	  } else {
	    doGetMany(request, response);  // GET /products
	  }
	}

	@Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
	  // Strictly speaking POST is not a RESTful operation.
	  // However, we can use it to clearly distinguish between PUT (create) requests
	  // and PUT (update) request. All POST requests are treated as
	  // update requests.

	  doPut(request, response);
  }

  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    setRequestID(request);
    
    if ((request.getRequestURI().startsWith(request.getContextPath() + "/product/") ||
         request.getRequestURI().startsWith(request.getContextPath() + "/v1/product/")) &&
         request.getPathInfo().length() > 1) {
      doPutOne(request, response);  // PUT /product/<id>
    } else {
      doPutMany(request, response); // PUT /products
    }
  }

  @Override
  protected void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    setRequestID(request);

    // We only support deleting one Product at a time, but we could
    // have supported deleting Products in batch.
    // Maybe in a future version.

    if ((request.getRequestURI().startsWith(request.getContextPath() + "/product/") ||
         request.getRequestURI().startsWith(request.getContextPath() + "/v1/product/")) &&
         request.getPathInfo().length() > 1) {
      doDeleteOne(request, response); // DELETE /product/<id>
    }
  }


  /// Helpers
  
  /**
   * Increment a request counter for each session.
   * We return this request ID in each response, so a client
   * can keep track of the ordering of its requests and the
   * corresponding responses.
   *
   * @param request
   */
  private void setRequestID(HttpServletRequest request) {
    HttpSession session = request.getSession(true);  
    if (session.getAttribute("rid") == null) session.setAttribute("rid", 0);
    int i = (int)session.getAttribute("rid");
    session.setAttribute("rid", i + 1);
  }

  /// Handlers

  /**
   * Retrieve a single Product object with its given ID
   * specified within its URL: /product/<id>.
   * 
   * @param request
   * @param response
   * @throws ServletException
   * @throws IOException
   */
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

  /**
   * Retrieve multiple Products all at once. Filters the results
   * with the given query parameters. Can chains multiple request together
   * if chain query parameter is set. Allows an end-user client to drill-down
   * on a particular subset of results. Of course, a client could do the 
   * filter themselves in the browser, but we can provide that same mechanism
   * on the server-side.
   *
   * @param request
   * @param response
   * @throws ServletException
   * @throws IOException
   */
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

  /**
   * Create or update a single or multiple Product objects.
   * Takes a request of a Product object, an array of Products, or
   * a Products object containing an array of Products. Supports
   * both creating new Products, requiring all fields be provided,
   * or updating existing Products, with a selected sets of fields
   * provided. Support both PUT and POST requests. POST is strictly
   * for updates and PUT is by default for creation. PUT can be
   * switched to update existing Products with the "update" query
   * parameter set.
   * 
   *    PUT /products               - Create new Products
   *    PUT /products?update=true   - Update existing Products
   *    POST /products              - Same as: PUT /products?update=true
   *
   * @param request
   * @param response
   * @throws ServletException
   * @throws IOException
   */
  protected void doPutMany(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    ServletContext sc = request.getServletContext();
    APIRequest req    = APIRequest.getInstance();
    APIResponse res   = new APIResponse(request, response);
    ProductsDAO dao   = (ProductsDAO)sc.getAttribute("ProductsDAO");

    boolean update = "true".equals(request.getParameter("update"));

    // We support POST methods. While not strictly RESTful,
    // it is useful for distinguishing between a creation request
    // and an update request. POST is strictly for updates.
    if ("POST".equals(request.getMethod())) {
      update = true;
    }

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

  /**
   * Update the values of a single Product with its specified
   * Product ID within the URL: /product/<id>.
   * 
   * @param request
   * @param response
   * @throws ServletException
   * @throws IOException
   */
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

      if (req.isProduct(json, true)) { // Fuzzy match. Can be a Product or Product-like.
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

  /**
   * Delete the specified Product with the given ID within the
   * request URL: DELETE /product/<id>.
   * 
   * @param request
   * @param response
   * @throws ServletException
   * @throws IOException
   */
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
