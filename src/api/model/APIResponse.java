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

  protected Gson                gson;
  protected PrintStream         out;
  protected HttpServletRequest  req;
  protected HttpServletResponse res;
  protected HttpSession         session;

  public APIResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
    this.gson    = new Gson();
    this.out     = new PrintStream(response.getOutputStream());
    this.req     = request;
    this.res     = response;
    this.session = request.getSession(true);

    this.res.setContentType("application/json");
  }

  /**
   * Increment a request counter for each session.
   * We return this request ID in each response, so a client
   * can keep track of the ordering of its requests and the
   * corresponding responses.
   */
  protected void setRequestID() {  
    if (session.getAttribute("rid") == null) session.setAttribute("rid", 0);
    int i = (int)session.getAttribute("rid");
    session.setAttribute("rid", i + 1);
  }

  public JsonObject newResponse() {
    JsonObject response = new JsonObject();
    String qs = req.getQueryString();

    setRequestID();

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
   * If a JSON element is given, we add it as the context attribute.
   * If the terminate flag is given, outputs the response to the
   * client. If not, returns the JsonObject so other fields can
   * be added.
   * 
   * @param   exception
   * @param   context
   * @param   terminate
   * @return  JsonObject or void
   */

  public void serializeException(String    exception) { serializeException(exception, null, true); }
  public void serializeException(Exception exception) { serializeException(exception, null, true); }
  public void serializeException(String    exception, JsonElement context) { serializeException(exception, context, true); }
  public void serializeException(Exception exception, JsonElement context) { serializeException(exception, context, true); }
  public JsonObject serializeException(String exception, JsonElement context, boolean terminate) { 
    return serializeException(new RuntimeException(exception), context, terminate); }
  public JsonObject serializeException(Exception exception, JsonElement context, boolean terminate) {
    JsonObject response = newResponse();
    if (context != null) response.add("context", context);
    response.add("exception", gson.toJsonTree(exception));
    if (terminate) endResponse(response);
    return response;
  }
}
