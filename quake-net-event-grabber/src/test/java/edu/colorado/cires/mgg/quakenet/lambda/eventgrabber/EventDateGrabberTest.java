package edu.colorado.cires.mgg.quakenet.lambda.eventgrabber;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import edu.colorado.cires.mgg.quakenet.message.EventDetailGrabberMessage;
import edu.colorado.cires.mgg.quakenet.message.EventGrabberMessage;
import edu.colorado.cires.mgg.quakenet.message.InfoFile;
import edu.colorado.cires.mgg.quakenet.s3.util.InfoFileS3Actions;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

    InfoFile infoFile = InfoFile.Builder.builder().withDate(LocalDate.parse("2022-06-11")).build();

    EventGrabberProperties properties = new EventGrabberProperties();
    properties.setBaseUrl(baseUrl.toString().replaceAll("/$", ""));
    properties.setPageSize(200);
    properties.setConnectionTimeoutMs(1000);
    properties.setRequestTimeoutMs(1000);
    properties.setBucketName(bucketName);
    properties.setTopicArn(topicArn);

    InfoFileS3Actions infoFileSaver = mock(InfoFileS3Actions.class);
    MessageSender messageSender = mock(MessageSender.class);

    when(infoFileSaver.readInfoFile(eq(bucketName), eq("downloads/2022/06/2022-06-11/usgs-info-2022-06-11.json.gz"))).thenReturn(Optional.of(infoFile));

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



    List<String> eventIds = Arrays.asList(IOUtils.resourceToString("/usgs-response-2022-06-11.txt", StandardCharsets.UTF_8).split("\n"));

    verify(infoFileSaver, times(1)).readInfoFile(
        eq(bucketName),
        eq("downloads/2022/06/2022-06-11/usgs-info-2022-06-11.json.gz"));

    verify(infoFileSaver, times(1)).saveInfoFile(
        eq(bucketName),
        eq("downloads/2022/06/2022-06-11/usgs-info-2022-06-11.json.gz"),
        eq(InfoFile.Builder.builder().withDate(LocalDate.parse("2022-06-11")).withEventIds(eventIds).build()));

    eventIds.forEach(eventId -> verify(messageSender, times(1)).sendMessage(
        eq(topicArn),
        eq(EventDetailGrabberMessage.Builder.builder()
            .withEventId(eventId)
            .withDate("2022-06-11")
            .build()))
    );

    verifyNoMoreInteractions(messageSender, infoFileSaver);
  }
}