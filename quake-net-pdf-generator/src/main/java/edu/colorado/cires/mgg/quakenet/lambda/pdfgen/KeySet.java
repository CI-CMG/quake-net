package edu.colorado.cires.mgg.quakenet.lambda.pdfgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class KeySet implements Comparable<KeySet>{

  private String detailsKey;
  private String cdiKey;
  private String eventId; // parsed from file name
  private List<String> childEventIds = new ArrayList<>(0);
  private boolean primary;
  private boolean eventError = false;

  public void setDetailsKey(String detailsKey) {
    this.detailsKey = detailsKey;
  }

  public void setCdiKey(String cdiKey) {
    this.cdiKey = cdiKey;
  }

  public String getDetailsKey() {
    return detailsKey;
  }

  public String getCdiKey() {
    return cdiKey;
  }

  public String getEventId() {
    return eventId;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  public List<String> getChildEventIds() {
    return childEventIds;
  }

  public void setChildEventIds(List<String> childEventIds) {
    if (childEventIds == null) {
      childEventIds = new ArrayList<>(0);
    }
    this.childEventIds = childEventIds;
  }

  public boolean isPrimary() {
    return primary;
  }

  public void setPrimary(boolean primary) {
    this.primary = primary;
  }

  public boolean isEventError() {
    return eventError;
  }

  public void setEventError(boolean eventError) {
    this.eventError = eventError;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KeySet keySet = (KeySet) o;
    return Objects.equals(detailsKey, keySet.detailsKey) && Objects.equals(cdiKey, keySet.cdiKey) && Objects.equals(
        childEventIds, keySet.childEventIds) && Objects.equals(eventId, keySet.eventId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(detailsKey, cdiKey, childEventIds);
  }

  @Override
  public int compareTo(KeySet o) {
    return detailsKey.compareTo(o.detailsKey);
  }
}
