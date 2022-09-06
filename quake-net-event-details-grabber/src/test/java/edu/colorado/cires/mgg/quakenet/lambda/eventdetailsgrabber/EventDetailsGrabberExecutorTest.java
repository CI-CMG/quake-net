package edu.colorado.cires.mgg.quakenet.lambda.eventdetailsgrabber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.colorado.cires.mgg.quakenet.message.EventDetailGrabberMessage;
import edu.colorado.cires.mgg.quakenet.util.ObjectMapperCreator;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventDetailsGrabberExecutorTest {

  private MockWebServer mockWebServer;
  private HttpUrl baseUrl;

  @BeforeEach
  void setUp() throws IOException {
    mockWebServer = new MockWebServer();
    mockWebServer.start();
    baseUrl = mockWebServer.url("/");
  }

  @AfterEach
  void tearDown() throws IOException {
    mockWebServer.shutdown();
  }


  @Test
  void test() throws Exception {
    String json = IOUtils.resourceToString("/usgs-us7000fxq2.json", StandardCharsets.UTF_8)
        .replaceAll("\\Qhttps://earthquake.usgs.gov/\\E", baseUrl.toString());

    String cdi = IOUtils.resourceToString("/cdi-1659919988589.xml", StandardCharsets.UTF_8);

    String xml = IOUtils.resourceToString("/usgs-us7000fxq2.xml", StandardCharsets.UTF_8);

    mockWebServer.enqueue(new MockResponse().setBody(json));
    mockWebServer.enqueue(new MockResponse().setBody(cdi));
    mockWebServer.enqueue(new MockResponse().setBody(xml));

    String bucketName = "my-bucket";
    String topicArn = "topicArn";

    EventDetailsGrabberProperties properties = new EventDetailsGrabberProperties();
    properties.setBaseUrl(baseUrl.toString().replaceAll("/$", ""));
    properties.setBucketName(bucketName);
    properties.setTopicArn(topicArn);

    ObjectMapper objectMapper = ObjectMapperCreator.create();
    S3Doer s3Doer = mock(S3Doer.class);
    Notifier notifier = mock(Notifier.class);
    UsgsApiQueryier usgsApiQueryier = new UsgsApiQueryier(s3Doer, properties, objectMapper);
    EventDetailsGrabberExecutor executor = new EventDetailsGrabberExecutor(usgsApiQueryier, notifier, properties);

    String eventId = "us7000fxq2";
    String date = "2021-11-28";

    EventDetailGrabberMessage message = EventDetailGrabberMessage.Builder.builder().withEventId(eventId).withDate(date).build();

    executor.execute(message);

    RecordedRequest request1 = mockWebServer.takeRequest();
    assertEquals("/fdsnws/event/1/query", request1.getRequestUrl().encodedPath());
    assertEquals("format=geojson&eventid=us7000fxq2", request1.getRequestUrl().encodedQuery());

    RecordedRequest request2 = mockWebServer.takeRequest();
    assertEquals("/product/dyfi/us7000fxq2/us/1659919988589/cdi_zip.xml", request2.getRequestUrl().encodedPath());

    RecordedRequest request3 = mockWebServer.takeRequest();
    assertEquals("/fdsnws/event/1/query", request3.getRequestUrl().encodedPath());
    assertEquals("format=quakeml&eventid=us7000fxq2", request3.getRequestUrl().encodedQuery());

    verify(s3Doer, times(1)).saveFile(
        eq(bucketName),
        eq("downloads/2021/11/2021-11-28/us7000fxq2/event-details-2021-11-28-us7000fxq2.json.gz"),
        eq(json));

    verify(s3Doer, times(1)).saveFile(
        eq(bucketName),
        eq("downloads/2021/11/2021-11-28/us7000fxq2/event-cdi-2021-11-28-us7000fxq2.xml.gz"),
        eq(cdi));

    verify(s3Doer, times(1)).saveFile(
        eq(bucketName),
        eq("downloads/2021/11/2021-11-28/us7000fxq2/event-details-2021-11-28-us7000fxq2.xml.gz"),
        eq(xml));
    verify(notifier, times(1)).notify(eq(topicArn), eq(message));

    verifyNoMoreInteractions(notifier);
  }


  @Test
  void testTooMany() throws Exception {
    String json = IOUtils.resourceToString("/usgs-us7000fxq2.json", StandardCharsets.UTF_8)
        .replaceAll("\\Qhttps://earthquake.usgs.gov/\\E", baseUrl.toString());

    String cdi = IOUtils.resourceToString("/cdi-1659919988589.xml", StandardCharsets.UTF_8);

    String xml = IOUtils.resourceToString("/usgs-us7000fxq2.xml", StandardCharsets.UTF_8);

    mockWebServer.enqueue(new MockResponse().setResponseCode(429));

    String bucketName = "my-bucket";
    String topicArn = "topicArn";
    String queueUrl = "queueUrl";
    int delay = 3;

    EventDetailsGrabberProperties properties = new EventDetailsGrabberProperties();
    properties.setBaseUrl(baseUrl.toString().replaceAll("/$", ""));
    properties.setBucketName(bucketName);
    properties.setTopicArn(topicArn);
    properties.setRetryQueueUrl(queueUrl);
    properties.setRetryDelaySeconds(delay);

    ObjectMapper objectMapper = ObjectMapperCreator.create();
    S3Doer s3Doer = mock(S3Doer.class);
    Notifier notifier = mock(Notifier.class);
    UsgsApiQueryier usgsApiQueryier = new UsgsApiQueryier(s3Doer, properties, objectMapper);
    EventDetailsGrabberExecutor executor = new EventDetailsGrabberExecutor(usgsApiQueryier, notifier, properties);

    String eventId = "us7000fxq2";
    String date = "2021-11-28";

    EventDetailGrabberMessage message = EventDetailGrabberMessage.Builder.builder().withEventId(eventId).withDate(date).build();

    executor.execute(message);

    RecordedRequest request1 = mockWebServer.takeRequest();
    assertEquals("/fdsnws/event/1/query", request1.getRequestUrl().encodedPath());
    assertEquals("format=geojson&eventid=us7000fxq2", request1.getRequestUrl().encodedQuery());


    verify(s3Doer, times(0)).saveFile(
        any(),
        any(),
        any());


    verify(notifier, times(0)).notify(any(), any());

    verify(notifier, times(1)).retry(eq(queueUrl), eq(message), eq(delay));

    verifyNoMoreInteractions(notifier);
  }
}