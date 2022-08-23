package edu.colorado.cires.mgg.quakenet.lambda.pdfgen;

public class KeySet {

  private final String detailsKey;
  private final String cdiKey;

  public KeySet(String detailsKey, String cdiKey) {
    this.detailsKey = detailsKey;
    this.cdiKey = cdiKey;
  }

  public String getDetailsKey() {
    return detailsKey;
  }

  public String getCdiKey() {
    return cdiKey;
  }
}
