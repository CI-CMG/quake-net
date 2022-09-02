package edu.colorado.cires.mgg.quakenet.lambda.initiator;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.colorado.cires.cmg.s3out.S3ClientMultipartUpload;
import edu.colorado.cires.mgg.quakenet.s3.util.BucketIterator;
import java.time.LocalDate;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sns.SnsClient;

public class EventGrabberProcessor {

  private final QueryRangeDeterminer queryRangeDeterminer;
  private final QueryRangeIterator queryRangeIterator;

  public EventGrabberProcessor(InitiatorProperties properties, S3Client s3Client, SnsClient snsClient,
      ObjectMapper objectMapper, S3ClientMultipartUpload s3) {
    queryRangeDeterminer = new QueryRangeDeterminer(properties, s3Client, BucketIterator::new, LocalDate::now);
    InfoFileSaver fileInfoSaver = new InfoFileSaver(s3, objectMapper);
    MessageSender messageSender = new MessageSender(snsClient, objectMapper);
    queryRangeIterator = new QueryRangeIterator(fileInfoSaver, messageSender, properties);
  }

  public void process() {
    queryRangeDeterminer.getQueryRange().ifPresent(queryRangeIterator::forEachDate);
  }
}
