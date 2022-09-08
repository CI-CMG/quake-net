package edu.colorado.cires.mgg.quakenet.message;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@JsonDeserialize(builder = EventDetailGrabberMessage.Builder.class)
public class EventDetailGrabberMessage {

  private final String eventId;
  private final String date;
  private final String error;
  private final Map<String, Object> otherFields;

  private EventDetailGrabberMessage(String eventId, String date, String error, Map<String, Object> otherFields) {
    this.eventId = eventId;
    this.date = date;
    this.error = error;
    this.otherFields = otherFields;
  }

  public String getEventId() {
    return eventId;
  }

  public String getDate() {
    return date;
  }

  public String getError() {
    return error;
  }

  @JsonAnyGetter
  public Map<String, Object> getOtherFields() {
    return otherFields;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EventDetailGrabberMessage message = (EventDetailGrabberMessage) o;
    return Objects.equals(eventId, message.eventId) && Objects.equals(date, message.date) && Objects.equals(error,
        message.error) && Objects.equals(otherFields, message.otherFields);
  }

  @Override
  public int hashCode() {
    return Objects.hash(eventId, date, error, otherFields);
  }

  @Override
  public String toString() {
    return "EventDetailGrabberMessage{" +
        "eventId='" + eventId + '\'' +
        ", date='" + date + '\'' +
        ", error='" + error + '\'' +
        ", otherFields=" + otherFields +
        '}';
  }

  public static class Builder {

    public static Builder builder() {
      return new Builder();
    }

    public static Builder builder(EventDetailGrabberMessage message) {
      return new Builder(message);
    }

    private String eventId;
    private String date;
    private String error;
    private Map<String, Object> otherFields = new HashMap<>();

    private Builder() {

    }

    private Builder(EventDetailGrabberMessage message) {
      if (message != null) {
        eventId = message.eventId;
        date = message.date;
        error = message.error;
        otherFields = message.otherFields;
      }
    }

    public Builder withEventId(String eventId) {
      this.eventId = eventId;
      return this;
    }


    public Builder withDate(String date) {
      this.date = date;
      return this;
    }

    public Builder withError(String error) {
      this.error = error;
      return this;
    }

    @JsonIgnore
    public Builder withOtherFields(Map<String, Object> otherFields) {
      if (otherFields == null) {
        otherFields = new HashMap<>();
      }
      this.otherFields = otherFields;
      return this;
    }

    @JsonAnySetter
    public Builder withOtherField(String name, Object value) {
      this.otherFields.put(name, value);
      return this;
    }

    public EventDetailGrabberMessage build() {
      return new EventDetailGrabberMessage(eventId, date, error, otherFields);
    }
  }
}
