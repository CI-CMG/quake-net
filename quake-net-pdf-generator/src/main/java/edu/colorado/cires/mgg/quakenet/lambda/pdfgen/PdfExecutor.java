package edu.colorado.cires.mgg.quakenet.lambda.pdfgen;

import edu.colorado.cires.mgg.quakenet.message.ReportGenerateMessage;
import edu.colorado.cires.mgg.quakenet.model.QnEvent;
import gov.noaa.ncei.xmlns.cdidata.Cdidata;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.quakeml.xmlns.quakeml._1.Quakeml;

public class PdfExecutor {

  private final DataParser dataParser;
  private final DataOperations dataOperations;
  private final PdfGenProperties properties;

  public PdfExecutor(PdfGenProperties properties, DataParser dataParser, DataOperations dataOperations) {
    this.dataParser = dataParser;
    this.dataOperations = dataOperations;
    this.properties = properties;
  }

  public void execute(ReportGenerateMessage message) {
    List<KeySet> keySets = dataParser.getRequiredKeys(message.getYear(), message.getMonth());
    List<QnEvent> events = new ArrayList<>();
    for (KeySet keySet : keySets) {
      Quakeml quakeml = dataOperations.readQuakeMl(properties.getBucketName(), keySet.getDetailsKey())
          .orElseThrow(() -> new RuntimeException("Unable to read quake details: " + keySet.getDetailsKey()));
      Optional<Cdidata> cdidata = dataOperations.readCdi(properties.getBucketName(), keySet.getCdiKey());
      QnEvent event = DataParser.parseQuakeDetails(quakeml);
      cdidata.ifPresent(data -> DataParser.enrichCdi(event, data));
      events.add(event);
    }

    String key = String.format("reports/%d/%02d/earthquake-info-%d-%02d.pdf",
        message.getYear(), message.getMonth(), message.getYear(), message.getMonth());

    dataOperations.writePdf(properties.getBucketName(), key, events, message);
  }
}
