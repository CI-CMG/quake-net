package edu.colorado.cires.mgg.quakenet.lambda.eventdetailsgrabber;

public class EventDetailsGrabberProperties {


  private long connectionTimeoutMs;
  private long requestTimeoutMs;
  private String bucketName;
  private String topicArn;
  private String baseUrl;
  private String retryQueueUrl;
  private int retryDelaySeconds;

  public int getRetryDelaySeconds() {
    return retryDelaySeconds;
  }

  public void setRetryDelaySeconds(int retryDelaySeconds) {
    this.retryDelaySeconds = retryDelaySeconds;
  }

  public String getRetryQueueUrl() {
    return retryQueueUrl;
  }

  public void setRetryQueueUrl(String retryQueueUrl) {
    this.retryQueueUrl = retryQueueUrl;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public long getConnectionTimeoutMs() {
    return connectionTimeoutMs;
  }

  public void setConnectionTimeoutMs(long connectionTimeoutMs) {
    this.connectionTimeoutMs = connectionTimeoutMs;
  }

  public long getRequestTimeoutMs() {
    return requestTimeoutMs;
  }

  public void setRequestTimeoutMs(long requestTimeoutMs) {
    this.requestTimeoutMs = requestTimeoutMs;
  }

  public String getBucketName() {
    return bucketName;
  }

  public void setBucketName(String bucketName) {
    this.bucketName = bucketName;
  }

  public String getTopicArn() {
    return topicArn;
  }

  public void setTopicArn(String topicArn) {
    this.topicArn = topicArn;
  }
}
