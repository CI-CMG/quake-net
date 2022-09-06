package edu.colorado.cires.mgg.quakenet.lambda.initiator;

import edu.colorado.cires.mgg.quakenet.s3.util.InfoFileS3Actions;
import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryRangeDeterminer {

  private static final Logger LOGGER = LoggerFactory.getLogger(QueryRangeDeterminer.class);

  private final InitiatorProperties initiatorProperties;
  private final Supplier<LocalDate> nowFactory;
  private final InfoFileS3Actions infoFileS3Actions;

  public QueryRangeDeterminer(InitiatorProperties initiatorProperties,
      Supplier<LocalDate> nowFactory,
      InfoFileS3Actions infoFileS3Actions) {
    this.initiatorProperties = initiatorProperties;
    this.nowFactory = nowFactory;
    this.infoFileS3Actions = infoFileS3Actions;
  }

  private String getKey(LocalDate date) {
    return String.format("downloads/%d/%02d/%s/usgs-info-%s.json.gz", date.getYear(), date.getMonthValue(), date.toString(), date.toString());
  }

  // downloads/2012/05/2012-05-10/usgs-info-2012-05-10.json.gz
  // downloads/2012/05/2012-05-10/<eventId>/event-details-2012-05-10-<eventId>.json.gz
  public Optional<QueryRange> getQueryRange() {

    LocalDate earliestDate = LocalDate.parse(initiatorProperties.getDefaultStartDate()).minusDays(1);
    LocalDate lastYear = nowFactory.get().minusYears(1);
    LocalDate maxDate = lastYear;
    while (maxDate.isAfter(earliestDate) && !infoFileS3Actions.isFileExists(initiatorProperties.getDownloadBucket(), getKey(maxDate))) {
      maxDate = maxDate.minusDays(1);
    }

    LOGGER.info("Max Date: {}", maxDate);

    if (maxDate.isEqual(lastYear) || maxDate.isAfter(lastYear)) {
      return Optional.empty();
    }
    return Optional.of(new QueryRange(maxDate.plusDays(1), lastYear));
  }

}
