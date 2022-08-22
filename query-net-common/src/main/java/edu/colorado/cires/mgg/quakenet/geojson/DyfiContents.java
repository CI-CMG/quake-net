package edu.colorado.cires.mgg.quakenet.geojson;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DyfiContents {

  @JsonProperty("cdi_geo.xml")
  private CdiXml cdiGeoXml;
  @JsonProperty("cdi_zip.xml")
  private CdiXml cdiZipXml;

  public CdiXml getCdiGeoXml() {
    return cdiGeoXml;
  }

  public void setCdiGeoXml(CdiXml cdiGeoXml) {
    this.cdiGeoXml = cdiGeoXml;
  }

  public CdiXml getCdiZipXml() {
    return cdiZipXml;
  }

  public void setCdiZipXml(CdiXml cdiZipXml) {
    this.cdiZipXml = cdiZipXml;
  }
}
