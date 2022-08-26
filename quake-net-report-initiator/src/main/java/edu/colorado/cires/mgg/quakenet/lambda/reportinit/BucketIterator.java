package edu.colorado.cires.mgg.quakenet.lambda.reportinit;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

public class BucketIterator implements Iterator<S3Object> {

  private ListObjectsV2Response listObjectsResponse;
  private LinkedList<S3Object> contents = null;
  private final ListObjectsV2Request listObjectsRequest;
  private final S3Client s3;

  public BucketIterator(S3Client s3, String bucketName, String prefix) {
    this.s3 = s3;
    listObjectsRequest = ListObjectsV2Request.builder()
        .bucket(bucketName)
        .prefix(prefix)
        .build();
  }


  @Override
  public boolean hasNext() {
    if(contents == null || (contents.isEmpty() && !listObjectsResponse.isTruncated())) {
      listObjectsResponse = s3.listObjectsV2(listObjectsRequest);
      contents = new LinkedList<>(listObjectsResponse.contents());
    }
    return !contents.isEmpty();
  }

  @Override
  public S3Object next() {
    if(!hasNext()) {
      throw new NoSuchElementException("No more S3 objects");
    }
    return contents.pop();
  }
}
