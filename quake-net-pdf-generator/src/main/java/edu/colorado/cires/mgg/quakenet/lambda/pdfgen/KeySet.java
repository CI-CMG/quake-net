package edu.colorado.cires.mgg.quakenet.lambda.pdfgen;

import java.util.Objects;

public class KeySet implements Comparable<KeySet>{

  private String detailsKey;
  private String cdiKey;

  public void setDetailsKey(String detailsKey) {
    this.detailsKey = detailsKey;
  }

  public void setCdiKey(String cdiKey) {
    this.cdiKey = cdiKey;
  }

  public String getDetailsKey() {
    return detailsKey;
  }

  public String getCdiKey() {
    return cdiKey;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KeySet keySet = (KeySet) o;
    return Objects.equals(detailsKey, keySet.detailsKey);
  }

  @Override
  public int hashCode() {
    return Objects.hash(detailsKey);
  }

  @Override
  public int compareTo(KeySet o) {
    return detailsKey.compareTo(o.detailsKey);
  }
}
