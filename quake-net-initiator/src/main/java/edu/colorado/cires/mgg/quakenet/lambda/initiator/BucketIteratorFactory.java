package edu.colorado.cires.mgg.quakenet.lambda.initiator;

import java.util.Iterator;
import software.amazon.awssdk.services.s3.S3Client;

public interface BucketIteratorFactory {

  Iterator<String> create(S3Client s3, String bucketName, String prefix);

}
