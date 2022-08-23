package edu.colorado.cires.mgg.quakenet.lambda.initiator;

public class InitiatorProperties {

  private String downloadBucket;
  private String defaultStartDate;
  private long connectionTimeoutMs;
  private long requestTimeoutMs;

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
}
