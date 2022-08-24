package edu.colorado.cires.mgg.quakenet.lambda.eventgrabber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.colorado.cires.mgg.quakenet.message.EventDetailGrabberMessage;
import edu.colorado.cires.mgg.quakenet.message.EventGrabberMessage;
import java.time.LocalDate;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

public class EventDetailsGrabber {

  private static final Logger LOGGER = LoggerFactory.getLogger(EventDetailsGrabber.class);

  private final SnsClient snsClient;
  private final ObjectMapper objectMapper;
  private final EventGrabberProperties properties;

  public EventDetailsGrabber(SnsClient snsClient, ObjectMapper objectMapper, EventGrabberProperties properties) {
    this.snsClient = snsClient;
    this.objectMapper = objectMapper;
    this.properties = properties;
  }

  public void grabDetails(EventGrabberMessage message) {
    UsgsApiQueryier.query(
        properties,
        message.getStartTime(),
        message.getEndTime(),
        eventId -> handleEventId(eventId, message.getStartTime().atZone(ZoneId.of("UTC")).toLocalDate()));
  }

  private void handleEventId(String eventId, LocalDate date) {
    String json;
    try {
      EventDetailGrabberMessage message = new EventDetailGrabberMessage();
      message.setEventId(eventId);
      message.setDate(date.toString());
      json = objectMapper.writeValueAsString(message);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to serialize message", e);
    }

    LOGGER.info("Triggering Detail Query: {}-{}", date, eventId);
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
