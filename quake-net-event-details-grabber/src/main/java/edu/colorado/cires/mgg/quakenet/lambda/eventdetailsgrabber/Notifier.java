package edu.colorado.cires.mgg.quakenet.lambda.eventdetailsgrabber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.colorado.cires.mgg.quakenet.message.EventDetailGrabberMessage;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

public class Notifier {

  private static final Logger LOGGER = LoggerFactory.getLogger(Notifier.class);

  private final SnsClient snsClient;
  private final ObjectMapper objectMapper;
  private final EventDetailsGrabberProperties properties;

  public Notifier(SnsClient snsClient, ObjectMapper objectMapper,
      EventDetailsGrabberProperties properties) {
    this.snsClient = snsClient;
    this.objectMapper = objectMapper;
    this.properties = properties;
  }

  public void notify(String eventId, LocalDate date) {
    String json;
    try {
      EventDetailGrabberMessage message = new EventDetailGrabberMessage();
      message.setEventId(eventId);
      message.setDate(date.toString());
      json = objectMapper.writeValueAsString(message);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to serialize message", e);
    }

    LOGGER.info("Notifying Report Generator: {}-{}", date, eventId);
    try {
      PublishRequest request = PublishRequest.builder()
          .message(json)
          .topicArn(properties.getTopicArn())
          .build();

      PublishResponse result = snsClient.publish(request);
      LOGGER.info("Message sent. Status is {}", result.sdkHttpResponse().statusCode());

    } catch (SnsException e) {
      throw new IllegalStateException("An error occurred sending message", e);
    }
  }

}
