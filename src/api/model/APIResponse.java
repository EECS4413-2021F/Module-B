package api.model;

import java.io.IOException;
import java.io.PrintStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public class APIResponse {

  public static final String API_VERSION = "1.0"; 

  private Gson                gson;
  private PrintStream         out;
  private HttpServletRequest  req;
  private HttpServletResponse res;
  private HttpSession         session;

  public APIResponse(HttpServletRequest req, HttpServletResponse res) throws IOException {
    this.gson    = new Gson();
    this.out     = new PrintStream(res.getOutputStream());
    this.req     = req;
    this.res     = res;
    this.session = req.getSession();

    this.res.setContentType("application/json");
  }

  public JsonObject newResponse() {
    JsonObject response = new JsonObject();
    String qs = req.getQueryString();

    // To be future-proof our API, our response specifies the API version that we are using.
    // That way, if there are multiple versions or revisions of the API,
    // a client application can read the JSON response, determine its version
    // and process it accordingly.
    response.addProperty("version", API_VERSION);

    response.addProperty("rid",    (int)session.getAttribute("rid"));
    response.addProperty("method", req.getMethod());
    response.addProperty("uri",    req.getRequestURI() + (qs == null ? "" : "?" + qs));
    return response;
  }

  /**
   * Send the response.
   * 
   * @param response
   */
  public void endResponse(JsonElement response) {
    out.println(response);
  }
 
  /**
   * Serialize the given Exception within an API response.
   * If a string is given, we treat this an a RuntimeException.
   * If the terminate flag is given, outputs the response to the
   * client. If not, returns the JsonObject so other fields can
   * be added.
   * 
   * @param   exception
   * @param   terminate
   * @return  JsonObject or void
   */

  public void serializeException(String    exception) { serializeException(exception, true); }
  public void serializeException(Exception exception) { serializeException(exception, true); }
  public JsonObject serializeException(String exception, boolean terminate) { 
    return serializeException(new RuntimeException(exception), terminate); }
  public JsonObject serializeException(Exception exception, boolean terminate) {
    JsonObject response = newResponse();
    response.add("exception", gson.toJsonTree(exception));
    if (terminate) endResponse(response);
    return response;
  }

  /**
   * Serialize a JSON response for retrieving a single Product.
   * Outputs the given Product object in the "result" field
   * of the API response.
   * 
   * @param product
   */
  public void serializeGetOne(Product product) { serializeGetOne(product, true); }
  public JsonObject serializeGetOne(Product product, boolean terminate) {
    JsonObject response = newResponse();
    if (product == null) {
      response.addProperty("result", "No results found.");
    } else {
      response.add("result", gson.toJsonTree(product));
    }
    if (terminate) endResponse(response);
    return response;
  }

  /**
   * Serialize a JSON response for retrieving a collection of Products.
   * Outputs the given Products list in the "results" field
   * of the API response. Also, add the length of the results
   * and the search parameters used to filter the results.
   * 
   * @param products
   */  
  public void serializeGetMany(Products products) { serializeGetMany(products, true); }
  public JsonObject serializeGetMany(Products products, boolean terminate) {
    JsonObject response = newResponse();
    JsonObject search   = ((ProductFilter)session.getAttribute("filter")).toJson();
    JsonObject results  = gson.toJsonTree(products).getAsJsonObject();

    response.addProperty("length", products.getLength());
    response.add("search", search);
    response.add("results", results);
    if (terminate) endResponse(response);
    return response;
  }

  /**
   * Serialize the JSON response after successfully creating/inserting
   * a new Product or updating an existing Product. Adds in the response,
   * a "inserted" or "updated" field with the retrieved database record,
   * after the Product is inserted or updated.
   * 
   * @param product
   */  
  public void serializeCreateOne(Product product) { serializePutOne(product, "inserted", true); }
  public void serializeUpdateOne(Product product) { serializePutOne(product, "updated",  true); }
  public JsonObject serializePutOne(Product product, String name, boolean terminate) {
    JsonObject response = newResponse();
    response.add(name, gson.toJsonTree(product));
    if (terminate) endResponse(response);
    return response;
  }

  /**
   * Serialize the JSON response after successfully creating/inserting
   * one or more Products or updating one or more existing Products. Adds in
   * the response, a "inserted" or "updated" field with the retrieved
   * Product records in the database after the insertions or updates.
   * Also adds the length field with the number of Products added or
   * updated.
   * 
   * @param product
   */  
  public void serializeCreateMany(Products products) { serializePutMany(products, "inserted", true); }
  public void serializeUpdateMany(Products products) { serializePutMany(products, "updated",  true); }
  public JsonObject serializePutMany(Products products, String name, boolean terminate) {
    JsonObject response = newResponse();
    JsonObject changed  = gson.toJsonTree(products).getAsJsonObject();

    response.addProperty("length", products.getLength());
    response.add(name, changed);
    if (terminate) endResponse(response);
    return response;
  }

  /**
   * Serialize a JSON response after a deletion request for the
   * specified Product ID. The given product should be null. If it
   * is outputs the old Product, prior to deletion as the removed
   * field and add the deleted field as the deleted Product ID.
   * 
   * @param product
   * @param old
   * @param id
   */
  public JsonObject serializeDeleteOne(Product product, Product old, String id) {
    return serializeDeleteOne(product, old, id, true); }
  public JsonObject serializeDeleteOne(Product product, Product old, String id, boolean terminate) {
    JsonObject response = newResponse();
    if (product == null) {
      response.addProperty("deleted", id);
      response.add("removed", gson.toJsonTree(old));
    } else {
      response.addProperty("failed", id);
    }
    if (terminate) endResponse(response);
    return response;
  }
}
