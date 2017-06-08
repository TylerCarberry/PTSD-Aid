package com.tytanapps.ptsd.facility;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.tytanapps.ptsd.Searchable;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A facility is a VA facility that offers PTSD programs
 */
public class Facility implements Comparable<Facility>, Serializable, Searchable {

    private static final long serialVersionUID = 2L;

    // Unique VA id
    private final int FACILITY_ID;

    private String name, description;
    private String phoneNumber, url;
    private String streetAddress, city, state, zip;
    private double latitude, longitude;

    // Distance from the user in miles
    private double distance;

    private Bitmap facilityImage;

    // PTSD programs offered at that location
    private final Set<String> programs;

    /**
     * Create a new VA Facility given their id
     * @param facilityId The unique id of the facility
     */
    public Facility(int facilityId) {
        FACILITY_ID = facilityId;
        programs = new HashSet<>();
    }

    /**
     * Create a new VA Facility given all of the information about it
     * @param facilityId The unique id of the facility
     * @param name The name of the facility
     * @param description The description of the facility
     * @param phoneNumber The phone number of the facility
     * @param url The url for more information about the facility
     * @param streetAddress The street address of the facility
     * @param city The city the facility is located in
     * @param state The US state that the facility is located in
     * @param zip The zip code of the facility
     * @param latitude The latitude of the facility
     * @param longitude The longitude of the facility
     * @param distance The distance between the user and the facility in miles
     */
    public Facility(int facilityId, String name, String description, String phoneNumber, String url,
                    String streetAddress, String city, String state, String zip,
                    double latitude, double longitude, double distance) {

        this.FACILITY_ID = facilityId;
        this.name = name;
        this.description = description;
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

    /**
     * Get the facility id
     * @return The id of the facility
     */
    public int getFacilityId() {
        return FACILITY_ID;
    }

    /**
     * Get the name of the facility
     * @return The name of the facility
     */
    public String getName() {
        return name;
    }

    /**
     * Change the name of the facility
     * @param name The new name of the facility
     */
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

    /**
     * Get the full address of the facility (street, city, and state)
     * @return The full address of the facility
     */
    public String getFullAddress() {
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

    /**
     * Get the distance to the user in miles
     * @return The distance between the user and the facility in miles
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Set the distance between the user and the facility in miles
     * @param distance The new distance to the facility
     */
    public void setDistance(double distance) {
        this.distance = distance;
    }

    /**
     * Add a PTSD program offered at the VA facility
     * @param program The name of the PTSD program offered
     */
    public void addProgram(String program) {
        programs.add(program);
    }

    /**
     * Get all of the PTSD programs offered at the facility
     * @return All of the PTSD programs offered at the facility
     */
    public Set<String> getPrograms() {
        return programs;
    }

    public void setAddress(String streetAddress, String city, String state, String zipCode) {
        this.streetAddress = streetAddress;
        this.city = city;
        this.state = state;
        this.zip = zipCode;
    }

    public Bitmap getFacilityImage() {
        return facilityImage;
    }

    public void setFacilityImage(Bitmap facilityImage) {
        this.facilityImage = facilityImage;
    }

    /**
     * Compare two facilities by distance to the user
     * @param another The other facility to compare to
     * @return A positive number if the facility is closer than the parameter.
     *         Negative if it is farther away. 0 if they have the same distance
     */
    @Override
    public int compareTo(@NonNull Facility another) {
        if (getDistance() < another.getDistance())
            return -1;
        if (getDistance() > another.getDistance())
            return 1;
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Facility facility = (Facility) o;

        if (FACILITY_ID != facility.FACILITY_ID) return false;
        if (Double.compare(facility.latitude, latitude) != 0) return false;
        if (Double.compare(facility.longitude, longitude) != 0) return false;
        if (!name.equals(facility.name)) return false;
        if (!description.equals(facility.description)) return false;
        if (!phoneNumber.equals(facility.phoneNumber)) return false;
        if (!url.equals(facility.url)) return false;
        if (!streetAddress.equals(facility.streetAddress)) return false;
        if (!city.equals(facility.city)) return false;
        if (!state.equals(facility.state)) return false;
        return zip.equals(facility.zip);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = FACILITY_ID;
        result = 31 * result + name.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + phoneNumber.hashCode();
        result = 31 * result + url.hashCode();
        result = 31 * result + streetAddress.hashCode();
        result = 31 * result + city.hashCode();
        result = 31 * result + state.hashCode();
        result = 31 * result + zip.hashCode();
        temp = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean search(String text) {
        if (text == null || text.isEmpty())
            return true;
        text = text.toLowerCase();
        return name.toLowerCase().contains(text) ||
                phoneNumber.toLowerCase().contains(text) ||
                getFullAddress().toLowerCase().contains(text);
    }
}
