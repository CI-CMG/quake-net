package edu.colorado.cires.mgg.quakenet.lambda.eventgrabber;

import edu.colorado.cires.mgg.quakenet.model.QnEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
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
import org.quakeml.xmlns.quakeml._1.Quakeml;
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

  private static CloseableHttpClient createClient(EventGrabberProperties properties) {

    RequestConfig config = RequestConfig.custom().setConnectTimeout(Timeout.of(properties.getConnectionTimeoutMs(), TimeUnit.MILLISECONDS))
        .setConnectionRequestTimeout(Timeout.of(properties.getConnectionTimeoutMs(), TimeUnit.MILLISECONDS))
        .setResponseTimeout(Timeout.of(properties.getRequestTimeoutMs(), TimeUnit.MILLISECONDS)).build();
    return HttpClientBuilder.create().setDefaultRequestConfig(config).build();

  }

  private static String buildUri(Instant startTime, Instant endTime, int pageSize, int offset) {
    try {
      return new URIBuilder("https://earthquake.usgs.gov/fdsnws/event/1/query")
          .addParameter("format", "quakeml")
          .addParameter("starttime", startTime.toString())
          .addParameter("endtime", endTime.toString())
          .addParameter("includeallorigins", "false")
          .addParameter("includeallmagnitudes", "false")
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

  private static Quakeml parseQuakeml(String content) {
    try {
      return (Quakeml) JAXBContext.newInstance(Quakeml.class).createUnmarshaller()
          .unmarshal(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    } catch (JAXBException e) {
      throw new IllegalStateException("Unable to parse QuakeML content", e);
    }
  }

  public static void query(EventGrabberProperties properties, Instant startTime, Instant endTime, Consumer<List<String>> eventIdConsumer) {
    final int pageSize = properties.getPageSize();

    int resultCount = -1;
    int page = 0;
    List<String> allEventIds = new ArrayList<>();
    while (resultCount != 0) {

      int offset = page * pageSize + 1;
      page++;
      String uri = buildUri(startTime, endTime, pageSize, offset);
      LOGGER.info("Request: {}", uri);

      List<String> eventIds;

      CloseableHttpClient httpclient = createClient(properties);
      try {
        CloseableHttpResponse response = executeGet(uri, httpclient);
        try {
          int responseCode = response.getCode();
          String content = readContent(response);
          if (responseCode == 200) {
            Quakeml quakeml = parseQuakeml(content);
            eventIds = DataParser.parseEventIds(quakeml);
          } else if (responseCode == 204) {
            LOGGER.info("No More Results: {} : {} : {}", uri, responseCode, response.getReasonPhrase());
            break;
          } else {
            LOGGER.error("Unexpected Response: {} : {} : {} : {}", uri, responseCode, response.getReasonPhrase(), content);
            break;
          }
        } finally {
          IOUtils.closeQuietly(response);
        }
      } catch (Exception e) {
        LOGGER.error("An error occurred: {} ", uri, e);
        break;
      } finally {
        IOUtils.closeQuietly(httpclient);
      }

      resultCount = eventIds.size();
      allEventIds.addAll(eventIds);

    }

    if (!allEventIds.isEmpty()) {
      eventIdConsumer.accept(allEventIds);
    }
  }


}
