package edu.colorado.cires.mgg.quakenet.lambda.initiator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.colorado.cires.cmg.s3out.MultipartUploadRequest;
import edu.colorado.cires.cmg.s3out.S3ClientMultipartUpload;
import edu.colorado.cires.cmg.s3out.S3OutputStream;
import edu.colorado.cires.mgg.quakenet.message.InfoFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.io.IOUtils;

public class InfoFileSaver {


  private final S3ClientMultipartUpload s3;
  private final ObjectMapper objectMapper;

  public InfoFileSaver(S3ClientMultipartUpload s3, ObjectMapper objectMapper) {
    this.s3 = s3;
    this.objectMapper = objectMapper;
  }

  public void saveInfoFile(String bucketName, String key, InfoFile infoFile) {
    String json;
    try {
      json = objectMapper.writeValueAsString(infoFile);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to serialize message", e);
    }

    try (
        InputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        S3OutputStream s3OutputStream = S3OutputStream.builder()
            .s3(s3)
            .uploadRequest(MultipartUploadRequest.builder().bucket(bucketName).key(key).build())
            .autoComplete(false)
            .build();
        OutputStream outputStream = new GZIPOutputStream(s3OutputStream)
    ) {
      IOUtils.copy(inputStream, outputStream);
      s3OutputStream.done();
    } catch (IOException e) {
      throw new IllegalStateException("An error occurred saving USGS data", e);
    }

  }
}
