package edu.colorado.cires.mgg.quakenet.lambda.pdfgen;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse.BatchItemFailure;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.colorado.cires.cmg.s3out.AwsS3ClientMultipartUpload;
import edu.colorado.cires.cmg.s3out.S3ClientMultipartUpload;
import edu.colorado.cires.mgg.quakenet.message.ReportGenerateMessage;
import edu.colorado.cires.mgg.quakenet.s3.util.BucketIterator;
import edu.colorado.cires.mgg.quakenet.util.ObjectMapperCreator;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;

public class PdfGenLambda implements RequestHandler<SQSEvent, SQSBatchResponse> {

  private static final Logger LOGGER = LoggerFactory.getLogger(PdfGenLambda.class);

  private static final String downloadBucket = System.getenv("DOWNLOAD_BUCKET");
  private static final S3Client s3Client = S3Client.builder().build();
  private static final ObjectMapper objectMapper = ObjectMapperCreator.create();
  private static final S3ClientMultipartUpload s3 = AwsS3ClientMultipartUpload.builder().s3(S3Client.builder().build()).build();


  private static final PdfGenProperties properties;
  private static final DataParser dataParser;
  private static final DataOperations dataWriter;
  private static final PdfExecutor executor;

  static {
    properties = new PdfGenProperties();
    properties.setBucketName(downloadBucket);
    dataWriter = new DataOperations(s3, s3Client);
    dataParser = new DataParser(properties, dataWriter, (bucketName, prefix) -> new BucketIterator(s3Client, bucketName, prefix));
    executor = new PdfExecutor(properties, dataParser, dataWriter);
  }

  @Override
  public SQSBatchResponse handleRequest(SQSEvent sqsEvent, Context context) {
    LOGGER.info("Event: {}", sqsEvent);

    List<BatchItemFailure> batchItemFailures = new ArrayList<>();
    String messageId = "";
    for (SQSMessage record : sqsEvent.getRecords()) {
      try {
        messageId = record.getMessageId();

        ReportGenerateMessage message = objectMapper.readValue(record.getBody(), ReportGenerateMessage.class);

        executor.execute(message);

      } catch (Exception e) {
        batchItemFailures.add(new BatchItemFailure(messageId));
        LOGGER.error("Unable to process " + messageId, e);
      }
    }
    return new SQSBatchResponse(batchItemFailures);

  }
}
