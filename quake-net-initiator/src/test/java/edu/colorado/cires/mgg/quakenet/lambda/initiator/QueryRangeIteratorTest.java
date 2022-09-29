package edu.colorado.cires.mgg.quakenet.lambda.initiator;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import edu.colorado.cires.mgg.quakenet.message.EventGrabberMessage;
import edu.colorado.cires.mgg.quakenet.message.InfoFile;
import edu.colorado.cires.mgg.quakenet.message.ReportInfoFile;
import edu.colorado.cires.mgg.quakenet.s3.util.InfoFileS3Actions;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class QueryRangeIteratorTest {

  @Test
  void test() {
    String bucketName = "my-bucket";
    String topicArn = "topicArn";
    InfoFileSaver fileInfoSaver = mock(InfoFileSaver.class);
    MessageSender messageSender = mock(MessageSender.class);
    InitiatorProperties properties = new InitiatorProperties();
    properties.setDownloadBucket(bucketName);
    properties.setTopicArn(topicArn);
    properties.setMaxMonthsPerTrigger(366);

    InfoFileS3Actions infoFileS3Actions = mock(InfoFileS3Actions.class);
    Instant now = Instant.now();
    QueryRangeIterator queryRangeIterator = new QueryRangeIterator(fileInfoSaver, messageSender, properties, () -> now, infoFileS3Actions);
    QueryRange queryRange = new QueryRange(LocalDate.parse("2022-06-11"), LocalDate.parse("2022-06-15"));
    queryRangeIterator.forEachDate(queryRange);
    verify(fileInfoSaver, times(1)).saveInfoFile(
        eq(bucketName),
        eq("downloads/2022/06/2022-06-11/usgs-info-2022-06-11.json.gz"),
        eq(InfoFile.Builder.builder().withDate(LocalDate.parse("2022-06-11")).build()));
    verify(fileInfoSaver, times(1)).saveInfoFile(
        eq(bucketName),
        eq("downloads/2022/06/2022-06-12/usgs-info-2022-06-12.json.gz"),
        eq(InfoFile.Builder.builder().withDate(LocalDate.parse("2022-06-12")).build()));
    verify(fileInfoSaver, times(1)).saveInfoFile(
        eq(bucketName),
        eq("downloads/2022/06/2022-06-13/usgs-info-2022-06-13.json.gz"),
        eq(InfoFile.Builder.builder().withDate(LocalDate.parse("2022-06-13")).build()));
    verify(fileInfoSaver, times(1)).saveInfoFile(
        eq(bucketName),
        eq("downloads/2022/06/2022-06-14/usgs-info-2022-06-14.json.gz"),
        eq(InfoFile.Builder.builder().withDate(LocalDate.parse("2022-06-14")).build()));
    verify(fileInfoSaver, times(1)).saveInfoFile(
        eq(bucketName),
        eq("downloads/2022/06/2022-06-15/usgs-info-2022-06-15.json.gz"),
        eq(InfoFile.Builder.builder().withDate(LocalDate.parse("2022-06-15")).build()));
    verify(messageSender, times(1)).sendMessage(
        eq(topicArn),
        eq(EventGrabberMessage.Builder.builder()
            .withStartTime(LocalDate.parse("2022-06-11").atStartOfDay(ZoneId.of("UTC")).toInstant())
            .withEndTime(LocalDate.parse("2022-06-12").atStartOfDay(ZoneId.of("UTC")).toInstant())
            .build()));
    verify(messageSender, times(1)).sendMessage(
        eq(topicArn),
        eq(EventGrabberMessage.Builder.builder()
            .withStartTime(LocalDate.parse("2022-06-12").atStartOfDay(ZoneId.of("UTC")).toInstant())
            .withEndTime(LocalDate.parse("2022-06-13").atStartOfDay(ZoneId.of("UTC")).toInstant())
            .build()));
    verify(messageSender, times(1)).sendMessage(
        eq(topicArn),
        eq(EventGrabberMessage.Builder.builder()
            .withStartTime(LocalDate.parse("2022-06-13").atStartOfDay(ZoneId.of("UTC")).toInstant())
            .withEndTime(LocalDate.parse("2022-06-14").atStartOfDay(ZoneId.of("UTC")).toInstant())
            .build()));
    verify(messageSender, times(1)).sendMessage(
        eq(topicArn),
        eq(EventGrabberMessage.Builder.builder()
            .withStartTime(LocalDate.parse("2022-06-14").atStartOfDay(ZoneId.of("UTC")).toInstant())
            .withEndTime(LocalDate.parse("2022-06-15").atStartOfDay(ZoneId.of("UTC")).toInstant())
            .build()));
    verify(messageSender, times(1)).sendMessage(
        eq(topicArn),
        eq(EventGrabberMessage.Builder.builder()
            .withStartTime(LocalDate.parse("2022-06-15").atStartOfDay(ZoneId.of("UTC")).toInstant())
            .withEndTime(LocalDate.parse("2022-06-16").atStartOfDay(ZoneId.of("UTC")).toInstant())
            .build()));
    verifyNoMoreInteractions(fileInfoSaver, messageSender);

    verify(infoFileS3Actions, times(1)).saveReportInfoFile(
        eq(bucketName),
        eq("reports/2022/06/report-info-2022-06.json.gz"),
        eq(ReportInfoFile.Builder.builder().withStartTime(now).build()));
  }

  @Test
  void testLeapYear() {
    String bucketName = "my-bucket";
    String topicArn = "topicArn";
    InfoFileSaver fileInfoSaver = mock(InfoFileSaver.class);
    MessageSender messageSender = mock(MessageSender.class);
    InitiatorProperties properties = new InitiatorProperties();
    properties.setDownloadBucket(bucketName);
    properties.setTopicArn(topicArn);
    properties.setMaxMonthsPerTrigger(366);

    InfoFileS3Actions infoFileS3Actions = mock(InfoFileS3Actions.class);
    Instant now = Instant.now();
    QueryRangeIterator queryRangeIterator = new QueryRangeIterator(fileInfoSaver, messageSender, properties, () -> now, infoFileS3Actions);
    QueryRange queryRange = new QueryRange(LocalDate.parse("2024-02-27"), LocalDate.parse("2024-02-29"));
    queryRangeIterator.forEachDate(queryRange);
    verify(fileInfoSaver, times(1)).saveInfoFile(
        eq(bucketName),
        eq("downloads/2024/02/2024-02-27/usgs-info-2024-02-27.json.gz"),
        eq(InfoFile.Builder.builder().withDate(LocalDate.parse("2024-02-27")).build()));
    verify(fileInfoSaver, times(1)).saveInfoFile(
        eq(bucketName),
        eq("downloads/2024/02/2024-02-28/usgs-info-2024-02-28.json.gz"),
        eq(InfoFile.Builder.builder().withDate(LocalDate.parse("2024-02-28")).build()));
    verify(fileInfoSaver, times(1)).saveInfoFile(
        eq(bucketName),
        eq("downloads/2024/02/2024-02-29/usgs-info-2024-02-29.json.gz"),
        eq(InfoFile.Builder.builder().withDate(LocalDate.parse("2024-02-29")).build()));
    verify(messageSender, times(1)).sendMessage(
        eq(topicArn),
        eq(EventGrabberMessage.Builder.builder()
            .withStartTime(LocalDate.parse("2024-02-27").atStartOfDay(ZoneId.of("UTC")).toInstant())
            .withEndTime(LocalDate.parse("2024-02-28").atStartOfDay(ZoneId.of("UTC")).toInstant())
            .build()));
    verify(messageSender, times(1)).sendMessage(
        eq(topicArn),
        eq(EventGrabberMessage.Builder.builder()
            .withStartTime(LocalDate.parse("2024-02-28").atStartOfDay(ZoneId.of("UTC")).toInstant())
            .withEndTime(LocalDate.parse("2024-02-29").atStartOfDay(ZoneId.of("UTC")).toInstant())
            .build()));
    verify(messageSender, times(1)).sendMessage(
        eq(topicArn),
        eq(EventGrabberMessage.Builder.builder()
            .withStartTime(LocalDate.parse("2024-02-29").atStartOfDay(ZoneId.of("UTC")).toInstant())
            .withEndTime(LocalDate.parse("2024-03-01").atStartOfDay(ZoneId.of("UTC")).toInstant())
            .build()));
    verifyNoMoreInteractions(fileInfoSaver, messageSender);

    verify(infoFileS3Actions, times(1)).saveReportInfoFile(
        eq(bucketName),
        eq("reports/2024/02/report-info-2024-02.json.gz"),
        eq(ReportInfoFile.Builder.builder().withStartTime(now).build()));
  }

}