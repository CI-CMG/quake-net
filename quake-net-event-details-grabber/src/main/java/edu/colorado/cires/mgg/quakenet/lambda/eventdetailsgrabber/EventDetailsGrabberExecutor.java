package edu.colorado.cires.mgg.quakenet.lambda.eventdetailsgrabber;

import edu.colorado.cires.mgg.quakenet.message.EventDetailGrabberMessage;
import java.time.LocalDate;
import java.util.Optional;

public class EventDetailsGrabberExecutor {

  private final UsgsApiQueryier usgsApiQueryier;
  private final Notifier notifier;

  public EventDetailsGrabberExecutor(UsgsApiQueryier usgsApiQueryier, Notifier notifier) {
    this.usgsApiQueryier = usgsApiQueryier;
    this.notifier = notifier;
  }


  public void execute(EventDetailGrabberMessage message) {
    Optional<String> cdiUri = usgsApiQueryier.parseCdiUri(usgsApiQueryier.queryDetailsJson(message));
    cdiUri.ifPresent(uri -> usgsApiQueryier.queryCdi(message, uri));
    //This file must be written last.  It indicates that all data was retrieved.
    usgsApiQueryier.queryDetailsQuakeMl(message);

    notifier.notify(message.getEventId(), LocalDate.parse(message.getDate()));
  }
}
