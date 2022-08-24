package edu.colorado.cires.mgg.quakenet.lambda.eventgrabber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.colorado.cires.cmg.s3out.MultipartUploadRequest;
import edu.colorado.cires.cmg.s3out.S3ClientMultipartUpload;
import edu.colorado.cires.cmg.s3out.S3OutputStream;
import edu.colorado.cires.mgg.quakenet.message.EventDetailGrabberMessage;
import edu.colorado.cires.mgg.quakenet.message.EventGrabberMessage;
import edu.colorado.cires.mgg.quakenet.message.InfoFile;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

public class EventDateGrabber {

  private static final Logger LOGGER = LoggerFactory.getLogger(EventDateGrabber.class);

  private final S3Client s3Client;
  private final S3ClientMultipartUpload s3;
  private final SnsClient snsClient;
  private final ObjectMapper objectMapper;
  private final EventGrabberProperties properties;

  public EventDateGrabber(S3Client s3Client, S3ClientMultipartUpload s3, SnsClient snsClient,
      ObjectMapper objectMapper,
      EventGrabberProperties properties) {
    this.s3Client = s3Client;
    this.s3 = s3;
    this.snsClient = snsClient;
    this.objectMapper = objectMapper;
    this.properties = properties;
  }

  public void grabDetails(EventGrabberMessage message) {
    UsgsApiQueryier.query(
        properties,
        message.getStartTime(),
        message.getEndTime(),
        eventIds -> handleEventIds(eventIds, message.getStartTime().atZone(ZoneId.of("UTC")).toLocalDate()));
  }

  private void handleEventIds(List<String> eventIds, LocalDate date) {
    String key = String.format(
        "downloads/%s/%02d/%s/usgs-info-%s.json.gz",
        date.getYear(),
        date.getDayOfMonth(),
        date,
        date);
    saveInfoFile(key, InfoFile.Builder.builder(readInfo(key)).withEventIds(eventIds).build());
    eventIds.forEach(eventId -> notifyEventId(eventId, date));
  }


  public InfoFile readInfo(String key) {
    GetObjectRequest objectRequest = GetObjectRequest
        .builder()
        .bucket(properties.getBucketName())
        .key(key)
        .build();

    try (InputStream in = new GZIPInputStream(new BufferedInputStream(s3Client.getObject(objectRequest)))) {
      return objectMapper.readValue(in, InfoFile.class);
    } catch (IOException e) {
      throw new IllegalStateException("Unable to read info data", e);
    }
  }

  private void saveInfoFile(String key, InfoFile infoFile) {

    String json;
    try {
      json = objectMapper.writeValueAsString(infoFile);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to serialize message", e);
    }

    try (
        InputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        S3OutputStream s3OutputStream = S3OutputStream.builder()
            .s3(s3)
            .uploadRequest(MultipartUploadRequest.builder().bucket(properties.getBucketName()).key(key).build())
            .autoComplete(false)
            .build();
        OutputStream outputStream = new GZIPOutputStream(s3OutputStream)
    ) {
      IOUtils.copy(inputStream, outputStream);
      s3OutputStream.done();
    } catch (IOException e) {
      throw new IllegalStateException("An error occurred saving info data", e);
    }

  }

  private void notifyEventId(String eventId, LocalDate date) {
    String json;
    try {
      EventDetailGrabberMessage message = new EventDetailGrabberMessage();
      message.setEventId(eventId);
      message.setDate(date.toString());
      json = objectMapper.writeValueAsString(message);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to serialize message", e);
    }

    LOGGER.info("Triggering Detail Query: {}-{}", date, eventId);
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
