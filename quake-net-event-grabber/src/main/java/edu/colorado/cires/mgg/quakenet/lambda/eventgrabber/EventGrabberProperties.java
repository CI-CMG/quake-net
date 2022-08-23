package edu.colorado.cires.mgg.quakenet.lambda.eventgrabber;

public class EventGrabberProperties {

  private int pageSize;
  private long connectionTimeoutMs;
  private long requestTimeoutMs;

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
}
