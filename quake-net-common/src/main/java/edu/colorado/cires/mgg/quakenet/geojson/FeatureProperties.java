package edu.colorado.cires.mgg.quakenet.geojson;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FeatureProperties {

  private Products products;
  Set<String> ids;
  private String net;
  private String code;
  public Set<String> getIds() {
    return ids;
  }

  public void setIds(String ids) {
    if (!ids.isEmpty()) {
      ids = ids.startsWith(",") ? ids.substring(1) : ids;
      ids = ids.endsWith(",") ? ids.substring(0,ids.length()-1) : ids;
      this.ids = new HashSet<>(Arrays.asList(ids.split(",")));
    }
  }

  public String getNet() {
    return net;
  }

  public void setNet(String net) {
    this.net = net == null ? "" : net;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code == null ? "" : code;
  }

  public String getPrimary(){
    return this.net + this.code;
  }

  public Products getProducts() {
    return products;
  }

  public void setProducts(Products products) {
    this.products = products;
  }


}
