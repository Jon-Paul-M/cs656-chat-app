package edu.njit.cs656.chapapplication.tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jon-paul on 11/22/17.
 */

/**
 * Used to convert date to/from string.
 * The default jdk DateFormat class is not thread safe.
 * Java 8 has an alternative, but forcing java 8 could limit penetration.
 *
 * @author jm727@njit.edu
 */
public class ConcurrentDateFormater {


  private String pattern;
  private ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>() {

    @Override
    public DateFormat get() {
      return super.get();
    }

    @Override
    protected DateFormat initialValue() {
      return new SimpleDateFormat(pattern);
    }

    @Override
    public void remove() {
      super.remove();
    }

    @Override
    public void set(DateFormat value) {
      super.set(value);
    }

  };

  public ConcurrentDateFormater(String pattern) {
    this.pattern = pattern;
  }

  synchronized public Date parse(String dateString) {
    Date date = null;
    try {
      date = dateFormat.get().parse(dateString);
    } catch (Exception e) {
      date = null;
    }
    return date;
  }

  synchronized public String format(Date date) {
    return dateFormat.get().format(date);
  }


}
