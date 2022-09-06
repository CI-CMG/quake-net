package edu.colorado.cires.mgg.quakenet.lambda.eventdetailsgrabber;

import edu.colorado.cires.mgg.quakenet.message.EventDetailGrabberMessage;

public class TooManyRequestsException extends RuntimeException {

  private final EventDetailGrabberMessage message;

  public TooManyRequestsException(String messageStr, EventDetailGrabberMessage message) {
    super(messageStr);
    this.message = message;
  }

  public EventDetailGrabberMessage getEventMessage() {
    return message;
  }
}
