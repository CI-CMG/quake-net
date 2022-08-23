package edu.colorado.cires.mgg.quakenet.lambda.initiator;

import software.amazon.awssdk.services.s3.S3Client;

public class EventGrabberProcessor {

  private final InitiatorProperties eventGrabberProperties;
  private final S3Client s3;

  public EventGrabberProcessor(InitiatorProperties eventGrabberProperties, S3Client s3) {
    this.eventGrabberProperties = eventGrabberProperties;
    this.s3 = s3;
  }

  public void process() {


  }
}
