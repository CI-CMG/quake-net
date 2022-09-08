package edu.colorado.cires.mgg.quakenet.lambda.eventgrabber;

public class EventGrabberProperties {

  private int pageSize;
  private long connectionTimeoutMs;
  private long requestTimeoutMs;
  private String topicArn;
  private String bucketName;
  private String baseUrl;
  private String minimumMagnitude;

  public String getMinimumMagnitude() {
    return minimumMagnitude;
  }

  public void setMinimumMagnitude(String minimumMagnitude) {
    this.minimumMagnitude = minimumMagnitude;
  }

  public int getPageSize() {
    return pageSize;
  }

  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
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

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }
}
