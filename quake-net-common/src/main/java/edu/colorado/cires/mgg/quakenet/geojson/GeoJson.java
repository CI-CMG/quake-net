package edu.colorado.cires.mgg.quakenet.geojson;

public class GeoJson {

  private FeatureProperties properties;
  private String id;

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
}
