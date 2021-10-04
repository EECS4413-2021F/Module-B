package api.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "product")
public class Product implements Serializable {
  private static final long serialVersionUID = 1L;

  private String id          = null;
  private String name        = null;
  private String description = null;
  private String category    = null;
  private String vendor      = null;
  private int    quantity    = -1;
  private double cost        = -1;
  private double msrp        = -1;

  public Product() { }

  public String getId()   {
    return id; }
  public String getName() {
    return name; }
  public String getDescription() {
    return description; }
  public String getCategory() {
    return category; }
  public String getVendor() {
    return vendor; }
  public int getQuantity() {
    return quantity; }
  public double getCost() {
    return cost; }
  @XmlElement(name = "msrp")
  public double getMSRP() {
    return msrp; }

  public void setId(String id) {
    this.id = id; }
  public void setName(String name) {
    this.name = name; }
  public void setDescription(String description) {
    this.description = description; }
  public void setCategory(String category) {
    this.category = category; }
  public void setVendor(String vendor) {
    this.vendor = vendor; }
  public void setQuantity(int quantity) {
    this.quantity = quantity; }
  public void setCost(double cost) {
    this.cost = cost; }
  public void setMSRP(double msrp) {
    this.msrp = msrp; }
}
