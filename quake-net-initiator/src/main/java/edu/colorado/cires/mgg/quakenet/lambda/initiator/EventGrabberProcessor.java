package edu.colorado.cires.mgg.quakenet.lambda.initiator;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.colorado.cires.cmg.s3out.S3ClientMultipartUpload;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sns.SnsClient;

public class EventGrabberProcessor {

  private final QueryRangeDeterminer queryRangeDeterminer;
  private final QueryRangeIterator queryRangeIterator;
  private final SnsClient snsClient;
  private final ObjectMapper objectMapper;
  private final S3ClientMultipartUpload s3;

  public EventGrabberProcessor(InitiatorProperties properties, S3Client s3Client, SnsClient snsClient,
      ObjectMapper objectMapper, S3ClientMultipartUpload s3) {
    this.snsClient = snsClient;
    this.objectMapper = objectMapper;
    this.s3 = s3;
    queryRangeDeterminer = new QueryRangeDeterminer(properties, s3Client);
    queryRangeIterator = new QueryRangeIterator(this.snsClient, this.objectMapper, properties, this.s3);
  }

  public void process() {
    queryRangeIterator.forEachDate(queryRangeDeterminer.getQueryRange());
  }
}
