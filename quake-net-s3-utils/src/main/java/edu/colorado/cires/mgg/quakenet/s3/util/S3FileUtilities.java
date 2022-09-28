package edu.colorado.cires.mgg.quakenet.s3.util;

import edu.colorado.cires.cmg.s3out.MultipartUploadRequest;
import edu.colorado.cires.cmg.s3out.S3ClientMultipartUpload;
import edu.colorado.cires.cmg.s3out.S3OutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;

public class S3FileUtilities {

  private static final Logger LOGGER = LoggerFactory.getLogger(S3FileUtilities.class);

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
      throw new IllegalStateException("An error occurred saving data: " + bucketName + "/" + key, e);
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
      throw new IllegalStateException("An error occurred saving data: " + bucketName + "/" + key, e);
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
      throw new IllegalStateException("Unable to read data: " + bucketName + "/" + key, e);
    }
  }

  private List<ObjectIdentifier> getPage(String bucketName, String keyPrefix) {
    List<ObjectIdentifier> objIds = new ArrayList<>(1000);
    BucketIterator it = new BucketIterator(s3Client, bucketName, keyPrefix);
    while (it.hasNext() && objIds.size() < 1000) {
      String key = it.next();
      objIds.add(ObjectIdentifier.builder().key(key).build());
    }
    return objIds;
  }

  public void deleteFiles(String bucketName, String keyPrefix) {

    try {

      List<ObjectIdentifier> objIds;
      while (!(objIds = getPage(bucketName, keyPrefix)).isEmpty()) {
        LOGGER.info("Deleting {} keys", objIds.size());
        s3Client.deleteObjects(DeleteObjectsRequest.builder()
            .bucket(bucketName)
            .delete(Delete.builder().objects(objIds).build())
            .build());
      }

      LOGGER.info("Done deleting keys: {}/{}", bucketName, keyPrefix != null ? keyPrefix : "");
    } catch (Exception e) {
      LOGGER.warn("Unable to delete keys '{}/{}'", bucketName, keyPrefix != null ? keyPrefix : "", e);
    }


  }

}
