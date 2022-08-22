package edu.colorado.cires.mgg.quakenet.lambda.initiator;

import java.time.LocalDate;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

public class QueryRangeDeterminer {

  private final EventGrabberProperties eventGrabberProperties;
  private final S3Client s3;

  public QueryRangeDeterminer(EventGrabberProperties eventGrabberProperties, S3Client s3) {
    this.eventGrabberProperties = eventGrabberProperties;
    this.s3 = s3;
  }

  public QueryRange getQueryRange() {
    ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
        .bucket(eventGrabberProperties.getDownloadBucket())
        .prefix("downloads/")
        .build();

    LocalDate maxDate = null;

    ListObjectsV2Response listObjectsResponse;
    do {
      listObjectsResponse = s3.listObjectsV2(listObjectsRequest);
      for (S3Object s3Object : listObjectsResponse.contents()) {
        String[] parts = s3Object.key().split("/");
        String date = parts[2];
        String file = parts[3];
        if(file.equals("usgs-info-" + date + ".json.gz")) {
          LocalDate localDate = LocalDate.parse(date);
          if (maxDate == null || localDate.isAfter(maxDate)) {
            maxDate = localDate;
          }
        }
      }
    } while (listObjectsResponse.isTruncated());

    LocalDate today = LocalDate.now();
    LocalDate lastYear = today.minusYears(1);
    return new QueryRange(maxDate, lastYear);
  }

}
