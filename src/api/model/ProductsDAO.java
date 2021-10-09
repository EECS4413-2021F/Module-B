package api.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.sqlite.SQLiteException;


public class ProductsDAO {

  private static ProductsDAO singleton = null;

  public static final String 
      ALL_PRODUCTS   = "SELECT P.id, P.name, P.description, P.cost, P.msrp, "
                     + "P.qty AS quantity, C.name AS category, V.name AS vendor "
                     + "FROM Product P, Category C, Vendor V "
                     + "WHERE C.id = P.catid AND V.id = P.venid "
    , ALL_CATEGORIES = "SELECT * FROM Category"
    , ALL_VENDORS    = "SELECT * FROM Vendor"
    
    , PRODUCTS_GET_BY_ID          = "AND P.id = ? "
    , PRODUCTS_GET_BY_NAME        = "AND UPPER(P.name) LIKE UPPER(?) "
    , PRODUCTS_GET_BY_DESCRIPTION = "AND UPPER(P.description) LIKE UPPER(?) "
    , PRODUCTS_GET_BY_CATEGORY    = "AND UPPER(C.name) LIKE UPPER(?) "
    , PRODUCTS_GET_BY_VENDOR      = "AND UPPER(V.name) LIKE UPPER(?) "
    , PRODUCTS_GET_BY_MINCOST     = "AND P.cost >= ? "
    , PRODUCTS_GET_BY_MINMSRP     = "AND P.msrp >= ? "
    , PRODUCTS_GET_BY_MINQUANTITY = "AND P.qty  >= ? "
    , PRODUCTS_GET_BY_MAXCOST     = "AND P.cost <= ? "
    , PRODUCTS_GET_BY_MAXMSRP     = "AND P.msrp <= ? "
    , PRODUCTS_GET_BY_MAXQUANTITY = "AND P.qty  <= ? "
    , PRODUCTS_ORDER_BY           = "ORDER BY %s %s " // (id|name|c.name|v.name|qnty|cost|msrp) (asc|desc) 
    , PRODUCTS_PAGINATION_LIMIT   = "LIMIT ? "
    , PRODUCTS_PAGINATION_OFFSET  = "OFFSET ? "
    
    , PRODUCTS_INSERT       = "INSERT INTO Product "
                            + "(id, name, description, catId, venId, qty, cost, msrp) VALUES"
                            + "(?, ?, ?, ?, ?, ?, ?, ?)"
    , PRODUCTS_UPDATE       = "UPDATE Product SET %s WHERE id = ?"
    , PRODUCTS_DELETE_BY_ID = "DELETE FROM Product WHERE id = ?"
    ;

  public static final String
      PRODUCT_ALREADY_EXISTS  = "Product already exists with the given ID, use update instead: "
    , PRODUCT_DOES_NOT_EXIST  = "No product exists with the given ID, use insert instead: "
    , PRODUCT_ID_MUST_MATCH   = "Product ID in body if given must match ID in request URL: "
    , PRODUCT_ID_MISSING      = "No product ID given, none in body nor in request URL."
    , CATEGORY_DOES_NOT_EXIST = "No category exists with the given name: "
    , VENDOR_DOES_NOT_EXIST   = "No vendor exists with the given name: "
    , SQL_SYNTAX_ERROR        = "Syntax Error in SQL: "
    ;

  private final DataSource datasource;
  private final Map<String, Integer> categories;
  private final Map<String, Integer> vendors;

  /**
   * ProductsDAO constructor.
   * Obtains a reference to the pooled connections
   * to the data source.
   *
   * @throws      NamingException
   */
  private ProductsDAO() throws NamingException, SQLException {
    Context initCtx = new InitialContext();
    Context envCtx = (Context) initCtx.lookup("java:comp/env");
    datasource = (DataSource) envCtx.lookup("jdbc/EECS");
    categories = getCategories();
    vendors    = getVendors();
  }

  /**
   * Retrieve all of the Categories from the database.
   * Returns a Map with the Category names mapped to Category ID.
   * This is for reverse lookup for INSERT and UPDATE SQL queries.
   * 
   * @return
   * @throws SQLException
   */
  private Map<String, Integer> getCategories() throws SQLException {
    Map<String, Integer> categories = new HashMap<>();
    try (Connection con = datasource.getConnection()) {
      PreparedStatement ps = con.prepareStatement(ALL_CATEGORIES);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) categories.put(rs.getString("name"), rs.getInt("id")); // reverse lookup
      }      
    }    
    return categories;
  }

  /**
   * Retrieve all of the Vendors from the database.
   * Returns a Map with the Vendor names mapped to Vendor ID.
   * This is for reverse lookup for INSERT and UPDATE SQL queries.
   * 
   * @return
   * @throws SQLException
   */
  private Map<String, Integer> getVendors() throws SQLException {
    Map<String, Integer> vendors = new HashMap<>();
    try (Connection con = datasource.getConnection()) {
      PreparedStatement ps = con.prepareStatement(ALL_VENDORS);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) vendors.put(rs.getString("name"), rs.getInt("id")); // reverse lookup
      }      
    }    
    return vendors;
  }

  /**
   * Return a Product object populated by the ResultSet from a
   * database query.
   * 
   * @param rs
   * @return
   * @throws SQLException
   */
  private Product getProduct(ResultSet rs) throws SQLException {
    Product product = new Product();
    product.setId(rs.getString("id"));
    product.setName(rs.getString("name"));
    product.setDescription(rs.getString("description"));
    product.setCategory(rs.getString("category"));
    product.setVendor(rs.getString("vendor"));
    product.setQuantity(rs.getInt("quantity"));
    product.setCost(rs.getDouble("cost"));
    product.setMSRP(rs.getDouble("msrp"));
    return product;
  }

  /**
   * Retrieve a Product from the database with the specified Product ID.
   * If none found, return null.
   * 
   * @param id
   * @return
   * @throws SQLException
   */
  public Product getProductById(String id) throws SQLException {
    try (Connection con = datasource.getConnection()) {
      PreparedStatement ps = con.prepareStatement(ALL_PRODUCTS + PRODUCTS_GET_BY_ID);
      ps.setString(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return getProduct(rs);
        }
      }
    }
    return null;
  }

  /**
   * Retrieves a collection of Product objects from the database
   * with the given ProductFilter. The ProductFilter generates the
   * SQL query and prepares it.
   * 
   * @param filter
   * @return
   * @throws SQLException
   */
  public Products getProducts(ProductFilter filter) throws SQLException {
    try (Connection con = datasource.getConnection()) {
      PreparedStatement ps = con.prepareStatement(filter.toSQL());
      filter.prepare(ps);

      Products products = new Products();
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          products.add(getProduct(rs));
        }
      }
      return products;
    } catch (SQLiteException e) {
      throw new SQLException(SQL_SYNTAX_ERROR + filter.toPreparedSQL(), e);
    }
  }

  /**
   * Add the given Product to the database.
   * 
   * @param product
   * @throws SQLException
   */
  public void addProduct(Product product) throws SQLException {
    if (getProductById(product.getId()) != null) // already exists
      throw new RuntimeException(PRODUCT_ALREADY_EXISTS  + product.getId());
    if (!categories.containsKey(product.getCategory())) // invalid category
      throw new RuntimeException(CATEGORY_DOES_NOT_EXIST + product.getCategory());
    if (!vendors.containsKey(product.getVendor())) // invalid vendor
      throw new RuntimeException(VENDOR_DOES_NOT_EXIST   + product.getVendor());

    try (Connection con = datasource.getConnection()) {
      PreparedStatement ps = con.prepareStatement(PRODUCTS_INSERT);

      ps.setString(1, product.getId());
      ps.setString(2, product.getName());
      ps.setString(3, product.getDescription());
      ps.setInt(4, categories.get(product.getCategory()));
      ps.setInt(5, vendors.get(product.getVendor()));
      ps.setInt(6, product.getQuantity());
      ps.setDouble(7, product.getCost());
      ps.setDouble(8, product.getMSRP());

      ps.executeUpdate();
    }
  }

  /**
   * Update the given Product in the database. If no ID is given,
   * use the Product ID in the object. If that is null and no ID is given
   * or the given ID and Product object's ID don't match, throw
   * RuntimeExceptions.
   * 
   * @param product
   * @throws SQLException
   */
  public void updateProduct(Product product) throws SQLException { updateProduct(product, null); }
  public void updateProduct(Product product, String id) throws SQLException {
    if (id == null && product.getId() == null) // no product ID given
      throw new RuntimeException(PRODUCT_ID_MISSING);
    if (id != null && product.getId() != null && !id.equals(product.getId())) // does not match
      throw new RuntimeException(PRODUCT_ID_MUST_MATCH + product.getId());
    if (id == null) id = product.getId();
    if (getProductById(id) == null) // does not exist
      throw new RuntimeException(PRODUCT_DOES_NOT_EXIST + id);
    
    if (product.getCategory() != null && !categories.containsKey(product.getCategory())) // invalid category
      throw new RuntimeException(CATEGORY_DOES_NOT_EXIST + product.getCategory());
    if (product.getVendor()   != null && !vendors.containsKey(product.getVendor())) // invalid vendor
      throw new RuntimeException(VENDOR_DOES_NOT_EXIST   + product.getVendor());
    
    List<String> assigns = new ArrayList<>(); 

    // Based on what fields have values, add these fields to be set in the update query. 
    
    if (product.getName()        != null) assigns.add("name = ?");
    if (product.getDescription() != null) assigns.add("description = ?");
    if (product.getCategory()    != null) assigns.add("catId = ?");
    if (product.getVendor()      != null) assigns.add("venId = ?");
    if (product.getQuantity()    >= 0)    assigns.add("qty = ?");  
    if (product.getCost()        >= 0)    assigns.add("cost = ?");
    if (product.getMSRP()        >= 0)    assigns.add("msrp = ?");

    final String SQL = String.format(PRODUCTS_UPDATE, String.join(",", assigns));

    try (Connection con = datasource.getConnection()) {
      PreparedStatement ps = con.prepareStatement(SQL);

      int i = 0;

      if (product.getName()        != null) ps.setString(++i, product.getName());
      if (product.getDescription() != null) ps.setString(++i, product.getDescription());
      if (product.getCategory()    != null) ps.setInt(++i, categories.get(product.getCategory()));
      if (product.getVendor()      != null) ps.setInt(++i, vendors.get(product.getVendor()));
      if (product.getQuantity()    >= 0)    ps.setInt(++i, product.getQuantity());  
      if (product.getCost()        >= 0)    ps.setDouble(++i, product.getCost());
      if (product.getMSRP()        >= 0)    ps.setDouble(++i, product.getMSRP());

      ps.setString(++i, id);      
      ps.executeUpdate();
    }
  }
  
  /**
   * Delete the Product with the given ID.
   * 
   * @param id
   * @throws SQLException
   */
  public void deleteProduct(String id) throws SQLException {
    if (getProductById(id) == null) // does not exist
      throw new RuntimeException(PRODUCT_DOES_NOT_EXIST + id);

    try (Connection con = datasource.getConnection()) {
      PreparedStatement ps = con.prepareStatement(PRODUCTS_DELETE_BY_ID);
      ps.setString(1, id);
      ps.executeUpdate();
    }
  }
  
  public static ProductsDAO getInstance() throws NamingException, SQLException {
    if (singleton == null) {
      singleton = new ProductsDAO();
    }
    return singleton;
  }
}

