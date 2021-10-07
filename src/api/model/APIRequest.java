package api.model;

import java.io.IOException;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


public class APIRequest {
  
  protected final Gson gson = new Gson();
  
  /**
   * Deserialize the request body as a JSON element.
   * 
   * @param request
   * @return
   * @throws IOException
   */
  public JsonElement getRequestBody(HttpServletRequest request) throws IOException {
    try (Scanner in = new Scanner(request.getInputStream())) {
      StringBuffer sb = new StringBuffer();      
      while (in.hasNextLine())
        sb.append(in.nextLine());

      JsonParser parser = new JsonParser();

      if (!sb.toString().isEmpty()) {
        return parser.parse(sb.toString());
      } else {
        return null;
      }
    }
  }

  /**
   * Determine if the given JSON element is empty or falsy.
   * If it is null or false or an empty array or object, return true.
   * Otherwise return false.
   * 
   * @param json
   * @return
   */
  public boolean isEmptyJson(JsonElement json) {
    if (json == null) return true;
    if (json.isJsonNull()) return true;
    if (json.isJsonPrimitive() && !json.getAsBoolean()) return true;
    if (json.isJsonArray() && json.getAsJsonArray().size() == 0) return true;
    if (json.isJsonObject() && json.getAsJsonObject().entrySet().size() == 0) return true;
    return false;
  }
}
