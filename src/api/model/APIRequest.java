package api.model;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class APIRequest {
  
  private static APIRequest singleton = null;
  
  // Array of fields that Product must have, or partially have if Product-like.
  private static final String[] ProductFields = {
    "id", "name", "description", 
    "category", "vendor",
    "quantity","cost","msrp"
  };

  private final Gson gson = new Gson();
  
  private APIRequest() { }
  
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
      return parser.parse(sb.toString());
    }
  }

  /// Validation
  
  /**
   * Test if the given JSON is a Product or Product-like.
   * If it is a Product, it should contain all of the fields within
   * a Product object. If it is Product-like, it will contains
   * some of the fields, but not all. It will however not contain
   * any fields that aren't recognized fields within a Product object.
   * 
   * @param value
   * @param productLike
   * @return
   */
  public boolean isProduct(JsonElement value, boolean productLike) {
    return productLike ? isProductLike(value) : isProduct(value);
  }

  public boolean isProduct(JsonElement value) {
    if (!value.isJsonObject()) 
      return false;

    JsonObject json = value.getAsJsonObject();

    return Arrays.stream(ProductFields).allMatch(m -> json.has(m));
  }
  
  public boolean isProductLike(JsonElement value) {
    if (!value.isJsonObject()) 
      return false;

    JsonObject json = value.getAsJsonObject();

    for (Entry<String, JsonElement> e : json.entrySet())
      if (Arrays.stream(ProductFields).noneMatch(f -> f.equals(e.getKey())))
        return false;

    return true;
  }
  
  /**
   * Test if the given JSON element is a Products array or a
   * Products object containing Products or Product-like objects.
   * If it is a Products array, it is a JsonArray containing
   * Product or Product-like objects. If it is a Products object,
   * then it is an object that contains the "products" field which
   * is an array of Product or Product-like objects. 
   * 
   * @param value
   * @param productLike
   * @return
   */
  public boolean isProducts(JsonElement value, boolean productLike) {
    return isProductsArray(value, productLike) || 
           isProductsObject(value, productLike);
  }

  public boolean isProductsArray(JsonElement value, boolean productLike) {
    if (!value.isJsonArray())
      return false;

    for (JsonElement elem : value.getAsJsonArray())
      if (!isProduct(elem, productLike))
        return false;

    return true;
  }

  public boolean isProductsObject(JsonElement value, boolean productLike) {
    if (!value.isJsonObject()) 
      return false;

    JsonObject obj = value.getAsJsonObject();

    return obj.has("products") &&
           isProductsArray(obj.get("products"), productLike);
  }

  /// Deserialize

  /**
   * Given a JSON element, deserialize it and return it
   * represented as a Product object. The JSON can be Product-like
   * and only partially complete, with missing fields. 
   * 
   * @param value
   * @param productLike
   * @return
   */
  public Product getProductFromJson(JsonElement value, boolean productLike) {
    return isProduct(value, productLike)
        ? gson.fromJson(value, Product.class)
        : null;
  }

  /**
   * Given a JSON element, deserialize it and return it
   * represented as a Products object, containing a list of Product
   * objects. The JSON can contain objects that are Product-like or only
   * contain some of the Product fields, not all of them.
   * 
   * @param value
   * @param productLike
   * @return
   */
  public Products getProductsFromJson(JsonElement value, boolean productLike) {   
    if (isProductsObject(value, productLike))
      return gson.fromJson(value, Products.class);

    if (isProductsArray(value, productLike)) {
      JsonObject json = new JsonObject();
      json.add("products", value);      
      return gson.fromJson(json, Products.class);
    }

    if (!isProduct(value, productLike))
      return null;

    Products products = new Products();
    products.add(getProductFromJson(value, productLike));
    return products;
  }

  public static APIRequest getInstance() {
    if (singleton == null) {
      singleton = new APIRequest();
    }
    return singleton;
  }
}
