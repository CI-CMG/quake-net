package edu.colorado.cires.mgg.quakenet.lambda.initiator;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.colorado.cires.cmg.s3out.AwsS3ClientMultipartUpload;
import edu.colorado.cires.cmg.s3out.S3ClientMultipartUpload;
import edu.colorado.cires.mgg.quakenet.util.ObjectMapperCreator;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sns.SnsClient;

public class InitiatorLambda implements RequestStreamHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(InitiatorLambda.class);

  private static final String downloadBucket = System.getenv("DOWNLOAD_BUCKET");
  private static final String defaultStartDate = System.getenv("DEFAULT_START_DATE");
  private static final String topicArn = System.getenv("TOPIC_ARN");
  private static final Integer maxDatesPerTrigger = Integer.parseInt(System.getenv("MAX_DATES_PER_TRIGGER"));
  private static final S3Client s3Client = S3Client.builder().build();
  private static final SnsClient snsClient = SnsClient.builder().build();
  private static final ObjectMapper objectMapper = ObjectMapperCreator.create();
  private static final S3ClientMultipartUpload s3 = AwsS3ClientMultipartUpload.builder().s3(s3Client).build();
  private static final InitiatorProperties properties;
  private static final EventGrabberProcessor processor;

  static {
    properties = new InitiatorProperties();
    properties.setDownloadBucket(downloadBucket);
    properties.setDefaultStartDate(defaultStartDate);
    properties.setTopicArn(topicArn);
    properties.setMaxDatesPerTrigger(maxDatesPerTrigger);
    processor = new EventGrabberProcessor(properties, s3Client, snsClient, objectMapper, s3);
  }

  @Override
  public void handleRequest(InputStream in, OutputStream out, Context context) throws IOException {
    String triggerEvent = IOUtils.toString(in, StandardCharsets.UTF_8);
    LOGGER.info("Trigger Event: {}", triggerEvent);
    processor.process();
  }
}
