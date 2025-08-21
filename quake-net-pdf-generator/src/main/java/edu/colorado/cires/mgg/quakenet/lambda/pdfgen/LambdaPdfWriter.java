package edu.colorado.cires.mgg.quakenet.lambda.pdfgen;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Utilities;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import edu.colorado.cires.mgg.quakenet.message.ReportGenerateMessage;
import edu.colorado.cires.mgg.quakenet.model.QnCdi;
import edu.colorado.cires.mgg.quakenet.model.QnEvent;
import java.awt.Color;
import java.io.OutputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import software.amazon.awssdk.utils.StringUtils;

public class LambdaPdfWriter {

  private static String getRegion(List<QnEvent> events) {
    Set<String> regions = new LinkedHashSet<>();
    for (QnEvent event : events) {
      String region = null;
      if (event.getFlinnEngdahlRegion() != null && !event.getFlinnEngdahlRegion().isEmpty()) {
        region = event.getFlinnEngdahlRegion();
      }
      if (event.getEarthquakeName() != null && !event.getEarthquakeName().isEmpty()) {
        region = event.getEarthquakeName();
      }
      if (event.getRegionName() != null && !event.getRegionName().isEmpty()) {
        region = event.getRegionName();
      }
      if (StringUtils.isNotBlank(region)){
        regions.add(region);
      }
    }
    return String.join("\n ", regions);
  }

  private static String getFeltAt(QnCdi cdi) {
    StringBuilder sb = new StringBuilder();
    String country = "";
    String state = "";
    String city = "";
    String[] codeParts = cdi.getCode().split("::");
    if (codeParts.length == 3) {
      city = codeParts[0];
      state = codeParts[1];
      country = codeParts[2];
    } else if (cdi.getCode().matches("[0-9]+")) {
      city = cdi.getName();
      state = cdi.getState();
      country = "US";
    } else if (cdi.getCity() != null && !cdi.getCity().isEmpty()) {
      city = cdi.getCity();
    } else {
      city = cdi.getName();
    }

    if (state.isEmpty() && cdi.getState() != null && !cdi.getState().isEmpty()) {
      state = cdi.getState();
    }

    List<String> parts = new ArrayList<>();

    if (!city.isEmpty()) {
      parts.add(city);
    }
    if (!state.isEmpty()) {
      parts.add(state);
    }
    if (!country.isEmpty()) {
      parts.add(country);
    }

    sb.append(String.join(", ", parts)).append(" - CDI: ").append(String.format("%.0f", cdi.getCdi()));


    return sb.toString();
  }

  private static String getFeltAt(List<QnEvent> events) {
    Set<String> cdis = new LinkedHashSet<>();
    for(QnEvent event:events ) {
      event.getCdis().forEach(cdi -> {
        if (StringUtils.isNotBlank(getFeltAt(cdi))){
          cdis.add(getFeltAt(cdi));
        }
      });
    }
    return String.join("\n ", cdis);
  }

  private static String getComments(List<QnEvent> events) {
    Set<String> comments = new LinkedHashSet<>();
    for(QnEvent event:events) {
      StringBuilder sb = new StringBuilder();
      event.getComments().forEach(comment -> sb.append(comment).append("\n"));
      if (event.getFeltDescription() != null && !event.getFeltDescription().isEmpty()) {
        sb.append(event.getFeltDescription()).append("\n");
      }
      for (Entry<String, List<String>> entry : event.getOtherDescriptions().entrySet()) {
        for (String comment : entry.getValue()) {
          sb.append(entry.getKey()).append(": ").append(comment).append("\n");
        }
      }
      if (StringUtils.isNotBlank(sb.toString())){
        comments.add(sb.toString());
      }
    }
    return String.join("\n---\n", comments);
  }

  private static String getIds(List<QnEvent> events){
    Set<String> ids = new LinkedHashSet<>();
    for(QnEvent event:events){
      ids.add(event.getEventId());
    }
    return String.join("\n", ids);
  }

  private static String depthToKm(Double depthM) {
    if (depthM == null) {
      return "";
    }
    double depthKm = depthM / 1000D;
    return String.format("%.0f", depthKm);
  }

  private static double getSeconds(ZonedDateTime dt) {
    double sec = dt.getSecond();
    double part = ((double) dt.getNano()) / 1000000000D;
    return sec + part;
  }

  public static void writePdf(List<QnEvent> events, ReportGenerateMessage message, OutputStream outputStream) throws DocumentException {

    String title = String.format("Earthquakes %d-%02d", message.getYear(), message.getMonth());

    float marginBottom = Utilities.inchesToPoints(0.5f);
    float marginSides = Utilities.inchesToPoints(0.25f);
    Rectangle pageSize = PageSize.LEGAL.rotate();

    final int defaultFontFamily = Font.HELVETICA;
    final float defaultFontSize = 8f;
    final Font defaultFont = new Font(defaultFontFamily, defaultFontSize);

    Document document = new Document(
        pageSize,
        marginSides,
        marginSides,
        Utilities.inchesToPoints(0.25f),
        marginBottom
    );

    PdfWriter writer = PdfWriter.getInstance(document, outputStream);
    writer.setPageEvent(new HeaderFooterPageEvent(marginBottom, marginSides, pageSize.getWidth()));


    document.open();

    LinkedHashMap<String, Integer> columns = new LinkedHashMap<>();
    columns.put("Day", 3);
    columns.put("Hr", 3);
    columns.put("Min", 3);
    columns.put("Sec", 3);
    columns.put("Lat", 5);
    columns.put("Long", 5);
    columns.put("Depth (km)", 6);
    columns.put("Mag", 4);
    columns.put("Mag. Type", 6);
    columns.put("Region", 25);
    columns.put("Felt At", 30);
    columns.put("Other Info", 20);
    columns.put("IDs", 20);
    int[] widths = columns.values().stream().mapToInt(Integer::intValue).toArray();

    PdfPTable table = new PdfPTable(columns.size());
    table.setWidthPercentage(100f);
    table.setWidths(widths);

    PdfPCell header = new PdfPCell();
    header.setColspan(columns.size());
    header.setBackgroundColor(Color.BLACK);
    header.setPhrase(new Phrase(title, new Font(defaultFontFamily, defaultFontSize, Font.UNDEFINED, Color.WHITE)));
    header.setHorizontalAlignment(Element.ALIGN_CENTER);
    table.addCell(header);

    table.getDefaultCell().setBackgroundColor(Color.LIGHT_GRAY);

    columns.keySet().stream().map(t -> new Phrase(t, defaultFont)).forEach(table::addCell);

    table.getDefaultCell().setBackgroundColor(null);

    table.setHeaderRows(2);

    for (QnEvent event : events) {
      List<QnEvent> allEvents = new ArrayList<>();
      allEvents.add(event);
      allEvents.addAll(event.getChildren());
      ZonedDateTime dt = event.getOriginTime().atZone(ZoneId.of("UTC"));
      table.addCell(new Phrase(String.format("%02d", dt.getDayOfMonth()), defaultFont));
      table.addCell(new Phrase(String.format("%02d", dt.getHour()), defaultFont));
      table.addCell(new Phrase(String.format("%02d", dt.getMinute()), defaultFont));
      table.addCell(new Phrase(String.format("%.1f", getSeconds(dt)), defaultFont));
      table.addCell(new Phrase(String.format("%.3f", event.getLatitude()), defaultFont));
      table.addCell(new Phrase(String.format("%.3f", event.getLongitude()), defaultFont));
      table.addCell(new Phrase(depthToKm(event.getDepth()), defaultFont));
      table.addCell(new Phrase(event.getMagnitude() == null ? "" : String.format("%.1f", event.getMagnitude()), defaultFont));
      table.addCell(new Phrase(event.getMagnitudeType() == null ? "" : event.getMagnitudeType(), defaultFont));
      table.addCell(new Phrase(getRegion(allEvents), defaultFont));
      table.addCell(new Phrase(getFeltAt(allEvents), defaultFont));
      table.addCell(new Phrase(getComments(allEvents), defaultFont));
      table.addCell(new Phrase(getIds(allEvents), defaultFont));
    }

    document.add(table);

    document.close();


  }
}
