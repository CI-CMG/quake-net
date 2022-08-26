package edu.colorado.cires.mgg.quakenet.lambda.initiator;

import java.time.LocalDate;
import java.util.Objects;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    QueryRange that = (QueryRange) o;
    return Objects.equals(start, that.start) && Objects.equals(end, that.end);
  }

  @Override
  public int hashCode() {
    return Objects.hash(start, end);
  }

  @Override
  public String toString() {
    return "QueryRange{" +
        "start=" + start +
        ", end=" + end +
        '}';
  }
}
