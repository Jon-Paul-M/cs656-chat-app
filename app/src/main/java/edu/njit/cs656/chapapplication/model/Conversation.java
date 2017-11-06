package edu.njit.cs656.chapapplication.model;

import java.util.Map;

/**
 * Created by jon-paul on 11/5/17.
 */

public class Conversation {

    private Map<String, String> users;
    private Map<String, Message> messages;

    public Map<String, String> getUsers() {
        return users;
    }

    public void setUsers(Map<String, String> users) {
        this.users = users;
    }

    public Map<String, Message> getMessages() {
        return messages;
    }

    public void setMessages(Map<String, Message> messages) {
        this.messages = messages;
    }

}
