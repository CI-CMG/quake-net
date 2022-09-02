package edu.colorado.cires.mgg.quakenet.lambda.reportinit;

import edu.colorado.cires.mgg.quakenet.message.EventDetailGrabberMessage;
import edu.colorado.cires.mgg.quakenet.message.InfoFile;
import edu.colorado.cires.mgg.quakenet.message.ReportGenerateMessage;
import edu.colorado.cires.mgg.quakenet.s3.util.BucketIterator;
import edu.colorado.cires.mgg.quakenet.s3.util.InfoFileS3Actions;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportTrigger {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReportTrigger.class);

  private ReportInitiatorProperties properties;
  private final MessageSender messageSender;
  private final BucketIteratorFactory bucketIteratorFactory;
  private final InfoFileS3Actions infoFileS3Actions;

  public ReportTrigger(ReportInitiatorProperties properties,
      MessageSender messageSender, BucketIteratorFactory bucketIteratorFactory,
      InfoFileS3Actions infoFileS3Actions) {
    this.properties = properties;
    this.messageSender = messageSender;
    this.bucketIteratorFactory = bucketIteratorFactory;
    this.infoFileS3Actions = infoFileS3Actions;
  }

  private boolean isReportExists(LocalDate date) {
    return infoFileS3Actions.isFileExists(
        properties.getBucketName(),
        String.format("reports/%d/%02d/earthquake-info-%d-%02d.pdf", date.getYear(), date.getMonthValue(), date.getYear(), date.getMonthValue()));
  }

  public void triggerReports(EventDetailGrabberMessage message) {

    LocalDate date = LocalDate.parse(message.getDate());

    if (!isReportExists(date)) {
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

      if(hasAllFiles) {
        Iterator<String> bucketIterator = bucketIteratorFactory.create(
            properties.getBucketName(),
            String.format("downloads/%d/%02d/", date.getYear(), date.getMonthValue()));

        while (bucketIterator.hasNext()) {
          String key = bucketIterator.next();
          String[] parts = key.split("/");
          if (parts.length == 6) {
            String fileDate = parts[3];
            String eventId = parts[4];
            String file = parts[5];
            if (file.equals("event-details-" + fileDate + "-" + eventId + ".xml.gz")) {
              expectedEventIds.remove(eventId);
            }
          }
        }

        if (expectedEventIds.isEmpty()) {
          sendGenerateReportMessage(date.getYear(), date.getMonthValue());
        }
      }

    } else {
      LOGGER.info("Report already existed: {}", message.getDate());
    }

  }

  private void sendGenerateReportMessage(int year, int month) {

    ReportGenerateMessage message = ReportGenerateMessage.Builder.builder()
        .withYear(year)
        .withMonth(month)
        .build();

    LOGGER.info("Triggering Report: {}-{}", year, month);

    messageSender.sendMessage(properties.getTopicArn(), message);

  }

}
