package edu.colorado.cires.mgg.quakenet.lambda.initiator;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventGrabberProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(EventGrabberProcessor.class);

  private final InitiatorProperties properties;
  private final QueryRangeDeterminer queryRangeDeterminer;
  private final QueryRangeIterator queryRangeIterator;
  private final ReportRetryProcessor reportRetryProcessor;


  public EventGrabberProcessor(InitiatorProperties properties,
      QueryRangeDeterminer queryRangeDeterminer, QueryRangeIterator queryRangeIterator,
      ReportRetryProcessor reportRetryProcessor) {
    this.properties = properties;

    this.queryRangeDeterminer = queryRangeDeterminer;
    this.queryRangeIterator = queryRangeIterator;
    this.reportRetryProcessor = reportRetryProcessor;
  }

  public void process() {
    List<QueryRange> retries = new ArrayList<>(reportRetryProcessor.prepareFailedReports());
    if (retries.size() < properties.getMaxMonthsPerTrigger()) {
      queryRangeDeterminer.getQueryRange().ifPresent(retries::add);
    }
    if (!retries.isEmpty()) {
      LOGGER.info("Processing: {}", retries);
    }
    retries.forEach(queryRangeIterator::forEachDate);
  }
}
