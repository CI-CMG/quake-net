package edu.colorado.cires.mgg.quakenet.lambda.eventdetailsgrabber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.colorado.cires.cmg.s3out.MultipartUploadRequest;
import edu.colorado.cires.cmg.s3out.S3ClientMultipartUpload;
import edu.colorado.cires.cmg.s3out.S3OutputStream;
import edu.colorado.cires.mgg.quakenet.geojson.GeoJson;
import edu.colorado.cires.mgg.quakenet.message.EventDetailGrabberMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UsgsApiQueryier {

  private static final Logger LOGGER = LoggerFactory.getLogger(UsgsApiQueryier.class);

  private static String readContent(CloseableHttpResponse response) {
    try {
      HttpEntity entity = response.getEntity();
      try {
        try (InputStream in = entity.getContent()) {
          return IOUtils.toString(in, StandardCharsets.UTF_8);
        }
      } finally {
        EntityUtils.consume(entity);
      }
    } catch (IOException e) {
      throw new IllegalStateException("Unable to read content", e);
    }
  }

  private static CloseableHttpClient createClient(EventDetailsGrabberProperties properties) {

    RequestConfig config = RequestConfig.custom().setConnectTimeout(Timeout.of(properties.getConnectionTimeoutMs(), TimeUnit.MILLISECONDS))
        .setConnectionRequestTimeout(Timeout.of(properties.getConnectionTimeoutMs(), TimeUnit.MILLISECONDS))
        .setResponseTimeout(Timeout.of(properties.getRequestTimeoutMs(), TimeUnit.MILLISECONDS)).build();
    return HttpClientBuilder.create().setDefaultRequestConfig(config).build();

  }

  private static String buildUri(String eventId, String type) {
    try {

      return new URIBuilder("https://earthquake.usgs.gov/fdsnws/event/1/query")
          .addParameter("format", type)
          .addParameter("eventid", eventId)
          .build().toString();

    } catch (URISyntaxException e) {
      throw new IllegalStateException("Unable to build URI", e);
    }
  }

  private static CloseableHttpResponse executeGet(String uri, CloseableHttpClient httpclient) {
    try {
      return httpclient.execute(new HttpGet(uri));
    } catch (IOException e) {
      throw new IllegalStateException("Unable to execute GET: " + uri, e);
    }
  }

  private static String queryDetails(
      EventDetailsGrabberProperties properties,
      EventDetailGrabberMessage message,
      S3ClientMultipartUpload s3,
      Supplier<String> uriSupplier,
      Function<String, String> fileNameSupplier
  ) {

    String uri = uriSupplier.get();
    LOGGER.info("Request: {}", uri);

    CloseableHttpClient httpclient = createClient(properties);
    try {
      CloseableHttpResponse response = executeGet(uri, httpclient);
      try {
        int responseCode = response.getCode();
        String content = readContent(response);
        if (responseCode == 200) {
          // downloads/2012/05/2012-05-10/usgs-info-2012-05-10.json.gz
          String date = message.getDate();
          String[] parts = date.split("-");
          String year = parts[0];
          String month = parts[1];
          String key = "downloads/" + year + "/" + month + "/" + date + "/" + fileNameSupplier.apply(date);

          try (
              InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
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
            throw new IllegalStateException("An error occurred getting USGS data: " + uri, e);
          }
        } else if (responseCode == 204) {
          LOGGER.error("No Content: {} : {} : {}", uri, responseCode, response.getReasonPhrase());
          throw new IllegalStateException("No Content: " + uri);
        } else {
          LOGGER.error("Unexpected Response: {} : {} : {} : {}", uri, responseCode, response.getReasonPhrase(), content);
          throw new IllegalStateException("Unexpected Response: " + uri);
        }
        return content;
      } finally {
        IOUtils.closeQuietly(response);
      }
    } finally {
      IOUtils.closeQuietly(httpclient);
    }
  }


  public static String queryDetailsQuakeMl(EventDetailsGrabberProperties properties, EventDetailGrabberMessage message, S3ClientMultipartUpload s3) {
    return queryDetails(
        properties,
        message,
        s3,
        () -> buildUri(message.getEventId(), "quakeml"),
        date -> "event-details-" + date + ".xml.gz");
  }

  public static String queryDetailsJson(EventDetailsGrabberProperties properties, EventDetailGrabberMessage message, S3ClientMultipartUpload s3) {
    return queryDetails(
        properties,
        message,
        s3,
        () -> buildUri(message.getEventId(), "geojson"),
        date -> "event-details-" + date + ".json.gz");
  }

  public static Optional<String> parseCdiUri(String content, ObjectMapper objectMapper) {
    GeoJson geoJson;
    try {
      geoJson = objectMapper.readValue(content, GeoJson.class);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to parse geojson", e);
    }
    return DataParser.parseCdiUrl(geoJson);
  }


  public static String queryCdi(
      EventDetailsGrabberProperties properties,
      EventDetailGrabberMessage message,
      S3ClientMultipartUpload s3,
      String uri
  ) {
    return queryDetails(
        properties,
        message,
        s3,
        () -> uri,
        date -> "event-cdi-" + date + ".xml.gz");
  }


}
