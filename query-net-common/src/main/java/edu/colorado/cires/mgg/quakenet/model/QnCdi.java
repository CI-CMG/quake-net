package edu.colorado.cires.mgg.quakenet.model;

public class QnCdi {


  // not always present
  private String city;
  private double cdi;
  private int numResp;
  private double distKm;
  private double latitude;
  private double longitude;
  private String name;
  // not always present
  private String state;
  // could be zip or "Santo Domingo::Pichincha::Ecuador"
  private String code;



  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public double getCdi() {
    return cdi;
  }

  public void setCdi(double cdi) {
    this.cdi = cdi;
  }

  public int getNumResp() {
    return numResp;
  }

  public void setNumResp(int numResp) {
    this.numResp = numResp;
  }

  public double getDistKm() {
    return distKm;
  }

  public void setDistKm(double distKm) {
    this.distKm = distKm;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  @Override
  public String toString() {
    return "QnCdi{" +
        "city='" + city + '\'' +
        ", cdi=" + cdi +
        ", numResp=" + numResp +
        ", distKm=" + distKm +
        ", latitude=" + latitude +
        ", longitude=" + longitude +
        ", name='" + name + '\'' +
        ", state='" + state + '\'' +
        ", code='" + code + '\'' +
        '}';
  }
}
