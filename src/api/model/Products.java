package api.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "products")
public class Products implements Serializable {
  private static final long serialVersionUID = 1L;

  private List<Product> products = new ArrayList<>();

  public Products() { }

  @XmlElement(name = "product")
  public List<Product> getProducts() {
    return products; }

  @XmlAttribute(name = "length")
  public int getLength() {
    return products.size(); }  

  public void setProducts(List<Product> products) {
    this.products = products; }
  public void add(Product product) {
    this.products.add(product); }

}
