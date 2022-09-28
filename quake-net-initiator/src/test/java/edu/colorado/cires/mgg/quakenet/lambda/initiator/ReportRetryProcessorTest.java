package edu.colorado.cires.mgg.quakenet.lambda.initiator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import edu.colorado.cires.mgg.quakenet.message.ReportInfoFile;
import edu.colorado.cires.mgg.quakenet.s3.util.InfoFileS3Actions;
import edu.colorado.cires.mgg.quakenet.s3.util.S3FileUtilities;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ReportRetryProcessorTest {

  @Test
  void test() throws Exception {

//        //missing reports/2013/01
//        "reports/2013/02/report-info-2013-02.json.gz",
//        "reports/2013/02/earthquake-info-2013-02.pdf", //done
//        //missing reports/2013/03
//        "reports/2013/04/report-info-2013-04.json.gz", //no start time
//        "reports/2013/05/report-info-2013-05.json.gz",
//        "reports/2013/05/earthquake-info-2013-05.pdf", //done
//        "reports/2013/06/earthquake-info-2013-06.pdf",  //no report start time, old start time
//        "reports/2013/07/report-info-2013-07.json.gz",
//        "reports/2013/07/earthquake-info-2013-07.pdf", //done
//        "reports/2013/08/earthquake-info-2013-08.pdf",  //old report start time
//        "reports/2013/09/earthquake-info-2013-09.pdf",  //recent report start time
//        "reports/2013/10/earthquake-info-2013-10.pdf",  //no report start time, recent start time
//        "reports/2013/11/report-info-2013-11.json.gz",
//        "reports/2013/11/earthquake-info-2013-11.pdf", //done
//        "reports/2013/12/report-info-2013-12.json.gz" //current month

    final int quietMinutes = 60;

    String bucketName = "my-bucket";
    InitiatorProperties initiatorProperties = new InitiatorProperties();
    initiatorProperties.setDownloadBucket(bucketName);
    initiatorProperties.setDefaultStartDate("2013-01-01");
    initiatorProperties.setMaxMonthsPerTrigger(12);
    initiatorProperties.setRetryQuietTimeMinutes(quietMinutes);
    LocalDate today = LocalDate.parse("2014-12-05");
    Instant now = today.atStartOfDay(ZoneId.of("UTC")).toInstant();
    InfoFileS3Actions infoFileS3Actions = mock(InfoFileS3Actions.class);
    S3FileUtilities s3FileUtilities = mock(S3FileUtilities.class);

    when(s3FileUtilities.isFileExists(eq(bucketName), eq("reports/2013/01/earthquake-info-2013-01.pdf"))).thenReturn(false);
    when(s3FileUtilities.isFileExists(eq(bucketName), eq("reports/2013/02/earthquake-info-2013-02.pdf"))).thenReturn(true);
    when(s3FileUtilities.isFileExists(eq(bucketName), eq("reports/2013/03/earthquake-info-2013-03.pdf"))).thenReturn(false);
    when(s3FileUtilities.isFileExists(eq(bucketName), eq("reports/2013/04/earthquake-info-2013-04.pdf"))).thenReturn(false);
    when(s3FileUtilities.isFileExists(eq(bucketName), eq("reports/2013/05/earthquake-info-2013-05.pdf"))).thenReturn(true);
    when(s3FileUtilities.isFileExists(eq(bucketName), eq("reports/2013/06/earthquake-info-2013-06.pdf"))).thenReturn(false);
    when(s3FileUtilities.isFileExists(eq(bucketName), eq("reports/2013/07/earthquake-info-2013-07.pdf"))).thenReturn(true);
    when(s3FileUtilities.isFileExists(eq(bucketName), eq("reports/2013/08/earthquake-info-2013-08.pdf"))).thenReturn(false);
    when(s3FileUtilities.isFileExists(eq(bucketName), eq("reports/2013/09/earthquake-info-2013-09.pdf"))).thenReturn(false);
    when(s3FileUtilities.isFileExists(eq(bucketName), eq("reports/2013/10/earthquake-info-2013-10.pdf"))).thenReturn(false);
    when(s3FileUtilities.isFileExists(eq(bucketName), eq("reports/2013/11/earthquake-info-2013-11.pdf"))).thenReturn(true);

    when(infoFileS3Actions.readReportInfoFile(eq(bucketName), eq("reports/2013/01/report-info-2013-01.json.gz"))).thenReturn(Optional.empty());
    when(infoFileS3Actions.readReportInfoFile(eq(bucketName), eq("reports/2013/03/report-info-2013-03.json.gz"))).thenReturn(Optional.empty());
    when(infoFileS3Actions.readReportInfoFile(eq(bucketName), eq("reports/2013/04/report-info-2013-04.json.gz")))
        .thenReturn(Optional.of(ReportInfoFile.Builder.builder().build()));
    when(infoFileS3Actions.readReportInfoFile(eq(bucketName), eq("reports/2013/06/report-info-2013-06.json.gz")))
        .thenReturn(Optional.of(ReportInfoFile.Builder.builder().withStartTime(now.minusSeconds(quietMinutes * 60).minusSeconds(1)).build()));
    when(infoFileS3Actions.readReportInfoFile(eq(bucketName), eq("reports/2013/08/report-info-2013-08.json.gz")))
        .thenReturn(Optional.of(ReportInfoFile.Builder.builder()
            .withStartTime(now.minusSeconds(quietMinutes * 60).minusSeconds(1))
            .withStartReportGeneration(now.minusSeconds(quietMinutes * 60).minusSeconds(1))
            .build()));
    when(infoFileS3Actions.readReportInfoFile(eq(bucketName), eq("reports/2013/09/report-info-2013-09.json.gz")))
        .thenReturn(Optional.of(ReportInfoFile.Builder.builder()
            .withStartTime(now.minusSeconds(quietMinutes * 60).minusSeconds(1))
            .withStartReportGeneration(now.minusSeconds(quietMinutes * 60).plusSeconds(1))
            .build()));
    when(infoFileS3Actions.readReportInfoFile(eq(bucketName), eq("reports/2013/10/report-info-2013-10.json.gz")))
        .thenReturn(Optional.of(ReportInfoFile.Builder.builder()
            .withStartTime(now.minusSeconds(quietMinutes * 60).plusSeconds(1))
            .build()));

    ReportRetryProcessor processor = new ReportRetryProcessor(initiatorProperties, () -> today, () -> now, infoFileS3Actions, s3FileUtilities);
    List<QueryRange> retries = processor.prepareFailedReports();
    List<QueryRange> expectedRetries = new ArrayList<>();
    expectedRetries.add(new QueryRange(YearMonth.of(2013, 1).atDay(1), YearMonth.of(2013, 1).atEndOfMonth()));
    expectedRetries.add(new QueryRange(YearMonth.of(2013, 3).atDay(1), YearMonth.of(2013, 3).atEndOfMonth()));
    expectedRetries.add(new QueryRange(YearMonth.of(2013, 4).atDay(1), YearMonth.of(2013, 4).atEndOfMonth()));
    expectedRetries.add(new QueryRange(YearMonth.of(2013, 6).atDay(1), YearMonth.of(2013, 6).atEndOfMonth()));
    expectedRetries.add(new QueryRange(YearMonth.of(2013, 8).atDay(1), YearMonth.of(2013, 8).atEndOfMonth()));

    assertEquals(expectedRetries, retries);

    verify(s3FileUtilities).isFileExists(eq(bucketName), eq("reports/2013/01/earthquake-info-2013-01.pdf"));
    verify(s3FileUtilities).isFileExists(eq(bucketName), eq("reports/2013/02/earthquake-info-2013-02.pdf"));
    verify(s3FileUtilities).isFileExists(eq(bucketName), eq("reports/2013/03/earthquake-info-2013-03.pdf"));
    verify(s3FileUtilities).isFileExists(eq(bucketName), eq("reports/2013/04/earthquake-info-2013-04.pdf"));
    verify(s3FileUtilities).isFileExists(eq(bucketName), eq("reports/2013/05/earthquake-info-2013-05.pdf"));
    verify(s3FileUtilities).isFileExists(eq(bucketName), eq("reports/2013/06/earthquake-info-2013-06.pdf"));
    verify(s3FileUtilities).isFileExists(eq(bucketName), eq("reports/2013/07/earthquake-info-2013-07.pdf"));
    verify(s3FileUtilities).isFileExists(eq(bucketName), eq("reports/2013/08/earthquake-info-2013-08.pdf"));
    verify(s3FileUtilities).isFileExists(eq(bucketName), eq("reports/2013/09/earthquake-info-2013-09.pdf"));
    verify(s3FileUtilities).isFileExists(eq(bucketName), eq("reports/2013/10/earthquake-info-2013-10.pdf"));
    verify(s3FileUtilities).isFileExists(eq(bucketName), eq("reports/2013/11/earthquake-info-2013-11.pdf"));

    verify(infoFileS3Actions).readReportInfoFile(eq(bucketName), eq("reports/2013/01/report-info-2013-01.json.gz"));
    verify(infoFileS3Actions).readReportInfoFile(eq(bucketName), eq("reports/2013/03/report-info-2013-03.json.gz"));
    verify(infoFileS3Actions).readReportInfoFile(eq(bucketName), eq("reports/2013/04/report-info-2013-04.json.gz"));
    verify(infoFileS3Actions).readReportInfoFile(eq(bucketName), eq("reports/2013/06/report-info-2013-06.json.gz"));
    verify(infoFileS3Actions).readReportInfoFile(eq(bucketName), eq("reports/2013/08/report-info-2013-08.json.gz"));
    verify(infoFileS3Actions).readReportInfoFile(eq(bucketName), eq("reports/2013/09/report-info-2013-09.json.gz"));
    verify(infoFileS3Actions).readReportInfoFile(eq(bucketName), eq("reports/2013/10/report-info-2013-10.json.gz"));

    verify(s3FileUtilities).deleteFiles(eq(bucketName), eq("reports/2013/01/"));
    verify(s3FileUtilities).deleteFiles(eq(bucketName), eq("downloads/2013/01/"));
    verify(s3FileUtilities).deleteFiles(eq(bucketName), eq("reports/2013/03/"));
    verify(s3FileUtilities).deleteFiles(eq(bucketName), eq("downloads/2013/03/"));
    verify(s3FileUtilities).deleteFiles(eq(bucketName), eq("reports/2013/04/"));
    verify(s3FileUtilities).deleteFiles(eq(bucketName), eq("downloads/2013/04/"));
    verify(s3FileUtilities).deleteFiles(eq(bucketName), eq("reports/2013/06/"));
    verify(s3FileUtilities).deleteFiles(eq(bucketName), eq("downloads/2013/06/"));
    verify(s3FileUtilities).deleteFiles(eq(bucketName), eq("reports/2013/08/"));
    verify(s3FileUtilities).deleteFiles(eq(bucketName), eq("downloads/2013/08/"));

    verifyNoMoreInteractions(s3FileUtilities, infoFileS3Actions);
  }

  @Test
  void testLimited() throws Exception {

//        //missing reports/2013/01
//        "reports/2013/02/report-info-2013-02.json.gz",
//        "reports/2013/02/earthquake-info-2013-02.pdf", //done
//        //missing reports/2013/03
//        "reports/2013/04/report-info-2013-04.json.gz", //no start time
//        "reports/2013/05/report-info-2013-05.json.gz",
//        "reports/2013/05/earthquake-info-2013-05.pdf", //done
//        "reports/2013/06/earthquake-info-2013-06.pdf",  //no report start time, old start time
//        "reports/2013/07/report-info-2013-07.json.gz",
//        "reports/2013/07/earthquake-info-2013-07.pdf", //done
//        "reports/2013/08/earthquake-info-2013-08.pdf",  //old report start time
//        "reports/2013/09/earthquake-info-2013-09.pdf",  //recent report start time
//        "reports/2013/10/earthquake-info-2013-10.pdf",  //no report start time, recent start time
//        "reports/2013/11/report-info-2013-11.json.gz",
//        "reports/2013/11/earthquake-info-2013-11.pdf", //done
//        "reports/2013/12/report-info-2013-12.json.gz" //current month

    final int quietMinutes = 60;

    String bucketName = "my-bucket";
    InitiatorProperties initiatorProperties = new InitiatorProperties();
    initiatorProperties.setDownloadBucket(bucketName);
    initiatorProperties.setDefaultStartDate("2013-01-01");
    initiatorProperties.setMaxMonthsPerTrigger(3);
    initiatorProperties.setRetryQuietTimeMinutes(quietMinutes);
    LocalDate today = LocalDate.parse("2014-12-05");
    Instant now = today.atStartOfDay(ZoneId.of("UTC")).toInstant();
    InfoFileS3Actions infoFileS3Actions = mock(InfoFileS3Actions.class);
    S3FileUtilities s3FileUtilities = mock(S3FileUtilities.class);

    when(s3FileUtilities.isFileExists(eq(bucketName), eq("reports/2013/01/earthquake-info-2013-01.pdf"))).thenReturn(false);
    when(s3FileUtilities.isFileExists(eq(bucketName), eq("reports/2013/02/earthquake-info-2013-02.pdf"))).thenReturn(true);
    when(s3FileUtilities.isFileExists(eq(bucketName), eq("reports/2013/03/earthquake-info-2013-03.pdf"))).thenReturn(false);
    when(s3FileUtilities.isFileExists(eq(bucketName), eq("reports/2013/04/earthquake-info-2013-04.pdf"))).thenReturn(false);
    when(s3FileUtilities.isFileExists(eq(bucketName), eq("reports/2013/05/earthquake-info-2013-05.pdf"))).thenReturn(true);
    when(s3FileUtilities.isFileExists(eq(bucketName), eq("reports/2013/06/earthquake-info-2013-06.pdf"))).thenReturn(false);
    when(s3FileUtilities.isFileExists(eq(bucketName), eq("reports/2013/07/earthquake-info-2013-07.pdf"))).thenReturn(true);
    when(s3FileUtilities.isFileExists(eq(bucketName), eq("reports/2013/08/earthquake-info-2013-08.pdf"))).thenReturn(false);
    when(s3FileUtilities.isFileExists(eq(bucketName), eq("reports/2013/09/earthquake-info-2013-09.pdf"))).thenReturn(false);
    when(s3FileUtilities.isFileExists(eq(bucketName), eq("reports/2013/10/earthquake-info-2013-10.pdf"))).thenReturn(false);
    when(s3FileUtilities.isFileExists(eq(bucketName), eq("reports/2013/11/earthquake-info-2013-11.pdf"))).thenReturn(true);

    when(infoFileS3Actions.readReportInfoFile(eq(bucketName), eq("reports/2013/01/report-info-2013-01.json.gz"))).thenReturn(Optional.empty());
    when(infoFileS3Actions.readReportInfoFile(eq(bucketName), eq("reports/2013/03/report-info-2013-03.json.gz"))).thenReturn(Optional.empty());
    when(infoFileS3Actions.readReportInfoFile(eq(bucketName), eq("reports/2013/04/report-info-2013-04.json.gz")))
        .thenReturn(Optional.of(ReportInfoFile.Builder.builder().build()));

    ReportRetryProcessor processor = new ReportRetryProcessor(initiatorProperties, () -> today, () -> now, infoFileS3Actions, s3FileUtilities);
    List<QueryRange> retries = processor.prepareFailedReports();
    List<QueryRange> expectedRetries = new ArrayList<>();
    expectedRetries.add(new QueryRange(YearMonth.of(2013, 1).atDay(1), YearMonth.of(2013, 1).atEndOfMonth()));
    expectedRetries.add(new QueryRange(YearMonth.of(2013, 3).atDay(1), YearMonth.of(2013, 3).atEndOfMonth()));
    expectedRetries.add(new QueryRange(YearMonth.of(2013, 4).atDay(1), YearMonth.of(2013, 4).atEndOfMonth()));


    assertEquals(expectedRetries, retries);

    verify(s3FileUtilities).isFileExists(eq(bucketName), eq("reports/2013/01/earthquake-info-2013-01.pdf"));
    verify(s3FileUtilities).isFileExists(eq(bucketName), eq("reports/2013/02/earthquake-info-2013-02.pdf"));
    verify(s3FileUtilities).isFileExists(eq(bucketName), eq("reports/2013/03/earthquake-info-2013-03.pdf"));
    verify(s3FileUtilities).isFileExists(eq(bucketName), eq("reports/2013/04/earthquake-info-2013-04.pdf"));
    verify(s3FileUtilities).isFileExists(eq(bucketName), eq("reports/2013/05/earthquake-info-2013-05.pdf"));
    verify(s3FileUtilities).isFileExists(eq(bucketName), eq("reports/2013/06/earthquake-info-2013-06.pdf"));
    verify(s3FileUtilities).isFileExists(eq(bucketName), eq("reports/2013/07/earthquake-info-2013-07.pdf"));
    verify(s3FileUtilities).isFileExists(eq(bucketName), eq("reports/2013/08/earthquake-info-2013-08.pdf"));
    verify(s3FileUtilities).isFileExists(eq(bucketName), eq("reports/2013/09/earthquake-info-2013-09.pdf"));
    verify(s3FileUtilities).isFileExists(eq(bucketName), eq("reports/2013/10/earthquake-info-2013-10.pdf"));
    verify(s3FileUtilities).isFileExists(eq(bucketName), eq("reports/2013/11/earthquake-info-2013-11.pdf"));

    verify(infoFileS3Actions).readReportInfoFile(eq(bucketName), eq("reports/2013/01/report-info-2013-01.json.gz"));
    verify(infoFileS3Actions).readReportInfoFile(eq(bucketName), eq("reports/2013/03/report-info-2013-03.json.gz"));
    verify(infoFileS3Actions).readReportInfoFile(eq(bucketName), eq("reports/2013/04/report-info-2013-04.json.gz"));


    verify(s3FileUtilities).deleteFiles(eq(bucketName), eq("reports/2013/01/"));
    verify(s3FileUtilities).deleteFiles(eq(bucketName), eq("downloads/2013/01/"));
    verify(s3FileUtilities).deleteFiles(eq(bucketName), eq("reports/2013/03/"));
    verify(s3FileUtilities).deleteFiles(eq(bucketName), eq("downloads/2013/03/"));
    verify(s3FileUtilities).deleteFiles(eq(bucketName), eq("reports/2013/04/"));
    verify(s3FileUtilities).deleteFiles(eq(bucketName), eq("downloads/2013/04/"));

    verifyNoMoreInteractions(s3FileUtilities, infoFileS3Actions);
  }
}