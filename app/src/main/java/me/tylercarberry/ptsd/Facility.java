package me.tylercarberry.ptsd;

/**
 * Created by Tyler on 11/5/15.
 */
public class Facility {

    private final int FACILITY_ID;
    private String name;

    private String phoneNumber;
    private String streetAddress;
    private String city;
    private String state;
    private String zip;

    private double latitude;
    private double longitude;

    public Facility(int facilityId) {
        FACILITY_ID = facilityId;
    }

    public Facility(int facilityId, String name, String phoneNumber, String streetAddress,
                    String city, String state, String zip, double latitude, double longitude) {

        FACILITY_ID = facilityId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.streetAddress = streetAddress;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getFacilityId() {
        return FACILITY_ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
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
}