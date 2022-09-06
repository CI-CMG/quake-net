package edu.colorado.cires.mgg.quakenet.lambda.reportinit;

import edu.colorado.cires.mgg.quakenet.message.EventDetailGrabberMessage;
import edu.colorado.cires.mgg.quakenet.message.InfoFile;
import edu.colorado.cires.mgg.quakenet.message.ReportGenerateMessage;
import edu.colorado.cires.mgg.quakenet.s3.util.InfoFileS3Actions;
import edu.colorado.cires.mgg.quakenet.s3.util.S3FileUtilities;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportTrigger {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReportTrigger.class);

  private ReportInitiatorProperties properties;
  private final MessageSender messageSender;
  private final BucketIteratorFactory bucketIteratorFactory;
  private final InfoFileS3Actions infoFileS3Actions;
  private final S3FileUtilities s3FileUtilities;

  public ReportTrigger(ReportInitiatorProperties properties,
      MessageSender messageSender, BucketIteratorFactory bucketIteratorFactory,
      InfoFileS3Actions infoFileS3Actions, S3FileUtilities s3FileUtilities) {
    this.properties = properties;
    this.messageSender = messageSender;
    this.bucketIteratorFactory = bucketIteratorFactory;
    this.infoFileS3Actions = infoFileS3Actions;
    this.s3FileUtilities = s3FileUtilities;
  }

  private boolean isReportInfoFileExists(LocalDate date) {
    return infoFileS3Actions.isFileExists(
        properties.getBucketName(),
        String.format("reports/%d/%02d/report-info-%d-%02d.json.gz", date.getYear(), date.getMonthValue(), date.getYear(), date.getMonthValue()));
  }

  public void triggerReports(EventDetailGrabberMessage message) {

    LocalDate date = LocalDate.parse(message.getDate());

    if (!isReportInfoFileExists(date)) {
      Set<String> expectedEventIds = new HashSet<>();

      YearMonth month = YearMonth.from(date);
      LocalDate start = month.atDay(1);
      LocalDate end = month.atEndOfMonth();

      List<String> infoKeys = start.datesUntil(end.plusDays(1))
          .map(day -> String.format("downloads/%d/%02d/%s/usgs-info-%s.json.gz", day.getYear(), day.getMonthValue(), day.toString(), day.toString()))
          .collect(Collectors.toList());

      boolean hasAllFiles = true;
      for (String key : infoKeys) {
        Optional<InfoFile> infoFile = infoFileS3Actions.readInfoFile(properties.getBucketName(), key);
        if (infoFile.isPresent()) {
          expectedEventIds.addAll(infoFile.get().getEventIds());
        } else {
          LOGGER.info("Missing: {}", key);
          hasAllFiles = false;
          break;
        }
      }

      if (hasAllFiles) {

        LOGGER.info("Required {} keys for {}-{}", expectedEventIds.size(), date.getYear(), date.getMonthValue());

        String monthKey = String.format("downloads/%d/%02d/", date.getYear(), date.getMonthValue());
        Iterator<String> bucketIterator = bucketIteratorFactory.create(properties.getBucketName(), monthKey);
        LOGGER.info("Scanning {}", monthKey);
        if (!bucketIterator.hasNext()) {
          LOGGER.info("No results found for {}", monthKey);
        }

        while (bucketIterator.hasNext()) {
          String key = bucketIterator.next();
          String[] parts = key.split("/");
          if (parts.length == 6) {
            String fileDate = parts[3];
            String eventId = parts[4];
            String file = parts[5];
            if (file.equals("event-details-" + fileDate + "-" + eventId + ".xml.gz") || file.equals("event-error-" + fileDate + "-" + eventId + ".json.gz")) {
              expectedEventIds.remove(eventId);
            }
          }
        }

        if (expectedEventIds.isEmpty()) {
          sendGenerateReportMessage(date.getYear(), date.getMonthValue());
        } else {
          LOGGER.info("Missing {} keys for {}-{}", expectedEventIds.size(), date.getYear(), date.getMonthValue());
        }
      }

    } else {
      LOGGER.info("Report info already existed: {}", message.getDate());
    }

  }

  private void sendGenerateReportMessage(int year, int month) {

    s3FileUtilities.saveFile(
        properties.getBucketName(),
        String.format("reports/%d/%02d/report-info-%d-%02d.json.gz", year, month, year, month),
        outputStream -> {
          try {
            IOUtils.write("{}", outputStream, StandardCharsets.UTF_8);
          } catch (IOException e) {
            throw new IllegalStateException("Unable to write report info file");
          }
        }
    );

    ReportGenerateMessage message = ReportGenerateMessage.Builder.builder()
        .withYear(year)
        .withMonth(month)
        .build();

    LOGGER.info("Triggering Report: {}-{}", year, month);

    messageSender.sendMessage(properties.getTopicArn(), message);

  }

}
