package edu.colorado.cires.mgg.quakenet.lambda.initiator;

import java.time.LocalDate;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

public class QueryRangeDeterminer {

  private final InitiatorProperties initiatorProperties;
  private final S3Client s3;

  public QueryRangeDeterminer(InitiatorProperties initiatorProperties, S3Client s3) {
    this.initiatorProperties = initiatorProperties;
    this.s3 = s3;
  }

  // downloads/2012/05/2012-05-10/usgs-info-2012-05-10.json.gz
  // downloads/2012/05/2012-05-10/<eventId>/event-details-2012-05-10-<eventId>.json.gz
  public QueryRange getQueryRange() {
    ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
        .bucket(initiatorProperties.getDownloadBucket())
        .prefix("downloads/")
        .build();

    LocalDate maxDate = LocalDate.parse(initiatorProperties.getDefaultStartDate());

    ListObjectsV2Response listObjectsResponse;
    do {
      listObjectsResponse = s3.listObjectsV2(listObjectsRequest);
      for (S3Object s3Object : listObjectsResponse.contents()) {
        String[] parts = s3Object.key().split("/");
        if (parts.length == 5) {
          String date = parts[3];
          String file = parts[4];
          if(file.equals("usgs-info-" + date + ".json.gz")) {
            LocalDate localDate = LocalDate.parse(date);
            if (localDate.isAfter(maxDate)) {
              maxDate = localDate;
            }
          }
        }
      }
    } while (listObjectsResponse.isTruncated());

    LocalDate today = LocalDate.now();
    LocalDate lastYear = today.minusYears(1);
    return new QueryRange(maxDate, lastYear);
  }

}
