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

import api.model.ProductsAPIRequest;
import api.model.ProductsAPIResponse;
import api.model.Product;
import api.model.ProductFilter;
import api.model.Products;
import api.model.ProductsDAO;


@WebServlet(
  name = "Products",
  urlPatterns = {
    "/products",
    "/products/*",
    "/v1/products",   // We should version our RESTful APIs.
    "/v1/products/*"  // This is version 1.
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
    String reqURI  = request.getRequestURI();
    String ctxPath = request.getContextPath();
    String pInfo   = request.getPathInfo();

    if ((reqURI.startsWith(ctxPath + "/products/") || reqURI.startsWith(ctxPath + "/v1/products/")) && pInfo.length() > 1) {
      doGetOne(request, response);  // GET /products/<id>
    } else {
      doGetMany(request, response); // GET /products
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {   
    doCreate(request, response); // POST /products
  }

  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    String reqURI  = request.getRequestURI();
    String ctxPath = request.getContextPath();
    String pInfo   = request.getPathInfo();

    if ((reqURI.startsWith(ctxPath + "/products/") || reqURI.startsWith(ctxPath + "/v1/products/")) && pInfo.length() > 1) {
      doUpdateOne(request, response);  // PUT /products/<id>
    }
  }

  @Override
  protected void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    // We only support deleting one Product at a time, but we could
    // have supported deleting Products in batch.
    // Maybe in a future version.

    String reqURI  = request.getRequestURI();
    String ctxPath = request.getContextPath();
    String pInfo   = request.getPathInfo();

    if ((reqURI.startsWith(ctxPath + "/products/") || reqURI.startsWith(ctxPath + "/v1/products/")) && pInfo.length() > 1) {
      doDeleteOne(request, response); // DELETE /products/<id>
    }
  }


  /// Handlers

  /**
   * Retrieve a single Product object with its given ID
   * specified within its URL: /products/<id>.
   * 
   * @param request
   * @param response
   * @throws ServletException
   * @throws IOException
   */
  protected void doGetOne(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    ServletContext sc       = request.getServletContext();
    HttpSession session     = request.getSession(true);
    ProductsDAO dao         = (ProductsDAO)sc.getAttribute("ProductsDAO");
    ProductsAPIResponse res = new ProductsAPIResponse(request, response);
    String pid              = request.getPathInfo().substring(1);
    ProductFilter filter    = new ProductFilter();

    try {
      filter.setId(pid);
      session.setAttribute("filter", filter);
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
    ServletContext sc       = request.getServletContext();
    HttpSession session     = request.getSession(true);
    ProductsAPIResponse res = new ProductsAPIResponse(request, response);
    ProductsDAO dao         = (ProductsDAO)sc.getAttribute("ProductsDAO");
    String chain            = request.getParameter("chain");

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
   * Create a single or multiple Product objects.
   * Takes a request of a Product object, an array of Products, or
   * a Products object containing an array of Products.
   *
   * @param request
   * @param response
   * @throws ServletException
   * @throws IOException
   */
  protected void doCreate(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    ServletContext sc       = request.getServletContext();
    ProductsAPIRequest req  = ProductsAPIRequest.getInstance();
    ProductsAPIResponse res = new ProductsAPIResponse(request, response);
    ProductsDAO dao         = (ProductsDAO)sc.getAttribute("ProductsDAO");

    try {
      JsonElement json = req.getRequestBody(request);

      if (req.isProducts(json, false)) {
        Products products = req.getProductsFromJson(json, false);
        Products changed  = new Products();

        for (Product p : products.getProducts()) {
          dao.addProduct(p);
          changed.add(dao.getProductById(p.getId()));
        }
        res.serializeCreateMany(changed);
      } else if (req.isProduct(json, false)) {
        Product product = req.getProductFromJson(json, false);
        dao.addProduct(product);
        res.serializeCreateOne(dao.getProductById(product.getId()));
      } else {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        res.serializeException(REQUEST_CANNOT_PARSE, json);
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
   * Product ID within the URL: /products/<id>.
   * 
   * @param request
   * @param response
   * @throws ServletException
   * @throws IOException
   */
  protected void doUpdateOne(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    ServletContext sc       = request.getServletContext();
    ProductsAPIRequest req  = ProductsAPIRequest.getInstance();
    ProductsAPIResponse res = new ProductsAPIResponse(request, response);
    ProductsDAO dao         = (ProductsDAO)sc.getAttribute("ProductsDAO");
    String pid              = request.getPathInfo().substring(1);

    try {
      JsonElement json = req.getRequestBody(request);

      if (req.isProduct(json, true)) { // Fuzzy match. Can be a Product or Product-like.
        Product product = req.getProductFromJson(json, true);
        dao.updateProduct(product, pid);
        res.serializeUpdateOne(dao.getProductById(pid));
      } else {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        res.serializeException(REQUEST_CANNOT_PARSE, json);
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
   * request URL: DELETE /products/<id>.
   * 
   * @param request
   * @param response
   * @throws ServletException
   * @throws IOException
   */
  protected void doDeleteOne(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {   
    ServletContext sc       = request.getServletContext();
    ProductsDAO dao         = (ProductsDAO)sc.getAttribute("ProductsDAO");
    ProductsAPIResponse res = new ProductsAPIResponse(request, response);
    String pid              = request.getPathInfo().substring(1);

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
