package edu.njit.cs656.chapapplication.model;

import java.util.Date;

/**
 * Created by Ashley Le on 11/12/2017.
 */

public class ChatRoom {

    private String title;
    private Long createTime;
    private Long lastMessageTime;
    private String password;    // password
    private String type;    // public or private
    private boolean open;

    public ChatRoom() {
        super();
    }

    public ChatRoom(String title) {
        super();
        this.title = title;
        this.createTime = (new Date()).getTime();
        this.lastMessageTime = this.createTime + 1;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(Long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public void setPassword(String password) { this.password = password; }

    public String getPassword() { return password; }

    public void setType(String type) { this.type = type; }

    public String getType() { return type; }
}
