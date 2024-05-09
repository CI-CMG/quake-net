package edu.colorado.cires.mgg.quakenet.lambda.pdfgen;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.colorado.cires.mgg.quakenet.geojson.GeoJson;
import edu.colorado.cires.mgg.quakenet.message.InfoFile;
import edu.colorado.cires.mgg.quakenet.message.ReportGenerateMessage;
import edu.colorado.cires.mgg.quakenet.message.ReportInfoFile;
import edu.colorado.cires.mgg.quakenet.model.QnEvent;
import edu.colorado.cires.mgg.quakenet.s3.util.InfoFileS3Actions;
import edu.colorado.cires.mgg.quakenet.util.ObjectMapperCreator;
import gov.noaa.ncei.xmlns.cdidata.Cdidata;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import javax.xml.bind.JAXBContext;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.quakeml.xmlns.quakeml._1.Quakeml;

class PdfExecutorTest {
  ObjectMapper objectMapper = ObjectMapperCreator.create();

  public List<String> listFilesUsingFileWalk(String dir, int depth) throws IOException {
    String base = "src/test/resources";
    String downloads = base+"/downloads";
    List<Path> files;
    try (Stream<Path> stream = Files.walk(Paths.get(downloads))){
      files = stream.filter(file -> !Files.isDirectory(file))
          .collect(Collectors.toList());
//      return stream
//          .filter(file -> !Files.isDirectory(file))
//          .map(Path::toString)
//          .collect(Collectors.toSet());
    }
    List<String> results = new ArrayList<>();
    Path basePath = Paths.get(base);
    for (Path file:files){
      results.add(basePath.relativize(file).toString());
    }
    return results;
  }

  @Test
  void test() throws Exception {
    List<String> s3ObjectList = listFilesUsingFileWalk("src/test/resources/downloads", 3);

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

    InfoFileS3Actions infoFileS3Actions = mock(InfoFileS3Actions.class);
    when(infoFileS3Actions.readInfoFile(eq(bucketName), any())).thenAnswer(new Answer<Optional<InfoFile>>() {
      @Override
      public Optional<InfoFile> answer(InvocationOnMock invocationOnMock) throws Throwable {
        Path path = Paths.get("src/test/resources/" + invocationOnMock.getArgument(1, String.class));
        if (!Files.exists(path)) {
          return Optional.empty();
        }
        try (InputStream in = new GZIPInputStream(Files.newInputStream(path))) {
          try {
            return Optional.of(objectMapper.readValue(in, InfoFile.class));
          } catch (IOException e) {
            throw new IllegalStateException("Unable to parse infoFile", e);
          }
        }
      }
    });
    when(dataOperations.readJson(eq(bucketName), any())).thenAnswer(new Answer<Optional<GeoJson>>() {
      @Override
      public Optional<GeoJson> answer(InvocationOnMock invocationOnMock) throws Throwable {
        Path path = Paths.get("src/test/resources/" + invocationOnMock.getArgument(1, String.class));
        if (!Files.exists(path)) {
          return Optional.empty();
        }
        try (InputStream in = new GZIPInputStream(Files.newInputStream(path))) {
          try {
            return Optional.of(objectMapper.readValue(in, GeoJson.class));
          } catch (IOException e) {
            throw new IllegalStateException("Unable to parse geojson", e);
          }
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

    when(infoFileS3Actions.readReportInfoFile(eq(bucketName), eq("reports/2020/01/report-info-2020-01.json.gz"))).thenReturn(Optional.of(reportInfoFile));
    DataParser dataParser = new DataParser(properties, dataOperations, bucketIteratorFactory, infoFileS3Actions);
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