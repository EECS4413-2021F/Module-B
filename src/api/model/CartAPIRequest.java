package api.model;

import java.util.Arrays;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public class CartAPIRequest extends APIRequest {
  
  private static CartAPIRequest singleton = null;
  
  // Array of fields that Product must have, or partially have if Product-like.
  private static final String[] FilterFields = {
    "id", "name", "description", 
    "category", "vendor",
    "minQuantity", "minCost", "minMSRP",
    "maxQuantity", "maxCost", "maxMSRP"
  };

  private CartAPIRequest() {
    super();
  }

  
  /// Validation

  /**
   * Test if the given JSON is a ProductFilter or ProductFilter-like.
   * If it is a ProductFilter, it should contain all of the fields within
   * a ProductFilter object. If it is ProductFilter-like, it will contains
   * some of the fields, but not all. It will however not contain
   * any fields that aren't recognized fields within a ProductFilter object.
   * 
   * @param value
   * @param filterLike
   * @return
   */
  public boolean isFilter(JsonElement value, boolean filterLike) {
    return filterLike
        ? isFilterLike(value)
        : isFilter(value);
  }

  public boolean isFilter(JsonElement value) {
    if (!value.isJsonObject()) 
      return false;

    JsonObject json = value.getAsJsonObject();

    return Arrays.stream(FilterFields).allMatch(m -> json.has(m));
  }
  
  public boolean isFilterLike(JsonElement value) {
    if (!value.isJsonObject()) 
      return false;

    JsonObject json = value.getAsJsonObject();

    for (Entry<String, JsonElement> e : json.entrySet())
      if (Arrays.stream(FilterFields).noneMatch(f -> f.equals(e.getKey())))
        return false;

    return true;
  }


  /// Deserialize

  /**
   * Given a JSON element, deserialize it and return it
   * represented as a ProductFilter object. The JSON can be ProductFilter-like
   * and only partially complete, with missing fields. 
   * 
   * @param value
   * @param filterLike
   * @return
   */
  public ProductFilter getFilterFromJson(JsonElement value, boolean filterLike) {
    return isFilter(value, filterLike)
        ? gson.fromJson(value, ProductFilter.class)
        : null;
  }

  public static CartAPIRequest getInstance() {
    if (singleton == null) {
      singleton = new CartAPIRequest();
    }
    return singleton;
  }
}
