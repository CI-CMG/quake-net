package edu.colorado.cires.mgg.quakenet.lambda.initiator;

import java.time.LocalDate;

public class QueryRange {

  private final LocalDate start;
  private final LocalDate end;

  public QueryRange(LocalDate start, LocalDate end) {
    this.start = start;
    this.end = end;
  }

  public LocalDate getStart() {
    return start;
  }

  public LocalDate getEnd() {
    return end;
  }
}
