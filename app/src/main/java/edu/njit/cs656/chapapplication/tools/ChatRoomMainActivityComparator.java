package edu.njit.cs656.chapapplication.tools;

import java.util.Comparator;

import edu.njit.cs656.chapapplication.model.ChatRoom;
import edu.njit.cs656.chapapplication.model.Message;

/**
 * Created by jon-paul on 11/16/17.
 */

public class ChatRoomMainActivityComparator implements Comparator<ChatRoom> {
  @Override
  public int compare(ChatRoom o1, ChatRoom o2) {
    return o1.getSort().compareTo(o2.getSort());
  }
}
