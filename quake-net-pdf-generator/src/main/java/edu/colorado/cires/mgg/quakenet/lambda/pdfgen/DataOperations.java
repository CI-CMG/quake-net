package edu.colorado.cires.mgg.quakenet.lambda.pdfgen;

import edu.colorado.cires.cmg.s3out.S3ClientMultipartUpload;
import edu.colorado.cires.mgg.quakenet.s3.util.S3FileUtilities;
import gov.noaa.ncei.xmlns.cdidata.Cdidata;
import java.io.IOException;
import java.util.Optional;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.apache.commons.io.IOUtils;
import org.quakeml.xmlns.quakeml._1.Quakeml;
import software.amazon.awssdk.services.s3.S3Client;

public class DataOperations {


  private final S3FileUtilities fileUploader;

  public DataOperations(S3ClientMultipartUpload s3, S3Client s3Client) {
    fileUploader = new S3FileUtilities(s3, s3Client);
  }

  public boolean isReportExists(String bucketName, String key) {
    return fileUploader.isFileExists(bucketName, key);
  }

  public void writePdf(String bucketName, String key, byte[] content) {
    fileUploader.saveUncompressedFile(bucketName, key, outputStream -> {
      try {
        IOUtils.write(content, outputStream);
      } catch (IOException e) {
        throw new IllegalStateException("An error occurred writing report", e);
      }
    });
  }

  public Optional<Cdidata> readCdi(String bucketName, String key) {
    return fileUploader.readFile(bucketName, key, in -> {
      try {
        return (Cdidata) JAXBContext.newInstance(Cdidata.class).createUnmarshaller().unmarshal(in);
      } catch (JAXBException e) {
        throw new IllegalStateException("Unable to read CDI data", e);
      }
    });
  }


  public Optional<Quakeml> readQuakeMl(String bucketName, String key) {
    return fileUploader.readFile(bucketName, key, in -> {
      try {
        return (Quakeml) JAXBContext.newInstance(Quakeml.class).createUnmarshaller().unmarshal(in);
      } catch (JAXBException e) {
        throw new IllegalStateException("Unable to read QuakeML data", e);
      }
    });
  }


}
