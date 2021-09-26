package model;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="students")
public class Students implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<Student> students = new ArrayList<>();

  public Students() { }

  @XmlElement(name="student")
  public List<Student> getStudents() {
    return students;
  }

  public void setStudents(List<Student> students) {
    this.students = students;
  }

  public void add(Student student) {
    this.students.add(student);
  }
}
