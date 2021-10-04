package api.model;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.JsonObject;


@XmlRootElement(name = "product-filter")
public class ProductFilter extends Product {
  private static final long serialVersionUID = 1L;

  private static final Map<String, String> orderBys = new HashMap<>();

  static {
    orderBys.put("id",       "P.id");
    orderBys.put("name",     "P.name");
    orderBys.put("cost",     "P.cost");
    orderBys.put("msrp",     "P.msrp");
    orderBys.put("quantity", "P.qnty");
    orderBys.put("category", "C.name");
    orderBys.put("vender",   "V.name");
  }

  private double  maxCost     = -1;
  private double  maxMSRP     = -1;
  private int     maxQuantity = -1;
  private String  orderBy     = null;
  private boolean ascending   = true;
  private int     limit       = -1;
  private int     offset      = -1;

  public double getMinCost() { return getCost(); }  
  public double getMaxCost() { return maxCost; }
  public double getMinMSRP() { return getMSRP(); }
  public double getMaxMSRP() { return maxMSRP; }
  public int getMinQuantity() { return getQuantity(); }
  public int getMaxQuantity() { return maxQuantity; }

  public String getOrderBy()   { return orderBy; }
  public boolean isAscending() { return ascending; }
  public int getLimit()        { return limit; }
  public int getOffset()       { return offset; }

  public void setMinCost(double minCost)      { setCost(minCost); }
  public void setMaxCost(double maxCost)      { this.maxCost = maxCost; }
  public void setMinMSRP(double minMSRP)      { setMSRP(minMSRP); }
  public void setMaxMSRP(double maxMSRP)      { this.maxMSRP = maxMSRP; }
  public void setMinQuantity(int minQuantity) { setQuantity(minQuantity); }
  public void setMaxQuantity(int maxQuantity) { this.maxQuantity = maxQuantity; }

  public void setMinCost(String minCost)         { setMinCost(Double.parseDouble(minCost)); }
  public void setMaxCost(String maxCost)         { setMaxCost(Double.parseDouble(maxCost)); }
  public void setMinMSRP(String minMSRP)         { setMinMSRP(Double.parseDouble(minMSRP)); }
  public void setMaxMSRP(String maxMSRP)         { setMaxMSRP(Double.parseDouble(maxMSRP)); }
  public void setMinQuantity(String minQuantity) { setMinQuantity(Integer.parseInt(minQuantity, 10)); }
  public void setMaxQuantity(String maxQuantity) { setMaxQuantity(Integer.parseInt(maxQuantity, 10)); }

  public void setOrderBy(String orderBy) { setOrderBy(orderBy, true); } 
  public void setOrderBy(String orderBy, boolean ascending) {
    if (orderBys.containsKey(orderBy)) {
      this.orderBy   = orderBys.get(orderBy);
      this.ascending = !!ascending;
    }
  }

  public void paginate(int limit, int offset) {
    this.limit  = limit;
    this.offset = offset;
  }

  public void setPagination(String limit, String offset) {
    paginate(
      Integer.parseInt(limit,  10),
      Integer.parseInt(offset, 10)
    );
  }

  private String getParam(Map<String, String[]> params, String key) {
    return params.get(key)[0].isEmpty() ? null : params.get(key)[0];
  }

  public void populate(Map<String, String[]> params) {
    if (params.containsKey("id"))          setId(getParam(params, "id"));
    if (params.containsKey("name"))        setName(getParam(params, "name"));
    if (params.containsKey("description")) setDescription(getParam(params, "description"));
    if (params.containsKey("category"))    setCategory(getParam(params, "category"));
    if (params.containsKey("vendor"))      setVendor(getParam(params, "vendor"));
    if (params.containsKey("minCost"))     setMinCost(params.get("minCost")[0]);
    if (params.containsKey("maxCost"))     setMaxCost(params.get("maxCost")[0]);
    if (params.containsKey("minMSRP"))     setMinMSRP(params.get("minMSRP")[0]);
    if (params.containsKey("maxMSRP"))     setMaxMSRP(params.get("maxMSRP")[0]);
    if (params.containsKey("minQuantity")) setMinQuantity(params.get("minQuantity")[0]);
    if (params.containsKey("maxQuantity")) setMaxQuantity(params.get("maxQuantity")[0]);
    if (params.containsKey("limit"))       setPagination(params.get("limit")[0], params.containsKey("offset") ? params.get("offset")[0] : "0");
    if (params.containsKey("orderBy"))     setOrderBy(getParam(params, "orderBy"), !params.containsKey("reversed")
                                                || params.get("reversed")[0] == null
                                                || params.get("reversed")[0].isEmpty()
                                                || "false".equals(params.get("reversed")[0]));
  }

  public void prepare(PreparedStatement ps) throws SQLException {
    int i = 0;
    
    if (getId()          != null) ps.setString(++i, getId());
    if (getName()        != null) ps.setString(++i, "%" + getName() + "%");
    if (getDescription() != null) ps.setString(++i, "%" + getDescription() + "%");
    if (getCategory()    != null) ps.setString(++i, "%" + getCategory() + "%");
    if (getVendor()      != null) ps.setString(++i, "%" + getVendor() + "%");
    if (getMinCost()     >= 0)    ps.setDouble(++i, getMinCost());
    if (getMaxCost()     >= 0)    ps.setDouble(++i, getMaxCost());
    if (getMinMSRP()     >= 0)    ps.setDouble(++i, getMinMSRP());
    if (getMaxMSRP()     >= 0)    ps.setDouble(++i, getMaxMSRP());
    if (getMinQuantity() >= 0)    ps.setInt(++i, getMinQuantity());
    if (getMaxQuantity() >= 0)    ps.setInt(++i, getMaxQuantity());
    if (getOrderBy()     != null) { ps.setString(++i, getOrderBy()); ps.setString(++i, isAscending() ? "ASC" : "DESC"); }
    if (getLimit()       >= 0)    ps.setInt(++i, getLimit());
    if (getOffset()      >= 0)    ps.setInt(++i, getOffset());
  }

  public String toString() {
    return ProductsDAO.ALL_PRODUCTS
      + (getId()          == null ? "" : ProductsDAO.PRODUCTS_GET_BY_ID)
      + (getName()        == null ? "" : ProductsDAO.PRODUCTS_GET_BY_NAME)
      + (getDescription() == null ? "" : ProductsDAO.PRODUCTS_GET_BY_DESCRIPTION)
      + (getCategory()    == null ? "" : ProductsDAO.PRODUCTS_GET_BY_CATEGORY)
      + (getVendor()      == null ? "" : ProductsDAO.PRODUCTS_GET_BY_VENDOR)
      + (getMinCost()     <  0    ? "" : ProductsDAO.PRODUCTS_GET_BY_MINCOST)
      + (getMaxCost()     <  0    ? "" : ProductsDAO.PRODUCTS_GET_BY_MAXCOST)
      + (getMinMSRP()     <  0    ? "" : ProductsDAO.PRODUCTS_GET_BY_MINMSRP)
      + (getMaxMSRP()     <  0    ? "" : ProductsDAO.PRODUCTS_GET_BY_MAXMSRP)
      + (getMinQuantity() <  0    ? "" : ProductsDAO.PRODUCTS_GET_BY_MINQUANTITY)
      + (getMaxQuantity() <  0    ? "" : ProductsDAO.PRODUCTS_GET_BY_MAXQUANTITY)
      + (getOrderBy()     == null ? "" : ProductsDAO.PRODUCTS_ORDER_BY)
      + (getLimit()       <  0    ? "" : ProductsDAO.PRODUCTS_PAGINATION_LIMIT//) offset requires limit
      + (getOffset()      <  0    ? "" : ProductsDAO.PRODUCTS_PAGINATION_OFFSET))
      ;
  }
  
  public JsonObject toJson() {
    JsonObject obj = new JsonObject();
  
    if (getId()          != null) obj.addProperty("id", getId());
    if (getName()        != null) obj.addProperty("name", getName());
    if (getDescription() != null) obj.addProperty("description", getDescription());
    if (getCategory()    != null) obj.addProperty("category", getCategory());
    if (getVendor()      != null) obj.addProperty("vendor", getVendor());
    if (getMinCost()     >= 0)    obj.addProperty("minCost", getMinCost());
    if (getMaxCost()     >= 0)    obj.addProperty("maxCost", getMaxCost());
    if (getMinMSRP()     >= 0)    obj.addProperty("minMSRP", getMinMSRP());
    if (getMaxMSRP()     >= 0)    obj.addProperty("maxMSRP", getMaxMSRP());
    if (getMinQuantity() >= 0)    obj.addProperty("minQuantity", getMinQuantity());
    if (getMaxQuantity() >= 0)    obj.addProperty("maxQuantity", getMaxQuantity());
    if (getOrderBy()     != null) obj.addProperty("orderBy", getOrderBy());
    if (!isAscending())           obj.addProperty("reversed", !isAscending());
    if (getLimit()       >= 0)    obj.addProperty("limit", getLimit());
    if (getOffset()      >= 0)    obj.addProperty("offset", getOffset());

    return obj;
  }
}
