package edu.colorado.cires.mgg.quakenet.message;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@JsonDeserialize(builder = EventGrabberMessage.Builder.class)
public class EventGrabberMessage {

  private final Instant startTime;
  private final Instant endTime;
  private final Map<String, Object> otherFields;

  private EventGrabberMessage(Instant startTime, Instant endTime, Map<String, Object> otherFields) {
    this.startTime = startTime;
    this.endTime = endTime;
    this.otherFields = Collections.unmodifiableMap(new HashMap<>(otherFields));
  }


  public Instant getStartTime() {
    return startTime;
  }

  public Instant getEndTime() {
    return endTime;
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
    EventGrabberMessage that = (EventGrabberMessage) o;
    return Objects.equals(startTime, that.startTime) && Objects.equals(endTime, that.endTime) && Objects.equals(
        otherFields, that.otherFields);
  }

  @Override
  public String toString() {
    return "EventGrabberMessage{" +
        "startTime=" + startTime +
        ", endTime=" + endTime +
        ", otherFields=" + otherFields +
        '}';
  }

  @Override
  public int hashCode() {
    return Objects.hash(startTime, endTime, otherFields);
  }

  public static class Builder {

    public static Builder builder() {
      return new Builder();
    }

    public static Builder builder(EventGrabberMessage message) {
      return new Builder(message);
    }

    private Instant startTime;
    private Instant endTime;
    private Map<String, Object> otherFields = new HashMap<>();

    private Builder() {

    }

    private Builder(EventGrabberMessage message) {
      if (message != null) {
        startTime = message.startTime;
        endTime = message.endTime;
        otherFields = message.otherFields;
      }
    }

    public Builder withStartTime(Instant startTime) {
      this.startTime = startTime;
      return this;
    }

    public Builder withEndTime(Instant endTime) {
      this.endTime = endTime;
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

    public EventGrabberMessage build() {
      return new EventGrabberMessage(startTime, endTime, otherFields);
    }
  }
}
