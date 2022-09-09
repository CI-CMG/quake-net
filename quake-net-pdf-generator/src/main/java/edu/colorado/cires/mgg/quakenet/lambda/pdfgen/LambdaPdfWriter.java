package edu.colorado.cires.mgg.quakenet.lambda.pdfgen;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Utilities;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import edu.colorado.cires.mgg.quakenet.message.ReportGenerateMessage;
import edu.colorado.cires.mgg.quakenet.model.QnCdi;
import edu.colorado.cires.mgg.quakenet.model.QnEvent;
import java.io.OutputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public class LambdaPdfWriter {

  private static String getRegion(QnEvent event) {
    if (event.getFlinnEngdahlRegion() != null && !event.getFlinnEngdahlRegion().isEmpty()) {
      return event.getFlinnEngdahlRegion();
    }
    if (event.getEarthquakeName() != null && !event.getEarthquakeName().isEmpty()) {
      return event.getEarthquakeName();
    }
    if (event.getRegionName() != null && !event.getRegionName().isEmpty()) {
      return event.getRegionName();
    }
    return "";
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

  private static String getFeltAt(QnEvent event) {
    StringBuilder sb = new StringBuilder();
    event.getCdis().forEach(cdi -> {
      sb.append(getFeltAt(cdi)).append("\n");
    });
    return sb.toString();
  }

  private static String getComments(QnEvent event) {
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
    return sb.toString();
  }

  private static String depthToKm(Double depthM) {
    if (depthM == null) {
      return "";
    }
    double depthKm = depthM / 1000D;
    return String.format("%.0f", depthKm);
  }

  public static void writePdf(List<QnEvent> events, ReportGenerateMessage message, OutputStream outputStream) throws DocumentException {

    String title = String.format("Earthquakes %d-%02d", message.getYear(), message.getMonth());

    float marginBottom = Utilities.inchesToPoints(0.5f);
    float marginSides = Utilities.inchesToPoints(0.25f);
    Rectangle pageSize = PageSize.LEGAL.rotate();

    final FontFamily defaultFontFamily = FontFamily.HELVETICA;
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
    int[] widths = columns.values().stream().mapToInt(Integer::intValue).toArray();

    PdfPTable table = new PdfPTable(columns.size());
    table.setWidthPercentage(100f);
    table.setWidths(widths);

    PdfPCell header = new PdfPCell();
    header.setColspan(columns.size());
    header.setBackgroundColor(BaseColor.BLACK);
    header.setPhrase(new Phrase(title, new Font(defaultFontFamily, defaultFontSize, Font.UNDEFINED, BaseColor.WHITE)));
    header.setHorizontalAlignment(Element.ALIGN_CENTER);
    table.addCell(header);

    table.getDefaultCell().setBackgroundColor(BaseColor.LIGHT_GRAY);

    columns.keySet().stream().map(t -> new Phrase(t, defaultFont)).forEach(table::addCell);

    table.getDefaultCell().setBackgroundColor(null);

    table.setHeaderRows(2);

    for (QnEvent event : events) {
      ZonedDateTime dt = event.getOriginTime().atZone(ZoneId.of("UTC"));
      table.addCell(new Phrase(String.format("%02d", dt.getDayOfMonth()), defaultFont));
      table.addCell(new Phrase(String.format("%02d", dt.getHour()), defaultFont));
      table.addCell(new Phrase(String.format("%02d", dt.getMinute()), defaultFont));
      table.addCell(new Phrase(String.format("%02d", dt.getSecond()), defaultFont));
      table.addCell(new Phrase(String.format("%.3f", event.getLatitude()), defaultFont));
      table.addCell(new Phrase(String.format("%.3f", event.getLongitude()), defaultFont));
      table.addCell(new Phrase(depthToKm(event.getDepth()), defaultFont));
      table.addCell(new Phrase(event.getMagnitude() == null ? "" : String.format("%.2f", event.getMagnitude()), defaultFont));
      table.addCell(new Phrase(event.getMagnitudeType() == null ? "" : event.getMagnitudeType(), defaultFont));
      table.addCell(new Phrase(getRegion(event), defaultFont));
      table.addCell(new Phrase(getFeltAt(event), defaultFont));
      table.addCell(new Phrase(getComments(event), defaultFont));
    }

    document.add(table);

    document.close();


  }
}
