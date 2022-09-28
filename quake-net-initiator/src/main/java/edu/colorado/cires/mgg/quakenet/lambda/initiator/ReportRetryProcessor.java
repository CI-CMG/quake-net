package edu.colorado.cires.mgg.quakenet.lambda.initiator;

import edu.colorado.cires.mgg.quakenet.message.ReportInfoFile;
import edu.colorado.cires.mgg.quakenet.s3.util.InfoFileS3Actions;
import edu.colorado.cires.mgg.quakenet.s3.util.S3FileUtilities;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportRetryProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReportRetryProcessor.class);

  private final InitiatorProperties initiatorProperties;
  private final Supplier<LocalDate> nowFactory;
  private final Supplier<Instant> nowTimeFactory;
  private final InfoFileS3Actions infoFileS3Actions;
  private final S3FileUtilities s3FileUtilities;
  private final YearMonth begin;

  public ReportRetryProcessor(InitiatorProperties initiatorProperties, Supplier<LocalDate> nowFactory,
      Supplier<Instant> nowTimeFactory, InfoFileS3Actions infoFileS3Actions, S3FileUtilities s3FileUtilities) {
    this.initiatorProperties = initiatorProperties;
    this.nowFactory = nowFactory;
    this.nowTimeFactory = nowTimeFactory;
    this.infoFileS3Actions = infoFileS3Actions;
    this.s3FileUtilities = s3FileUtilities;
    LocalDate beginDate = LocalDate.parse(initiatorProperties.getDefaultStartDate());
    begin = YearMonth.of(beginDate.getYear(), beginDate.getMonthValue());
  }

  public List<QueryRange> prepareFailedReports() {
    List<YearMonth> reportsToCheck = getPossibleReports();
    List<YearMonth> completedReports = getCompletedReports(reportsToCheck);
    reportsToCheck.removeAll(completedReports);
    List<YearMonth> reportsToReprocess = findReportsToReprocess(reportsToCheck);
    if(!reportsToReprocess.isEmpty()) {
      LOGGER.info("Scheduling reprocess for {}", reportsToReprocess);
    }
    return reprocessReports(reportsToReprocess);
  }

  private List<QueryRange> reprocessReports(List<YearMonth> reportsToReprocess) {
    List<QueryRange> ranges = new ArrayList<>(reportsToReprocess.size());
    for (YearMonth yearMonth : reportsToReprocess) {
      ranges.add(new QueryRange(yearMonth.atDay(1), yearMonth.atEndOfMonth()));
      delete(initiatorProperties.getDownloadBucket(), String.format("downloads/%d/%02d/", yearMonth.getYear(), yearMonth.getMonthValue()));
      delete(initiatorProperties.getDownloadBucket(), String.format("reports/%d/%02d/", yearMonth.getYear(), yearMonth.getMonthValue()));
    }
    return ranges;
  }

  private void delete(String bucket, String prefix) {
    s3FileUtilities.deleteFiles(bucket, prefix);
  }

  private Instant getMinQuietTime() {
    return nowTimeFactory.get().minus(initiatorProperties.getRetryQuietTimeMinutes(), ChronoUnit.MINUTES);
  }

  private boolean canReprocess(ReportInfoFile infoFile) {
    if (infoFile == null || infoFile.getStartTime() == null) {
      return true;
    }
    Instant minQuietTime = getMinQuietTime();
    if (infoFile.getStartReportGeneration() != null) {
      return infoFile.getStartReportGeneration().isBefore(minQuietTime);
    }
    return infoFile.getStartTime().isBefore(minQuietTime);
  }

  private List<YearMonth> findReportsToReprocess(List<YearMonth> reportsToCheck) {
    List<YearMonth> reportsToReprocess = new ArrayList<>(initiatorProperties.getMaxMonthsPerTrigger());
    for (YearMonth yearMonth : reportsToCheck) {
      String reportInfoKey = String.format("reports/%d/%02d/report-info-%d-%02d.json.gz", yearMonth.getYear(), yearMonth.getMonthValue(),
          yearMonth.getYear(), yearMonth.getMonthValue());
      ReportInfoFile infoFile = readReportInfo(initiatorProperties.getDownloadBucket(), reportInfoKey).orElse(null);
      if (canReprocess(infoFile)) {
        reportsToReprocess.add(yearMonth);
        if (reportsToReprocess.size() >= initiatorProperties.getMaxMonthsPerTrigger()) {
          break;
        }
      }
    }
    return reportsToReprocess;
  }

  private List<YearMonth> getCompletedReports(List<YearMonth> possibleReports) {
    List<YearMonth> completedReports = new ArrayList<>(possibleReports.size());
    for (YearMonth yearMonth : possibleReports) {
      String key = String.format("reports/%d/%02d/earthquake-info-%d-%02d.pdf",
          yearMonth.getYear(), yearMonth.getMonthValue(), yearMonth.getYear(), yearMonth.getMonthValue());
      if (hasKey(initiatorProperties.getDownloadBucket(), key)) {
        completedReports.add(yearMonth);
      }
    }
    return completedReports;
  }

  private List<YearMonth> getPossibleReports() {
    LocalDate lastYear = nowFactory.get().minusYears(1);
    int year = lastYear.getYear();
    int month = lastYear.getMonthValue();
    YearMonth end = YearMonth.of(year, month).minusMonths(1);
    List<YearMonth> possibleReports = new ArrayList<>();
    YearMonth yearMonth = begin;
    while (yearMonth.isBefore(end) || yearMonth.equals(end)) {
      possibleReports.add(yearMonth);
      yearMonth = yearMonth.plusMonths(1);
    }
    return possibleReports;
  }


  private Optional<ReportInfoFile> readReportInfo(String bucket, String key) {
    return infoFileS3Actions.readReportInfoFile(bucket, key);
  }

  private boolean hasKey(String bucket, String key) {
    return s3FileUtilities.isFileExists(bucket, key);
  }

}
