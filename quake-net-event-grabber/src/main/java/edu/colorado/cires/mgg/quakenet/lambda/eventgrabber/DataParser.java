package edu.colorado.cires.mgg.quakenet.lambda.eventgrabber;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.xml.namespace.QName;
import org.quakeml.xmlns.bed._1.Event;
import org.quakeml.xmlns.bed._1.EventParameters;
import org.quakeml.xmlns.quakeml._1.Quakeml;

public class DataParser {


  public static List<String> parseEventIds(Quakeml quakeml) {
    List<String> results = new ArrayList<>();
    EventParameters eventParameters = quakeml.getEventParameters();
    // 0..1 eventParameters
    if (eventParameters != null) {
      // 0..* events
      List<Event> events = getEvents(eventParameters);
      for (Event event : events) {
        getEventId(event).ifPresent(results::add);
      }
    }
    return results;
  }


  private static List<Event> getEvents(EventParameters eventParameters) {
    return getElements(Event.class, eventParameters::getCommentsAndEventsAndDescriptions);
  }

  private static Optional<String> getEventId(Event event) {
    Map<QName, String> attrs = event.getOtherAttributes();
    String datasource = null;
    String eventsource = null;
    String eventid = null;
    for (Entry<QName, String> entry : attrs.entrySet()) {
      if (entry.getKey().getLocalPart().equals("eventid")) {
        eventid = entry.getValue();
      }
      if (entry.getKey().getLocalPart().equals("eventsource")) {
        eventsource = entry.getValue();
      }
      if (entry.getKey().getLocalPart().equals("datasource")) {
        datasource = entry.getValue();
      }
      if (datasource != null && eventid != null) {
        break;
      }
    }
    if (eventsource != null && eventid != null) {
      return Optional.of(eventsource + eventid);
    }
    if (datasource != null && eventid != null) {
      return Optional.of(datasource + eventid);
    }
    return Optional.empty();
  }


  private static <C> List<C> getElements(Class<C> childClass, Supplier<List<?>> childGetter) {
    return childGetter.get().stream()
        .filter(o -> childClass.isAssignableFrom(o.getClass()))
        .map(childClass::cast)
        .collect(Collectors.toList());
  }

}
