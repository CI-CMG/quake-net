package edu.colorado.cires.mgg.quakenet.lambda.initiator;

import edu.colorado.cires.mgg.quakenet.s3.util.InfoFileS3Actions;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryRangeDeterminer {

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

  private Optional<LocalDate> getMaxExistingDate(LocalDate earliestDate, LocalDate latestDate) {
    List<LocalDate> datesToCheck = new ArrayList<>(earliestDate.datesUntil(latestDate.plusDays(1))
        .collect(Collectors.toList()));
    Collections.reverse(datesToCheck);

    for (LocalDate date : datesToCheck) {
      if(infoFileS3Actions.isFileExists(initiatorProperties.getDownloadBucket(), getKey(date))) {
        return Optional.of(date);
      }
    }

    return Optional.empty();
  }

  // downloads/2012/05/2012-05-10/usgs-info-2012-05-10.json.gz
  // downloads/2012/05/2012-05-10/<eventId>/event-details-2012-05-10-<eventId>.json.gz
  public Optional<QueryRange> getQueryRange() {

    LocalDate now = nowFactory.get();
    LocalDate latestDate = now.minusYears(1);
    if(latestDate.isLeapYear() && latestDate.getMonthValue() == 2 && latestDate.getDayOfMonth() == 28) {
      latestDate = latestDate.plusDays(1);
    }
    LocalDate earliestDate = YearMonth.of(latestDate.getYear(), latestDate.getMonthValue()).atDay(1);
    LocalDate maxExistingDate = getMaxExistingDate(earliestDate, latestDate).orElse(earliestDate.minusDays(1));

    if (maxExistingDate.isEqual(latestDate) || maxExistingDate.isAfter(latestDate)) {
      return Optional.empty();
    }
    return Optional.of(new QueryRange(maxExistingDate.plusDays(1), latestDate));
  }

}
