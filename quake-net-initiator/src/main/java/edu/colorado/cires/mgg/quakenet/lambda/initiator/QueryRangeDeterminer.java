package edu.colorado.cires.mgg.quakenet.lambda.initiator;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Supplier;
import software.amazon.awssdk.services.s3.S3Client;

public class QueryRangeDeterminer {

  private final InitiatorProperties initiatorProperties;
  private final S3Client s3;
  private final BucketIteratorFactory bucketIteratorFactory;
  private final Supplier<LocalDate> nowFactory;

  public QueryRangeDeterminer(InitiatorProperties initiatorProperties, S3Client s3,
      BucketIteratorFactory bucketIteratorFactory, Supplier<LocalDate> nowFactory) {
    this.initiatorProperties = initiatorProperties;
    this.s3 = s3;
    this.bucketIteratorFactory = bucketIteratorFactory;
    this.nowFactory = nowFactory;
  }

  // downloads/2012/05/2012-05-10/usgs-info-2012-05-10.json.gz
  // downloads/2012/05/2012-05-10/<eventId>/event-details-2012-05-10-<eventId>.json.gz
  public Optional<QueryRange> getQueryRange() {

    LocalDate maxDate = LocalDate.parse(initiatorProperties.getDefaultStartDate()).minusDays(1);

    Iterator<String> bucketIterator = bucketIteratorFactory.create(s3, initiatorProperties.getDownloadBucket(), "downloads/");
    while (bucketIterator.hasNext()) {
      String key = bucketIterator.next();
      String[] parts = key.split("/");
      if (parts.length == 5) {
        String date = parts[3];
        String file = parts[4];
        if (file.equals("usgs-info-" + date + ".json.gz")) {
          LocalDate localDate = LocalDate.parse(date);
          if (localDate.isAfter(maxDate)) {
            maxDate = localDate;
          }
        }
      }
    }

    LocalDate lastYear = nowFactory.get().minusYears(1);
    QueryRange queryRange = new QueryRange(maxDate.plusDays(1), lastYear);
    if (maxDate.isEqual(lastYear) || maxDate.isAfter(lastYear)) {
      return Optional.empty();
    }
    return Optional.of(queryRange);
  }

}
