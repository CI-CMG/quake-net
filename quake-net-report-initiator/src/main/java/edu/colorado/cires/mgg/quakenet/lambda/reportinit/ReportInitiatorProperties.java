package edu.colorado.cires.mgg.quakenet.lambda.reportinit;

public class ReportInitiatorProperties {

  private String bucketName;
  private String topicArn;

  public String getTopicArn() {
    return topicArn;
  }

  public void setTopicArn(String topicArn) {
    this.topicArn = topicArn;
  }

  public String getBucketName() {
    return bucketName;
  }

  public void setBucketName(String bucketName) {
    this.bucketName = bucketName;
  }
}
