package edu.colorado.cires.mgg.quakenet.lambda.reportinit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.colorado.cires.mgg.quakenet.message.EventDetailGrabberMessage;
import edu.colorado.cires.mgg.quakenet.message.InfoFile;
import edu.colorado.cires.mgg.quakenet.message.ReportGenerateMessage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
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

  private boolean isReportExists(LocalDate date) {
    HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
        .bucket(properties.getBucketName())
        .key(String.format("reports/%d/%02d/earthquake-info-%d-%02d.pdf", date.getYear(), date.getDayOfMonth(), date.getYear(), date.getDayOfMonth()))
        .build();
    try {
      s3.headObject(headObjectRequest);
    } catch (NoSuchKeyException e) {
      return false;
    }
    return true;
  }

  private InfoFile readInfo(String key) {

    GetObjectRequest objectRequest = GetObjectRequest
        .builder()
        .bucket(properties.getBucketName())
        .key(key)
        .build();

    try (InputStream in = new GZIPInputStream(new BufferedInputStream(s3.getObject(objectRequest)))) {
      return objectMapper.readValue(in, InfoFile.class);
    } catch (IOException e) {
      throw new IllegalStateException("Unable to read info data", e);
    }
  }

  public void triggerReports(EventDetailGrabberMessage message) {

    LocalDate date = LocalDate.parse(message.getDate());

    if(!isReportExists(date)) {
      Set<String> expectedEventIds = new HashSet<>();
      Set<String> actualEventIds = new HashSet<>();

      ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
          .bucket(properties.getBucketName())
          .prefix(String.format("downloads/%d/%02d/", date.getYear(), date.getDayOfMonth()))
          .build();

      ListObjectsV2Response listObjectsResponse;
      do {
        listObjectsResponse = s3.listObjectsV2(listObjectsRequest);
        for (S3Object s3Object : listObjectsResponse.contents()) {
          String key = s3Object.key();
          String[] parts = key.split("/");
          if (parts.length == 5) {
            String dateStr = parts[3];
            String file = parts[4];
            if (file.equals(String.format("usgs-info-%s.json.gz", dateStr))) {
              InfoFile infoFile = readInfo(key);
              expectedEventIds.addAll(infoFile.getEventIds());
            }
          } else if (parts.length == 6) {
            String eventId = parts[4];
            String file = parts[5];
            if (file.equals("event-details-" + date + "-" + eventId + ".xml.gz")) {
              actualEventIds.add(eventId);
            }
          }
        }
      } while (listObjectsResponse.isTruncated());

      if (expectedEventIds.equals(actualEventIds)) {
        sendGenerateReportMessage(date.getYear(), date.getDayOfMonth());
      }
    } else {
      LOGGER.info("Report already existed: {}", message.getDate());
    }

  }

  private void sendGenerateReportMessage(int year, int month) {

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
