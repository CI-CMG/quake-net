package edu.colorado.cires.mgg.quakenet.lambda.cf;

import edu.colorado.cires.mgg.quakenet.s3.util.BucketIterator;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;

public final class BucketDrain {

  private static final Logger LOGGER = LoggerFactory.getLogger(EmptyBucketLambda.class);

  private static List<ObjectIdentifier> getPage(S3Client s3, String bucketName) {
    List<ObjectIdentifier> objIds = new ArrayList<>(1000);
    BucketIterator it = new BucketIterator(s3, bucketName, null);
    while (it.hasNext() && objIds.size() < 1000) {
      String key = it.next();
      objIds.add(ObjectIdentifier.builder().key(key).build());
    }
    return objIds;
  }

  public static void emptyBucket(S3Client s3, String bucketName) {
    LOGGER.info("Emptying Bucket: {}", bucketName);

    try {

      List<ObjectIdentifier> objIds;
      while (!(objIds = getPage(s3, bucketName)).isEmpty()) {
        LOGGER.info("Deleting {} keys", objIds.size());
        s3.deleteObjects(DeleteObjectsRequest.builder()
            .bucket(bucketName)
            .delete(Delete.builder().objects(objIds).build())
            .build());
      }

      LOGGER.info("Done Emptying Bucket: {}", bucketName);
    } catch (Exception e) {
      LOGGER.warn("Unable to delete bucket '{}'", bucketName, e);
    }


  }

  private BucketDrain() {

  }

}
