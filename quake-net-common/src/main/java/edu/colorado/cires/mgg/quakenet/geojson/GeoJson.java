package edu.colorado.cires.mgg.quakenet.geojson;

import java.util.HashSet;
import java.util.Set;

public class GeoJson {

  private FeatureProperties properties;
  private String id; // id in json file not always the eventId - sometimes it contains the parent eventId

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public FeatureProperties getProperties() {
    return properties;
  }

  public void setProperties(FeatureProperties properties) {
    this.properties = properties;
  }

  public Set<String> getIds(){
    Set<String> ids = new HashSet<>();
    ids.addAll(this.properties.getIds());
    return ids;
  }
}
