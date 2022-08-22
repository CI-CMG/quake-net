package edu.colorado.cires.mgg.quakenet.geojson;

import java.util.ArrayList;
import java.util.List;

public class Products {

  private List<Dyfi> dyfi = new ArrayList<>(0);

  public List<Dyfi> getDyfi() {
    return dyfi;
  }

  public void setDyfi(List<Dyfi> dyfi) {
    this.dyfi = dyfi;
  }
}
