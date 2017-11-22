package edu.njit.cs656.chapapplication.tools;

import java.util.Comparator;

import edu.njit.cs656.chapapplication.model.ChatRoom;

/**
 * Created by jon-paul on 11/16/17.
 */

public class ChatRoomMainActivityComparator implements Comparator<ChatRoom> {
  @Override
  public int compare(ChatRoom o1, ChatRoom o2) {
    return -1;
    //return o1.getSort().compareTo(o2.getSort());
  }
}
