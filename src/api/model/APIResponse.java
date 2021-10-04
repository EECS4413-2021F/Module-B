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

    response.addProperty("rid",    (int)session.getAttribute("rid"));
    response.addProperty("method", req.getMethod());
    response.addProperty("uri",    req.getRequestURI() + (qs == null ? "" : "?" + qs));
    return response;
  }

  public void endResponse(JsonElement response) {
    out.println(response);
  }

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

  public void serializeCreateOne(Product product) { serializePutOne(product, "inserted", true); }
  public void serializeUpdateOne(Product product) { serializePutOne(product, "updated",  true); }
  public JsonObject serializePutOne(Product product, String name, boolean terminate) {
    JsonObject response = newResponse();
    response.add(name, gson.toJsonTree(product));
    if (terminate) endResponse(response);
    return response;
  }

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
