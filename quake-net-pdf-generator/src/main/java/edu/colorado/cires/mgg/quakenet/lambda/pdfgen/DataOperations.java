package edu.colorado.cires.mgg.quakenet.lambda.pdfgen;

import edu.colorado.cires.mgg.quakenet.geojson.GeoJson;
import gov.noaa.ncei.xmlns.cdidata.Cdidata;
import java.util.Optional;
import org.quakeml.xmlns.quakeml._1.Quakeml;

public interface DataOperations {

  boolean isReportExists(String bucketName, String key);
  void writePdf(String bucketName, String key, byte[] content);
  Optional<Cdidata> readCdi(String bucketName, String key);
  Optional<GeoJson> readJson(String bucketName, String key);
  Optional<Quakeml> readQuakeMl(String bucketName, String key);

}
