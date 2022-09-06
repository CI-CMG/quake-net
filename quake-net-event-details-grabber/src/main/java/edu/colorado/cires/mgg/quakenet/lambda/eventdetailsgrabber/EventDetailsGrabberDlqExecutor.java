package edu.colorado.cires.mgg.quakenet.lambda.eventdetailsgrabber;

import edu.colorado.cires.mgg.quakenet.message.EventDetailGrabberMessage;
import java.io.IOException;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventDetailsGrabberDlqExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(EventDetailsGrabberDlqExecutor.class);

  private final Notifier notifier;
  private final S3Doer s3Doer;
  private final EventDetailsGrabberProperties properties;

  public EventDetailsGrabberDlqExecutor(S3Doer s3Doer, Notifier notifier, EventDetailsGrabberProperties properties) {
    this.s3Doer = s3Doer;
    this.notifier = notifier;
    this.properties = properties;
  }


  public void execute(String message, EventDetailGrabberMessage eventMessage) {
    LocalDate date = LocalDate.parse(eventMessage.getDate());
    int year = date.getYear();
    int month = date.getMonthValue();
    String eventId = eventMessage.getEventId();
    String key = String.format("downloads/%d/%02d/%s/%s/event-error-%s-%s.json.gz", year, month, date, eventId, date, eventId);
    LOGGER.info("Saving: {}", key);
    try {
      s3Doer.saveFile(properties.getBucketName(), key, message);
    } catch (IOException e) {
      throw new IllegalStateException("Unable to save error file: " + key + " : " + message, e);
    }
    LOGGER.info("Notifying: {}: {}", properties.getTopicArn(), eventMessage);
    notifier.notify(properties.getTopicArn(), eventMessage);
  }
}
