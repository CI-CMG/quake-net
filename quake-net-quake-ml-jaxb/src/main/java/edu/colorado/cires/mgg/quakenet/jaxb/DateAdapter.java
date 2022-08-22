package edu.colorado.cires.mgg.quakenet.jaxb;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.bind.DatatypeConverter;

public final class DateAdapter {
  public static Instant parseDate(String s) {
    return DatatypeConverter.parseDate(s).getTime().toInstant();
  }

  public static String printDate(Instant dt) {
    Calendar cal = new GregorianCalendar();
    cal.setTime(Date.from(dt));
    return DatatypeConverter.printDate(cal);
  }
}
