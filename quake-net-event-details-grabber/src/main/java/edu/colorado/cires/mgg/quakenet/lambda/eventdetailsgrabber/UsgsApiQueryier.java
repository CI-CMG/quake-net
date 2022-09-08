package edu.colorado.cires.mgg.quakenet.lambda.eventdetailsgrabber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.colorado.cires.mgg.quakenet.geojson.GeoJson;
import edu.colorado.cires.mgg.quakenet.message.EventDetailGrabberMessage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
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

  private final S3Doer s3Doer;
  private final EventDetailsGrabberProperties properties;
  private final ObjectMapper objectMapper;

  public UsgsApiQueryier(S3Doer s3Doer, EventDetailsGrabberProperties properties, ObjectMapper objectMapper) {
    this.s3Doer = s3Doer;
    this.properties = properties;
    this.objectMapper = objectMapper;
  }


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

  private String buildUri(String eventId, String type) {
    try {

      return new URIBuilder(properties.getBaseUrl() + "/fdsnws/event/1/query")
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

  private String queryDetails(
      EventDetailGrabberMessage message,
      String uri,
      BiFunction<String, String, String> fileNameSupplier
  ) throws TooManyRequestsException {

    LOGGER.info("Request: {}", uri);

    CloseableHttpClient httpclient = createClient(properties);
    try {
      CloseableHttpResponse response = executeGet(uri, httpclient);
      try {
        int responseCode = response.getCode();
        String content = readContent(response);
        if (responseCode == 200) {
          // downloads/2012/05/2012-05-10/<eventId>/<name>-2012-05-10-<eventId>.json.gz
          String date = message.getDate();
          String eventId = message.getEventId();
          String[] parts = date.split("-");
          String year = parts[0];
          String month = parts[1];
          String key = "downloads/" + year + "/" + month + "/" + date + "/" + eventId + "/" + fileNameSupplier.apply(date, eventId);

          try {
            s3Doer.saveFile(properties.getBucketName(), key, content);
          } catch (IOException e) {
            throw new IllegalStateException("An error occurred getting USGS data: " + uri, e);
          }
        } else if (responseCode == 204) {
          LOGGER.error("No Content: {} : {} : {}", uri, responseCode, response.getReasonPhrase());
          throw new IllegalStateException("No Content: " + uri);
        } else if (responseCode == 429) {
          LOGGER.error("Too many requests. Will try again later: {} : {} : {} : {}", uri, responseCode, response.getReasonPhrase(), content);
          throw new TooManyRequestsException(
              String.format("Too many requests. Will try again later: %s : %d : %s : %s", uri, responseCode, response.getReasonPhrase(), content),
              message);
        } else if (responseCode == 409) {
          String error = String.format("Conflict. The event was deleted: %s : %d : %s : %s", uri, responseCode, response.getReasonPhrase(), content);
          LOGGER.error(error);
          throw new ApiAbortException(error, EventDetailGrabberMessage.Builder.builder(message).withError(error).build());
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


  public String queryDetailsQuakeMl(EventDetailGrabberMessage message) throws TooManyRequestsException {
    return queryDetails(
        message,
        buildUri(message.getEventId(), "quakeml"),
        (date, eventId) -> "event-details-" + date + "-" + eventId + ".xml.gz");
  }

  public String queryDetailsJson(EventDetailGrabberMessage message) throws TooManyRequestsException {
    return queryDetails(
        message,
        buildUri(message.getEventId(), "geojson"),
        (date, eventId) -> "event-details-" + date + "-" + eventId + ".json.gz");
  }

  public Optional<String> parseCdiUri(String content) {
    GeoJson geoJson;
    try {
      geoJson = objectMapper.readValue(content, GeoJson.class);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to parse geojson", e);
    }
    return DataParser.parseCdiUrl(geoJson);
  }


  public String queryCdi(
      EventDetailGrabberMessage message,
      String uri
  ) throws TooManyRequestsException {
    return queryDetails(
        message,
        uri,
        (date, eventId) -> "event-cdi-" + date + "-" + eventId + ".xml.gz");
  }


}
