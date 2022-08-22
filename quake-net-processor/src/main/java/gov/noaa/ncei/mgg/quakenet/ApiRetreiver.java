package gov.noaa.ncei.mgg.quakenet;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.DocumentException;
import gov.noaa.ncei.mgg.quakenet.domain.QnEvent;
import gov.noaa.ncei.mgg.quakenet.geojson.GeoJson;
import gov.noaa.ncei.xmlns.cdidata.Cdidata;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.quakeml.xmlns.quakeml._1.Quakeml;

public class ApiRetreiver {

  private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));

  public static void main(String[] args) throws JAXBException, IOException, URISyntaxException, DocumentException {
    ApiParameters apiParameters = new ApiParameters();
    apiParameters.setStartTime(DTF.parse("2022-08-01 00:00:00", Instant::from));
    apiParameters.setEndTime(DTF.parse("2022-08-02 00:00:00", Instant::from));

    List<QnEvent> eventList = new ArrayList<>();

    queryEventSummaryApi(apiParameters, events -> {
      events.forEach(event -> {
        try {
          queryEventDetailApi(event);
          queryEventDetailGeoJsonApi(event);
          System.out.println(event);
          eventList.add(event);
        } catch (URISyntaxException | JAXBException | IOException e) {
          throw new RuntimeException(e);
        }
      });
    });

    DataWriter.writePdf(eventList, Paths.get("2022-08-quake-net.pdf"), "August 2022");
  }

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
      .disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  private static final int PAGE_SIZE = 200;

  public static void queryEventSummaryApi(ApiParameters apiParameters, Consumer<List<QnEvent>> eventConsumer)
      throws IOException, URISyntaxException, JAXBException {
    int resultCount = -1;
    int page = 0;
    while (resultCount != 0) {

      resultCount = 0;

      try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
        int offset = page * PAGE_SIZE + 1;

        URI uri = new URIBuilder("https://earthquake.usgs.gov/fdsnws/event/1/query")
            .addParameter("format", "quakeml")
            .addParameter("starttime", apiParameters.getStartTime().toString())
            .addParameter("endtime", apiParameters.getEndTime().toString())
            .addParameter("includeallorigins", "false")
            .addParameter("includeallmagnitudes", "false")
            .addParameter("orderby", "time-asc")
            .addParameter("limit", Integer.toString(PAGE_SIZE))
            .addParameter("offset", Integer.toString(offset))
            .build();

        page++;

        List<QnEvent> events = new ArrayList<>(0);

        HttpGet httpGet = new HttpGet(uri.toString());
        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
          if (response1.getCode() == 200) {
            HttpEntity entity1 = response1.getEntity();
            try {
              try (InputStream in = entity1.getContent()) {
                JAXBContext context = JAXBContext.newInstance(Quakeml.class);
                Quakeml quakeml = (Quakeml) context.createUnmarshaller().unmarshal(in);
                events = DataParser.parseQuakeSummary(quakeml);
              }
            } finally {
              EntityUtils.consume(entity1);
            }
          } else if (response1.getCode() == 204) {
            //TODO
            System.out.println(response1.getCode() + " " + response1.getReasonPhrase());
            System.out.println("No More Results");
          } else {
            //TODO
            System.out.println(response1.getCode() + " " + response1.getReasonPhrase());
          }

        }

        resultCount = events.size();
        if (resultCount > 0) {
          eventConsumer.accept(events);
        }

      }
    }
  }

  public static void queryEventDetailApi(QnEvent event) throws IOException, URISyntaxException, JAXBException {

    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

      URI uri = new URIBuilder("https://earthquake.usgs.gov/fdsnws/event/1/query")
          .addParameter("format", "quakeml")
          .addParameter("eventid", event.getEventId())
          .build();

      HttpGet httpGet = new HttpGet(uri.toString());
      try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
        if (response1.getCode() == 200) {
          HttpEntity entity1 = response1.getEntity();
          try {
            try (InputStream in = entity1.getContent()) {
              JAXBContext context = JAXBContext.newInstance(Quakeml.class);
              String xml = IOUtils.toString(in, StandardCharsets.UTF_8);
              System.out.println(xml);
              Quakeml quakeml = (Quakeml) context.createUnmarshaller().unmarshal(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
              DataParser.parseEnrichmentDetails(quakeml, event);
            }
          } finally {
            EntityUtils.consume(entity1);
          }
        } else if (response1.getCode() == 204) {
          //TODO
          System.out.println(response1.getCode() + " " + response1.getReasonPhrase());
          System.out.println(event);
          System.out.println("^^^^^ No More Results");
        } else {
          //TODO
          System.out.println(response1.getCode() + " " + response1.getReasonPhrase());
        }

      }

    }

  }

  public static void queryCdi(String url, Consumer<Cdidata> eventConsumer) throws IOException, URISyntaxException, JAXBException {

    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

      System.out.println(url);
      HttpGet httpGet = new HttpGet(url);
      try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
        if (response1.getCode() == 200) {
          HttpEntity entity1 = response1.getEntity();
          try {
            try (InputStream in = entity1.getContent()) {
              String xml = IOUtils.toString(in, StandardCharsets.UTF_8);
              System.out.println(xml);
              JAXBContext context = JAXBContext.newInstance(Cdidata.class);
              Cdidata cdidata = (Cdidata) context.createUnmarshaller().unmarshal(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

              eventConsumer.accept(cdidata);
            }
          } finally {
            EntityUtils.consume(entity1);
          }
        } else {
          //TODO
          System.out.println(response1.getCode() + " " + response1.getReasonPhrase());
        }

      }

    }

  }


  public static void queryEventDetailGeoJsonApi(QnEvent event) throws IOException, URISyntaxException {

    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

      URI uri = new URIBuilder("https://earthquake.usgs.gov/fdsnws/event/1/query")
          .addParameter("format", "geojson")
          .addParameter("eventid", event.getEventId())
          .build();

      HttpGet httpGet = new HttpGet(uri.toString());
      try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
        if (response1.getCode() == 200) {
          HttpEntity entity1 = response1.getEntity();
          try {
            try (InputStream in = entity1.getContent()) {
              String json = IOUtils.toString(in, StandardCharsets.UTF_8);
              System.out.println(json);
              GeoJson geoJson = OBJECT_MAPPER.readValue(json, GeoJson.class);
              DataParser.parseGeoJsonEnrichmentDetails(geoJson, event);
            }
          } finally {
            EntityUtils.consume(entity1);
          }
        } else if (response1.getCode() == 204) {
          //TODO
          System.out.println(response1.getCode() + " " + response1.getReasonPhrase());
          System.out.println(event);
          System.out.println("^^^^^ No More Results");
        } else {
          //TODO
          System.out.println(response1.getCode() + " " + response1.getReasonPhrase());
        }

      }

    }

  }

}
