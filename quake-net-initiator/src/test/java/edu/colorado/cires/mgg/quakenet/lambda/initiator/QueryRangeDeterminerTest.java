package edu.colorado.cires.mgg.quakenet.lambda.initiator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.colorado.cires.mgg.quakenet.s3.util.InfoFileS3Actions;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

class QueryRangeDeterminerTest {

  @Test
  void testHasObjects() {
    Set<String> s3ObjectList = new HashSet<>(Arrays.asList(
        "downloads/2012/05/2012-05-10/usgs-info-2012-05-10.json.gz",
        "downloads/2012/05/2012-05-10/id1/event-details-2012-05-10-id1.json.gz",
        "downloads/2012/05/2012-05-10/id2/event-details-2012-05-10-id2.json.gz",
        "downloads/2012/05/2012-05-10/id3/event-details-2012-05-10-id3.json.gz",
        "downloads/2013/05/2013-05-10/usgs-info-2013-05-10.json.gz",
        "downloads/2013/05/2013-05-10/id1/event-details-2013-05-10-id4.json.gz",
        "downloads/2013/05/2013-05-10/id2/event-details-2013-05-10-id5.json.gz",
        "downloads/2013/05/2013-05-10/id3/event-details-2013-05-10-id6.json.gz"
    ));
    String bucketName = "my-bucket";
    InitiatorProperties initiatorProperties = new InitiatorProperties();
    initiatorProperties.setDownloadBucket(bucketName);
    initiatorProperties.setDefaultStartDate("1600-01-01");
    InfoFileS3Actions infoFileS3Actions = mock(InfoFileS3Actions.class);
    when(infoFileS3Actions.isFileExists(eq(bucketName), any())).thenAnswer(new Answer<Boolean>() {
      @Override
      public Boolean answer(InvocationOnMock invocationOnMock) throws Throwable {
        String key = invocationOnMock.getArgument(1, String.class);
        return s3ObjectList.contains(key);
      }
    });

    LocalDate now = LocalDate.parse("2017-01-20");
    QueryRangeDeterminer queryRangeDeterminer = new QueryRangeDeterminer(initiatorProperties, () -> now, infoFileS3Actions);
    QueryRange queryRange = queryRangeDeterminer.getQueryRange().get();
    QueryRange expected = new QueryRange(LocalDate.parse("2013-05-11"), LocalDate.parse("2016-01-20"));
    assertEquals(expected, queryRange);
  }

  @Test
  void testEmpty() {
    String bucketName = "my-bucket";
    InitiatorProperties initiatorProperties = new InitiatorProperties();
    initiatorProperties.setDownloadBucket(bucketName);
    initiatorProperties.setDefaultStartDate("2013-01-01");
    InfoFileS3Actions infoFileS3Actions = mock(InfoFileS3Actions.class);
    when(infoFileS3Actions.isFileExists(eq(bucketName), any())).thenReturn(false);
    LocalDate now = LocalDate.parse("2017-01-20");
    QueryRangeDeterminer queryRangeDeterminer = new QueryRangeDeterminer(initiatorProperties, () -> now, infoFileS3Actions);
    QueryRange queryRange = queryRangeDeterminer.getQueryRange().get();
    QueryRange expected = new QueryRange(LocalDate.parse("2013-01-01"), LocalDate.parse("2016-01-20"));
    assertEquals(expected, queryRange);
  }


  @Test
  void testLeapYear() {
    Set<String> s3ObjectList = new HashSet<>(Arrays.asList(
        "downloads/2012/05/2012-05-10/usgs-info-2012-05-10.json.gz",
        "downloads/2012/05/2012-05-10/id1/event-details-2012-05-10-id1.json.gz",
        "downloads/2012/05/2012-05-10/id2/event-details-2012-05-10-id2.json.gz",
        "downloads/2012/05/2012-05-10/id3/event-details-2012-05-10-id3.json.gz",
        "downloads/2013/05/2013-05-10/usgs-info-2013-05-10.json.gz",
        "downloads/2013/05/2013-05-10/id1/event-details-2013-05-10-id4.json.gz",
        "downloads/2013/05/2013-05-10/id2/event-details-2013-05-10-id5.json.gz",
        "downloads/2013/05/2013-05-10/id3/event-details-2013-05-10-id6.json.gz"
    ));
    String bucketName = "my-bucket";
    InitiatorProperties initiatorProperties = new InitiatorProperties();
    initiatorProperties.setDownloadBucket(bucketName);
    initiatorProperties.setDefaultStartDate("1600-01-01");
    InfoFileS3Actions infoFileS3Actions = mock(InfoFileS3Actions.class);
    when(infoFileS3Actions.isFileExists(eq(bucketName), any())).thenAnswer(new Answer<Boolean>() {
      @Override
      public Boolean answer(InvocationOnMock invocationOnMock) throws Throwable {
        String key = invocationOnMock.getArgument(1, String.class);
        return s3ObjectList.contains(key);
      }
    });
    LocalDate now = LocalDate.parse("2024-02-29");
    QueryRangeDeterminer queryRangeDeterminer = new QueryRangeDeterminer(initiatorProperties, () -> now, infoFileS3Actions);
    QueryRange queryRange = queryRangeDeterminer.getQueryRange().get();
    QueryRange expected = new QueryRange(LocalDate.parse("2013-05-11"), LocalDate.parse("2023-02-28"));
    assertEquals(expected, queryRange);
  }

  @Test
  void testCurrentDate() {
    Set<String> s3ObjectList = new HashSet<>(Arrays.asList(
        "downloads/2012/05/2012-05-10/usgs-info-2012-05-10.json.gz",
        "downloads/2012/05/2012-05-10/id1/event-details-2012-05-10-id1.json.gz",
        "downloads/2012/05/2012-05-10/id2/event-details-2012-05-10-id2.json.gz",
        "downloads/2012/05/2012-05-10/id3/event-details-2012-05-10-id3.json.gz",
        "downloads/2013/05/2013-05-10/usgs-info-2013-05-10.json.gz",
        "downloads/2013/05/2013-05-10/id1/event-details-2013-05-10-id4.json.gz",
        "downloads/2013/05/2013-05-10/id2/event-details-2013-05-10-id5.json.gz",
        "downloads/2013/05/2013-05-10/id3/event-details-2013-05-10-id6.json.gz"
    ));
    String bucketName = "my-bucket";
    InitiatorProperties initiatorProperties = new InitiatorProperties();
    initiatorProperties.setDownloadBucket(bucketName);
    initiatorProperties.setDefaultStartDate("1600-01-01");
    InfoFileS3Actions infoFileS3Actions = mock(InfoFileS3Actions.class);
    when(infoFileS3Actions.isFileExists(eq(bucketName), any())).thenAnswer(new Answer<Boolean>() {
      @Override
      public Boolean answer(InvocationOnMock invocationOnMock) throws Throwable {
        String key = invocationOnMock.getArgument(1, String.class);
        return s3ObjectList.contains(key);
      }
    });
    LocalDate now = LocalDate.parse("2013-05-10");
    QueryRangeDeterminer queryRangeDeterminer = new QueryRangeDeterminer(initiatorProperties, () -> now, infoFileS3Actions);
    assertTrue(queryRangeDeterminer.getQueryRange().isEmpty());
  }
}