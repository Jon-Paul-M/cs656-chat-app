package edu.njit.cs656.chapapplication.tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jon-paul on 11/22/17.
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

  public Date parse(String dateString) {
    Date date = null;
    try {
      date = dateFormat.get().parse(dateString);
    } catch (Exception e) {
      date = null;
    }
    return date;
  }

  public String format(Date date) {
    return dateFormat.get().format(date);
  }


}
