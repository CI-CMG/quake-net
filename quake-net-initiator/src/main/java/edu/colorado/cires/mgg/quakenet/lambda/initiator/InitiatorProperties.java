package edu.colorado.cires.mgg.quakenet.lambda.initiator;

public class InitiatorProperties {

  private String downloadBucket;
  private String defaultStartDate;
  private String topicArn;
  private Integer maxMonthsPerTrigger;
  private Integer retryQuietTimeMinutes;

  public Integer getRetryQuietTimeMinutes() {
    return retryQuietTimeMinutes;
  }

  public void setRetryQuietTimeMinutes(Integer retryQuietTimeMinutes) {
    this.retryQuietTimeMinutes = retryQuietTimeMinutes;
  }

  public Integer getMaxMonthsPerTrigger() {
    return maxMonthsPerTrigger;
  }

  public void setMaxMonthsPerTrigger(Integer maxMonthsPerTrigger) {
    this.maxMonthsPerTrigger = maxMonthsPerTrigger;
  }

  public String getDownloadBucket() {
    return downloadBucket;
  }

  public void setDownloadBucket(String downloadBucket) {
    this.downloadBucket = downloadBucket;
  }

  public String getDefaultStartDate() {
    return defaultStartDate;
  }

  public void setDefaultStartDate(String defaultStartDate) {
    this.defaultStartDate = defaultStartDate;
  }

  public String getTopicArn() {
    return topicArn;
  }

  public void setTopicArn(String topicArn) {
    this.topicArn = topicArn;
  }
}
