package edu.colorado.cires.mgg.quakenet.lambda.eventgrabber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.colorado.cires.mgg.quakenet.geojson.FeatureCollection;
import edu.colorado.cires.mgg.quakenet.geojson.GeoJson;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
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
      if (entity != null) {
        try {
          try (InputStream in = entity.getContent()) {
            return IOUtils.toString(in, StandardCharsets.UTF_8);
          }
        } finally {
          EntityUtils.consume(entity);
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException("Unable to read content", e);
    }
    return "";
  }

  private static CloseableHttpClient createClient(EventGrabberProperties properties) {

    RequestConfig config = RequestConfig.custom().setConnectTimeout(Timeout.of(properties.getConnectionTimeoutMs(), TimeUnit.MILLISECONDS))
        .setConnectionRequestTimeout(Timeout.of(properties.getConnectionTimeoutMs(), TimeUnit.MILLISECONDS))
        .setResponseTimeout(Timeout.of(properties.getRequestTimeoutMs(), TimeUnit.MILLISECONDS)).build();
    return HttpClientBuilder.create().setDefaultRequestConfig(config).build();

  }

  private static String buildUri(String baseUrl, Instant startTime, Instant endTime, int pageSize, int offset, String minimumMagnitude) {
    try {
      return new URIBuilder(baseUrl + "/fdsnws/event/1/query")
          .addParameter("format", "geojson") //Important!  GeoJSON must be used or deleted events may be returned
          .addParameter("starttime", startTime.toString())
          .addParameter("endtime", endTime.minusMillis(1).toString())
          .addParameter("includeallorigins", "false")
          .addParameter("includeallmagnitudes", "false")
          .addParameter("minmagnitude", minimumMagnitude)
          .addParameter("orderby", "time-asc")
          .addParameter("limit", Integer.toString(pageSize))
          .addParameter("offset", Integer.toString(offset)).build().toString();
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

  private static FeatureCollection parseGeoJson(String content, ObjectMapper objectMapper) {
    try {
      return objectMapper.readValue(content, FeatureCollection.class);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to parse json", e);
    }
  }

  public static void query(EventGrabberProperties properties, Instant startTime, Instant endTime, ObjectMapper objectMapper,
      Consumer<List<String>> eventIdConsumer) {
    final int pageSize = properties.getPageSize();


    int page = 0;
    List<String> allEventIds = new ArrayList<>();
    while (true) {

      int offset = page * pageSize + 1;
      page++;
      String uri = buildUri(properties.getBaseUrl(), startTime, endTime, pageSize, offset, properties.getMinimumMagnitude());
      LOGGER.info("Request: {}", uri);


      CloseableHttpClient httpclient = createClient(properties);
      try {
        CloseableHttpResponse response = executeGet(uri, httpclient);
        try {
          int responseCode = response.getCode();
          String content = readContent(response);
          if (responseCode == 200) {
            FeatureCollection featureCollection = parseGeoJson(content, objectMapper);
            List<String> eventIds = featureCollection.getFeatures().stream().map(GeoJson::getId).collect(Collectors.toList());
            allEventIds.addAll(eventIds);
            if (eventIds.isEmpty()) {
              LOGGER.info("No More Results: {} : {} : {}", uri);
              break;
            }
          } else if (responseCode == 204) {
            LOGGER.info("No More Results: {} : {} : {}", uri, responseCode, response.getReasonPhrase());
            break;
          } else {
            LOGGER.error("Unexpected Response: {} : {} : {} : {}", uri, responseCode, response.getReasonPhrase(), content);
            throw new IllegalStateException(String.format("Unexpected Response: %s : %d : %s : %s", uri, responseCode, response.getReasonPhrase(), content));
          }
        } finally {
          IOUtils.closeQuietly(response);
        }
      } finally {
        IOUtils.closeQuietly(httpclient);
      }

    }

    eventIdConsumer.accept(allEventIds);
  }


}
