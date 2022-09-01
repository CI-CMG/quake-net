package edu.colorado.cires.mgg.quakenet.lambda.initiator;

public class InitiatorProperties {

  private String downloadBucket;
  private String defaultStartDate;
  private String topicArn;
  private Integer maxDatesPerTrigger;

  public Integer getMaxDatesPerTrigger() {
    return maxDatesPerTrigger;
  }

  public void setMaxDatesPerTrigger(Integer maxDatesPerTrigger) {
    this.maxDatesPerTrigger = maxDatesPerTrigger;
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
