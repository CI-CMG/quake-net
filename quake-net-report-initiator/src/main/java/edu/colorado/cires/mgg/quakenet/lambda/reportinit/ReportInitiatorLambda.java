package edu.colorado.cires.mgg.quakenet.lambda.reportinit;

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

public class ReportInitiatorLambda implements RequestStreamHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReportInitiatorLambda.class);

  private static final String downloadBucket = System.getenv("DOWNLOAD_BUCKET");
  private static final S3Client s3Client = S3Client.builder().build();

  @Override
  public void handleRequest(InputStream in, OutputStream out, Context context) throws IOException {
    String triggerEvent = IOUtils.toString(in, StandardCharsets.UTF_8);
    LOGGER.info("Trigger Event: {}", triggerEvent);
    ReportInitiatorProperties properties = new ReportInitiatorProperties();
    properties.setBucketName(downloadBucket);

    ReportTrigger reportTrigger = new ReportTrigger(properties, s3Client);
    reportTrigger.triggerReports();

  }
}
