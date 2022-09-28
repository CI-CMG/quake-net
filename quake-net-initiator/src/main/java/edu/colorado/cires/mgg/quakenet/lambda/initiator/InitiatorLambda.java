package edu.colorado.cires.mgg.quakenet.lambda.initiator;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.colorado.cires.cmg.s3out.AwsS3ClientMultipartUpload;
import edu.colorado.cires.cmg.s3out.S3ClientMultipartUpload;
import edu.colorado.cires.mgg.quakenet.s3.util.InfoFileS3Actions;
import edu.colorado.cires.mgg.quakenet.s3.util.S3FileUtilities;
import edu.colorado.cires.mgg.quakenet.util.ObjectMapperCreator;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
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
  private static final Integer maxMonthsPerTrigger = Integer.parseInt(System.getenv("MAX_MONTHS_PER_TRIGGER"));
  private static final Integer retryQuietTimeMinutes = Integer.parseInt(System.getenv("RETRY_QUIET_TIME_MINUTES"));
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
    properties.setMaxMonthsPerTrigger(maxMonthsPerTrigger);
    properties.setRetryQuietTimeMinutes(retryQuietTimeMinutes);
    InfoFileS3Actions infoFileS3Actions = new InfoFileS3Actions(s3, s3Client, objectMapper);
    S3FileUtilities s3FileUtilities = new S3FileUtilities(s3, s3Client);
    InfoFileSaver fileInfoSaver = new InfoFileSaver(s3, objectMapper);
    MessageSender messageSender = new MessageSender(snsClient, objectMapper);
    QueryRangeDeterminer queryRangeDeterminer = new QueryRangeDeterminer(properties, LocalDate::now, infoFileS3Actions);
    QueryRangeIterator queryRangeIterator = new QueryRangeIterator(fileInfoSaver, messageSender, properties, Instant::now, infoFileS3Actions);
    ReportRetryProcessor reportRetryProcessor = new ReportRetryProcessor(properties, LocalDate::now, Instant::now, infoFileS3Actions, s3FileUtilities);
    processor = new EventGrabberProcessor(properties, queryRangeDeterminer, queryRangeIterator, reportRetryProcessor);
  }

  @Override
  public void handleRequest(InputStream in, OutputStream out, Context context) throws IOException {
    String triggerEvent = IOUtils.toString(in, StandardCharsets.UTF_8);
    LOGGER.info("Trigger Event: {}", triggerEvent);
    processor.process();
  }
}
