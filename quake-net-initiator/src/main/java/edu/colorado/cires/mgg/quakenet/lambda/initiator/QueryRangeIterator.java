package edu.colorado.cires.mgg.quakenet.lambda.initiator;

import java.time.LocalDate;

public class QueryRangeIterator {

  public void forEachDate(QueryRange queryRange) {
    LocalDate date = queryRange.getStart();
    while (date.isBefore(queryRange.getEnd()) || date.isEqual(queryRange.getEnd())) {
      saveInfoFile(date);
      sendMessage(date);
      date = date.plusDays(1L);
    }
  }

  private void sendMessage(LocalDate date) {
    /*
                .addParameter("starttime", queryRange.getStart().atStartOfDay(ZoneId.of("UTC")).toInstant().toString())
            .addParameter("endtime", queryRange.getEnd().plusDays(1L).atStartOfDay(ZoneId.of("UTC")).toInstant().toString())
     */

  }

  private void saveInfoFile(LocalDate date) {
  }
}
