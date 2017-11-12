package edu.njit.cs656.chapapplication.model;

/**
 * Created by jon-paul on 11/11/17.
 */

public class Contact {

  private String id;
  private String display;
  private Long timeAdded;

  public Contact() {
    super();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDisplay() {
    return display;
  }

  public void setDisplay(String display) {
    this.display = display;
  }

  public Long getTimeAdded() {
    return timeAdded;
  }

  public void setTimeAdded(Long timeAdded) {
    this.timeAdded = timeAdded;
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("Contact{");
    sb.append("id='").append(id).append('\'');
    sb.append(", display='").append(display).append('\'');
    sb.append(", timeAdded=").append(timeAdded);
    sb.append('}');
    return sb.toString();
  }
}
