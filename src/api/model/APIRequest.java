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
  
  private static final String[] ProductFields = {
    "id", "name", "description", 
    "category", "vendor",
    "quantity","cost","msrp"
  };

  private final Gson gson = new Gson();
  
  private APIRequest() { }
  
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

  public Product getProductFromJson(JsonElement value, boolean productLike) {
    return isProduct(value, productLike)
        ? gson.fromJson(value, Product.class)
        : null;
  }

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
