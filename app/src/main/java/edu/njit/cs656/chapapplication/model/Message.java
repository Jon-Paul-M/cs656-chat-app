package edu.njit.cs656.chapapplication.model;

/**
 * Created by jon-paul on 11/5/17.
 */

public class Message {

  private String fromDisplay;
  private String fromId;
  private String message;
  private Long time;

  public Message() {
    super();
  }

  public Message(String fromDisplay, String fromId, String message, Long time) {
    this.fromDisplay = fromDisplay;
    this.fromId = fromId;
    this.message = message;
    this.time = time;
  }

  public String getFromDisplay() {
    return fromDisplay;
  }

  public void setFromDisplay(String fromDisplay) {
    this.fromDisplay = fromDisplay;
  }

  public String getFromId() {
    return fromId;
  }

  public void setFromId(String fromId) {
    this.fromId = fromId;
  }


  public Long getTime() {
    return time;
  }

  public void setTime(Long time) {
    this.time = time;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }


  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Message{");
    sb.append("fromDisplay='").append(fromDisplay).append('\'');
    sb.append(", fromId='").append(fromId).append('\'');
    sb.append(", message='").append(message).append('\'');
    sb.append(", time=").append(time);
    sb.append('}');
    return sb.toString();
  }
}
