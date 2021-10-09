package api.services;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

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

import api.model.CartAPIRequest;
import api.model.CartAPIResponse;
import api.model.Product;
import api.model.ProductFilter;
import api.model.Products;
import api.model.ProductsDAO;


@WebServlet(
  urlPatterns = {
    "/cart",
    "/cart/add",
    "/cart/remove",
    "/v1/cart",
    "/v1/cart/add",
    "/v1/cart/remove",
  }
)
public class CartAPIService extends HttpServlet {
	private static final long serialVersionUID = 1L;

  private static final String
      REQUEST_CANNOT_PARSE   = "Cannot parse request as a Product Filter object"
    , REQUEST_ATTEMPT_ADDALL = "Attempting to add all Products to cart, requires 'addAll' query parameter"
    ;

  public CartAPIService() {
    super();
  }

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

    HttpSession session = request.getSession(); 
    CartAPIResponse res = new CartAPIResponse(request, response);

    if (!(reqURI.equals(ctxPath + "/cart") || 
          reqURI.equals(ctxPath + "/v1/cart"))) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.serializeException("URL Endpoint Not Found");
      return;
    }

    Products cart = session.getAttribute("cart") != null
        ? (Products)session.getAttribute("cart")
        : new Products();

    try {
      session.setAttribute("cart", cart);
      res.serializeGetCart(cart);
    } catch (Exception e) {
      e.printStackTrace();
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      res.serializeException(e);
    }
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException
	{
	  String reqURI  = request.getRequestURI();
    String ctxPath = request.getContextPath();

	  if (reqURI.equals(ctxPath + "/cart/add") || 
	      reqURI.equals(ctxPath + "/v1/cart/add")) {
	    doCartUpdate(request, response, true);  // POST /cart/add
	  } else if (reqURI.equals(ctxPath + "/cart/remove") ||
	             reqURI.equals(ctxPath + "/v1/cart/remove")) {
	    doCartUpdate(request, response, false); // POST /cart/remove
	  } else {
	    CartAPIResponse res = new CartAPIResponse(request, response);
	    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	    res.serializeException("URL Endpoint Not Found");
	  }
	}

	/**
	 * Add or remove items from the shopping cart. Items are specified using a
	 * ProductFilter. Any Products that match the filter will either be added or
	 * removed from the cart. The cart is a Products collection object stored
	 * within the session. Can chain the filter from the previous request.
	 * To protect against accidentally adding all products to the cart when the
	 * filter is blank, we require the addAll flag be given. 
	 * 
	 * @param request
	 * @param response
	 * @param addUpdate
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doCartUpdate(HttpServletRequest request, HttpServletResponse response, boolean addUpdate)
      throws ServletException, IOException
  {
	  ServletContext sc   = request.getServletContext();
	  HttpSession session = request.getSession();
	  ProductsDAO dao     = (ProductsDAO)sc.getAttribute("ProductsDAO");
	  CartAPIRequest req  = CartAPIRequest.getInstance();
	  CartAPIResponse res = new CartAPIResponse(request, response);

	  boolean chain  = "true".equals(request.getParameter("chain"));
	  boolean addAll = "true".equals(request.getParameter("addAll"));

    Products cart = session.getAttribute("cart") != null
        ? (Products)session.getAttribute("cart")
        : new Products();

    try {
      JsonElement json     = req.getRequestBody(request);
      ProductFilter filter = chain && session.getAttribute("filter") != null          
          ? (ProductFilter)session.getAttribute("filter")
          : new ProductFilter();

      if (req.isFilter(json, true)) {      
        filter.populate(req.getFilterFromJson(json, true));
      } else if (!req.isEmptyJson(json)) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        res.serializeException(REQUEST_CANNOT_PARSE, json);
        return;
      }

      if (addUpdate && req.isEmptyJson(filter.toJson()) && !addAll) { // no filter
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        res.serializeException(REQUEST_ATTEMPT_ADDALL, json);
        return;
      }

      Products products = dao.getProducts(filter);
      Products changed  = new Products();
      
      List<Product> shoppingList = cart.getProducts();

      for (Product product : products.getProducts()) {
        if (addUpdate) {
          if (!shoppingList.contains(product)) {
            shoppingList.add(product);
            changed.add(product);
          }
        } else {
          if (shoppingList.contains(product)) {
            shoppingList.remove(product);
            changed.add(product);
          }
        }
      }

      session.setAttribute("filter", filter);
      session.setAttribute("cart", cart);
      if (addUpdate) {      
        res.serializeAddToCart(changed);
      } else {
        res.serializeRemovedFromCart(changed);
      }
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
