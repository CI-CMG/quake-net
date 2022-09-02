package edu.colorado.cires.mgg.quakenet.lambda.reportinit;

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
import edu.colorado.cires.mgg.quakenet.s3.util.BucketIterator;
import edu.colorado.cires.mgg.quakenet.s3.util.InfoFileS3Actions;
import edu.colorado.cires.mgg.quakenet.util.ObjectMapperCreator;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sns.SnsClient;

public class ReportInitiatorLambda implements RequestHandler<SQSEvent, SQSBatchResponse> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReportInitiatorLambda.class);

  private static final String downloadBucket = System.getenv("DOWNLOAD_BUCKET");
  private static final String topicArn = System.getenv("TOPIC_ARN");
  private static final S3Client s3Client = S3Client.builder().build();
  private static final S3ClientMultipartUpload s3 = AwsS3ClientMultipartUpload.builder().s3(s3Client).build();
  private static final SnsClient snsClient = SnsClient.builder().build();
  private static final ObjectMapper objectMapper = ObjectMapperCreator.create();
  private static final ReportInitiatorProperties properties;
  private static final ReportTrigger reportTrigger;

  static {
    properties = new ReportInitiatorProperties();
    properties.setBucketName(downloadBucket);
    properties.setTopicArn(topicArn);
    MessageSender messageSender = new MessageSender(snsClient, objectMapper);
    InfoFileS3Actions infoFileS3Actions = new InfoFileS3Actions(s3, s3Client, objectMapper);
    reportTrigger = new ReportTrigger(
        properties,
        messageSender,
        (bucketName, prefix) -> new BucketIterator(s3Client, bucketName, prefix),
        infoFileS3Actions);
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

        reportTrigger.triggerReports(message);

      } catch (Exception e) {
        batchItemFailures.add(new BatchItemFailure(messageId));
        LOGGER.error("Unable to process " + messageId, e);
      }
    }
    return new SQSBatchResponse(batchItemFailures);

  }
}
