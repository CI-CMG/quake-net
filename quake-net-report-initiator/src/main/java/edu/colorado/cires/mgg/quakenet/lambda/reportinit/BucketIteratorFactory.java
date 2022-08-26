package edu.colorado.cires.mgg.quakenet.lambda.reportinit;

import software.amazon.awssdk.services.s3.S3Client;

public interface BucketIteratorFactory {

  BucketIterator create(S3Client s3, String bucketName, String prefix);

}
