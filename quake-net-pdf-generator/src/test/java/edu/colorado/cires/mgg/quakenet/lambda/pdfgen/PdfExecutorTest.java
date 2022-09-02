package edu.colorado.cires.mgg.quakenet.lambda.pdfgen;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.colorado.cires.mgg.quakenet.message.ReportGenerateMessage;
import edu.colorado.cires.mgg.quakenet.model.QnEvent;
import gov.noaa.ncei.xmlns.cdidata.Cdidata;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import javax.xml.bind.JAXBContext;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.quakeml.xmlns.quakeml._1.Quakeml;

class PdfExecutorTest {

  @Test
  void test() throws Exception {

    List<String> s3ObjectList = Arrays.asList(
        "downloads/2020/01/2020-01-01/ak0201o9tt2/event-details-2020-01-01-ak0201o9tt2.json.gz",
        "downloads/2020/01/2020-01-01/ak0201o9tt2/event-details-2020-01-01-ak0201o9tt2.xml.gz",
        "downloads/2020/01/2020-01-01/pr2020001119/event-details-2020-01-01-pr2020001119.json.gz",
        "downloads/2020/01/2020-01-01/pr2020001119/event-details-2020-01-01-pr2020001119.xml.gz",
        "downloads/2020/01/2020-01-01/us70006t2a/event-cdi-2020-01-01-us70006t2a.json.gz",
        "downloads/2020/01/2020-01-01/us70006t2a/event-cdi-2020-01-01-us70006t2a.xml.gz",
        "downloads/2020/01/2020-01-01/us70006t2a/event-details-2020-01-01-us70006t2a.xml.gz",
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
        if(!Files.exists(path)) {
          return Optional.empty();
        }
        try(InputStream in = new GZIPInputStream(Files.newInputStream(path))) {
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
        if(!Files.exists(path)) {
          return Optional.empty();
        }
        try(InputStream in = new GZIPInputStream(Files.newInputStream(path))) {
          return Optional.of((Cdidata) JAXBContext.newInstance(Cdidata.class)
              .createUnmarshaller()
              .unmarshal(in));
        }
      }
    });

    BucketIteratorFactory bucketIteratorFactory = mock(BucketIteratorFactory.class);
    when(bucketIteratorFactory.create(eq(bucketName), eq("downloads/2020/01/"))).thenReturn(s3ObjectList.iterator());

    DataParser dataParser = new DataParser(properties, dataOperations, bucketIteratorFactory);
    PdfExecutor executor = new PdfExecutor(properties, dataParser, dataOperations);

    ReportGenerateMessage message = ReportGenerateMessage.Builder.builder().withYear(2020).withMonth(1).build();

    executor.execute(message);

    List<QnEvent> events = new ArrayList<>();

    verify(dataOperations, times(1)).writePdf(
        eq(bucketName),
        eq("reports/2020/01/earthquake-info-2020-01.pdf"),
        any(), //TODO
        eq(message)
    );
  }

}