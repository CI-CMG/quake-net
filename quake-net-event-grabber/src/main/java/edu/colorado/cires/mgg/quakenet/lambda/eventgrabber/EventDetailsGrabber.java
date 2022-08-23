package edu.colorado.cires.mgg.quakenet.lambda.eventgrabber;

import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.sns.SnsClient;

public class EventDetailsGrabber {

  private final SnsClient snsClient;
  private final ObjectMapper objectMapper;

  public EventDetailsGrabber(SnsClient snsClient, ObjectMapper objectMapper, EventGrabberProperties properties) {
    this.snsClient = snsClient;
    this.objectMapper = objectMapper;
  }

  public void grabDetails(String eventId) {



  }

}
