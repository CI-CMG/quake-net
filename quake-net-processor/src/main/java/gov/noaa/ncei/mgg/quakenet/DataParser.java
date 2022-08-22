package gov.noaa.ncei.mgg.quakenet;

import gov.noaa.ncei.mgg.quakenet.domain.QnCdi;
import gov.noaa.ncei.mgg.quakenet.domain.QnEvent;
import gov.noaa.ncei.mgg.quakenet.geojson.CdiXml;
import gov.noaa.ncei.mgg.quakenet.geojson.Dyfi;
import gov.noaa.ncei.mgg.quakenet.geojson.DyfiContents;
import gov.noaa.ncei.mgg.quakenet.geojson.FeatureProperties;
import gov.noaa.ncei.mgg.quakenet.geojson.GeoJson;
import gov.noaa.ncei.mgg.quakenet.geojson.Products;
import gov.noaa.ncei.xmlns.cdidata.Cdidata;
import gov.noaa.ncei.xmlns.cdidata.Location;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import org.quakeml.xmlns.bed._1.Comment;
import org.quakeml.xmlns.bed._1.Event;
import org.quakeml.xmlns.bed._1.EventDescription;
import org.quakeml.xmlns.bed._1.EventDescriptionType;
import org.quakeml.xmlns.bed._1.EventParameters;
import org.quakeml.xmlns.bed._1.Magnitude;
import org.quakeml.xmlns.bed._1.Origin;
import org.quakeml.xmlns.bed._1.RealQuantity;
import org.quakeml.xmlns.bed._1.TimeQuantity;
import org.quakeml.xmlns.quakeml._1.Quakeml;

public class DataParser {

  public static void main(String[] args) throws JAXBException, FileNotFoundException {
    JAXBContext context = JAXBContext.newInstance(Quakeml.class);
    Quakeml quakeml = (Quakeml) context.createUnmarshaller()
        .unmarshal(new FileReader(
            "/Users/cslater/projects/quake-net/quake-net-processor/src/test/resources/201330_1374538994_C000IRGM_24_Long.quakeml_Verified.xml"));
    parseQuakeSummary(quakeml);
  }

  public static List<QnEvent> parseQuakeSummary(Quakeml quakeml) {

    List<QnEvent> results = new ArrayList<>();

    EventParameters eventParameters = quakeml.getEventParameters();

    // 0..1 eventParameters
    if (eventParameters != null) {

      // 0..* events
      List<Event> events = getEvents(eventParameters);
      for (Event event : events) {

        QnEvent qnEvent = new QnEvent();
        results.add(qnEvent);

        Optional<String> maybeEventId = getEventId(event);
        maybeEventId.ifPresent(qnEvent::setEventId);

//        // 0..* description
//        List<EventDescription> descriptions = getDescription(event);
//
//        Optional<String> earthquakeName = getEarthquakeName(descriptions);
//        earthquakeName.ifPresent(qnEvent::setEarthquakeName);
//
//        Optional<String> where = getWhere(descriptions);
//        where.ifPresent(qnEvent::setFlinnEngdahlRegion);
//
//        Optional<String> regionName = getRegionName(descriptions);
//        regionName.ifPresent(qnEvent::setRegionName);
//
//        Optional<String> feltDescription = getFeltReport(descriptions);
//        feltDescription.ifPresent(qnEvent::setFeltDescription);
//
//        setDescriptions(
//            qnEvent,
//            descriptions,
//            new HashSet<>(Arrays.asList(
//                EventDescriptionType.FLINN_ENGDAHL_REGION,
//                EventDescriptionType.EARTHQUAKE_NAME,
//                EventDescriptionType.REGION_NAME,
//                EventDescriptionType.FELT_REPORT
//            )));
//
//        // 0..* comment
//        List<Comment> comments = getComments(event);
//        for (Comment comment : comments) {
//          // 1..1 text
//          Optional<String> maybeText = getCommentText(comment);
//          maybeText.ifPresent(qnEvent::addComment);
//        }

        // 0..* magnitude
        // When parsing from the API there should only be one Magnitude
        Optional<Magnitude> maybeMagnitude = getMagnitude(event);

        // 0..1 type
        Optional<String> maybeMagnitudeType = maybeMagnitude.flatMap(DataParser::getMagnitudeType);
        maybeMagnitudeType.ifPresent(qnEvent::setMagnitudeType);

        // 1..1 mag
        Optional<RealQuantity> maybeMag = maybeMagnitude.flatMap(DataParser::getMag);
        // 1..1 value
        Optional<Double> maybeMagValue = maybeMag.flatMap(DataParser::getRealQuantityValues);
        maybeMagValue.ifPresent(qnEvent::setMagnitude);

        // 0..* origin
        // When parsing from the API there should only be one origin
        Optional<Origin> maybeOrigin = getOrigin(event);

        // 1..1
        Optional<TimeQuantity> maybeTime = maybeOrigin.flatMap(DataParser::getTime);
        // 1..1
        Optional<Instant> maybeTimeValue = maybeTime.flatMap(DataParser::getTimeValue);
        maybeTimeValue.ifPresent(qnEvent::setOriginTime);

        // 1..1
        Optional<RealQuantity> maybeLatitude = maybeOrigin.flatMap(DataParser::getLatitude);
        // 1..1
        Optional<Double> maybeLatitudeValue = maybeLatitude.flatMap(DataParser::getRealQuantityValues);
        maybeLatitudeValue.ifPresent(qnEvent::setLatitude);

        // 1..1
        Optional<RealQuantity> maybeLongitude = maybeOrigin.flatMap(DataParser::getLongitude);
        // 1..1
        Optional<Double> maybeLongitudeValue = maybeLongitude.flatMap(DataParser::getRealQuantityValues);
        maybeLongitudeValue.ifPresent(qnEvent::setLongitude);

        // 0..1
        Optional<RealQuantity> maybeDepth = maybeOrigin.flatMap(DataParser::getDepth);
        // 0..1
        Optional<Double> maybeDepthValue = maybeDepth.flatMap(DataParser::getRealQuantityValues);
        maybeDepthValue.ifPresent(qnEvent::setDepth);

      }
    }
    return results;
  }

  public static void parseCdi(Cdidata cdidata, QnEvent qnEvent) {
//    qnEvent.getCdis()
//    for(Location location : cdidata.getCdi().getLocations()) {
//
//    }
  }

  public static void parseEnrichmentDetails(Quakeml quakeml, QnEvent qnEvent) {

    EventParameters eventParameters = quakeml.getEventParameters();

    // 0..1 eventParameters
    if (eventParameters != null) {

      // There should only be one from the detail API call
      Event event = getEvents(eventParameters).get(0);

      // 0..* description
      List<EventDescription> descriptions = getDescription(event);

      Optional<String> earthquakeName = getEarthquakeName(descriptions);
      earthquakeName.ifPresent(qnEvent::setEarthquakeName);

      Optional<String> where = getWhere(descriptions);
      where.ifPresent(qnEvent::setFlinnEngdahlRegion);

      Optional<String> regionName = getRegionName(descriptions);
      regionName.ifPresent(qnEvent::setRegionName);

      Optional<String> feltDescription = getFeltReport(descriptions);
      feltDescription.ifPresent(qnEvent::setFeltDescription);

      setDescriptions(
          qnEvent,
          descriptions,
          new HashSet<>(Arrays.asList(
              EventDescriptionType.FLINN_ENGDAHL_REGION,
              EventDescriptionType.EARTHQUAKE_NAME,
              EventDescriptionType.REGION_NAME,
              EventDescriptionType.FELT_REPORT
          )));

      // 0..* comment
      List<Comment> comments = getComments(event);
      for (Comment comment : comments) {
        // 1..1 text
        Optional<String> maybeText = getCommentText(comment);
        maybeText.ifPresent(qnEvent::addComment);
      }

    }

  }

  public static void parseGeoJsonEnrichmentDetails(GeoJson geoJson, QnEvent qnEvent) {

    FeatureProperties properties = geoJson.getProperties();
    if(properties != null) {
      Products products = properties.getProducts();
      if (products != null) {
        List<Dyfi> dyfis = products.getDyfi();
        if(!dyfis.isEmpty()) {
          Dyfi dyfi = dyfis.get(0);
          DyfiContents contents = dyfi.getContents();
          if(contents != null) {
            //TODO check as back up?
//            CdiXml cdiGeoXml = contents.getCdiGeoXml();

            CdiXml cdiZipXml = contents.getCdiZipXml();
            if(cdiZipXml != null) {
              String url = cdiZipXml.getUrl();
              if(url != null) {
                try {
                  ApiRetreiver.queryCdi(url, cdidata -> {
                    if(cdidata.getCdi() != null && cdidata.getCdi().getLocations() != null) {
                      for (Location location : cdidata.getCdi().getLocations()) {
                        QnCdi cdi = new QnCdi();
                        cdi.setCdi(location.getCdi());
                        cdi.setNumResp(location.getNresp());
                        cdi.setDistKm(location.getDist());
                        cdi.setLatitude(location.getLat());
                        cdi.setLongitude(location.getLon());
                        cdi.setName(location.getName());
                        cdi.setState(location.getState());
                        cdi.setCode(location.getLocationName());
                        qnEvent.addCdi(cdi);
                      }
                    }
                  });
                } catch (IOException | URISyntaxException | JAXBException e) {
                  //TODO
                  System.out.println("Unable to parse CDI data");
                  e.printStackTrace();
                }
              }
            }
          }
        }
      }
    }

  }

  private static List<Event> getEvents(EventParameters eventParameters) {
    return getElements(Event.class, eventParameters::getCommentsAndEventsAndDescriptions);
  }

  private static Optional<Instant> getTimeValue(TimeQuantity time) {
    return getSingleElement(Instant.class, () -> new ArrayList<>(time.getValuesAndUncertaintiesAndLowerUncertainties()), "value");
  }

  private static Optional<TimeQuantity> getTime(Origin origin) {
    return getSingleElement(TimeQuantity.class, origin::getCompositeTimesAndCommentsAndOriginUncertainties, "time");
  }

  private static List<Comment> getComments(Event event) {
    return getElements(Comment.class, event::getDescriptionsAndCommentsAndFocalMechanisms, "comment");
  }

  //When parsing from the API, there should only be one magnitude
  private static Optional<Magnitude> getMagnitude(Event event) {
    return getSingleElement(Magnitude.class, event::getDescriptionsAndCommentsAndFocalMechanisms, "magnitude");
  }

  private static List<EventDescription> getDescription(Event event) {
    return getElements(EventDescription.class, event::getDescriptionsAndCommentsAndFocalMechanisms, "description");
  }

  private static Optional<String> getDescriptionText(EventDescription description) {
    return getSingleElement(String.class, description::getTextsAndTypes);
  }

  private static Optional<EventDescriptionType> getDescriptionType(EventDescription description) {
    return getSingleElement(EventDescriptionType.class, description::getTextsAndTypes);
  }

  private static Optional<RealQuantity> getMag(Magnitude magnitude) {
    return getSingleElement(RealQuantity.class, magnitude::getCommentsAndStationMagnitudeContributionsAndMags, "mag");
  }

  private static Optional<RealQuantity> getLatitude(Origin origin) {
    return getSingleElement(RealQuantity.class, origin::getCompositeTimesAndCommentsAndOriginUncertainties, "latitude");
  }

  private static Optional<RealQuantity> getLongitude(Origin origin) {
    return getSingleElement(RealQuantity.class, origin::getCompositeTimesAndCommentsAndOriginUncertainties, "longitude");
  }

  private static Optional<RealQuantity> getDepth(Origin origin) {
    return getSingleElement(RealQuantity.class, origin::getCompositeTimesAndCommentsAndOriginUncertainties, "depth");
  }

  private static Optional<String> getEventId(Event event) {
    Map<QName, String> attrs = event.getOtherAttributes();
    String datasource = null;
    String eventid = null;
    for (Entry<QName, String> entry : attrs.entrySet()) {
      if (entry.getKey().getLocalPart().equals("eventid")) {
        eventid = entry.getValue();
      }
      if (entry.getKey().getLocalPart().equals("datasource")) {
        datasource = entry.getValue();
      }
      if (datasource != null && eventid != null) {
        break;
      }
    }
    if (datasource != null && eventid != null) {
      return Optional.of(datasource + eventid);
    }
    return Optional.empty();
  }

  private static Optional<String> getMagnitudeType(Magnitude magnitude) {
    return getSingleElement(String.class, magnitude::getCommentsAndStationMagnitudeContributionsAndMags, "type");
  }

  private static Optional<Double> getRealQuantityValues(RealQuantity realQuantity) {
    return getSingleElement(Double.class, () -> new ArrayList<>(realQuantity.getValuesAndUncertaintiesAndLowerUncertainties()), "value");
  }

  private static Optional<String> getCommentText(Comment comment) {
    return getSingleElement(String.class, comment::getTextsAndCreationInfos);
  }

  private static <C> Optional<C> getSingleElement(Class<C> childClass, Supplier<List<JAXBElement<?>>> childGetter, String name) {
    List<C> list = getElements(childClass, childGetter, name);
    return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
  }

  private static <C> List<C> getElements(Class<C> childClass, Supplier<List<JAXBElement<?>>> childGetter, String name) {
    return childGetter.get().stream()
        .filter(o -> o.getName().getLocalPart().equals(name))
        .map(o -> childClass.cast(o.getValue()))
        .collect(Collectors.toList());
  }

  private static <C> Optional<C> getSingleElement(Class<C> childClass, Supplier<List<?>> childGetter) {
    List<C> list = getElements(childClass, childGetter);
    return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
  }

  private static <C> List<C> getElements(Class<C> childClass, Supplier<List<?>> childGetter) {
    return childGetter.get().stream()
        .filter(o -> childClass.isAssignableFrom(o.getClass()))
        .map(childClass::cast)
        .collect(Collectors.toList());
  }

  //When parsing from the API, there should only be one origin
  private static Optional<Origin> getOrigin(Event event) {
    return getSingleElement(Origin.class, event::getDescriptionsAndCommentsAndFocalMechanisms, "origin");
  }

  private static Optional<String> getWhere(List<EventDescription> descriptions) {
    return getDescriptionForType(descriptions, EventDescriptionType.FLINN_ENGDAHL_REGION);
  }

  private static Optional<String> getRegionName(List<EventDescription> descriptions) {
    return getDescriptionForType(descriptions, EventDescriptionType.REGION_NAME);
  }

  private static Optional<String> getEarthquakeName(List<EventDescription> descriptions) {
    return getDescriptionForType(descriptions, EventDescriptionType.EARTHQUAKE_NAME);
  }

  private static Optional<String> getFeltReport(List<EventDescription> descriptions) {
    return getDescriptionForType(descriptions, EventDescriptionType.FELT_REPORT);
  }

  private static Optional<String> getDescriptionForType(List<EventDescription> descriptions, EventDescriptionType type) {
    String desc = null;
    for (EventDescription description : descriptions) {
      // 1..1 text
      Optional<String> maybeText = getDescriptionText(description);
      // 0..1 type
      Optional<EventDescriptionType> maybeType = getDescriptionType(description);
      if (maybeText.isPresent()) {
        if (maybeType.isPresent()) {
          if (maybeType.get() == type) {
            desc = maybeText.get();
            break;
          }
        }
      }
    }
    return Optional.ofNullable(desc);
  }

  private static void setDescriptions(QnEvent event, List<EventDescription> descriptions, Set<EventDescriptionType> excluded) {
    String desc = null;
    for (EventDescription description : descriptions) {
      // 1..1 text
      Optional<String> maybeText = getDescriptionText(description);
      if (maybeText.isPresent()) {
        // 0..1 type
        EventDescriptionType type = getDescriptionType(description).orElse(null);
        if (type == null) {
          event.addOtherDescription(null, maybeText.get());
        } else if (!excluded.contains(type)) {
          event.addOtherDescription(type.value(), maybeText.get());
        }

      }
    }
  }
}
