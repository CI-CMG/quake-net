package edu.colorado.cires.mgg.quakenet.lambda.cf;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.S3Object;

public final class BucketDrain {

  private static final Logger LOGGER = LoggerFactory.getLogger(EmptyBucketLambda.class);

  public static void emptyBucket(S3Client s3, String bucketName) {
    LOGGER.info("Emptying Bucket: {}", bucketName);

    try {

      ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
          .bucket(bucketName)
          .build();

      List<List<ObjectIdentifier>> objIdGroups = new ArrayList<>();
      ListObjectsV2Response listObjectsResponse;
      do {
        List<ObjectIdentifier> objIds = new ArrayList<>();
        listObjectsResponse = s3.listObjectsV2(listObjectsRequest);
        for (S3Object s3Object : listObjectsResponse.contents()) {
          objIds.add(ObjectIdentifier.builder().key(s3Object.key()).build());
        }
        if(!objIds.isEmpty()) {
          objIdGroups.add(objIds);
        }
      } while (listObjectsResponse.isTruncated());

      objIdGroups.stream().parallel().forEach(objIds -> {
        s3.deleteObjects(DeleteObjectsRequest.builder()
            .bucket(bucketName)
            .delete(Delete.builder().objects(objIds).build())
            .build());
      });


      LOGGER.info("Done Emptying Bucket: {}", bucketName);
    } catch (Exception e) {
      LOGGER.warn("Unable to delete bucket '{}'", bucketName, e);
    }


  }

  private BucketDrain() {

  }

}
