package edu.colorado.cires.mgg.quakenet.message;

public class EventDetailGrabberMessage {

  private String eventId;
  private String date;

  public String getEventId() {
    return eventId;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }
}
