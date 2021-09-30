package httpd.v5.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "student")
public class Student implements Serializable {
  
  private static final long serialVersionUID = 1L;

  private int id;
  private String surname;
  private String givenName;
  private double gpa;
  private int yearAdmitted;
  
  public Student() { }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getSurname() {
    return surname;
  }

  public void setSurname(String surname) {
    this.surname = surname;
  }

  public String getGivenName() {
    return givenName;
  }

  public void setGivenName(String givenName) {
    this.givenName = givenName;
  }
  
  public double getGpa() {
    return gpa;
  }

  public void setGpa(double gpa) {
    this.gpa = gpa;
  }

  public int getYearAdmitted() {
    return yearAdmitted;
  }

  public void setYearAdmitted(int yearAdmitted) {
    this.yearAdmitted = yearAdmitted;
  }  
}
