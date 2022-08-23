package edu.colorado.cires.mgg.quakenet.lambda.reportinit;

import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Object;

public class ReportTrigger {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReportTrigger.class);

  private final ReportInitiatorProperties properties;
  private final S3Client s3;

  public ReportTrigger(ReportInitiatorProperties properties, S3Client s3) {
    this.properties = properties;
    this.s3 = s3;
  }

  private boolean isReportExists(String year, String month) {
    HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
        .bucket(properties.getBucketName())
        .key("reports/" + year + "/" + month + "/earthquake-info-" + year + "-" + month + ".pdf")
        .build();
    try {
      s3.headObject(headObjectRequest);
    } catch (NoSuchKeyException e) {
      return  false;
    }
    return true;
  }

  public void triggerReports() {
    ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
        .bucket(properties.getBucketName())
        .prefix("downloads/")
        .build();

    ListObjectsV2Response listObjectsResponse;
    do {
      listObjectsResponse = s3.listObjectsV2(listObjectsRequest);
      for (S3Object s3Object : listObjectsResponse.contents()) {
        String[] parts = s3Object.key().split("/");
        String year = parts[1];
        String month = parts[2];
        LocalDate date = LocalDate.parse(parts[3]);
        String file = parts[4];
        if(date.getDayOfMonth() == 1 && file.equals("usgs-info-" + date + ".json.gz") && !isReportExists(year, month)) {
          sendGenerateReportMessage(year, month);
        }
      }
    } while (listObjectsResponse.isTruncated());

  }

  private void sendGenerateReportMessage(String year, String month) {
    LOGGER.info("Triggering Report: {}-{}", year, month);
  }

}
