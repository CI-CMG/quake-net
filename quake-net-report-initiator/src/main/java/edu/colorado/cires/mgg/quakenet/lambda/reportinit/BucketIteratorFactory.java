package edu.colorado.cires.mgg.quakenet.lambda.reportinit;

import java.util.Iterator;

public interface BucketIteratorFactory {

  Iterator<String> create(String bucketName, String prefix);

}
