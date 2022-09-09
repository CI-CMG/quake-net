package edu.colorado.cires.mgg.quakenet.lambda.pdfgen;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class HeaderFooterPageEvent extends PdfPageEventHelper {

  private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final float marginBottom;
  private final float marginSides;
  private final float docWidth;

  private PdfTemplate t;
  private Image total;
  private String created;

  public HeaderFooterPageEvent(float marginBottom, float marginSides, float docWidth) {
    this.marginBottom = marginBottom;
    this.marginSides = marginSides;
    this.docWidth = docWidth;
  }

  @Override
  public void onOpenDocument(PdfWriter writer, Document document) {
    t = writer.getDirectContent().createTemplate(30, 16);
    try {
      total = Image.getInstance(t);
      total.setRole(PdfName.ARTIFACT);
    } catch (DocumentException de) {
      throw new ExceptionConverter(de);
    }
    created = DTF.format(Instant.now().atZone(ZoneId.of("UTC")));
  }

  @Override
  public void onEndPage(PdfWriter writer, Document document) {
    addFooter(writer);
  }

  private void addFooter(PdfWriter writer){
    PdfPTable footer = new PdfPTable(3);
    try {
      // set defaults
      footer.setWidths(new int[]{24, 2, 2});
      footer.setTotalWidth(docWidth - (marginSides * 2));
      footer.setLockedWidth(true);
      footer.getDefaultCell().setFixedHeight(marginBottom / 2);
      footer.getDefaultCell().setBorder(Rectangle.NO_BORDER);

      // add generated
      footer.addCell(new Phrase(String.format("Report Generated %s UTC", created), new Font(Font.FontFamily.HELVETICA, 8)));

      // add current page count
      footer.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
      footer.addCell(new Phrase(String.format("Page %d of", writer.getPageNumber()), new Font(Font.FontFamily.HELVETICA, 8)));

      // add placeholder for total page count
      PdfPCell totalPageCount = new PdfPCell(total);
      totalPageCount.setBorder(Rectangle.NO_BORDER);
      footer.addCell(totalPageCount);

      // write page
      PdfContentByte canvas = writer.getDirectContent();
      canvas.beginMarkedContentSequence(PdfName.ARTIFACT);
      footer.writeSelectedRows(0, -1, marginSides, marginBottom, canvas);
      canvas.endMarkedContentSequence();
    } catch(DocumentException de) {
      throw new ExceptionConverter(de);
    }
  }

  public void onCloseDocument(PdfWriter writer, Document document) {
    int totalLength = String.valueOf(writer.getPageNumber()).length();
    int totalWidth = totalLength * 15;
    ColumnText.showTextAligned(t, Element.ALIGN_RIGHT,
        new Phrase(String.valueOf(writer.getPageNumber()), new Font(Font.FontFamily.HELVETICA, 8)),
        totalWidth, 6, 0);
  }
}
