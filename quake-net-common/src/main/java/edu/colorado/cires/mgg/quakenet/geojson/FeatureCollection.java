package edu.colorado.cires.mgg.quakenet.geojson;

import java.util.ArrayList;
import java.util.List;

public class FeatureCollection {

  private List<GeoJson> features = new ArrayList<>();

  public List<GeoJson> getFeatures() {
    return features;
  }

  public void setFeatures(List<GeoJson> features) {
    if(features == null) {
      features = new ArrayList<>();
    }
    this.features = features;
  }
}
