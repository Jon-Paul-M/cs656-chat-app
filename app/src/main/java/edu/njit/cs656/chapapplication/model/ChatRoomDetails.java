package edu.njit.cs656.chapapplication.model;

/**
 * Created by Ashley Le on 11/12/2017.
 */

public class ChatRoomDetails {

    public static String chatRoomName = "";

    public ChatRoomDetails() {
        super();
    }

    public ChatRoomDetails(String name) {
        this.chatRoomName = name;
    }

    public static String getChatRoomName() {
        return chatRoomName;
    }

    public static void setChatRoomName(String chatRoomName) {
        ChatRoomDetails.chatRoomName = chatRoomName;
    }
}
