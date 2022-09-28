package edu.colorado.cires.mgg.quakenet.message;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@JsonDeserialize(builder = ReportInfoFile.Builder.class)
public class ReportInfoFile {

  private final Instant startTime;
  private final Instant startReportGeneration;
  private final Instant endReportGeneration;
  private final Map<String, Object> otherFields;

  public ReportInfoFile(Instant startTime, Instant startReportGeneration, Instant endReportGeneration, Map<String, Object> otherFields) {
    this.startTime = startTime;
    this.startReportGeneration = startReportGeneration;
    this.endReportGeneration = endReportGeneration;
    this.otherFields = otherFields;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public Instant getStartReportGeneration() {
    return startReportGeneration;
  }

  public Instant getEndReportGeneration() {
    return endReportGeneration;
  }

  @JsonAnyGetter
  public Map<String, Object> getOtherFields() {
    return otherFields;
  }

  @Override
  public String toString() {
    return "ReportInfoFile{" +
        "startTime=" + startTime +
        ", startReportGeneration=" + startReportGeneration +
        ", endReportGeneration=" + endReportGeneration +
        ", otherFields=" + otherFields +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReportInfoFile that = (ReportInfoFile) o;
    return Objects.equals(startTime, that.startTime) && Objects.equals(startReportGeneration, that.startReportGeneration)
        && Objects.equals(endReportGeneration, that.endReportGeneration) && Objects.equals(otherFields, that.otherFields);
  }

  @Override
  public int hashCode() {
    return Objects.hash(startTime, startReportGeneration, endReportGeneration, otherFields);
  }

  public static class Builder {

    public static Builder builder() {
      return new Builder();
    }

    public static Builder builder(ReportInfoFile reportInfoFile) {
      return new Builder(reportInfoFile);
    }

    private Instant startTime;
    private Instant startReportGeneration;
    private Instant endReportGeneration;
    private Map<String, Object> otherFields = new HashMap<>();

    private Builder() {

    }

    private Builder(ReportInfoFile reportInfoFile) {
      if (reportInfoFile != null) {
        startTime = reportInfoFile.startTime;
        startReportGeneration = reportInfoFile.startReportGeneration;
        endReportGeneration = reportInfoFile.endReportGeneration;
        otherFields = reportInfoFile.otherFields;
      }
    }

    public Builder withStartTime(Instant startTime) {
      this.startTime = startTime;
      return this;
    }

    public Builder withStartReportGeneration(Instant startReportGeneration) {
      this.startReportGeneration = startReportGeneration;
      return this;
    }

    public Builder withEndReportGeneration(Instant endReportGeneration) {
      this.endReportGeneration = endReportGeneration;
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

    public ReportInfoFile build() {
      return new ReportInfoFile(startTime, startReportGeneration, endReportGeneration, otherFields);
    }
  }
}
