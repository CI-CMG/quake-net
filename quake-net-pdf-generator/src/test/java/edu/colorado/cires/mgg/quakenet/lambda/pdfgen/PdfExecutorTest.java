package edu.colorado.cires.mgg.quakenet.lambda.pdfgen;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.colorado.cires.mgg.quakenet.message.ReportGenerateMessage;
import edu.colorado.cires.mgg.quakenet.message.ReportInfoFile;
import edu.colorado.cires.mgg.quakenet.model.QnEvent;
import edu.colorado.cires.mgg.quakenet.s3.util.InfoFileS3Actions;
import gov.noaa.ncei.xmlns.cdidata.Cdidata;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import javax.xml.bind.JAXBContext;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.quakeml.xmlns.quakeml._1.Quakeml;

class PdfExecutorTest {


  @Test
  void test() throws Exception {

    List<String> s3ObjectList = Arrays.asList(
        "downloads/2020/01/2020-01-01/ak0201o9tt2/event-details-2020-01-01-ak0201o9tt2.json.gz",
        "downloads/2020/01/2020-01-01/ak0201o9tt2/event-details-2020-01-01-ak0201o9tt2.xml.gz",
        "downloads/2020/01/2020-01-01/ak0201o9tt3/event-details-2020-01-01-ak0201o9tt3.json.gz",
        "downloads/2020/01/2020-01-01/ak0201o9tt3/event-details-2020-01-01-ak0201o9tt3.xml.gz",
        "downloads/2020/01/2020-01-01/ak0201o9tt4/event-details-2020-01-01-ak0201o9tt4.json.gz",
        "downloads/2020/01/2020-01-01/ak0201o9tt4/event-details-2020-01-01-ak0201o9tt4.xml.gz",
        "downloads/2020/01/2020-01-01/pr2020001119/event-details-2020-01-01-pr2020001119.json.gz",
        "downloads/2020/01/2020-01-01/pr2020001119/event-details-2020-01-01-pr2020001119.xml.gz",
        "downloads/2020/01/2020-01-01/pr2020001120/event-details-2020-01-01-pr2020001120.json.gz",
        "downloads/2020/01/2020-01-01/pr2020001120/event-details-2020-01-01-pr2020001120.xml.gz",
        "downloads/2020/01/2020-01-01/pr2020001121/event-details-2020-01-01-pr2020001121.json.gz",
        "downloads/2020/01/2020-01-01/pr2020001121/event-details-2020-01-01-pr2020001121.xml.gz",
        "downloads/2020/01/2020-01-01/pr2020001122/event-details-2020-01-01-pr2020001122.json.gz",
        "downloads/2020/01/2020-01-01/pr2020001122/event-details-2020-01-01-pr2020001122.xml.gz",
        "downloads/2020/01/2020-01-01/pr2020001123/event-details-2020-01-01-pr2020001123.json.gz",
        "downloads/2020/01/2020-01-01/pr2020001123/event-details-2020-01-01-pr2020001123.xml.gz",
        "downloads/2020/01/2020-01-01/pr2020001124/event-details-2020-01-01-pr2020001124.json.gz",
        "downloads/2020/01/2020-01-01/pr2020001124/event-details-2020-01-01-pr2020001124.xml.gz",
        "downloads/2020/01/2020-01-01/pr2020001125/event-details-2020-01-01-pr2020001125.json.gz",
        "downloads/2020/01/2020-01-01/pr2020001125/event-details-2020-01-01-pr2020001125.xml.gz",
        "downloads/2020/01/2020-01-01/pr2020001126/event-details-2020-01-01-pr2020001126.json.gz",
        "downloads/2020/01/2020-01-01/pr2020001126/event-details-2020-01-01-pr2020001126.xml.gz",
        "downloads/2020/01/2020-01-01/pr2020001127/event-details-2020-01-01-pr2020001127.json.gz",
        "downloads/2020/01/2020-01-01/pr2020001127/event-details-2020-01-01-pr2020001127.xml.gz",
        "downloads/2020/01/2020-01-01/pr2020001128/event-details-2020-01-01-pr2020001128.json.gz",
        "downloads/2020/01/2020-01-01/pr2020001128/event-details-2020-01-01-pr2020001128.xml.gz",
        "downloads/2020/01/2020-01-01/us70006t2a/event-cdi-2020-01-01-us70006t2a.json.gz",
        "downloads/2020/01/2020-01-01/us70006t2a/event-cdi-2020-01-01-us70006t2a.xml.gz",
        "downloads/2020/01/2020-01-01/us70006t2a/event-details-2020-01-01-us70006t2a.xml.gz",
        "downloads/2020/01/2020-01-01/us70006t2b/event-cdi-2020-01-01-us70006t2b.json.gz",
        "downloads/2020/01/2020-01-01/us70006t2b/event-cdi-2020-01-01-us70006t2b.xml.gz",
        "downloads/2020/01/2020-01-01/us70006t2b/event-details-2020-01-01-us70006t2b.xml.gz",
        "downloads/2020/01/2020-01-01/us70006t2c/event-cdi-2020-01-01-us70006t2c.json.gz",
        "downloads/2020/01/2020-01-01/us70006t2c/event-cdi-2020-01-01-us70006t2c.xml.gz",
        "downloads/2020/01/2020-01-01/us70006t2c/event-details-2020-01-01-us70006t2c.xml.gz",
        "downloads/2020/01/2020-01-01/us70006t2d/event-cdi-2020-01-01-us70006t2d.json.gz",
        "downloads/2020/01/2020-01-01/us70006t2d/event-cdi-2020-01-01-us70006t2d.xml.gz",
        "downloads/2020/01/2020-01-01/us70006t2d/event-details-2020-01-01-us70006t2d.xml.gz",
        "downloads/2020/01/2020-01-01/usgs-info-2020-01-01.json.gz"
    );

    String bucketName = "my-bucket";

    PdfGenProperties properties = new PdfGenProperties();
    properties.setBucketName(bucketName);

    DataOperations dataOperations = mock(DataOperations.class);

    when(dataOperations.isReportExists(eq(bucketName), eq("reports/2020/01/earthquake-info-2020-01.pdf"))).thenReturn(false);
    when(dataOperations.readQuakeMl(eq(bucketName), any())).thenAnswer(new Answer<Optional<Quakeml>>() {
      @Override
      public Optional<Quakeml> answer(InvocationOnMock invocationOnMock) throws Throwable {
        Path path = Paths.get("src/test/resources/" + invocationOnMock.getArgument(1, String.class));
        if (!Files.exists(path)) {
          return Optional.empty();
        }
        try (InputStream in = new GZIPInputStream(Files.newInputStream(path))) {
          return Optional.of((Quakeml) JAXBContext.newInstance(Quakeml.class)
              .createUnmarshaller()
              .unmarshal(in));
        }
      }
    });
    when(dataOperations.readCdi(eq(bucketName), any())).thenAnswer(new Answer<Optional<Cdidata>>() {
      @Override
      public Optional<Cdidata> answer(InvocationOnMock invocationOnMock) throws Throwable {
        Path path = Paths.get("src/test/resources/" + invocationOnMock.getArgument(1, String.class));
        if (!Files.exists(path)) {
          return Optional.empty();
        }
        try (InputStream in = new GZIPInputStream(Files.newInputStream(path))) {
          return Optional.of((Cdidata) JAXBContext.newInstance(Cdidata.class)
              .createUnmarshaller()
              .unmarshal(in));
        }
      }
    });

    BucketIteratorFactory bucketIteratorFactory = mock(BucketIteratorFactory.class);
    when(bucketIteratorFactory.create(eq(bucketName), eq("downloads/2020/01/"))).thenReturn(s3ObjectList.iterator());

    ReportInfoFile reportInfoFile = ReportInfoFile.Builder.builder().withStartTime(Instant.now().minusMillis(10000)).withStartReportGeneration(Instant.now().minusMillis(5000)).build();

    Instant now = Instant.now();

    InfoFileS3Actions infoFileS3Actions = mock(InfoFileS3Actions.class);
    when(infoFileS3Actions.readReportInfoFile(eq(bucketName), eq("reports/2020/01/report-info-2020-01.json.gz"))).thenReturn(Optional.of(reportInfoFile));
    DataParser dataParser = new DataParser(properties, dataOperations, bucketIteratorFactory);
    PdfExecutor executor = new PdfExecutor(properties, dataParser, dataOperations, infoFileS3Actions, () -> now);

    ReportGenerateMessage message = ReportGenerateMessage.Builder.builder().withYear(2020).withMonth(1).build();

    executor.execute(message);

    List<QnEvent> events = new ArrayList<>();

    ArgumentCaptor<byte[]> pdfCaptor = ArgumentCaptor.forClass(byte[].class);

    verify(dataOperations, times(1)).writePdf(
        eq(bucketName),
        eq("reports/2020/01/earthquake-info-2020-01.pdf"),
        pdfCaptor.capture()
    );

    Path file = Paths.get("target/earthquake-info-2020-01.pdf");
    Files.createDirectories(file.getParent());
    try (OutputStream outputStream = Files.newOutputStream(file)) {
      IOUtils.write(pdfCaptor.getValue(), outputStream);
    }


    verify(infoFileS3Actions, times(1)).saveReportInfoFile(
        eq(bucketName),
        eq("reports/2020/01/report-info-2020-01.json.gz"),
        eq(ReportInfoFile.Builder.builder(reportInfoFile).withEndReportGeneration(now).build()));

  }


}