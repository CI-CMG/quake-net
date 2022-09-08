package edu.colorado.cires.mgg.quakenet.lambda.eventdetailsgrabber;

import edu.colorado.cires.mgg.quakenet.message.EventDetailGrabberMessage;
import java.util.Optional;

public class EventDetailsGrabberExecutor {

  private final UsgsApiQueryier usgsApiQueryier;
  private final Notifier notifier;
  private final EventDetailsGrabberProperties properties;

  public EventDetailsGrabberExecutor(UsgsApiQueryier usgsApiQueryier, Notifier notifier, EventDetailsGrabberProperties properties) {
    this.usgsApiQueryier = usgsApiQueryier;
    this.notifier = notifier;
    this.properties = properties;
  }


  public void execute(EventDetailGrabberMessage message) {
    try {
      Optional<String> cdiUri = usgsApiQueryier.parseCdiUri(usgsApiQueryier.queryDetailsJson(message));
      cdiUri.ifPresent(uri -> usgsApiQueryier.queryCdi(message, uri));
      //This file must be written last.  It indicates that all data was retrieved.
      usgsApiQueryier.queryDetailsQuakeMl(message);
      notifier.notify(properties.getTopicArn(), message);
    } catch (TooManyRequestsException e) {
      notifier.retry(properties.getRetryQueueUrl(), message, properties.getRetryDelaySeconds());
    } catch (ApiAbortException e) {
      notifier.abort(properties.getAbortQueueUrl(), e.getEventMessage());
    }
  }
}
