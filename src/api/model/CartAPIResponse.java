package api.model;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

public class CartAPIResponse extends APIResponse {

  public CartAPIResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
    super(request, response);
  }

  /**
   * Serialize a JSON response for retrieving a collection of Products
   * in the shopping cart. Outputs the given Products list in the "cart"
   * field of the API response. Also, add the length of the results.
   * 
   * @param products
   */  
  public void serializeGetCart(Products products) { serializeGetCart(products, true); }
  public JsonObject serializeGetCart(Products products, boolean terminate) {
    JsonObject response = newResponse();
    JsonObject cart     = gson.toJsonTree(products).getAsJsonObject();

    response.addProperty("length", products.getLength());
    response.add("cart", cart);
    if (terminate) endResponse(response);
    return response;
  }

  /**
   * Serialize a JSON response for the collection of Products added to or removed
   * from the shopping cart. Outputs the given Products list in the "added" or "removed"
   * field of the API response. Also, add the length of the results
   * and the search parameters used to filter the results.
   * 
   * @param products
   */  
  public void serializeAddToCart(Products products) { serializeCartUpdate(products, "added", true); }
  public void serializeRemovedFromCart(Products products) { serializeCartUpdate(products, "removed", true); }
  public JsonObject serializeCartUpdate(Products products, String label, boolean terminate) {
    JsonObject response = newResponse();
    JsonObject search   = ((ProductFilter)session.getAttribute("filter")).toJson();
    JsonObject items    = gson.toJsonTree(products).getAsJsonObject();

    response.addProperty("length", products.getLength());
    response.add("search", search);
    response.add(label, items);
    if (terminate) endResponse(response);
    return response;
  }
}
