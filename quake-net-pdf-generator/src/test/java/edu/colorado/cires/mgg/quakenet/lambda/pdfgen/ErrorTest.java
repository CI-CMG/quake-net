package edu.colorado.cires.mgg.quakenet.lambda.pdfgen;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.colorado.cires.mgg.quakenet.geojson.GeoJson;
import edu.colorado.cires.mgg.quakenet.message.InfoFile;
import edu.colorado.cires.mgg.quakenet.message.ReportGenerateMessage;
import edu.colorado.cires.mgg.quakenet.message.ReportInfoFile;
import edu.colorado.cires.mgg.quakenet.s3.util.InfoFileS3Actions;
import edu.colorado.cires.mgg.quakenet.util.ObjectMapperCreator;
import gov.noaa.ncei.xmlns.cdidata.Cdidata;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.quakeml.xmlns.quakeml._1.Quakeml;

public class ErrorTest {

  @Test
  public void testError() {
    ObjectMapper objectMapper = ObjectMapperCreator.create();


    Path rootDir = Paths.get("src", "test", "resources", "npe");


    PdfGenProperties properties = new PdfGenProperties();
    properties.setBucketName("test");

    DataOperations dataOperations = new DataOperations(){

      @Override
      public boolean isReportExists(String bucketName, String key) {
        return Files.exists(rootDir.resolve(key));
      }

      @Override
      public void writePdf(String bucketName, String key, byte[] content) {
        //no-op
      }

      @Override
      public Optional<Cdidata> readCdi(String bucketName, String key) {
        try (InputStream in = new GZIPInputStream(Files.newInputStream(rootDir.resolve(key)))) {
          return Optional.of((Cdidata) JAXBContext.newInstance(Cdidata.class).createUnmarshaller().unmarshal(in));
        } catch (IOException | JAXBException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public Optional<GeoJson> readJson(String bucketName, String key) {
        try (InputStream in = new GZIPInputStream(Files.newInputStream(rootDir.resolve(key)))) {
          return Optional.of(objectMapper.readValue(in, GeoJson.class));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public Optional<Quakeml> readQuakeMl(String bucketName, String key) {
        try (InputStream in = new GZIPInputStream(Files.newInputStream(rootDir.resolve(key)))) {
          return Optional.of((Quakeml) JAXBContext.newInstance(Quakeml.class).createUnmarshaller().unmarshal(in));
        } catch (IOException | JAXBException e) {
          throw new RuntimeException(e);
        }
      }
    };
    InfoFileS3Actions infoFileS3Actions = Mockito.spy(new InfoFileS3Actions() {

      @Override
      public boolean isFileExists(String bucketName, String key) {
        return Files.exists(rootDir.resolve(key));
      }

      @Override
      public Optional<InfoFile> readInfoFile(String bucketName, String key) {
        try (InputStream in = new GZIPInputStream(Files.newInputStream(rootDir.resolve(key)))) {
          return Optional.of(objectMapper.readValue(in, InfoFile.class));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public Optional<ReportInfoFile> readReportInfoFile(String bucketName, String key) {
        try (InputStream in = new GZIPInputStream(Files.newInputStream(rootDir.resolve(key)))) {
          return Optional.of(objectMapper.readValue(in, ReportInfoFile.class));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public void saveInfoFile(String bucketName, String key, InfoFile infoFile) {
        //no-op
      }

      @Override
      public void saveReportInfoFile(String bucketName, String key, ReportInfoFile infoFile) {
        //no-op
      }
    });
    BucketIteratorFactory bucketIteratorFactory = new BucketIteratorFactory() {
      @Override
      public Iterator<String> create(String bucketName, String prefix) {
        List<String> keys = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(rootDir.resolve(prefix))) {
          stream.filter(Files::isRegularFile)
              .map(path -> rootDir.relativize(path))
              .map(Path::toString)
              .forEach(keys::add);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        return keys.iterator();
      }
    };
    DataParser dataParser = new DataParser(properties, dataOperations, bucketIteratorFactory, infoFileS3Actions);
    Instant now = Instant.now();
    PdfExecutor executor = new PdfExecutor(properties, dataParser, dataOperations, infoFileS3Actions, () -> now);
    executor.execute(ReportGenerateMessage.Builder.builder().withYear(2017).withMonth(5).build());
    Mockito.verify(infoFileS3Actions).saveReportInfoFile(eq("test"), eq("reports/2017/05/report-info-2017-05.json.gz"), any());

  }

}
