package edu.colorado.cires.mgg.quakenet.s3.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.colorado.cires.cmg.s3out.S3ClientMultipartUpload;
import edu.colorado.cires.mgg.quakenet.message.InfoFile;
import edu.colorado.cires.mgg.quakenet.message.ReportInfoFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.apache.commons.io.IOUtils;
import software.amazon.awssdk.services.s3.S3Client;

public class InfoFileS3Actions {


  private final ObjectMapper objectMapper;
  private final S3FileUtilities fileUploader;

  public InfoFileS3Actions(S3ClientMultipartUpload s3, S3Client s3Client, ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    fileUploader = new S3FileUtilities(s3, s3Client);
  }

  public boolean isFileExists(String bucketName, String key) {
    return fileUploader.isFileExists(bucketName, key);
  }

  public Optional<InfoFile> readInfoFile(String bucketName, String key) {
    return fileUploader.readFile(bucketName, key, in -> {
      try {
        return objectMapper.readValue(in, InfoFile.class);
      } catch (IOException e) {
        throw new IllegalStateException("Unable to read info data: " + bucketName + "/" + key, e);
      }
    });
  }

  public Optional<ReportInfoFile> readReportInfoFile(String bucketName, String key) {
    return fileUploader.readFile(bucketName, key, in -> {
      try {
        return objectMapper.readValue(in, ReportInfoFile.class);
      } catch (IOException e) {
        throw new IllegalStateException("Unable to read report info data: " + bucketName + "/" + key, e);
      }
    });
  }

  public void saveInfoFile(String bucketName, String key, InfoFile infoFile) {
    String json;
    try {
      json = objectMapper.writeValueAsString(infoFile);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to serialize message: " + bucketName + "/" + key, e);
    }

    try (InputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))) {
      fileUploader.saveFile(bucketName, key, outputStream -> {
        try {
          IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
          throw new IllegalStateException("An error occurred saving data: " + bucketName + "/" + key, e);
        }
      });
    } catch (IOException e) {
      throw new IllegalStateException("An error occurred saving data: " + bucketName + "/" + key, e);
    }
  }

  public void saveReportInfoFile(String bucketName, String key, ReportInfoFile infoFile) {
    String json;
    try {
      json = objectMapper.writeValueAsString(infoFile);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to serialize message: " + bucketName + "/" + key, e);
    }

    try (InputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))) {
      fileUploader.saveFile(bucketName, key, outputStream -> {
        try {
          IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
          throw new IllegalStateException("An error occurred saving data: " + bucketName + "/" + key, e);
        }
      });
    } catch (IOException e) {
      throw new IllegalStateException("An error occurred saving data: " + bucketName + "/" + key, e);
    }
  }

}
