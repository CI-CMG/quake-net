package edu.colorado.cires.mgg.quakenet.lambda.reportinit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.colorado.cires.mgg.quakenet.message.EventDetailGrabberMessage;
import edu.colorado.cires.mgg.quakenet.message.InfoFile;
import edu.colorado.cires.mgg.quakenet.message.ReportGenerateMessage;
import edu.colorado.cires.mgg.quakenet.s3.util.InfoFileS3Actions;
import edu.colorado.cires.mgg.quakenet.s3.util.S3FileUtilities;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

class ReportTriggerTest {

  @Test
  void testComplete() throws Exception {
    String bucketName = "my-bucket";
    String topicArn = "topicArn";
    ReportInitiatorProperties properties = new ReportInitiatorProperties();
    properties.setBucketName(bucketName);
    properties.setTopicArn(topicArn);

    MessageSender messageSender = mock(MessageSender.class);
    BucketIteratorFactory bucketIteratorFactory = mock(BucketIteratorFactory.class);
    InfoFileS3Actions infoFileS3Actions = mock(InfoFileS3Actions.class);
    S3FileUtilities s3FileUtilities =  mock(S3FileUtilities.class);

    List<String> s3ObjectList = Arrays.asList(
        "downloads/2012/05/2012-05-01/usgs-info-2012-05-01.json.gz",
        "downloads/2012/05/2012-05-01/id101/event-details-2012-05-01-id101.xml.gz",
        "downloads/2012/05/2012-05-01/id201/event-details-2012-05-01-id201.xml.gz",
        "downloads/2012/05/2012-05-01/id301/event-details-2012-05-01-id301.xml.gz",

        "downloads/2012/05/2012-05-02/usgs-info-2012-05-02.json.gz",

        "downloads/2012/05/2012-05-03/usgs-info-2012-05-03.json.gz",
        "downloads/2012/05/2012-05-03/id103/event-details-2012-05-03-id103.xml.gz",
        "downloads/2012/05/2012-05-03/id203/event-details-2012-05-03-id203.xml.gz",
        "downloads/2012/05/2012-05-03/id303/event-details-2012-05-03-id303.xml.gz",

        "downloads/2012/05/2012-05-04/usgs-info-2012-05-04.json.gz",
        "downloads/2012/05/2012-05-04/id104/event-details-2012-05-04-id104.xml.gz",
        "downloads/2012/05/2012-05-04/id204/event-details-2012-05-04-id204.xml.gz",
        "downloads/2012/05/2012-05-04/id304/event-details-2012-05-04-id304.xml.gz",

        "downloads/2012/05/2012-05-05/usgs-info-2012-05-05.json.gz",
        "downloads/2012/05/2012-05-05/id105/event-details-2012-05-05-id105.xml.gz",
        "downloads/2012/05/2012-05-05/id205/event-details-2012-05-05-id205.xml.gz",
        "downloads/2012/05/2012-05-05/id305/event-details-2012-05-05-id305.xml.gz",

        "downloads/2012/05/2012-05-06/usgs-info-2012-05-06.json.gz",
        "downloads/2012/05/2012-05-06/id106/event-details-2012-05-06-id106.xml.gz",
        "downloads/2012/05/2012-05-06/id206/event-details-2012-05-06-id206.xml.gz",
        "downloads/2012/05/2012-05-06/id306/event-details-2012-05-06-id306.xml.gz",

        "downloads/2012/05/2012-05-07/usgs-info-2012-05-07.json.gz",
        "downloads/2012/05/2012-05-07/id107/event-details-2012-05-07-id107.xml.gz",
        "downloads/2012/05/2012-05-07/id207/event-details-2012-05-07-id207.xml.gz",
        "downloads/2012/05/2012-05-07/id307/event-details-2012-05-07-id307.xml.gz",

        "downloads/2012/05/2012-05-08/usgs-info-2012-05-08.json.gz",
        "downloads/2012/05/2012-05-08/id108/event-details-2012-05-08-id108.xml.gz",
        "downloads/2012/05/2012-05-08/id208/event-details-2012-05-08-id208.xml.gz",
        "downloads/2012/05/2012-05-08/id308/event-details-2012-05-08-id308.xml.gz",

        "downloads/2012/05/2012-05-09/usgs-info-2012-05-09.json.gz",
        "downloads/2012/05/2012-05-09/id109/event-details-2012-05-09-id109.xml.gz",
        "downloads/2012/05/2012-05-09/id209/event-details-2012-05-09-id209.xml.gz",
        "downloads/2012/05/2012-05-09/id309/event-details-2012-05-09-id309.xml.gz",

        "downloads/2012/05/2012-05-10/usgs-info-2012-05-10.json.gz",
        "downloads/2012/05/2012-05-10/id110/event-details-2012-05-10-id110.xml.gz",
        "downloads/2012/05/2012-05-10/id210/event-details-2012-05-10-id210.xml.gz",
        "downloads/2012/05/2012-05-10/id310/event-details-2012-05-10-id310.xml.gz",

        "downloads/2012/05/2012-05-11/usgs-info-2012-05-11.json.gz",
        "downloads/2012/05/2012-05-11/id111/event-details-2012-05-11-id111.xml.gz",
        "downloads/2012/05/2012-05-11/id211/event-details-2012-05-11-id211.xml.gz",
        "downloads/2012/05/2012-05-11/id311/event-details-2012-05-11-id311.xml.gz",

        "downloads/2012/05/2012-05-12/usgs-info-2012-05-12.json.gz",
        "downloads/2012/05/2012-05-12/id112/event-details-2012-05-12-id112.xml.gz",
        "downloads/2012/05/2012-05-12/id212/event-details-2012-05-12-id212.xml.gz",
        "downloads/2012/05/2012-05-12/id312/event-details-2012-05-12-id312.xml.gz",

        "downloads/2012/05/2012-05-13/usgs-info-2012-05-13.json.gz",
        "downloads/2012/05/2012-05-13/id113/event-details-2012-05-13-id113.xml.gz",
        "downloads/2012/05/2012-05-13/id213/event-details-2012-05-13-id213.xml.gz",
        "downloads/2012/05/2012-05-13/id313/event-details-2012-05-13-id313.xml.gz",

        "downloads/2012/05/2012-05-14/usgs-info-2012-05-14.json.gz",
        "downloads/2012/05/2012-05-14/id114/event-details-2012-05-14-id114.xml.gz",
        "downloads/2012/05/2012-05-14/id214/event-details-2012-05-14-id214.xml.gz",
        "downloads/2012/05/2012-05-14/id314/event-details-2012-05-14-id314.xml.gz",

        "downloads/2012/05/2012-05-15/usgs-info-2012-05-15.json.gz",
        "downloads/2012/05/2012-05-15/id115/event-details-2012-05-15-id115.xml.gz",
        "downloads/2012/05/2012-05-15/id215/event-details-2012-05-15-id215.xml.gz",
        "downloads/2012/05/2012-05-15/id315/event-details-2012-05-15-id315.xml.gz",

        "downloads/2012/05/2012-05-16/usgs-info-2012-05-16.json.gz",
        "downloads/2012/05/2012-05-16/id116/event-details-2012-05-16-id116.xml.gz",
        "downloads/2012/05/2012-05-16/id216/event-details-2012-05-16-id216.xml.gz",
        "downloads/2012/05/2012-05-16/id316/event-details-2012-05-16-id316.xml.gz",

        "downloads/2012/05/2012-05-17/usgs-info-2012-05-17.json.gz",
        "downloads/2012/05/2012-05-17/id117/event-details-2012-05-17-id117.xml.gz",
        "downloads/2012/05/2012-05-17/id217/event-details-2012-05-17-id217.xml.gz",
        "downloads/2012/05/2012-05-17/id317/event-details-2012-05-17-id317.xml.gz",

        "downloads/2012/05/2012-05-18/usgs-info-2012-05-18.json.gz",
        "downloads/2012/05/2012-05-18/id118/event-details-2012-05-18-id118.xml.gz",
        "downloads/2012/05/2012-05-18/id218/event-details-2012-05-18-id218.xml.gz",
        "downloads/2012/05/2012-05-18/id318/event-details-2012-05-18-id318.xml.gz",

        "downloads/2012/05/2012-05-19/usgs-info-2012-05-19.json.gz",
        "downloads/2012/05/2012-05-19/id119/event-details-2012-05-19-id119.xml.gz",
        "downloads/2012/05/2012-05-19/id219/event-details-2012-05-19-id219.xml.gz",
        "downloads/2012/05/2012-05-19/id319/event-details-2012-05-19-id319.xml.gz",

        "downloads/2012/05/2012-05-20/usgs-info-2012-05-20.json.gz",
        "downloads/2012/05/2012-05-20/id120/event-details-2012-05-20-id120.xml.gz",
        "downloads/2012/05/2012-05-20/id220/event-details-2012-05-20-id220.xml.gz",
        "downloads/2012/05/2012-05-20/id320/event-details-2012-05-20-id320.xml.gz",

        "downloads/2012/05/2012-05-21/usgs-info-2012-05-21.json.gz",
        "downloads/2012/05/2012-05-21/id121/event-details-2012-05-21-id121.xml.gz",
        "downloads/2012/05/2012-05-21/id221/event-details-2012-05-21-id221.xml.gz",
        "downloads/2012/05/2012-05-21/id321/event-details-2012-05-21-id321.xml.gz",

        "downloads/2012/05/2012-05-22/usgs-info-2012-05-22.json.gz",
        "downloads/2012/05/2012-05-22/id122/event-details-2012-05-22-id122.xml.gz",
        "downloads/2012/05/2012-05-22/id222/event-details-2012-05-22-id222.xml.gz",
        "downloads/2012/05/2012-05-22/id322/event-details-2012-05-22-id322.xml.gz",

        "downloads/2012/05/2012-05-23/usgs-info-2012-05-23.json.gz",
        "downloads/2012/05/2012-05-23/id123/event-details-2012-05-23-id123.xml.gz",
        "downloads/2012/05/2012-05-23/id223/event-details-2012-05-23-id223.xml.gz",
        "downloads/2012/05/2012-05-23/id323/event-details-2012-05-23-id323.xml.gz",

        "downloads/2012/05/2012-05-24/usgs-info-2012-05-24.json.gz",
        "downloads/2012/05/2012-05-24/id124/event-details-2012-05-24-id124.xml.gz",
        "downloads/2012/05/2012-05-24/id224/event-details-2012-05-24-id224.xml.gz",
        "downloads/2012/05/2012-05-24/id324/event-details-2012-05-24-id324.xml.gz",

        "downloads/2012/05/2012-05-25/usgs-info-2012-05-25.json.gz",
        "downloads/2012/05/2012-05-25/id125/event-details-2012-05-25-id125.xml.gz",
        "downloads/2012/05/2012-05-25/id225/event-details-2012-05-25-id225.xml.gz",
        "downloads/2012/05/2012-05-25/id325/event-details-2012-05-25-id325.xml.gz",

        "downloads/2012/05/2012-05-26/usgs-info-2012-05-26.json.gz",
        "downloads/2012/05/2012-05-26/id126/event-details-2012-05-26-id126.xml.gz",
        "downloads/2012/05/2012-05-26/id226/event-details-2012-05-26-id226.xml.gz",
        "downloads/2012/05/2012-05-26/id326/event-details-2012-05-26-id326.xml.gz",

        "downloads/2012/05/2012-05-27/usgs-info-2012-05-27.json.gz",
        "downloads/2012/05/2012-05-27/id127/event-details-2012-05-27-id127.xml.gz",
        "downloads/2012/05/2012-05-27/id227/event-details-2012-05-27-id227.xml.gz",
        "downloads/2012/05/2012-05-27/id327/event-details-2012-05-27-id327.xml.gz",

        "downloads/2012/05/2012-05-28/usgs-info-2012-05-28.json.gz",
        "downloads/2012/05/2012-05-28/id128/event-details-2012-05-28-id128.xml.gz",
        "downloads/2012/05/2012-05-28/id228/event-details-2012-05-28-id228.xml.gz",
        "downloads/2012/05/2012-05-28/id328/event-details-2012-05-28-id328.xml.gz",

        "downloads/2012/05/2012-05-29/usgs-info-2012-05-29.json.gz",
        "downloads/2012/05/2012-05-29/id129/event-details-2012-05-29-id129.xml.gz",
        "downloads/2012/05/2012-05-29/id229/event-details-2012-05-29-id229.xml.gz",
        "downloads/2012/05/2012-05-29/id329/event-details-2012-05-29-id329.xml.gz",

        "downloads/2012/05/2012-05-30/usgs-info-2012-05-30.json.gz",
        "downloads/2012/05/2012-05-30/id130/event-details-2012-05-30-id130.xml.gz",
        "downloads/2012/05/2012-05-30/id230/event-details-2012-05-30-id230.xml.gz",
        "downloads/2012/05/2012-05-30/id330/event-details-2012-05-30-id330.xml.gz",

        "downloads/2012/05/2012-05-31/usgs-info-2012-05-31.json.gz",
        "downloads/2012/05/2012-05-31/id131/event-details-2012-05-31-id131.xml.gz",
        "downloads/2012/05/2012-05-31/id231/event-details-2012-05-31-id231.xml.gz",
        "downloads/2012/05/2012-05-31/id331/event-details-2012-05-31-id331.xml.gz"

    );

    when(bucketIteratorFactory.create(eq(bucketName), eq("downloads/2012/05/"))).thenReturn(s3ObjectList.iterator());
    when(infoFileS3Actions.isFileExists(eq(bucketName), eq("reports/2012/05/report-info-2012-05.json.gz"))).thenReturn(false);
    when(infoFileS3Actions.readInfoFile(eq(bucketName), any())).thenAnswer(new Answer<Optional<InfoFile>>() {
      @Override
      public Optional<InfoFile> answer(InvocationOnMock invocationOnMock) throws Throwable {
        String key = invocationOnMock.getArgument(1, String.class);
        String year = key.split("/")[1];
        String month = key.split("/")[2];
        String date = key.split("/")[3];
        String day = date.split("-")[2];
        assertEquals(String.format("downloads/%s/%s/%s/usgs-info-%s.json.gz", year, month, date, date), key);
        if (day.equals("02")) {
          return Optional.of(InfoFile.Builder.builder().withDate(LocalDate.parse(date)).build());
        }
        return Optional.of(InfoFile.Builder.builder()
            .withDate(LocalDate.parse(date))
            .withEventIds(Arrays.asList(
                String.format("id1%s", day),
                String.format("id2%s", day),
                String.format("id3%s", day)
            )).build());
      }
    });

    ReportTrigger reportTrigger = new ReportTrigger(properties, messageSender, bucketIteratorFactory, infoFileS3Actions, s3FileUtilities);
    EventDetailGrabberMessage message = EventDetailGrabberMessage.Builder.builder()
        .withEventId("id130")
        .withDate("2012-05-30")
        .build();
    reportTrigger.triggerReports(message);

    ReportGenerateMessage reportGenerateMessage = ReportGenerateMessage.Builder.builder()
        .withYear(2012)
        .withMonth(5)
        .build();

    verify(messageSender, times(1)).sendMessage(eq(topicArn), eq(reportGenerateMessage));
    verify(s3FileUtilities, times(1)).saveFile(eq(bucketName), eq("reports/2012/05/report-info-2012-05.json.gz"), any());

  }


  @Test
  void testCompleteWithError() throws Exception {
    String bucketName = "my-bucket";
    String topicArn = "topicArn";
    ReportInitiatorProperties properties = new ReportInitiatorProperties();
    properties.setBucketName(bucketName);
    properties.setTopicArn(topicArn);

    MessageSender messageSender = mock(MessageSender.class);
    BucketIteratorFactory bucketIteratorFactory = mock(BucketIteratorFactory.class);
    InfoFileS3Actions infoFileS3Actions = mock(InfoFileS3Actions.class);
    S3FileUtilities s3FileUtilities =  mock(S3FileUtilities.class);

    List<String> s3ObjectList = Arrays.asList(
        "downloads/2012/05/2012-05-01/usgs-info-2012-05-01.json.gz",
        "downloads/2012/05/2012-05-01/id101/event-details-2012-05-01-id101.xml.gz",
        "downloads/2012/05/2012-05-01/id201/event-details-2012-05-01-id201.xml.gz",
        "downloads/2012/05/2012-05-01/id301/event-details-2012-05-01-id301.xml.gz",

        "downloads/2012/05/2012-05-02/usgs-info-2012-05-02.json.gz",

        "downloads/2012/05/2012-05-03/usgs-info-2012-05-03.json.gz",
        "downloads/2012/05/2012-05-03/id103/event-details-2012-05-03-id103.xml.gz",
        "downloads/2012/05/2012-05-03/id203/event-details-2012-05-03-id203.xml.gz",
        "downloads/2012/05/2012-05-03/id303/event-details-2012-05-03-id303.xml.gz",

        "downloads/2012/05/2012-05-04/usgs-info-2012-05-04.json.gz",
        "downloads/2012/05/2012-05-04/id104/event-details-2012-05-04-id104.xml.gz",
        "downloads/2012/05/2012-05-04/id204/event-details-2012-05-04-id204.xml.gz",
        "downloads/2012/05/2012-05-04/id304/event-details-2012-05-04-id304.xml.gz",

        "downloads/2012/05/2012-05-05/usgs-info-2012-05-05.json.gz",
        "downloads/2012/05/2012-05-05/id105/event-details-2012-05-05-id105.xml.gz",
        "downloads/2012/05/2012-05-05/id205/event-details-2012-05-05-id205.xml.gz",
        "downloads/2012/05/2012-05-05/id305/event-details-2012-05-05-id305.xml.gz",

        "downloads/2012/05/2012-05-06/usgs-info-2012-05-06.json.gz",
        "downloads/2012/05/2012-05-06/id106/event-details-2012-05-06-id106.xml.gz",
        "downloads/2012/05/2012-05-06/id206/event-details-2012-05-06-id206.xml.gz",
        "downloads/2012/05/2012-05-06/id306/event-details-2012-05-06-id306.xml.gz",

        "downloads/2012/05/2012-05-07/usgs-info-2012-05-07.json.gz",
        "downloads/2012/05/2012-05-07/id107/event-details-2012-05-07-id107.xml.gz",
        "downloads/2012/05/2012-05-07/id207/event-details-2012-05-07-id207.xml.gz",
        "downloads/2012/05/2012-05-07/id307/event-details-2012-05-07-id307.xml.gz",

        "downloads/2012/05/2012-05-08/usgs-info-2012-05-08.json.gz",
        "downloads/2012/05/2012-05-08/id108/event-details-2012-05-08-id108.xml.gz",
        "downloads/2012/05/2012-05-08/id208/event-details-2012-05-08-id208.xml.gz",
        "downloads/2012/05/2012-05-08/id308/event-details-2012-05-08-id308.xml.gz",

        "downloads/2012/05/2012-05-09/usgs-info-2012-05-09.json.gz",
        "downloads/2012/05/2012-05-09/id109/event-error-2012-05-09-id109.json.gz",
        "downloads/2012/05/2012-05-09/id209/event-details-2012-05-09-id209.xml.gz",
        "downloads/2012/05/2012-05-09/id309/event-details-2012-05-09-id309.xml.gz",

        "downloads/2012/05/2012-05-10/usgs-info-2012-05-10.json.gz",
        "downloads/2012/05/2012-05-10/id110/event-details-2012-05-10-id110.xml.gz",
        "downloads/2012/05/2012-05-10/id210/event-details-2012-05-10-id210.xml.gz",
        "downloads/2012/05/2012-05-10/id310/event-details-2012-05-10-id310.xml.gz",

        "downloads/2012/05/2012-05-11/usgs-info-2012-05-11.json.gz",
        "downloads/2012/05/2012-05-11/id111/event-details-2012-05-11-id111.xml.gz",
        "downloads/2012/05/2012-05-11/id211/event-details-2012-05-11-id211.xml.gz",
        "downloads/2012/05/2012-05-11/id311/event-details-2012-05-11-id311.xml.gz",

        "downloads/2012/05/2012-05-12/usgs-info-2012-05-12.json.gz",
        "downloads/2012/05/2012-05-12/id112/event-details-2012-05-12-id112.xml.gz",
        "downloads/2012/05/2012-05-12/id212/event-details-2012-05-12-id212.xml.gz",
        "downloads/2012/05/2012-05-12/id312/event-details-2012-05-12-id312.xml.gz",

        "downloads/2012/05/2012-05-13/usgs-info-2012-05-13.json.gz",
        "downloads/2012/05/2012-05-13/id113/event-details-2012-05-13-id113.xml.gz",
        "downloads/2012/05/2012-05-13/id213/event-details-2012-05-13-id213.xml.gz",
        "downloads/2012/05/2012-05-13/id313/event-details-2012-05-13-id313.xml.gz",

        "downloads/2012/05/2012-05-14/usgs-info-2012-05-14.json.gz",
        "downloads/2012/05/2012-05-14/id114/event-details-2012-05-14-id114.xml.gz",
        "downloads/2012/05/2012-05-14/id214/event-details-2012-05-14-id214.xml.gz",
        "downloads/2012/05/2012-05-14/id314/event-details-2012-05-14-id314.xml.gz",

        "downloads/2012/05/2012-05-15/usgs-info-2012-05-15.json.gz",
        "downloads/2012/05/2012-05-15/id115/event-details-2012-05-15-id115.xml.gz",
        "downloads/2012/05/2012-05-15/id215/event-details-2012-05-15-id215.xml.gz",
        "downloads/2012/05/2012-05-15/id315/event-details-2012-05-15-id315.xml.gz",

        "downloads/2012/05/2012-05-16/usgs-info-2012-05-16.json.gz",
        "downloads/2012/05/2012-05-16/id116/event-details-2012-05-16-id116.xml.gz",
        "downloads/2012/05/2012-05-16/id216/event-details-2012-05-16-id216.xml.gz",
        "downloads/2012/05/2012-05-16/id316/event-details-2012-05-16-id316.xml.gz",

        "downloads/2012/05/2012-05-17/usgs-info-2012-05-17.json.gz",
        "downloads/2012/05/2012-05-17/id117/event-details-2012-05-17-id117.xml.gz",
        "downloads/2012/05/2012-05-17/id217/event-details-2012-05-17-id217.xml.gz",
        "downloads/2012/05/2012-05-17/id317/event-details-2012-05-17-id317.xml.gz",

        "downloads/2012/05/2012-05-18/usgs-info-2012-05-18.json.gz",
        "downloads/2012/05/2012-05-18/id118/event-details-2012-05-18-id118.xml.gz",
        "downloads/2012/05/2012-05-18/id218/event-details-2012-05-18-id218.xml.gz",
        "downloads/2012/05/2012-05-18/id318/event-details-2012-05-18-id318.xml.gz",

        "downloads/2012/05/2012-05-19/usgs-info-2012-05-19.json.gz",
        "downloads/2012/05/2012-05-19/id119/event-details-2012-05-19-id119.xml.gz",
        "downloads/2012/05/2012-05-19/id219/event-details-2012-05-19-id219.xml.gz",
        "downloads/2012/05/2012-05-19/id319/event-details-2012-05-19-id319.xml.gz",

        "downloads/2012/05/2012-05-20/usgs-info-2012-05-20.json.gz",
        "downloads/2012/05/2012-05-20/id120/event-details-2012-05-20-id120.xml.gz",
        "downloads/2012/05/2012-05-20/id220/event-details-2012-05-20-id220.xml.gz",
        "downloads/2012/05/2012-05-20/id320/event-details-2012-05-20-id320.xml.gz",

        "downloads/2012/05/2012-05-21/usgs-info-2012-05-21.json.gz",
        "downloads/2012/05/2012-05-21/id121/event-details-2012-05-21-id121.xml.gz",
        "downloads/2012/05/2012-05-21/id221/event-details-2012-05-21-id221.xml.gz",
        "downloads/2012/05/2012-05-21/id321/event-details-2012-05-21-id321.xml.gz",

        "downloads/2012/05/2012-05-22/usgs-info-2012-05-22.json.gz",
        "downloads/2012/05/2012-05-22/id122/event-details-2012-05-22-id122.xml.gz",
        "downloads/2012/05/2012-05-22/id222/event-details-2012-05-22-id222.xml.gz",
        "downloads/2012/05/2012-05-22/id322/event-details-2012-05-22-id322.xml.gz",

        "downloads/2012/05/2012-05-23/usgs-info-2012-05-23.json.gz",
        "downloads/2012/05/2012-05-23/id123/event-details-2012-05-23-id123.xml.gz",
        "downloads/2012/05/2012-05-23/id223/event-details-2012-05-23-id223.xml.gz",
        "downloads/2012/05/2012-05-23/id323/event-details-2012-05-23-id323.xml.gz",

        "downloads/2012/05/2012-05-24/usgs-info-2012-05-24.json.gz",
        "downloads/2012/05/2012-05-24/id124/event-details-2012-05-24-id124.xml.gz",
        "downloads/2012/05/2012-05-24/id224/event-details-2012-05-24-id224.xml.gz",
        "downloads/2012/05/2012-05-24/id324/event-details-2012-05-24-id324.xml.gz",

        "downloads/2012/05/2012-05-25/usgs-info-2012-05-25.json.gz",
        "downloads/2012/05/2012-05-25/id125/event-details-2012-05-25-id125.xml.gz",
        "downloads/2012/05/2012-05-25/id225/event-details-2012-05-25-id225.xml.gz",
        "downloads/2012/05/2012-05-25/id325/event-details-2012-05-25-id325.xml.gz",

        "downloads/2012/05/2012-05-26/usgs-info-2012-05-26.json.gz",
        "downloads/2012/05/2012-05-26/id126/event-details-2012-05-26-id126.xml.gz",
        "downloads/2012/05/2012-05-26/id226/event-details-2012-05-26-id226.xml.gz",
        "downloads/2012/05/2012-05-26/id326/event-details-2012-05-26-id326.xml.gz",

        "downloads/2012/05/2012-05-27/usgs-info-2012-05-27.json.gz",
        "downloads/2012/05/2012-05-27/id127/event-details-2012-05-27-id127.xml.gz",
        "downloads/2012/05/2012-05-27/id227/event-details-2012-05-27-id227.xml.gz",
        "downloads/2012/05/2012-05-27/id327/event-details-2012-05-27-id327.xml.gz",

        "downloads/2012/05/2012-05-28/usgs-info-2012-05-28.json.gz",
        "downloads/2012/05/2012-05-28/id128/event-details-2012-05-28-id128.xml.gz",
        "downloads/2012/05/2012-05-28/id228/event-details-2012-05-28-id228.xml.gz",
        "downloads/2012/05/2012-05-28/id328/event-details-2012-05-28-id328.xml.gz",

        "downloads/2012/05/2012-05-29/usgs-info-2012-05-29.json.gz",
        "downloads/2012/05/2012-05-29/id129/event-details-2012-05-29-id129.xml.gz",
        "downloads/2012/05/2012-05-29/id229/event-details-2012-05-29-id229.xml.gz",
        "downloads/2012/05/2012-05-29/id329/event-details-2012-05-29-id329.xml.gz",

        "downloads/2012/05/2012-05-30/usgs-info-2012-05-30.json.gz",
        "downloads/2012/05/2012-05-30/id130/event-details-2012-05-30-id130.xml.gz",
        "downloads/2012/05/2012-05-30/id230/event-details-2012-05-30-id230.xml.gz",
        "downloads/2012/05/2012-05-30/id330/event-details-2012-05-30-id330.xml.gz",

        "downloads/2012/05/2012-05-31/usgs-info-2012-05-31.json.gz",
        "downloads/2012/05/2012-05-31/id131/event-details-2012-05-31-id131.xml.gz",
        "downloads/2012/05/2012-05-31/id231/event-details-2012-05-31-id231.xml.gz",
        "downloads/2012/05/2012-05-31/id331/event-details-2012-05-31-id331.xml.gz"

    );

    when(bucketIteratorFactory.create(eq(bucketName), eq("downloads/2012/05/"))).thenReturn(s3ObjectList.iterator());
    when(infoFileS3Actions.isFileExists(eq(bucketName), eq("reports/2012/05/report-info-2012-05.json.gz"))).thenReturn(false);
    when(infoFileS3Actions.readInfoFile(eq(bucketName), any())).thenAnswer(new Answer<Optional<InfoFile>>() {
      @Override
      public Optional<InfoFile> answer(InvocationOnMock invocationOnMock) throws Throwable {
        String key = invocationOnMock.getArgument(1, String.class);
        String year = key.split("/")[1];
        String month = key.split("/")[2];
        String date = key.split("/")[3];
        String day = date.split("-")[2];
        assertEquals(String.format("downloads/%s/%s/%s/usgs-info-%s.json.gz", year, month, date, date), key);
        if (day.equals("02")) {
          return Optional.of(InfoFile.Builder.builder().withDate(LocalDate.parse(date)).build());
        }
        return Optional.of(InfoFile.Builder.builder()
            .withDate(LocalDate.parse(date))
            .withEventIds(Arrays.asList(
                String.format("id1%s", day),
                String.format("id2%s", day),
                String.format("id3%s", day)
            )).build());
      }
    });

    ReportTrigger reportTrigger = new ReportTrigger(properties, messageSender, bucketIteratorFactory, infoFileS3Actions, s3FileUtilities);
    EventDetailGrabberMessage message = EventDetailGrabberMessage.Builder.builder()
        .withEventId("id130")
        .withDate("2012-05-30")
        .build();
    reportTrigger.triggerReports(message);

    ReportGenerateMessage reportGenerateMessage = ReportGenerateMessage.Builder.builder()
        .withYear(2012)
        .withMonth(5)
        .build();

    verify(messageSender, times(1)).sendMessage(eq(topicArn), eq(reportGenerateMessage));
    verify(s3FileUtilities, times(1)).saveFile(eq(bucketName), eq("reports/2012/05/report-info-2012-05.json.gz"), any());

  }

  @Test
  void testNotComplete() throws Exception {
    String bucketName = "my-bucket";
    String topicArn = "topicArn";
    ReportInitiatorProperties properties = new ReportInitiatorProperties();
    properties.setBucketName(bucketName);
    properties.setTopicArn(topicArn);

    MessageSender messageSender = mock(MessageSender.class);
    BucketIteratorFactory bucketIteratorFactory = mock(BucketIteratorFactory.class);
    InfoFileS3Actions infoFileS3Actions = mock(InfoFileS3Actions.class);
    S3FileUtilities s3FileUtilities =  mock(S3FileUtilities.class);

    List<String> s3ObjectList = Arrays.asList(
        "downloads/2012/05/2012-05-01/usgs-info-2012-05-01.json.gz",
        "downloads/2012/05/2012-05-01/id101/event-details-2012-05-01-id101.xml.gz",
        "downloads/2012/05/2012-05-01/id201/event-details-2012-05-01-id201.xml.gz",
        "downloads/2012/05/2012-05-01/id301/event-details-2012-05-01-id301.xml.gz",

        "downloads/2012/05/2012-05-02/usgs-info-2012-05-02.json.gz",

        "downloads/2012/05/2012-05-03/usgs-info-2012-05-03.json.gz",
        "downloads/2012/05/2012-05-03/id103/event-details-2012-05-03-id103.xml.gz",
        "downloads/2012/05/2012-05-03/id203/event-details-2012-05-03-id203.xml.gz",
        "downloads/2012/05/2012-05-03/id303/event-details-2012-05-03-id303.xml.gz",

        "downloads/2012/05/2012-05-04/usgs-info-2012-05-04.json.gz",
        "downloads/2012/05/2012-05-04/id104/event-details-2012-05-04-id104.xml.gz",
        "downloads/2012/05/2012-05-04/id204/event-details-2012-05-04-id204.xml.gz",
        "downloads/2012/05/2012-05-04/id304/event-details-2012-05-04-id304.xml.gz",

        "downloads/2012/05/2012-05-05/usgs-info-2012-05-05.json.gz",
        "downloads/2012/05/2012-05-05/id105/event-details-2012-05-05-id105.xml.gz",
        "downloads/2012/05/2012-05-05/id205/event-details-2012-05-05-id205.xml.gz",
        "downloads/2012/05/2012-05-05/id305/event-details-2012-05-05-id305.xml.gz",

        "downloads/2012/05/2012-05-06/usgs-info-2012-05-06.json.gz",
        "downloads/2012/05/2012-05-06/id106/event-details-2012-05-06-id106.xml.gz",
        "downloads/2012/05/2012-05-06/id206/event-details-2012-05-06-id206.xml.gz",
        "downloads/2012/05/2012-05-06/id306/event-details-2012-05-06-id306.xml.gz",

        "downloads/2012/05/2012-05-07/usgs-info-2012-05-07.json.gz",
        "downloads/2012/05/2012-05-07/id107/event-details-2012-05-07-id107.xml.gz",
        "downloads/2012/05/2012-05-07/id207/event-details-2012-05-07-id207.xml.gz",
        "downloads/2012/05/2012-05-07/id307/event-details-2012-05-07-id307.xml.gz",

        "downloads/2012/05/2012-05-08/usgs-info-2012-05-08.json.gz",
        "downloads/2012/05/2012-05-08/id108/event-details-2012-05-08-id108.xml.gz",
        "downloads/2012/05/2012-05-08/id208/event-details-2012-05-08-id208.xml.gz",
        "downloads/2012/05/2012-05-08/id308/event-details-2012-05-08-id308.xml.gz",

        "downloads/2012/05/2012-05-09/usgs-info-2012-05-09.json.gz",
        "downloads/2012/05/2012-05-09/id109/event-details-2012-05-09-id109.xml.gz",
        "downloads/2012/05/2012-05-09/id209/event-details-2012-05-09-id209.xml.gz",
        "downloads/2012/05/2012-05-09/id309/event-details-2012-05-09-id309.xml.gz",

        "downloads/2012/05/2012-05-10/usgs-info-2012-05-10.json.gz",
        "downloads/2012/05/2012-05-10/id110/event-details-2012-05-10-id110.xml.gz",
        "downloads/2012/05/2012-05-10/id210/event-details-2012-05-10-id210.xml.gz",
        "downloads/2012/05/2012-05-10/id310/event-details-2012-05-10-id310.xml.gz",

        "downloads/2012/05/2012-05-11/usgs-info-2012-05-11.json.gz",
        "downloads/2012/05/2012-05-11/id111/event-details-2012-05-11-id111.xml.gz",
        "downloads/2012/05/2012-05-11/id211/event-details-2012-05-11-id211.xml.gz",
        "downloads/2012/05/2012-05-11/id311/event-details-2012-05-11-id311.xml.gz",

        "downloads/2012/05/2012-05-12/usgs-info-2012-05-12.json.gz",
        "downloads/2012/05/2012-05-12/id112/event-details-2012-05-12-id112.xml.gz",
        "downloads/2012/05/2012-05-12/id212/event-details-2012-05-12-id212.xml.gz",
        "downloads/2012/05/2012-05-12/id312/event-details-2012-05-12-id312.xml.gz",

        "downloads/2012/05/2012-05-13/usgs-info-2012-05-13.json.gz",
        "downloads/2012/05/2012-05-13/id113/event-details-2012-05-13-id113.xml.gz",
        "downloads/2012/05/2012-05-13/id213/event-details-2012-05-13-id213.xml.gz",
        "downloads/2012/05/2012-05-13/id313/event-details-2012-05-13-id313.xml.gz",

        "downloads/2012/05/2012-05-14/usgs-info-2012-05-14.json.gz",
        "downloads/2012/05/2012-05-14/id114/event-details-2012-05-14-id114.xml.gz",
        "downloads/2012/05/2012-05-14/id214/event-details-2012-05-14-id214.xml.gz",
        "downloads/2012/05/2012-05-14/id314/event-details-2012-05-14-id314.xml.gz",

        "downloads/2012/05/2012-05-15/usgs-info-2012-05-15.json.gz",
        "downloads/2012/05/2012-05-15/id115/event-details-2012-05-15-id115.xml.gz",
        "downloads/2012/05/2012-05-15/id215/event-details-2012-05-15-id215.xml.gz",
        "downloads/2012/05/2012-05-15/id315/event-details-2012-05-15-id315.xml.gz",

        "downloads/2012/05/2012-05-16/usgs-info-2012-05-16.json.gz",
        "downloads/2012/05/2012-05-16/id116/event-details-2012-05-16-id116.xml.gz",
        "downloads/2012/05/2012-05-16/id216/event-details-2012-05-16-id216.xml.gz",
        "downloads/2012/05/2012-05-16/id316/event-details-2012-05-16-id316.xml.gz",

        "downloads/2012/05/2012-05-17/usgs-info-2012-05-17.json.gz",
        "downloads/2012/05/2012-05-17/id117/event-details-2012-05-17-id117.xml.gz",
        "downloads/2012/05/2012-05-17/id217/event-details-2012-05-17-id217.xml.gz",
        "downloads/2012/05/2012-05-17/id317/event-details-2012-05-17-id317.xml.gz",

        "downloads/2012/05/2012-05-18/usgs-info-2012-05-18.json.gz",
        "downloads/2012/05/2012-05-18/id118/event-details-2012-05-18-id118.xml.gz",
        "downloads/2012/05/2012-05-18/id218/event-details-2012-05-18-id218.xml.gz",

        "downloads/2012/05/2012-05-19/usgs-info-2012-05-19.json.gz",
        "downloads/2012/05/2012-05-19/id119/event-details-2012-05-19-id119.xml.gz",
        "downloads/2012/05/2012-05-19/id219/event-details-2012-05-19-id219.xml.gz",
        "downloads/2012/05/2012-05-19/id319/event-details-2012-05-19-id319.xml.gz",

        "downloads/2012/05/2012-05-20/usgs-info-2012-05-20.json.gz",
        "downloads/2012/05/2012-05-20/id120/event-details-2012-05-20-id120.xml.gz",
        "downloads/2012/05/2012-05-20/id220/event-details-2012-05-20-id220.xml.gz",
        "downloads/2012/05/2012-05-20/id320/event-details-2012-05-20-id320.xml.gz",

        "downloads/2012/05/2012-05-21/usgs-info-2012-05-21.json.gz",
        "downloads/2012/05/2012-05-21/id121/event-details-2012-05-21-id121.xml.gz",
        "downloads/2012/05/2012-05-21/id221/event-details-2012-05-21-id221.xml.gz",
        "downloads/2012/05/2012-05-21/id321/event-details-2012-05-21-id321.xml.gz",

        "downloads/2012/05/2012-05-22/usgs-info-2012-05-22.json.gz",
        "downloads/2012/05/2012-05-22/id122/event-details-2012-05-22-id122.xml.gz",
        "downloads/2012/05/2012-05-22/id222/event-details-2012-05-22-id222.xml.gz",
        "downloads/2012/05/2012-05-22/id322/event-details-2012-05-22-id322.xml.gz",

        "downloads/2012/05/2012-05-23/usgs-info-2012-05-23.json.gz",
        "downloads/2012/05/2012-05-23/id123/event-details-2012-05-23-id123.xml.gz",
        "downloads/2012/05/2012-05-23/id223/event-details-2012-05-23-id223.xml.gz",
        "downloads/2012/05/2012-05-23/id323/event-details-2012-05-23-id323.xml.gz",

        "downloads/2012/05/2012-05-24/usgs-info-2012-05-24.json.gz",
        "downloads/2012/05/2012-05-24/id124/event-details-2012-05-24-id124.xml.gz",
        "downloads/2012/05/2012-05-24/id224/event-details-2012-05-24-id224.xml.gz",
        "downloads/2012/05/2012-05-24/id324/event-details-2012-05-24-id324.xml.gz",

        "downloads/2012/05/2012-05-25/usgs-info-2012-05-25.json.gz",
        "downloads/2012/05/2012-05-25/id125/event-details-2012-05-25-id125.xml.gz",
        "downloads/2012/05/2012-05-25/id225/event-details-2012-05-25-id225.xml.gz",
        "downloads/2012/05/2012-05-25/id325/event-details-2012-05-25-id325.xml.gz",

        "downloads/2012/05/2012-05-26/usgs-info-2012-05-26.json.gz",
        "downloads/2012/05/2012-05-26/id126/event-details-2012-05-26-id126.xml.gz",
        "downloads/2012/05/2012-05-26/id226/event-details-2012-05-26-id226.xml.gz",
        "downloads/2012/05/2012-05-26/id326/event-details-2012-05-26-id326.xml.gz",

        "downloads/2012/05/2012-05-27/usgs-info-2012-05-27.json.gz",
        "downloads/2012/05/2012-05-27/id127/event-details-2012-05-27-id127.xml.gz",
        "downloads/2012/05/2012-05-27/id227/event-details-2012-05-27-id227.xml.gz",
        "downloads/2012/05/2012-05-27/id327/event-details-2012-05-27-id327.xml.gz",

        "downloads/2012/05/2012-05-28/usgs-info-2012-05-28.json.gz",
        "downloads/2012/05/2012-05-28/id128/event-details-2012-05-28-id128.xml.gz",
        "downloads/2012/05/2012-05-28/id228/event-details-2012-05-28-id228.xml.gz",
        "downloads/2012/05/2012-05-28/id328/event-details-2012-05-28-id328.xml.gz",

        "downloads/2012/05/2012-05-29/usgs-info-2012-05-29.json.gz",
        "downloads/2012/05/2012-05-29/id129/event-details-2012-05-29-id129.xml.gz",
        "downloads/2012/05/2012-05-29/id229/event-details-2012-05-29-id229.xml.gz",
        "downloads/2012/05/2012-05-29/id329/event-details-2012-05-29-id329.xml.gz",

        "downloads/2012/05/2012-05-30/usgs-info-2012-05-30.json.gz",
        "downloads/2012/05/2012-05-30/id130/event-details-2012-05-30-id130.xml.gz",
        "downloads/2012/05/2012-05-30/id230/event-details-2012-05-30-id230.xml.gz",
        "downloads/2012/05/2012-05-30/id330/event-details-2012-05-30-id330.xml.gz",

        "downloads/2012/05/2012-05-31/usgs-info-2012-05-31.json.gz",
        "downloads/2012/05/2012-05-31/id131/event-details-2012-05-31-id131.xml.gz",
        "downloads/2012/05/2012-05-31/id231/event-details-2012-05-31-id231.xml.gz",
        "downloads/2012/05/2012-05-31/id331/event-details-2012-05-31-id331.xml.gz"

    );

    when(bucketIteratorFactory.create(eq(bucketName), eq("downloads/2012/05/"))).thenReturn(s3ObjectList.iterator());
    when(infoFileS3Actions.isFileExists(eq(bucketName), eq("reports/2012/05/report-info-2012-05.json.gz"))).thenReturn(false);
    when(infoFileS3Actions.readInfoFile(eq(bucketName), any())).thenAnswer(new Answer<Optional<InfoFile>>() {
      @Override
      public Optional<InfoFile> answer(InvocationOnMock invocationOnMock) throws Throwable {
        String key = invocationOnMock.getArgument(1, String.class);
        String date = key.split("/")[3];
        String day = date.split("-")[2];
        if (day.equals("02")) {
          return Optional.of(InfoFile.Builder.builder().withDate(LocalDate.parse(date)).build());
        }
        return Optional.of(InfoFile.Builder.builder()
            .withDate(LocalDate.parse(date))
            .withEventIds(Arrays.asList(
                String.format("id1%s", day),
                String.format("id2%s", day),
                String.format("id3%s", day)
            )).build());
      }
    });

    ReportTrigger reportTrigger = new ReportTrigger(properties, messageSender, bucketIteratorFactory, infoFileS3Actions, s3FileUtilities);
    EventDetailGrabberMessage message = EventDetailGrabberMessage.Builder.builder()
        .withEventId("id130")
        .withDate("2012-05-30")
        .build();
    reportTrigger.triggerReports(message);


    verify(messageSender, times(0)).sendMessage(any(), any());
    verify(s3FileUtilities, times(0)).saveFile(eq(bucketName), any(), any());

  }

  @Test
  void testReportExists() throws Exception {
    String bucketName = "my-bucket";
    String topicArn = "topicArn";
    ReportInitiatorProperties properties = new ReportInitiatorProperties();
    properties.setBucketName(bucketName);
    properties.setTopicArn(topicArn);

    MessageSender messageSender = mock(MessageSender.class);
    BucketIteratorFactory bucketIteratorFactory = mock(BucketIteratorFactory.class);
    InfoFileS3Actions infoFileS3Actions = mock(InfoFileS3Actions.class);
    S3FileUtilities s3FileUtilities =  mock(S3FileUtilities.class);

    List<String> s3ObjectList = Arrays.asList(
        "downloads/2012/05/2012-05-01/usgs-info-2012-05-01.json.gz",
        "downloads/2012/05/2012-05-01/id101/event-details-2012-05-01-id101.xml.gz",
        "downloads/2012/05/2012-05-01/id201/event-details-2012-05-01-id201.xml.gz",
        "downloads/2012/05/2012-05-01/id301/event-details-2012-05-01-id301.xml.gz",

        "downloads/2012/05/2012-05-02/usgs-info-2012-05-02.json.gz",

        "downloads/2012/05/2012-05-03/usgs-info-2012-05-03.json.gz",
        "downloads/2012/05/2012-05-03/id103/event-details-2012-05-03-id103.xml.gz",
        "downloads/2012/05/2012-05-03/id203/event-details-2012-05-03-id203.xml.gz",
        "downloads/2012/05/2012-05-03/id303/event-details-2012-05-03-id303.xml.gz",

        "downloads/2012/05/2012-05-04/usgs-info-2012-05-04.json.gz",
        "downloads/2012/05/2012-05-04/id104/event-details-2012-05-04-id104.xml.gz",
        "downloads/2012/05/2012-05-04/id204/event-details-2012-05-04-id204.xml.gz",
        "downloads/2012/05/2012-05-04/id304/event-details-2012-05-04-id304.xml.gz",

        "downloads/2012/05/2012-05-05/usgs-info-2012-05-05.json.gz",
        "downloads/2012/05/2012-05-05/id105/event-details-2012-05-05-id105.xml.gz",
        "downloads/2012/05/2012-05-05/id205/event-details-2012-05-05-id205.xml.gz",
        "downloads/2012/05/2012-05-05/id305/event-details-2012-05-05-id305.xml.gz",

        "downloads/2012/05/2012-05-06/usgs-info-2012-05-06.json.gz",
        "downloads/2012/05/2012-05-06/id106/event-details-2012-05-06-id106.xml.gz",
        "downloads/2012/05/2012-05-06/id206/event-details-2012-05-06-id206.xml.gz",
        "downloads/2012/05/2012-05-06/id306/event-details-2012-05-06-id306.xml.gz",

        "downloads/2012/05/2012-05-07/usgs-info-2012-05-07.json.gz",
        "downloads/2012/05/2012-05-07/id107/event-details-2012-05-07-id107.xml.gz",
        "downloads/2012/05/2012-05-07/id207/event-details-2012-05-07-id207.xml.gz",
        "downloads/2012/05/2012-05-07/id307/event-details-2012-05-07-id307.xml.gz",

        "downloads/2012/05/2012-05-08/usgs-info-2012-05-08.json.gz",
        "downloads/2012/05/2012-05-08/id108/event-details-2012-05-08-id108.xml.gz",
        "downloads/2012/05/2012-05-08/id208/event-details-2012-05-08-id208.xml.gz",
        "downloads/2012/05/2012-05-08/id308/event-details-2012-05-08-id308.xml.gz",

        "downloads/2012/05/2012-05-09/usgs-info-2012-05-09.json.gz",
        "downloads/2012/05/2012-05-09/id109/event-details-2012-05-09-id109.xml.gz",
        "downloads/2012/05/2012-05-09/id209/event-details-2012-05-09-id209.xml.gz",
        "downloads/2012/05/2012-05-09/id309/event-details-2012-05-09-id309.xml.gz",

        "downloads/2012/05/2012-05-10/usgs-info-2012-05-10.json.gz",
        "downloads/2012/05/2012-05-10/id110/event-details-2012-05-10-id110.xml.gz",
        "downloads/2012/05/2012-05-10/id210/event-details-2012-05-10-id210.xml.gz",
        "downloads/2012/05/2012-05-10/id310/event-details-2012-05-10-id310.xml.gz",

        "downloads/2012/05/2012-05-11/usgs-info-2012-05-11.json.gz",
        "downloads/2012/05/2012-05-11/id111/event-details-2012-05-11-id111.xml.gz",
        "downloads/2012/05/2012-05-11/id211/event-details-2012-05-11-id211.xml.gz",
        "downloads/2012/05/2012-05-11/id311/event-details-2012-05-11-id311.xml.gz",

        "downloads/2012/05/2012-05-12/usgs-info-2012-05-12.json.gz",
        "downloads/2012/05/2012-05-12/id112/event-details-2012-05-12-id112.xml.gz",
        "downloads/2012/05/2012-05-12/id212/event-details-2012-05-12-id212.xml.gz",
        "downloads/2012/05/2012-05-12/id312/event-details-2012-05-12-id312.xml.gz",

        "downloads/2012/05/2012-05-13/usgs-info-2012-05-13.json.gz",
        "downloads/2012/05/2012-05-13/id113/event-details-2012-05-13-id113.xml.gz",
        "downloads/2012/05/2012-05-13/id213/event-details-2012-05-13-id213.xml.gz",
        "downloads/2012/05/2012-05-13/id313/event-details-2012-05-13-id313.xml.gz",

        "downloads/2012/05/2012-05-14/usgs-info-2012-05-14.json.gz",
        "downloads/2012/05/2012-05-14/id114/event-details-2012-05-14-id114.xml.gz",
        "downloads/2012/05/2012-05-14/id214/event-details-2012-05-14-id214.xml.gz",
        "downloads/2012/05/2012-05-14/id314/event-details-2012-05-14-id314.xml.gz",

        "downloads/2012/05/2012-05-15/usgs-info-2012-05-15.json.gz",
        "downloads/2012/05/2012-05-15/id115/event-details-2012-05-15-id115.xml.gz",
        "downloads/2012/05/2012-05-15/id215/event-details-2012-05-15-id215.xml.gz",
        "downloads/2012/05/2012-05-15/id315/event-details-2012-05-15-id315.xml.gz",

        "downloads/2012/05/2012-05-16/usgs-info-2012-05-16.json.gz",
        "downloads/2012/05/2012-05-16/id116/event-details-2012-05-16-id116.xml.gz",
        "downloads/2012/05/2012-05-16/id216/event-details-2012-05-16-id216.xml.gz",
        "downloads/2012/05/2012-05-16/id316/event-details-2012-05-16-id316.xml.gz",

        "downloads/2012/05/2012-05-17/usgs-info-2012-05-17.json.gz",
        "downloads/2012/05/2012-05-17/id117/event-details-2012-05-17-id117.xml.gz",
        "downloads/2012/05/2012-05-17/id217/event-details-2012-05-17-id217.xml.gz",
        "downloads/2012/05/2012-05-17/id317/event-details-2012-05-17-id317.xml.gz",

        "downloads/2012/05/2012-05-18/usgs-info-2012-05-18.json.gz",
        "downloads/2012/05/2012-05-18/id118/event-details-2012-05-18-id118.xml.gz",
        "downloads/2012/05/2012-05-18/id218/event-details-2012-05-18-id218.xml.gz",
        "downloads/2012/05/2012-05-18/id318/event-details-2012-05-18-id318.xml.gz",

        "downloads/2012/05/2012-05-19/usgs-info-2012-05-19.json.gz",
        "downloads/2012/05/2012-05-19/id119/event-details-2012-05-19-id119.xml.gz",
        "downloads/2012/05/2012-05-19/id219/event-details-2012-05-19-id219.xml.gz",
        "downloads/2012/05/2012-05-19/id319/event-details-2012-05-19-id319.xml.gz",

        "downloads/2012/05/2012-05-20/usgs-info-2012-05-20.json.gz",
        "downloads/2012/05/2012-05-20/id120/event-details-2012-05-20-id120.xml.gz",
        "downloads/2012/05/2012-05-20/id220/event-details-2012-05-20-id220.xml.gz",
        "downloads/2012/05/2012-05-20/id320/event-details-2012-05-20-id320.xml.gz",

        "downloads/2012/05/2012-05-21/usgs-info-2012-05-21.json.gz",
        "downloads/2012/05/2012-05-21/id121/event-details-2012-05-21-id121.xml.gz",
        "downloads/2012/05/2012-05-21/id221/event-details-2012-05-21-id221.xml.gz",
        "downloads/2012/05/2012-05-21/id321/event-details-2012-05-21-id321.xml.gz",

        "downloads/2012/05/2012-05-22/usgs-info-2012-05-22.json.gz",
        "downloads/2012/05/2012-05-22/id122/event-details-2012-05-22-id122.xml.gz",
        "downloads/2012/05/2012-05-22/id222/event-details-2012-05-22-id222.xml.gz",
        "downloads/2012/05/2012-05-22/id322/event-details-2012-05-22-id322.xml.gz",

        "downloads/2012/05/2012-05-23/usgs-info-2012-05-23.json.gz",
        "downloads/2012/05/2012-05-23/id123/event-details-2012-05-23-id123.xml.gz",
        "downloads/2012/05/2012-05-23/id223/event-details-2012-05-23-id223.xml.gz",
        "downloads/2012/05/2012-05-23/id323/event-details-2012-05-23-id323.xml.gz",

        "downloads/2012/05/2012-05-24/usgs-info-2012-05-24.json.gz",
        "downloads/2012/05/2012-05-24/id124/event-details-2012-05-24-id124.xml.gz",
        "downloads/2012/05/2012-05-24/id224/event-details-2012-05-24-id224.xml.gz",
        "downloads/2012/05/2012-05-24/id324/event-details-2012-05-24-id324.xml.gz",

        "downloads/2012/05/2012-05-25/usgs-info-2012-05-25.json.gz",
        "downloads/2012/05/2012-05-25/id125/event-details-2012-05-25-id125.xml.gz",
        "downloads/2012/05/2012-05-25/id225/event-details-2012-05-25-id225.xml.gz",
        "downloads/2012/05/2012-05-25/id325/event-details-2012-05-25-id325.xml.gz",

        "downloads/2012/05/2012-05-26/usgs-info-2012-05-26.json.gz",
        "downloads/2012/05/2012-05-26/id126/event-details-2012-05-26-id126.xml.gz",
        "downloads/2012/05/2012-05-26/id226/event-details-2012-05-26-id226.xml.gz",
        "downloads/2012/05/2012-05-26/id326/event-details-2012-05-26-id326.xml.gz",

        "downloads/2012/05/2012-05-27/usgs-info-2012-05-27.json.gz",
        "downloads/2012/05/2012-05-27/id127/event-details-2012-05-27-id127.xml.gz",
        "downloads/2012/05/2012-05-27/id227/event-details-2012-05-27-id227.xml.gz",
        "downloads/2012/05/2012-05-27/id327/event-details-2012-05-27-id327.xml.gz",

        "downloads/2012/05/2012-05-28/usgs-info-2012-05-28.json.gz",
        "downloads/2012/05/2012-05-28/id128/event-details-2012-05-28-id128.xml.gz",
        "downloads/2012/05/2012-05-28/id228/event-details-2012-05-28-id228.xml.gz",
        "downloads/2012/05/2012-05-28/id328/event-details-2012-05-28-id328.xml.gz",

        "downloads/2012/05/2012-05-29/usgs-info-2012-05-29.json.gz",
        "downloads/2012/05/2012-05-29/id129/event-details-2012-05-29-id129.xml.gz",
        "downloads/2012/05/2012-05-29/id229/event-details-2012-05-29-id229.xml.gz",
        "downloads/2012/05/2012-05-29/id329/event-details-2012-05-29-id329.xml.gz",

        "downloads/2012/05/2012-05-30/usgs-info-2012-05-30.json.gz",
        "downloads/2012/05/2012-05-30/id130/event-details-2012-05-30-id130.xml.gz",
        "downloads/2012/05/2012-05-30/id230/event-details-2012-05-30-id230.xml.gz",
        "downloads/2012/05/2012-05-30/id330/event-details-2012-05-30-id330.xml.gz",

        "downloads/2012/05/2012-05-31/usgs-info-2012-05-31.json.gz",
        "downloads/2012/05/2012-05-31/id131/event-details-2012-05-31-id131.xml.gz",
        "downloads/2012/05/2012-05-31/id231/event-details-2012-05-31-id231.xml.gz",
        "downloads/2012/05/2012-05-31/id331/event-details-2012-05-31-id331.xml.gz"

    );

    when(bucketIteratorFactory.create(eq(bucketName), eq("downloads/2012/05/"))).thenReturn(s3ObjectList.iterator());
    when(infoFileS3Actions.isFileExists(eq(bucketName), eq("reports/2012/05/report-info-2012-05.json.gz"))).thenReturn(true);
    when(infoFileS3Actions.readInfoFile(eq(bucketName), any())).thenAnswer(new Answer<Optional<InfoFile>>() {
      @Override
      public Optional<InfoFile> answer(InvocationOnMock invocationOnMock) throws Throwable {
        String key = invocationOnMock.getArgument(1, String.class);
        String date = key.split("/")[3];
        String day = date.split("-")[2];
        if (day.equals("02")) {
          return Optional.of(InfoFile.Builder.builder().withDate(LocalDate.parse(date)).build());
        }
        return Optional.of(InfoFile.Builder.builder()
            .withDate(LocalDate.parse(date))
            .withEventIds(Arrays.asList(
                String.format("id1%s", day),
                String.format("id2%s", day),
                String.format("id3%s", day)
            )).build());
      }
    });

    ReportTrigger reportTrigger = new ReportTrigger(properties, messageSender, bucketIteratorFactory, infoFileS3Actions, s3FileUtilities);
    EventDetailGrabberMessage message = EventDetailGrabberMessage.Builder.builder()
        .withEventId("id130")
        .withDate("2012-05-30")
        .build();
    reportTrigger.triggerReports(message);

    verify(messageSender, times(0)).sendMessage(any(), any());
    verify(s3FileUtilities, times(0)).saveFile(eq(bucketName), any(), any());

  }
}