package edu.colorado.cires.mgg.quakenet.lambda.eventdetailsgrabber;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse.BatchItemFailure;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.colorado.cires.cmg.s3out.AwsS3ClientMultipartUpload;
import edu.colorado.cires.cmg.s3out.S3ClientMultipartUpload;
import edu.colorado.cires.mgg.quakenet.message.EventDetailGrabberMessage;
import edu.colorado.cires.mgg.quakenet.util.ObjectMapperCreator;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sns.SnsClient;

public class EventDetailsGrabberLambda implements RequestHandler<SQSEvent, SQSBatchResponse> {

  private static final Logger LOGGER = LoggerFactory.getLogger(EventDetailsGrabberLambda.class);

  private static final String downloadBucket = System.getenv("DOWNLOAD_BUCKET");
  private static final long connectionTimeoutMs = Long.parseLong(System.getenv("CONNECTION_TIMEOUT_MS"));
  private static final long requestTimeoutMs = Long.parseLong(System.getenv("REQUEST_TIMEOUT_MS"));
  private static final String topicArn = System.getenv("TOPIC_ARN");
  private static final String baseUrl = System.getenv("BASE_URL");
  private static final SnsClient snsClient = SnsClient.builder().build();
  private static final S3ClientMultipartUpload s3 = AwsS3ClientMultipartUpload.builder().s3(S3Client.builder().build()).build();
  private static final ObjectMapper objectMapper = ObjectMapperCreator.create();
  private static final EventDetailsGrabberProperties properties;
  private static final Notifier notifier;
  private static final EventDetailsGrabberExecutor executor;
  private static final UsgsApiQueryier usgsApiQueryier;
  private static final S3Doer s3Doer;

  static {
    properties = new EventDetailsGrabberProperties();
    properties.setBucketName(downloadBucket);
    properties.setConnectionTimeoutMs(connectionTimeoutMs);
    properties.setRequestTimeoutMs(requestTimeoutMs);
    properties.setTopicArn(topicArn);
    properties.setBaseUrl(baseUrl);
    notifier = new Notifier(snsClient, objectMapper, properties);
    s3Doer = new S3Doer(s3);
    usgsApiQueryier = new UsgsApiQueryier(s3Doer, properties, objectMapper);
    executor = new EventDetailsGrabberExecutor(usgsApiQueryier, notifier);
  }


  @Override
  public SQSBatchResponse handleRequest(SQSEvent sqsEvent, Context context) {
    LOGGER.info("Event: {}", sqsEvent);

    List<BatchItemFailure> batchItemFailures = new ArrayList<>();
    String messageId = "";
    for (SQSMessage record : sqsEvent.getRecords()) {
      try {
        messageId = record.getMessageId();

        EventDetailGrabberMessage message = objectMapper.readValue(record.getBody(), EventDetailGrabberMessage.class);

        executor.execute(message);

      } catch (Exception e) {
        batchItemFailures.add(new BatchItemFailure(messageId));
        LOGGER.error("Unable to process " + messageId, e);
      }
    }
    return new SQSBatchResponse(batchItemFailures);

  }
}
