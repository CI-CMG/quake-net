package edu.colorado.cires.mgg.quakenet.lambda.eventgrabber;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.colorado.cires.mgg.quakenet.message.EventDetailGrabberMessage;
import edu.colorado.cires.mgg.quakenet.message.EventGrabberMessage;
import edu.colorado.cires.mgg.quakenet.message.InfoFile;
import edu.colorado.cires.mgg.quakenet.s3.util.InfoFileS3Actions;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventDateGrabber {

  private static final Logger LOGGER = LoggerFactory.getLogger(EventDateGrabber.class);

  private final EventGrabberProperties properties;
  private final InfoFileS3Actions infoFileSaver;
  private final MessageSender messageSender;
  private final ObjectMapper objectMapper;

  public EventDateGrabber(EventGrabberProperties properties, InfoFileS3Actions infoFileSaver, MessageSender messageSender, ObjectMapper objectMapper) {
    this.properties = properties;
    this.infoFileSaver = infoFileSaver;
    this.messageSender = messageSender;
    this.objectMapper = objectMapper;
  }

  public void grabDetails(EventGrabberMessage message) {
    UsgsApiQueryier.query(
        properties,
        message.getStartTime(),
        message.getEndTime(),
        objectMapper,
        eventIds -> handleEventIds(eventIds, message.getStartTime().atZone(ZoneId.of("UTC")).toLocalDate()));
  }

  private void handleEventIds(List<String> eventIds, LocalDate date) {
    String key = String.format(
        "downloads/%s/%02d/%s/usgs-info-%s.json.gz",
        date.getYear(),
        date.getMonthValue(),
        date,
        date);
    InfoFile infoFile = infoFileSaver.readInfoFile(properties.getBucketName(), key)
        .orElseThrow(() -> new IllegalStateException("File missing: " + key));
    infoFile = InfoFile.Builder.builder(infoFile).withEventIds(eventIds).build();
    infoFileSaver.saveInfoFile(properties.getBucketName(), key, infoFile);
    eventIds.forEach(eventId -> notifyEventId(eventId, date));
  }


  private void notifyEventId(String eventId, LocalDate date) {

    EventDetailGrabberMessage message = EventDetailGrabberMessage.Builder.builder()
        .withEventId(eventId).withDate(date.toString()).build();

    LOGGER.info("Triggering Detail Query: {}-{}", date, eventId);

    messageSender.sendMessage(properties.getTopicArn(), message);

  }

}
