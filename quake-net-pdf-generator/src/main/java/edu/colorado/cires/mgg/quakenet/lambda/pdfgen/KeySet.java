package edu.colorado.cires.mgg.quakenet.lambda.pdfgen;

import java.util.Objects;

public class KeySet implements Comparable<KeySet>{

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
