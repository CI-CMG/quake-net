package edu.colorado.cires.mgg.quakenet.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QnEvent {

  private String eventId;
  private String earthquakeName;
  private String flinnEngdahlRegion;
  private String regionName;
  private Double latitude;
  private Double longitude;
  private Double depth;
  private Double magnitude;
  private String magnitudeType;
  private Instant originTime;
  private String feltDescription;
  private String cdiGeoXml;
  private List<QnCdi> cdis = new ArrayList<>();
  private List<String> comments = new ArrayList<>();
  private Map<String, List<String>> otherDescriptions = new HashMap<>();


  public String getFlinnEngdahlRegion() {
    return flinnEngdahlRegion;
  }

  public void setFlinnEngdahlRegion(String flinnEngdahlRegion) {
    this.flinnEngdahlRegion = flinnEngdahlRegion;
  }

  public String getRegionName() {
    return regionName;
  }

  public void setRegionName(String regionName) {
    this.regionName = regionName;
  }

  public String getEarthquakeName() {
    return earthquakeName;
  }

  public void setEarthquakeName(String earthquakeName) {
    this.earthquakeName = earthquakeName;
  }

  public Map<String, List<String>> getOtherDescriptions() {
    return Collections.unmodifiableMap(otherDescriptions);
  }

  public void addOtherDescription(String type, String value) {
    List<String> values = otherDescriptions.get(type);
    if(values == null) {
      values = new ArrayList<>(1);
      otherDescriptions.put(type, values);
    }
    values.add(value);
  }

  public String getEventId() {
    return eventId;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  public Double getLatitude() {
    return latitude;
  }

  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }

  public Double getLongitude() {
    return longitude;
  }

  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }

  public Double getDepth() {
    return depth;
  }

  public void setDepth(Double depth) {
    this.depth = depth;
  }

  public Double getMagnitude() {
    return magnitude;
  }

  public void setMagnitude(Double magnitude) {
    this.magnitude = magnitude;
  }

  public String getMagnitudeType() {
    return magnitudeType;
  }

  public void setMagnitudeType(String magnitudeType) {
    this.magnitudeType = magnitudeType;
  }

  public Instant getOriginTime() {
    return originTime;
  }

  public void setOriginTime(Instant originTime) {
    this.originTime = originTime;
  }

  public String getFeltDescription() {
    return feltDescription;
  }

  public void setFeltDescription(String feltDescription) {
    this.feltDescription = feltDescription;
  }

  public List<QnCdi> getCdis() {
    return Collections.unmodifiableList(cdis);
  }

  public void setCdis(List<QnCdi> cdis) {
    if(cdis == null) {
      cdis = new ArrayList<>(0);
    }
    this.cdis = cdis;
  }

  public void addCdi(QnCdi cdi) {
    cdis.add(cdi);
  }

  public List<String> getComments() {
    return Collections.unmodifiableList(comments);
  }

  public void setComments(List<String> comments) {
    if(comments == null) {
      comments = new ArrayList<>(0);
    }
    this.comments = comments;
  }

  public void addComment(String comment) {
    comments.add(comment);
  }

  public String getCdiGeoXml() {
    return cdiGeoXml;
  }

  public void setCdiGeoXml(String cdiGeoXml) {
    this.cdiGeoXml = cdiGeoXml;
  }

  @Override
  public String toString() {
    return "QnEvent{" +
        "eventId='" + eventId + '\'' +
        ", earthquakeName='" + earthquakeName + '\'' +
        ", flinnEngdahlRegion='" + flinnEngdahlRegion + '\'' +
        ", regionName='" + regionName + '\'' +
        ", latitude=" + latitude +
        ", longitude=" + longitude +
        ", depth=" + depth +
        ", magnitude=" + magnitude +
        ", magnitudeType='" + magnitudeType + '\'' +
        ", originTime=" + originTime +
        ", feltDescription='" + feltDescription + '\'' +
        ", cdiGeoXml='" + cdiGeoXml + '\'' +
        ", cdis=" + cdis +
        ", comments=" + comments +
        ", otherDescriptions=" + otherDescriptions +
        '}';
  }
}
