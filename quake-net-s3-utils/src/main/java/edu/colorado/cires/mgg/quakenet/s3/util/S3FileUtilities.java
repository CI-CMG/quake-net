package edu.colorado.cires.mgg.quakenet.s3.util;

import edu.colorado.cires.cmg.s3out.MultipartUploadRequest;
import edu.colorado.cires.cmg.s3out.S3ClientMultipartUpload;
import edu.colorado.cires.cmg.s3out.S3OutputStream;
import edu.colorado.cires.mgg.quakenet.message.InfoFile;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

public class S3FileUtilities {

  private final S3ClientMultipartUpload s3;
  private final S3Client s3Client;

  public S3FileUtilities(S3ClientMultipartUpload s3, S3Client s3Client) {
    this.s3 = s3;
    this.s3Client = s3Client;
  }

  public boolean isFileExists(String bucketName, String key) {
    HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .build();
    try {
      s3Client.headObject(headObjectRequest);
    } catch (NoSuchKeyException e) {
      return false;
    }
    return true;
  }

  public void saveUncompressedFile(String bucketName, String key, Consumer<OutputStream> doWithOutput) {
    try (
        S3OutputStream s3OutputStream = S3OutputStream.builder()
            .s3(s3)
            .uploadRequest(MultipartUploadRequest.builder().bucket(bucketName).key(key).build())
            .autoComplete(false)
            .build();
    ) {
      doWithOutput.accept(s3OutputStream);
      s3OutputStream.done();
    } catch (IOException e) {
      throw new IllegalStateException("An error occurred saving data", e);
    }
  }

  public void saveFile(String bucketName, String key, Consumer<OutputStream> doWithOutput) {

    try (
        S3OutputStream s3OutputStream = S3OutputStream.builder()
            .s3(s3)
            .uploadRequest(MultipartUploadRequest.builder().bucket(bucketName).key(key).build())
            .autoComplete(false)
            .build();
        OutputStream outputStream = new GZIPOutputStream(s3OutputStream)
    ) {
      doWithOutput.accept(outputStream);
      s3OutputStream.done();
    } catch (IOException e) {
      throw new IllegalStateException("An error occurred saving data", e);
    }
  }

  public <T> Optional<T> readFile(String bucketName, String key, Function<InputStream, T> doWithInput) {
    GetObjectRequest objectRequest = GetObjectRequest
        .builder()
        .bucket(bucketName)
        .key(key)
        .build();

    try (InputStream in = new GZIPInputStream(new BufferedInputStream(s3Client.getObject(objectRequest)))) {
      return Optional.ofNullable(doWithInput.apply(in));
    } catch (NoSuchKeyException e) {
      return Optional.empty();
    } catch (IOException e) {
      throw new IllegalStateException("Unable to read data", e);
    }
  }

}
