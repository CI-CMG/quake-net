package edu.colorado.cires.mgg.quakenet.lambda.initiator;

import edu.colorado.cires.mgg.quakenet.model.QnEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.quakeml.xmlns.quakeml._1.Quakeml;

public class UsgsApiQueryier {

  private static final int PAGE_SIZE = 200;

  public static void query(QueryRange queryRange, Consumer<List<QnEvent>> eventConsumer) throws IOException, URISyntaxException, JAXBException {
    int resultCount = -1;
    int page = 0;
    while (resultCount != 0) {

      resultCount = 0;

      try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
        int offset = page * PAGE_SIZE + 1;

        URI uri = new URIBuilder("https://earthquake.usgs.gov/fdsnws/event/1/query")
            .addParameter("format", "quakeml")
            .addParameter("starttime", queryRange.getStart().atStartOfDay(ZoneId.of("UTC")).toInstant().toString())
            .addParameter("endtime", queryRange.getEnd().plusDays(1L).atStartOfDay(ZoneId.of("UTC")).toInstant().toString())
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


}
