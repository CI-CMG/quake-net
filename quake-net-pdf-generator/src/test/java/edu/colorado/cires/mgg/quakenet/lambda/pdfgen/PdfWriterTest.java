package edu.colorado.cires.mgg.quakenet.lambda.pdfgen;

import edu.colorado.cires.mgg.quakenet.message.ReportGenerateMessage;
import edu.colorado.cires.mgg.quakenet.model.QnEvent;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class PdfWriterTest {

  @Disabled
  @Test
  void test() throws Exception {
    List<QnEvent> events = new ArrayList<>();
    ReportGenerateMessage message = ReportGenerateMessage.Builder.builder().withYear(2012).withMonth(5).build();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    LambdaPdfWriter.writePdf(events, message, out);
  }

}