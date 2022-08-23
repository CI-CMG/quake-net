package edu.colorado.cires.mgg.quakenet.lambda.eventdetailsgrabber;

import edu.colorado.cires.mgg.quakenet.geojson.CdiXml;
import edu.colorado.cires.mgg.quakenet.geojson.Dyfi;
import edu.colorado.cires.mgg.quakenet.geojson.DyfiContents;
import edu.colorado.cires.mgg.quakenet.geojson.FeatureProperties;
import edu.colorado.cires.mgg.quakenet.geojson.GeoJson;
import edu.colorado.cires.mgg.quakenet.geojson.Products;
import java.util.List;
import java.util.Optional;

public class DataParser {

  public static Optional<String> parseCdiUrl(GeoJson geoJson) {

    String url = null;

    FeatureProperties properties = geoJson.getProperties();
    if (properties != null) {
      Products products = properties.getProducts();
      if (products != null) {
        List<Dyfi> dyfis = products.getDyfi();
        if (!dyfis.isEmpty()) {
          Dyfi dyfi = dyfis.get(0);
          DyfiContents contents = dyfi.getContents();
          if (contents != null) {
            CdiXml cdiZipXml = contents.getCdiZipXml();
            if (cdiZipXml != null) {
              url = cdiZipXml.getUrl();
            }
          }
        }
      }
    }
    return Optional.ofNullable(url);
  }

}
