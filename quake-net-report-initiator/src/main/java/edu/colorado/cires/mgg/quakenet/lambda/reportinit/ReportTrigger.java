package edu.colorado.cires.mgg.quakenet.lambda.reportinit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.colorado.cires.mgg.quakenet.message.ReportGenerateMessage;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

public class ReportTrigger {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReportTrigger.class);

  private final ReportInitiatorProperties properties;
  private final S3Client s3;
  private final ObjectMapper objectMapper;
  private final SnsClient snsClient;

  public ReportTrigger(ReportInitiatorProperties properties, S3Client s3, ObjectMapper objectMapper,
      SnsClient snsClient) {
    this.properties = properties;
    this.s3 = s3;
    this.objectMapper = objectMapper;
    this.snsClient = snsClient;
  }

  private boolean isReportExists(String year, String month) {
    HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
        .bucket(properties.getBucketName())
        .key("reports/" + year + "/" + month + "/earthquake-info-" + year + "-" + month + ".pdf")
        .build();
    try {
      s3.headObject(headObjectRequest);
    } catch (NoSuchKeyException e) {
      return  false;
    }
    return true;
  }

  public void triggerReports() {
    ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
        .bucket(properties.getBucketName())
        .prefix("downloads/")
        .build();

    ListObjectsV2Response listObjectsResponse;
    do {
      listObjectsResponse = s3.listObjectsV2(listObjectsRequest);
      for (S3Object s3Object : listObjectsResponse.contents()) {
        String[] parts = s3Object.key().split("/");
        if(parts.length == 5) {
          String year = parts[1];
          String month = parts[2];
          LocalDate date = LocalDate.parse(parts[3]);
          String file = parts[4];
          if(date.getDayOfMonth() == 1 && file.equals("usgs-info-" + date + ".json.gz") && !isReportExists(year, month)) {
            sendGenerateReportMessage(year, month);
          }
        }
      }
    } while (listObjectsResponse.isTruncated());

  }

  private void sendGenerateReportMessage(String year, String month) {

    String json;
    try {
      ReportGenerateMessage message = new ReportGenerateMessage();
      message.setYear(year);
      message.setMonth(month);
      json = objectMapper.writeValueAsString(message);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to serialize message", e);
    }

    LOGGER.info("Triggering Report: {}-{}", year, month);
    try {
      PublishRequest request = PublishRequest.builder()
          .message(json)
          .topicArn(properties.getTopicArn())
          .build();

      PublishResponse result = snsClient.publish(request);
      LOGGER.info("Message sent. Status is {}", result.sdkHttpResponse().statusCode());

    } catch (SnsException e) {
      throw new IllegalStateException("An error occurred sending message", e);
    }
  }

}
