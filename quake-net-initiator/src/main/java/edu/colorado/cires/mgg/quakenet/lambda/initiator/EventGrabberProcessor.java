package edu.colorado.cires.mgg.quakenet.lambda.initiator;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.colorado.cires.cmg.s3out.S3ClientMultipartUpload;
import edu.colorado.cires.mgg.quakenet.s3.util.InfoFileS3Actions;
import java.time.LocalDate;
import software.amazon.awssdk.services.sns.SnsClient;

public class EventGrabberProcessor {

  private final QueryRangeDeterminer queryRangeDeterminer;
  private final QueryRangeIterator queryRangeIterator;

  public EventGrabberProcessor(InitiatorProperties properties, SnsClient snsClient,
      ObjectMapper objectMapper, S3ClientMultipartUpload s3, InfoFileS3Actions infoFileS3Actions) {
    queryRangeDeterminer = new QueryRangeDeterminer(properties, LocalDate::now, infoFileS3Actions);
    InfoFileSaver fileInfoSaver = new InfoFileSaver(s3, objectMapper);
    MessageSender messageSender = new MessageSender(snsClient, objectMapper);
    queryRangeIterator = new QueryRangeIterator(fileInfoSaver, messageSender, properties);
  }

  public void process() {
    queryRangeDeterminer.getQueryRange().ifPresent(queryRangeIterator::forEachDate);
  }
}
