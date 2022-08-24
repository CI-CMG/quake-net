package edu.colorado.cires.mgg.quakenet.message;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonDeserialize(builder = InfoFile.Builder.class)
public class InfoFile {

  private final LocalDate date;
  private final List<String> eventIds;
  private final Map<String, Object> otherFields;

  private InfoFile(LocalDate date, List<String> eventIds, Map<String, Object> otherFields) {
    this.date = date;
    this.eventIds = Collections.unmodifiableList(new ArrayList<>(eventIds));
    this.otherFields = Collections.unmodifiableMap(new HashMap<>(otherFields));
  }

  public LocalDate getDate() {
    return date;
  }

  public List<String> getEventIds() {
    return eventIds;
  }

  @JsonAnyGetter
  public Map<String, Object> getOtherFields() {
    return otherFields;
  }

  public static class Builder {

    public static Builder builder() {
      return new Builder();
    }

    public static Builder builder(InfoFile infoFile) {
      return new Builder(infoFile);
    }

    private LocalDate date;
    private List<String> eventIds = new ArrayList<>(0);
    private Map<String, Object> otherFields = new HashMap<>();

    private Builder() {

    }

    private Builder(InfoFile infoFile) {
      if (infoFile != null) {
        date = infoFile.date;
        otherFields = infoFile.otherFields;
      }
    }

    public Builder withDate(LocalDate date) {
      this.date = date;
      return this;
    }

    public Builder withEventIds(List<String> eventIds) {
      if(eventIds == null) {
        eventIds = new ArrayList<>(0);
      }
      this.eventIds = eventIds;
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

    public InfoFile build() {
      return new InfoFile(date, eventIds, otherFields);
    }
  }
}
