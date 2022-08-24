package edu.colorado.cires.mgg.quakenet.lambda.reportinit;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
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

public class ReportInitiatorLambda implements RequestStreamHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReportInitiatorLambda.class);

  private static final String downloadBucket = System.getenv("DOWNLOAD_BUCKET");
  private static final String topicArn = System.getenv("TOPIC_ARN");
  private static final S3Client s3Client = S3Client.builder().build();
  private static final SnsClient snsClient = SnsClient.builder().build();
  private static final ObjectMapper objectMapper = ObjectMapperCreator.create();
  private static final ReportInitiatorProperties properties;
  private static final ReportTrigger reportTrigger;

  static {
    properties = new ReportInitiatorProperties();
    properties.setBucketName(downloadBucket);
    properties.setTopicArn(topicArn);
    reportTrigger = new ReportTrigger(properties, s3Client, objectMapper, snsClient);
  }

  @Override
  public void handleRequest(InputStream in, OutputStream out, Context context) throws IOException {
    String triggerEvent = IOUtils.toString(in, StandardCharsets.UTF_8);
    LOGGER.info("Trigger Event: {}", triggerEvent);
    reportTrigger.triggerReports();
  }
}
