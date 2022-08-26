package edu.colorado.cires.mgg.quakenet.lambda.eventgrabber;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import edu.colorado.cires.mgg.quakenet.message.EventGrabberMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventDateGrabberTest {

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
    mockWebServer.enqueue(new MockResponse().setBody(IOUtils.resourceToString("/usgs-response-2022-06-11-1.xml", StandardCharsets.UTF_8)));
    mockWebServer.enqueue(new MockResponse().setBody(IOUtils.resourceToString("/usgs-response-2022-06-11-2.xml", StandardCharsets.UTF_8)));
    mockWebServer.enqueue(new MockResponse().setResponseCode(204));



    String topicArn = "topicArn";
    String bucketName = "my-bucket";

    EventGrabberProperties properties = new EventGrabberProperties();
    properties.setBaseUrl(baseUrl.toString().replaceAll("/$", ""));
    properties.setPageSize(200);
    properties.setConnectionTimeoutMs(1000);
    properties.setRequestTimeoutMs(1000);
    properties.setBucketName(bucketName);
    properties.setTopicArn(topicArn);

    InfoFileSaver infoFileSaver = mock(InfoFileSaver.class);
    MessageSender messageSender = mock(MessageSender.class);
    EventDateGrabber eventDateGrabber = new EventDateGrabber(properties, infoFileSaver, messageSender);

    EventGrabberMessage message = EventGrabberMessage.Builder.builder()
        .withStartTime(LocalDate.parse("2022-06-11").atStartOfDay(ZoneId.of("UTC")).toInstant())
        .withEndTime(LocalDate.parse("2022-06-12").atStartOfDay(ZoneId.of("UTC")).toInstant())
        .build();

    eventDateGrabber.grabDetails(message);



    RecordedRequest request1 = mockWebServer.takeRequest();
    assertEquals("/fdsnws/event/1/query", request1.getRequestUrl().encodedPath());
    assertEquals(
        "format=quakeml&starttime=2022-06-11T00%3A00%3A00Z&endtime=2022-06-11T23%3A59%3A59.999Z&includeallorigins=false&includeallmagnitudes=false&orderby=time-asc&limit=200&offset=1",
        request1.getRequestUrl().encodedQuery());

    RecordedRequest request2 = mockWebServer.takeRequest();
    assertEquals("/fdsnws/event/1/query", request2.getRequestUrl().encodedPath());
    assertEquals(
        "format=quakeml&starttime=2022-06-11T00%3A00%3A00Z&endtime=2022-06-11T23%3A59%3A59.999Z&includeallorigins=false&includeallmagnitudes=false&orderby=time-asc&limit=200&offset=201",
        request2.getRequestUrl().encodedQuery());

    RecordedRequest request3 = mockWebServer.takeRequest();
    assertEquals("/fdsnws/event/1/query", request3.getRequestUrl().encodedPath());
    assertEquals(
        "format=quakeml&starttime=2022-06-11T00%3A00%3A00Z&endtime=2022-06-11T23%3A59%3A59.999Z&includeallorigins=false&includeallmagnitudes=false&orderby=time-asc&limit=200&offset=401",
        request3.getRequestUrl().encodedQuery());

  }

}