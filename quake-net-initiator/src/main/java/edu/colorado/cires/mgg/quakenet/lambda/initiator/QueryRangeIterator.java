package edu.colorado.cires.mgg.quakenet.lambda.initiator;

import edu.colorado.cires.mgg.quakenet.message.EventGrabberMessage;
import edu.colorado.cires.mgg.quakenet.message.InfoFile;
import java.time.LocalDate;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryRangeIterator {


  private static final Logger LOGGER = LoggerFactory.getLogger(QueryRangeIterator.class);


  private final InfoFileSaver fileInfoSaver;
  private final MessageSender messageSender;
  private final InitiatorProperties properties;

  public QueryRangeIterator(InfoFileSaver fileInfoSaver, MessageSender messageSender, InitiatorProperties properties) {
    this.fileInfoSaver = fileInfoSaver;
    this.messageSender = messageSender;
    this.properties = properties;
  }


  public void forEachDate(QueryRange queryRange) {

    LOGGER.info("Processing Range {} - {}", queryRange.getStart(), queryRange.getEnd());

    LocalDate date = queryRange.getStart();
    while (date.isBefore(queryRange.getEnd()) || date.isEqual(queryRange.getEnd())) {
      LOGGER.info("Processing {}", date);
      saveInfoFile(date);
      sendMessage(date);
      date = date.plusDays(1);
    }
  }

  public void saveInfoFile(LocalDate date) {

    LOGGER.info("Saving Info File: {}", date);

    String key = String.format(
        "downloads/%s/%02d/%s/usgs-info-%s.json.gz",
        date.getYear(),
        date.getMonthValue(),
        date,
        date);
    fileInfoSaver.saveInfoFile(properties.getDownloadBucket(), key, InfoFile.Builder.builder().withDate(date).build());

  }

  public void sendMessage(LocalDate date) {
    EventGrabberMessage message = EventGrabberMessage.Builder.builder()
        .withStartTime(date.atStartOfDay(ZoneId.of("UTC")).toInstant())
        .withEndTime(date.plusDays(1L).atStartOfDay(ZoneId.of("UTC")).toInstant())
        .build();
    LOGGER.info("Triggering USGS Query: {}", date);
    messageSender.sendMessage(properties.getTopicArn(), message);
  }


}
