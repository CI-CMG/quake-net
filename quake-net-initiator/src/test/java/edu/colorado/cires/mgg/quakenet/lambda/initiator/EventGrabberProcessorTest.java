package edu.colorado.cires.mgg.quakenet.lambda.initiator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class EventGrabberProcessorTest {

  @Test
  void testNoRetry() throws Exception {
    InitiatorProperties properties = new InitiatorProperties();
    properties.setMaxMonthsPerTrigger(6);
    QueryRangeDeterminer queryRangeDeterminer = mock(QueryRangeDeterminer.class);
    QueryRangeIterator queryRangeIterator = mock(QueryRangeIterator.class);
    ReportRetryProcessor reportRetryProcessor = mock(ReportRetryProcessor.class);

    QueryRange currentRange = new QueryRange(LocalDate.parse("2020-01-01"), LocalDate.parse("2020-01-31"));
    List<QueryRange> ranges = new ArrayList<>();
    ranges.add(currentRange);

    when(reportRetryProcessor.prepareFailedReports()).thenReturn(Collections.emptyList());
    when(queryRangeDeterminer.getQueryRange()).thenReturn(Optional.of(currentRange));

    EventGrabberProcessor processor = new EventGrabberProcessor(properties, queryRangeDeterminer, queryRangeIterator, reportRetryProcessor);
    processor.process();

    for (QueryRange range : ranges) {
      verify(queryRangeIterator).forEachDate(eq(range));
    }

    verify(reportRetryProcessor).prepareFailedReports();
    verify(queryRangeDeterminer).getQueryRange();

    verifyNoMoreInteractions(queryRangeDeterminer, queryRangeIterator, reportRetryProcessor);

  }

  @Test
  void testRetry() throws Exception {
    InitiatorProperties properties = new InitiatorProperties();
    properties.setMaxMonthsPerTrigger(6);
    QueryRangeDeterminer queryRangeDeterminer = mock(QueryRangeDeterminer.class);
    QueryRangeIterator queryRangeIterator = mock(QueryRangeIterator.class);
    ReportRetryProcessor reportRetryProcessor = mock(ReportRetryProcessor.class);

    List<QueryRange> retries = new ArrayList<>();
    retries.add(new QueryRange(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-01-31")));
    retries.add(new QueryRange(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-01-31")));

    QueryRange currentRange = new QueryRange(LocalDate.parse("2020-01-01"), LocalDate.parse("2020-01-31"));
    List<QueryRange> ranges = new ArrayList<>();
    ranges.addAll(retries);
    ranges.add(currentRange);

    when(reportRetryProcessor.prepareFailedReports()).thenReturn(retries);
    when(queryRangeDeterminer.getQueryRange()).thenReturn(Optional.of(currentRange));

    EventGrabberProcessor processor = new EventGrabberProcessor(properties, queryRangeDeterminer, queryRangeIterator, reportRetryProcessor);
    processor.process();

    for (QueryRange range : ranges) {
      verify(queryRangeIterator).forEachDate(eq(range));
    }

    verify(reportRetryProcessor).prepareFailedReports();
    verify(queryRangeDeterminer).getQueryRange();

    verifyNoMoreInteractions(queryRangeDeterminer, queryRangeIterator, reportRetryProcessor);

  }

  @Test
  void testRetryLimited() throws Exception {
    InitiatorProperties properties = new InitiatorProperties();
    properties.setMaxMonthsPerTrigger(2);
    QueryRangeDeterminer queryRangeDeterminer = mock(QueryRangeDeterminer.class);
    QueryRangeIterator queryRangeIterator = mock(QueryRangeIterator.class);
    ReportRetryProcessor reportRetryProcessor = mock(ReportRetryProcessor.class);

    List<QueryRange> retries = new ArrayList<>();
    retries.add(new QueryRange(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-01-31")));
    retries.add(new QueryRange(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-01-31")));

    List<QueryRange> ranges = new ArrayList<>();
    ranges.addAll(retries);

    when(reportRetryProcessor.prepareFailedReports()).thenReturn(retries);

    EventGrabberProcessor processor = new EventGrabberProcessor(properties, queryRangeDeterminer, queryRangeIterator, reportRetryProcessor);
    processor.process();

    for (QueryRange range : ranges) {
      verify(queryRangeIterator).forEachDate(eq(range));
    }

    verify(reportRetryProcessor).prepareFailedReports();
    verify(queryRangeDeterminer, times(0)).getQueryRange();

    verifyNoMoreInteractions(queryRangeDeterminer, queryRangeIterator, reportRetryProcessor);

  }

  @Test
  void testDoNothing() throws Exception {
    InitiatorProperties properties = new InitiatorProperties();
    properties.setMaxMonthsPerTrigger(2);
    QueryRangeDeterminer queryRangeDeterminer = mock(QueryRangeDeterminer.class);
    QueryRangeIterator queryRangeIterator = mock(QueryRangeIterator.class);
    ReportRetryProcessor reportRetryProcessor = mock(ReportRetryProcessor.class);

    when(reportRetryProcessor.prepareFailedReports()).thenReturn(Collections.emptyList());
    when(queryRangeDeterminer.getQueryRange()).thenReturn(Optional.empty());

    EventGrabberProcessor processor = new EventGrabberProcessor(properties, queryRangeDeterminer, queryRangeIterator, reportRetryProcessor);
    processor.process();

    verify(queryRangeIterator, times(0)).forEachDate(any());
    verify(reportRetryProcessor).prepareFailedReports();
    verify(queryRangeDeterminer).getQueryRange();

    verifyNoMoreInteractions(queryRangeDeterminer, queryRangeIterator, reportRetryProcessor);

  }
}