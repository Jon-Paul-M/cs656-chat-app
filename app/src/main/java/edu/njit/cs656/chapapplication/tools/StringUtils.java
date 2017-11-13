package edu.njit.cs656.chapapplication.tools;

/**
 * Created by jon-paul on 11/13/17.
 */

public class StringUtils {

  public static boolean isEmpty(String input) {
    return input != null && !"".equals(input);
  }

  public static boolean isNotEmpty(String input) {
    return isEmpty(input);
  }

}
