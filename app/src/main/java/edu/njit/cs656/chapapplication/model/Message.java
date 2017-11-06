package edu.njit.cs656.chapapplication.model;

/**
 * Created by jon-paul on 11/5/17.
 */

public class Message {
    private String fromDisplay;
    private String fromId;

    private String text;
    private Long time;

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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Message{");
        sb.append("fromDisplay='").append(fromDisplay).append('\'');
        sb.append(", fromId='").append(fromId).append('\'');
        sb.append(", text='").append(text).append('\'');
        sb.append(", time=").append(time);
        sb.append('}');
        return sb.toString();
    }
}
