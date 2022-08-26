package edu.colorado.cires.mgg.quakenet.lambda.initiator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.colorado.cires.cmg.s3out.MultipartUploadRequest;
import edu.colorado.cires.cmg.s3out.S3ClientMultipartUpload;
import edu.colorado.cires.cmg.s3out.S3OutputStream;
import edu.colorado.cires.mgg.quakenet.message.EventGrabberMessage;
import edu.colorado.cires.mgg.quakenet.message.InfoFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

public class QueryRangeIterator {


  private static final Logger LOGGER = LoggerFactory.getLogger(QueryRangeIterator.class);

  private final SnsClient snsClient;
  private final ObjectMapper objectMapper;
  private final InitiatorProperties properties;
  private final S3ClientMultipartUpload s3;


  public QueryRangeIterator(SnsClient snsClient, ObjectMapper objectMapper,
      InitiatorProperties properties, S3ClientMultipartUpload s3) {
    this.snsClient = snsClient;
    this.objectMapper = objectMapper;
    this.properties = properties;
    this.s3 = s3;
  }

  public void forEachDate(QueryRange queryRange) {

    LOGGER.info("Processing Range {} - {}", queryRange.getStart(), queryRange.getEnd());

    LocalDate date = queryRange.getStart();
    while (date.isBefore(queryRange.getEnd()) || date.isEqual(queryRange.getEnd())) {
      LOGGER.info("Processing {}", date);
      saveInfoFile(date);
      sendMessage(date);
      date = date.plusDays(1L);
    }
  }

  private void sendMessage(LocalDate date) {
    String json;
    try {
      EventGrabberMessage message = new EventGrabberMessage();
      message.setStartTime(date.atStartOfDay(ZoneId.of("UTC")).toInstant());
      message.setEndTime(date.plusDays(1L).atStartOfDay(ZoneId.of("UTC")).toInstant());
      json = objectMapper.writeValueAsString(message);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to serialize message", e);
    }

    LOGGER.info("Triggering USGS Query: {}", date);
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

  private void saveInfoFile(LocalDate date) {

    LOGGER.info("Saving Info File: {}", date);

    String json;
    try {
      InfoFile infoFile = InfoFile.Builder.builder().withDate(date).build();
      json = objectMapper.writeValueAsString(infoFile);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to serialize message", e);
    }
    String key = String.format(
        "downloads/%s/%02d/%s/usgs-info-%s.json.gz",
        date.getYear(),
        date.getDayOfMonth(),
        date,
        date);
    try (
        InputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        S3OutputStream s3OutputStream = S3OutputStream.builder()
            .s3(s3)
            .uploadRequest(MultipartUploadRequest.builder().bucket(properties.getDownloadBucket()).key(key).build())
            .autoComplete(false)
            .build();
        OutputStream outputStream = new GZIPOutputStream(s3OutputStream)
    ) {
      IOUtils.copy(inputStream, outputStream);
      s3OutputStream.done();
    } catch (IOException e) {
      throw new IllegalStateException("An error occurred saving USGS data", e);
    }

  }
}
