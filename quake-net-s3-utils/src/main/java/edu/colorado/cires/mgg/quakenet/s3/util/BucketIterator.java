package edu.colorado.cires.mgg.quakenet.s3.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

public class BucketIterator implements Iterator<String> {

  private ListObjectsV2Response listObjectsResponse;
  private LinkedList<S3Object> contents;
  private ListObjectsV2Request listObjectsRequest;
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
    while (contents == null || (contents.isEmpty() && listObjectsResponse.nextContinuationToken() != null)) {
      if(listObjectsResponse == null) {
        listObjectsResponse = s3.listObjectsV2(listObjectsRequest);
        contents = new LinkedList<>(listObjectsResponse.contents());
      } else {
        listObjectsRequest = listObjectsRequest.toBuilder().continuationToken(listObjectsResponse.nextContinuationToken()).build();
        listObjectsResponse = s3.listObjectsV2(listObjectsRequest);
        contents = new LinkedList<>(listObjectsResponse.contents());
      }
    }
    return !contents.isEmpty();
  }

  @Override
  public String next() {
    if (!hasNext()) {
      throw new NoSuchElementException("No more S3 objects");
    }
    return contents.pop().key();
  }
}
