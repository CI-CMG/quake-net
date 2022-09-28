package edu.colorado.cires.mgg.quakenet.lambda.pdfgen;

import com.lowagie.text.DocumentException;
import edu.colorado.cires.mgg.quakenet.message.ReportGenerateMessage;
import edu.colorado.cires.mgg.quakenet.message.ReportInfoFile;
import edu.colorado.cires.mgg.quakenet.model.QnEvent;
import edu.colorado.cires.mgg.quakenet.s3.util.InfoFileS3Actions;
import gov.noaa.ncei.xmlns.cdidata.Cdidata;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.quakeml.xmlns.quakeml._1.Quakeml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PdfExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(PdfExecutor.class);

  private final DataParser dataParser;
  private final DataOperations dataOperations;
  private final PdfGenProperties properties;
  private final InfoFileS3Actions infoFileS3Actions;
  private final Supplier<Instant> nowFactory;

  public PdfExecutor(PdfGenProperties properties, DataParser dataParser, DataOperations dataOperations,
      InfoFileS3Actions infoFileS3Actions, Supplier<Instant> nowFactory) {
    this.dataParser = dataParser;
    this.dataOperations = dataOperations;
    this.properties = properties;
    this.infoFileS3Actions = infoFileS3Actions;
    this.nowFactory = nowFactory;
  }

  private ReportInfoFile getReportInfoFile(ReportGenerateMessage message) {

    Optional<ReportInfoFile> reportInfoFile = infoFileS3Actions.readReportInfoFile(properties.getBucketName(),
        String.format("reports/%d/%02d/report-info-%d-%02d.json.gz", message.getYear(), message.getMonth(), message.getYear(), message.getMonth()));

    return reportInfoFile.orElseGet(
        () -> ReportInfoFile.Builder.builder().withStartTime(nowFactory.get()).withStartReportGeneration(nowFactory.get()).build());

  }

  private void saveReportInfoFile(ReportGenerateMessage message, ReportInfoFile reportInfoFile) {
    infoFileS3Actions.saveReportInfoFile(
        properties.getBucketName(),
        String.format("reports/%d/%02d/report-info-%d-%02d.json.gz", message.getYear(), message.getMonth(), message.getYear(), message.getMonth()),
        reportInfoFile);
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
      if (keySet.getCdiKey() != null) {
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
    ReportInfoFile reportInfoFile = ReportInfoFile.Builder.builder(getReportInfoFile(message)).withEndReportGeneration(nowFactory.get()).build();
    saveReportInfoFile(message, reportInfoFile);
    LOGGER.info("Updated Report Info File: {}-{}", message.getYear(), message.getMonth());
  }
}
