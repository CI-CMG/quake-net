package edu.colorado.cires.mgg.quakenet.lambda.eventdetailsgrabber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.colorado.cires.mgg.quakenet.message.EventDetailGrabberMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

public class Notifier {

  private static final Logger LOGGER = LoggerFactory.getLogger(Notifier.class);

  private final SnsClient snsClient;
  private final SqsClient sqsClient;
  private final ObjectMapper objectMapper;

  public Notifier(SnsClient snsClient, SqsClient sqsClient, ObjectMapper objectMapper) {
    this.snsClient = snsClient;
    this.sqsClient = sqsClient;
    this.objectMapper = objectMapper;
  }

  public void retry(String queueUrl, EventDetailGrabberMessage message, int delaySeconds) {

    LOGGER.info("Retrying: {}", message);

    sqsClient.sendMessage(SendMessageRequest.builder()
        .queueUrl(queueUrl)
        .messageBody(toJson(message))
        .delaySeconds(delaySeconds)
        .build());
  }

  public void abort(String queueUrl, EventDetailGrabberMessage message) {

    LOGGER.info("Aborting: {}", message);

    sqsClient.sendMessage(SendMessageRequest.builder()
        .queueUrl(queueUrl)
        .messageBody(toJson(message))
        .build());
  }

  private String toJson(EventDetailGrabberMessage message) {
    try {
      return objectMapper.writeValueAsString(message);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to serialize message", e);
    }
  }

  public void notify(String topicArn, EventDetailGrabberMessage message) {

    LOGGER.info("Notifying Report Generator: {}", message);

    try {
      PublishRequest request = PublishRequest.builder()
          .message(toJson(message))
          .topicArn(topicArn)
          .build();

      PublishResponse result = snsClient.publish(request);
      LOGGER.info("Message sent. Status is {}", result.sdkHttpResponse().statusCode());

    } catch (SnsException e) {
      throw new IllegalStateException("An error occurred sending message", e);
    }
  }

}
