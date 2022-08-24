package edu.colorado.cires.mgg.quakenet.lambda.eventgrabber;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse.BatchItemFailure;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.colorado.cires.cmg.s3out.AwsS3ClientMultipartUpload;
import edu.colorado.cires.cmg.s3out.S3ClientMultipartUpload;
import edu.colorado.cires.mgg.quakenet.message.EventGrabberMessage;
import edu.colorado.cires.mgg.quakenet.util.ObjectMapperCreator;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sns.SnsClient;

public class EventGrabberLambda implements RequestHandler<SQSEvent, SQSBatchResponse> {

  private static final Logger LOGGER = LoggerFactory.getLogger(EventGrabberLambda.class);

  private static final long connectionTimeoutMs = Long.parseLong(System.getenv("CONNECTION_TIMEOUT_MS"));
  private static final long requestTimeoutMs = Long.parseLong(System.getenv("REQUEST_TIMEOUT_MS"));
  private static final int pageSize = Integer.parseInt(System.getenv("PAGE_SIZE"));
  private static final String topicArn = System.getenv("TOPIC_ARN");
  private static final String downloadBucket = System.getenv("DOWNLOAD_BUCKET");
  private static final SnsClient snsClient = SnsClient.builder().build();
  private static final ObjectMapper objectMapper = ObjectMapperCreator.create();
  private static final S3Client s3Client = S3Client.builder().build();
  private static final S3ClientMultipartUpload s3 = AwsS3ClientMultipartUpload.builder().s3(s3Client).build();
  private static final EventGrabberProperties properties;
  private static final EventDateGrabber eventDetailsGrabber;

  static {
    properties = new EventGrabberProperties();
    properties.setConnectionTimeoutMs(connectionTimeoutMs);
    properties.setRequestTimeoutMs(requestTimeoutMs);
    properties.setPageSize(pageSize);
    properties.setTopicArn(topicArn);
    properties.setBucketName(downloadBucket);
    eventDetailsGrabber = new EventDateGrabber(s3Client, s3, snsClient, objectMapper, properties);
  }

  @Override
  public SQSBatchResponse handleRequest(SQSEvent sqsEvent, Context context) {
    LOGGER.info("Event: {}", sqsEvent);

    List<BatchItemFailure> batchItemFailures = new ArrayList<>();
    String messageId = "";
    for (SQSMessage record : sqsEvent.getRecords()) {
      try {
        messageId = record.getMessageId();

        EventGrabberMessage message = objectMapper.readValue(record.getBody(), EventGrabberMessage.class);

        eventDetailsGrabber.grabDetails(message);

      } catch (Exception e) {
        batchItemFailures.add(new BatchItemFailure(messageId));
        LOGGER.error("Unable to process " + messageId, e);
      }
    }
    return new SQSBatchResponse(batchItemFailures);

  }
}
