package edu.colorado.cires.mgg.quakenet.message;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@JsonDeserialize(builder = ReportGenerateMessage.Builder.class)
public class ReportGenerateMessage {

  private final Integer year;
  private final Integer month;
  private final Map<String, Object> otherFields;

  private ReportGenerateMessage(Integer year, Integer month, Map<String, Object> otherFields) {
    this.year = year;
    this.month = month;
    this.otherFields = otherFields;
  }

  public Integer getYear() {
    return year;
  }

  public Integer getMonth() {
    return month;
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
    ReportGenerateMessage that = (ReportGenerateMessage) o;
    return Objects.equals(year, that.year) && Objects.equals(month, that.month) && Objects.equals(otherFields,
        that.otherFields);
  }

  @Override
  public int hashCode() {
    return Objects.hash(year, month, otherFields);
  }

  @Override
  public String toString() {
    return "ReportGenerateMessage{" +
        "year=" + year +
        ", month=" + month +
        ", otherFields=" + otherFields +
        '}';
  }

  public static class Builder {

    public static Builder builder() {
      return new Builder();
    }

    public static Builder builder(ReportGenerateMessage message) {
      return new Builder(message);
    }

    private Integer year;
    private Integer month;
    private Map<String, Object> otherFields = new HashMap<>();

    private Builder() {

    }

    private Builder(ReportGenerateMessage message) {
      if (message != null) {
        year = message.year;
        month = message.month;
        otherFields = message.otherFields;
      }
    }

    public Builder withYear(Integer year) {
      this.year = year;
      return this;
    }

    public Builder withMonth(Integer month) {
      this.month = month;
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

    public ReportGenerateMessage build() {
      return new ReportGenerateMessage(year, month, otherFields);
    }
  }
}
