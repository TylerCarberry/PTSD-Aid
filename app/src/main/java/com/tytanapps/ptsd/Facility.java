package com.tytanapps.ptsd;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A facility is a VA facility that offers PTSD programs
 */
public class Facility implements Comparable<Facility>, Serializable {

    private static final long serialVersionUID = 1L;

    private final int FACILITY_ID;
    private String name;
    private String description;

    private String phoneNumber;
    private String url;

    private String streetAddress;
    private String city;
    private String state;
    private String zip;

    private double latitude;
    private double longitude;

    private double distance;

    private Set<String> programs;

    public Facility(int facilityId) {
        FACILITY_ID = facilityId;
        programs = new HashSet<>();
    }

    public Facility(int facilityId, String name, String description, String phoneNumber, String url, String streetAddress,
                    String city, String state, String zip, double latitude, double longitude, double distance) {

        FACILITY_ID = facilityId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.url = url;
        this.streetAddress = streetAddress;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;

        programs = new HashSet<>();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public String getAddress() {
        return streetAddress + " " + city + " " + state;
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

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void addProgram(String program) {
        programs.add(program);
    }

    public Set<String> getPrograms() {
        return programs;
    }

    /**
     * Compare two facilities by distance to the user
     * @param another The other facility to compare to
     * @return A positive number is the facility is closer than the parameter.
     *         Negative if it is farther away. 0 if they have the same distance
     */
    @Override
    public int compareTo(Facility another) {
        if(getDistance() < another.getDistance())
            return -1;
        if(getDistance() > another.getDistance())
            return 1;
        return 0;
    }
}
