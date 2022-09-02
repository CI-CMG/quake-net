package edu.colorado.cires.mgg.quakenet.lambda.pdfgen;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Utilities;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import edu.colorado.cires.mgg.quakenet.message.ReportGenerateMessage;
import edu.colorado.cires.mgg.quakenet.model.QnCdi;
import edu.colorado.cires.mgg.quakenet.model.QnEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public class PdfWriter {

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

    sb.append(String.join(", ", parts))
        .append(" - CDI: ").append(cdi.getCdi())
        .append(" Dist (km): ").append(cdi.getDistKm())
        .append(" Lat: ").append(cdi.getLatitude())
        .append(" Lon: ").append(cdi.getLongitude())
        .append(" Resp: ").append(cdi.getNumResp());

    return sb.toString();
  }

  private static String getFeltAt(QnEvent event) {
    StringBuilder sb = new StringBuilder();
    if (event.getFeltDescription() != null && !event.getFeltDescription().isEmpty()) {
      sb.append(event.getFeltDescription()).append("\n");
    }
    event.getCdis().forEach(cdi -> {
      sb.append(getFeltAt(cdi)).append("\n");
    });
    return sb.toString();
  }

  private static String getComments(QnEvent event) {
    StringBuilder sb = new StringBuilder();
    event.getComments().forEach(comment -> sb.append(comment).append("\n"));
    for (Entry<String, List<String>> entry : event.getOtherDescriptions().entrySet()) {
      for (String comment : entry.getValue()) {
        sb.append(entry.getKey()).append(": ").append(comment).append("\n");
      }
    }
    return sb.toString();
  }

  public static void writePdf(List<QnEvent> events, ReportGenerateMessage message, OutputStream outputStream) throws DocumentException {

    String title = "Earthquakes " + message.getYear() + "-" + message.getMonth();
//    try () {

    Document document = new Document(
        PageSize.LEGAL.rotate(),
        Utilities.inchesToPoints(0.25f),
        Utilities.inchesToPoints(0.25f),
        Utilities.inchesToPoints(0.25f),
        Utilities.inchesToPoints(0.25f)
    );

//      try {

    com.itextpdf.text.pdf.PdfWriter.getInstance(document, outputStream);
    document.open();
//    Font font = FontFactory.getFont(FontFactory.COURIER, 12, BaseColor.BLACK);
//    Chunk chunk = new Chunk(title, font);
//    document.add(chunk);

    LinkedHashMap<String, Integer> columns = new LinkedHashMap<>();
    columns.put("ID", 15);
    columns.put("Date / Time", 15);
    columns.put("Latitude", 10);
    columns.put("Longitude", 10);
    columns.put("Depth (m)", 10);
    columns.put("Magnitude", 10);
    columns.put("Mag. Type", 10);
    columns.put("Region", 20);
    columns.put("Felt At", 30);
    columns.put("Other Info", 30);
    int[] widths = columns.values().stream().mapToInt(Integer::intValue).toArray();

    PdfPTable table = new PdfPTable(columns.size());
    table.setWidthPercentage(100f);
    table.setWidths(widths);

    PdfPCell header = new PdfPCell();
    header.setColspan(columns.size());
    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
    header.setPhrase(new Phrase(title));
    header.setHorizontalAlignment(Element.ALIGN_CENTER);
    table.addCell(header);

    table.getDefaultCell().setBackgroundColor(BaseColor.LIGHT_GRAY);

    columns.keySet().forEach(table::addCell);

    table.getDefaultCell().setBackgroundColor(null);

    table.setHeaderRows(2);

    for (QnEvent event : events) {
      table.addCell(event.getEventId());
      table.addCell(event.getOriginTime().toString());
      table.addCell(event.getLatitude().toString());
      table.addCell(event.getLongitude().toString());
      table.addCell(event.getDepth() == null ? "" : event.getDepth().toString());
      table.addCell(event.getMagnitude() == null ? "" : event.getMagnitude().toString());
      table.addCell(event.getMagnitudeType() == null ? "" : event.getMagnitudeType());
      table.addCell(getRegion(event));
      table.addCell(getFeltAt(event));
      table.addCell(getComments(event));
    }

    document.add(table);


        /*
            Path path = Paths.get(ClassLoader.getSystemResource("Java_logo.png").toURI());
    Image img = Image.getInstance(path.toAbsolutePath().toString());
    img.scalePercent(10);

    PdfPCell imageCell = new PdfPCell(img);
    table.addCell(imageCell);

    PdfPCell horizontalAlignCell = new PdfPCell(new Phrase("row 2, col 2"));
    horizontalAlignCell.setHorizontalAlignment(Element.ALIGN_CENTER);
    table.addCell(horizontalAlignCell);

    PdfPCell verticalAlignCell = new PdfPCell(new Phrase("row 2, col 3"));
    verticalAlignCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
    table.addCell(verticalAlignCell);
         */

//      } finally {
    document.close();
//      }

//    }

  }
}
