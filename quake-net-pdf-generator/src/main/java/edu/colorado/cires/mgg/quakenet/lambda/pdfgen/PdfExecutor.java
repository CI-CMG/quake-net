package edu.colorado.cires.mgg.quakenet.lambda.pdfgen;

import com.itextpdf.text.DocumentException;
import edu.colorado.cires.mgg.quakenet.message.ReportGenerateMessage;
import edu.colorado.cires.mgg.quakenet.model.QnEvent;
import gov.noaa.ncei.xmlns.cdidata.Cdidata;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.quakeml.xmlns.quakeml._1.Quakeml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PdfExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(PdfExecutor.class);

  private final DataParser dataParser;
  private final DataOperations dataOperations;
  private final PdfGenProperties properties;

  public PdfExecutor(PdfGenProperties properties, DataParser dataParser, DataOperations dataOperations) {
    this.dataParser = dataParser;
    this.dataOperations = dataOperations;
    this.properties = properties;
  }

  public void execute(ReportGenerateMessage message) {
    LOGGER.info("Processing: {}-{}", message.getYear(), message.getMonth());
    List<KeySet> keySets = dataParser.getRequiredKeys(message.getYear(), message.getMonth());
    LOGGER.info("Number of events for {}-{} = {}", message.getYear(), message.getMonth(), keySets.size());
    List<QnEvent> events = new ArrayList<>();
    for (KeySet keySet : keySets) {
      Quakeml quakeml = dataOperations.readQuakeMl(properties.getBucketName(), keySet.getDetailsKey())
          .orElseThrow(() -> new RuntimeException("Unable to read quake details: " + keySet.getDetailsKey()));
      Optional<Cdidata> cdidata;
      if(keySet.getCdiKey() != null) {
        cdidata = dataOperations.readCdi(properties.getBucketName(), keySet.getCdiKey());
      } else {
        cdidata = Optional.empty();
      }
      QnEvent event = DataParser.parseQuakeDetails(quakeml);
      cdidata.ifPresent(data -> DataParser.enrichCdi(event, data));
      events.add(event);
    }

    String key = String.format("reports/%d/%02d/earthquake-info-%d-%02d.pdf",
        message.getYear(), message.getMonth(), message.getYear(), message.getMonth());

    LOGGER.info("Writing PDF: {}-{}", message.getYear(), message.getMonth());
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      LambdaPdfWriter.writePdf(events.stream().sorted(Comparator.comparing(QnEvent::getOriginTime)).collect(Collectors.toList()), message, bos);
    } catch (DocumentException e) {
      throw new IllegalStateException("An error occurred generating report", e);
    }
    dataOperations.writePdf(properties.getBucketName(), key, bos.toByteArray());
    LOGGER.info("Done Writing PDF: {}-{}", message.getYear(), message.getMonth());
  }
}
