package edu.colorado.cires.mgg.quakenet.lambda.eventgrabber;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sns.SnsClient;

public class EventGrabberLambda implements RequestStreamHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(EventGrabberLambda.class);

  private static final String downloadBucket = System.getenv("DOWNLOAD_BUCKET");
  private static final String defaultStartDate = System.getenv("DEFAULT_START_DATE");
  private static final S3Client s3Client = S3Client.builder().build();
  private static final SnsClient snsClient = SnsClient.builder().build();

  @Override
  public void handleRequest(InputStream in, OutputStream out, Context context) throws IOException {
    String event = IOUtils.toString(in, StandardCharsets.UTF_8);
    LOGGER.info("Event: {}", event);
    EventGrabberProperties properties = new EventGrabberProperties();

  }
}
