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
import edu.colorado.cires.mgg.quakenet.model.QnEvent;
import edu.colorado.cires.mgg.quakenet.util.ObjectMapperCreator;
import gov.noaa.ncei.xmlns.cdidata.Cdidata;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.quakeml.xmlns.quakeml._1.Quakeml;
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

  static {
    properties = new PdfGenProperties();
    properties.setBucketName(downloadBucket);
  }

  @Override
  public SQSBatchResponse handleRequest(SQSEvent sqsEvent, Context context) {
    LOGGER.info("Event: {}", sqsEvent);

    DataParser dataParser = new DataParser(properties, s3Client);

    List<BatchItemFailure> batchItemFailures = new ArrayList<>();
    String messageId = "";
    for (SQSMessage record : sqsEvent.getRecords()) {
      try {
        messageId = record.getMessageId();

        ReportGenerateMessage message = objectMapper.readValue(record.getBody(), ReportGenerateMessage.class);
        List<KeySet> keySets = DataParser.getRequiredKeys(message.getYear(), message.getMonth());
        List<QnEvent> events = new ArrayList<>();
        for (KeySet keySet : keySets) {
          Quakeml quakeml = dataParser.readQuakeMl(keySet.getDetailsKey())
              .orElseThrow(() -> new RuntimeException("Unable to read quake details: " + keySet.getDetailsKey()));
          Optional<Cdidata> cdidata = dataParser.readCdi(keySet.getDetailsKey());
          QnEvent event = DataParser.parseQuakeDetails(quakeml);
          cdidata.ifPresent(data -> DataParser.enrichCdi(event, data));
          events.add(event);
        }

        DataWriter dataWriter = new DataWriter(properties, s3);
        dataWriter.writePdf(events, message);

      } catch (Exception e) {
        batchItemFailures.add(new BatchItemFailure(messageId));
        LOGGER.error("Unable to process " + messageId, e);
      }
    }
    return new SQSBatchResponse(batchItemFailures);

  }
}
