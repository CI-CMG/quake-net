package edu.colorado.cires.mgg.quakenet.message;

import java.time.Instant;

public class EventGrabberMessage {

  private Instant startTime;
  private Instant endTime;

  public Instant getStartTime() {
    return startTime;
  }

  public void setStartTime(Instant startTime) {
    this.startTime = startTime;
  }

  public Instant getEndTime() {
    return endTime;
  }

  public void setEndTime(Instant endTime) {
    this.endTime = endTime;
  }
}
