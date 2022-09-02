package edu.colorado.cires.mgg.quakenet.lambda.reportinit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.colorado.cires.mgg.quakenet.message.EventDetailGrabberMessage;
import edu.colorado.cires.mgg.quakenet.message.ReportGenerateMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

public class MessageSender {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageSender.class);

  private final SnsClient snsClient;
  private final ObjectMapper objectMapper;

  public MessageSender(SnsClient snsClient, ObjectMapper objectMapper) {
    this.snsClient = snsClient;
    this.objectMapper = objectMapper;
  }

  public void sendMessage(String topicArn, ReportGenerateMessage message) {
    String json;
    try {
      json = objectMapper.writeValueAsString(message);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to serialize message", e);
    }

    try {
      PublishRequest request = PublishRequest.builder()
          .message(json)
          .topicArn(topicArn)
          .build();

      PublishResponse result = snsClient.publish(request);
      LOGGER.info("Message sent. Status is {}", result.sdkHttpResponse().statusCode());

    } catch (SnsException e) {
      throw new IllegalStateException("An error occurred sending message", e);
    }
  }
}
