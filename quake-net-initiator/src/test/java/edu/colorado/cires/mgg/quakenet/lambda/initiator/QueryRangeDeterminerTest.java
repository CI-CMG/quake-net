package edu.colorado.cires.mgg.quakenet.lambda.initiator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;

class QueryRangeDeterminerTest {

  @Test
  void testHasObjects() {
    List<String> s3ObjectList = Arrays.asList(
        "downloads/2012/05/2012-05-10/usgs-info-2012-05-10.json.gz",
        "downloads/2012/05/2012-05-10/id1/event-details-2012-05-10-id1.json.gz",
        "downloads/2012/05/2012-05-10/id2/event-details-2012-05-10-id2.json.gz",
        "downloads/2012/05/2012-05-10/id3/event-details-2012-05-10-id3.json.gz",
        "downloads/2013/05/2013-05-10/usgs-info-2013-05-10.json.gz",
        "downloads/2013/05/2013-05-10/id1/event-details-2013-05-10-id4.json.gz",
        "downloads/2013/05/2013-05-10/id2/event-details-2013-05-10-id5.json.gz",
        "downloads/2013/05/2013-05-10/id3/event-details-2013-05-10-id6.json.gz"
    );
    String bucketName = "my-bucket";
    InitiatorProperties initiatorProperties = new InitiatorProperties();
    initiatorProperties.setDownloadBucket(bucketName);
    initiatorProperties.setDefaultStartDate("1600-01-01");
    S3Client s3 = mock(S3Client.class);
    BucketIteratorFactory bucketIteratorFactory = mock(BucketIteratorFactory.class);
    when(bucketIteratorFactory.create(s3, bucketName, "downloads/")).thenReturn(s3ObjectList.iterator());
    LocalDate now = LocalDate.parse("2017-01-20");
    QueryRangeDeterminer queryRangeDeterminer = new QueryRangeDeterminer(initiatorProperties, s3, bucketIteratorFactory, () -> now);
    QueryRange queryRange = queryRangeDeterminer.getQueryRange().get();
    QueryRange expected = new QueryRange(LocalDate.parse("2013-05-11"),  LocalDate.parse("2016-01-20"));
    assertEquals(expected, queryRange);
  }

  @Test
  void testEmpty() {
    List<String> s3ObjectList = Collections.emptyList();
    String bucketName = "my-bucket";
    InitiatorProperties initiatorProperties = new InitiatorProperties();
    initiatorProperties.setDownloadBucket(bucketName);
    initiatorProperties.setDefaultStartDate("2013-01-01");
    S3Client s3 = mock(S3Client.class);
    BucketIteratorFactory bucketIteratorFactory = mock(BucketIteratorFactory.class);
    when(bucketIteratorFactory.create(s3, bucketName, "downloads/")).thenReturn(s3ObjectList.iterator());
    LocalDate now = LocalDate.parse("2017-01-20");
    QueryRangeDeterminer queryRangeDeterminer = new QueryRangeDeterminer(initiatorProperties, s3, bucketIteratorFactory, () -> now);
    QueryRange queryRange = queryRangeDeterminer.getQueryRange().get();
    QueryRange expected = new QueryRange(LocalDate.parse("2013-01-01"),  LocalDate.parse("2016-01-20"));
    assertEquals(expected, queryRange);
  }


  @Test
  void testLeapYear() {
    List<String> s3ObjectList = Arrays.asList(
        "downloads/2012/05/2012-05-10/usgs-info-2012-05-10.json.gz",
        "downloads/2012/05/2012-05-10/id1/event-details-2012-05-10-id1.json.gz",
        "downloads/2012/05/2012-05-10/id2/event-details-2012-05-10-id2.json.gz",
        "downloads/2012/05/2012-05-10/id3/event-details-2012-05-10-id3.json.gz",
        "downloads/2013/05/2013-05-10/usgs-info-2013-05-10.json.gz",
        "downloads/2013/05/2013-05-10/id1/event-details-2013-05-10-id4.json.gz",
        "downloads/2013/05/2013-05-10/id2/event-details-2013-05-10-id5.json.gz",
        "downloads/2013/05/2013-05-10/id3/event-details-2013-05-10-id6.json.gz"
    );
    String bucketName = "my-bucket";
    InitiatorProperties initiatorProperties = new InitiatorProperties();
    initiatorProperties.setDownloadBucket(bucketName);
    initiatorProperties.setDefaultStartDate("1600-01-01");
    S3Client s3 = mock(S3Client.class);
    BucketIteratorFactory bucketIteratorFactory = mock(BucketIteratorFactory.class);
    when(bucketIteratorFactory.create(s3, bucketName, "downloads/")).thenReturn(s3ObjectList.iterator());
    LocalDate now = LocalDate.parse("2024-02-29");
    QueryRangeDeterminer queryRangeDeterminer = new QueryRangeDeterminer(initiatorProperties, s3, bucketIteratorFactory, () -> now);
    QueryRange queryRange = queryRangeDeterminer.getQueryRange().get();
    QueryRange expected = new QueryRange(LocalDate.parse("2013-05-11"),  LocalDate.parse("2023-02-28"));
    assertEquals(expected, queryRange);
  }

  @Test
  void testCurrentDate() {
    List<String> s3ObjectList = Arrays.asList(
        "downloads/2012/05/2012-05-10/usgs-info-2012-05-10.json.gz",
        "downloads/2012/05/2012-05-10/id1/event-details-2012-05-10-id1.json.gz",
        "downloads/2012/05/2012-05-10/id2/event-details-2012-05-10-id2.json.gz",
        "downloads/2012/05/2012-05-10/id3/event-details-2012-05-10-id3.json.gz",
        "downloads/2013/05/2013-05-10/usgs-info-2013-05-10.json.gz",
        "downloads/2013/05/2013-05-10/id1/event-details-2013-05-10-id4.json.gz",
        "downloads/2013/05/2013-05-10/id2/event-details-2013-05-10-id5.json.gz",
        "downloads/2013/05/2013-05-10/id3/event-details-2013-05-10-id6.json.gz"
    );
    String bucketName = "my-bucket";
    InitiatorProperties initiatorProperties = new InitiatorProperties();
    initiatorProperties.setDownloadBucket(bucketName);
    initiatorProperties.setDefaultStartDate("1600-01-01");
    S3Client s3 = mock(S3Client.class);
    BucketIteratorFactory bucketIteratorFactory = mock(BucketIteratorFactory.class);
    when(bucketIteratorFactory.create(s3, bucketName, "downloads/")).thenReturn(s3ObjectList.iterator());
    LocalDate now = LocalDate.parse("2013-05-10");
    QueryRangeDeterminer queryRangeDeterminer = new QueryRangeDeterminer(initiatorProperties, s3, bucketIteratorFactory, () -> now);
    assertTrue(queryRangeDeterminer.getQueryRange().isEmpty());
  }
}