package com.tytanapps.ptsd;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A facility is a VA facility that offers PTSD programs
 */
public class Facility implements Comparable<Facility>, Serializable {

    private static final long serialVersionUID = 2L;

    // Every VA facility has a unique id
    private final int FACILITY_ID;

    // The name of the facility
    private String name;

    // The description for the facility
    private String description;

    // Contact information for the facility
    private String phoneNumber;
    private String url;

    // The location of the facility
    private String streetAddress;
    private String city;
    private String state;
    private String zip;

    // The latitude and longitude of the facility. Used to determine distance from the user.
    private double latitude;
    private double longitude;

    // The distance from the user in miles
    private double distance;

    private Bitmap facilityImage;

    // The PTSD programs offered at that location
    private Set<String> programs;

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
    public Facility(int facilityId, String name, String description, String phoneNumber, String url, String streetAddress,
                    String city, String state, String zip, double latitude, double longitude, double distance) {

        FACILITY_ID = facilityId;
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

    /**
     * Get the description of the facility
     * @return The description of the facility
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description of the facility
     * @param description The new description of the facility
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the phone number for the facility
     * @return The phone number for the facility
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Change the phone number
     * @param phoneNumber The new phone number for the facility
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Get the url for more information about the facility
     * @return The url for the facility
     */
    public String getUrl() {
        return url;
    }

    /**
     * Set the url for the facility
     * @param url The new url for the facility
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Get the street address for the facility
     * @return The street address for the facility
     */
    public String getStreetAddress() {
        return streetAddress;
    }

    /**
     * Set the street address for the facility
     * @param streetAddress The new street address for the facility
     */
    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    /**
     * Get the city that the facility is located in
     * @return The facility's city
     */
    public String getCity() {
        return city;
    }

    /**
     * Set the city of the facility
     * @param city The new city of the facility
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Get the US state that the facility is located in.
     * All facilities are currently located in the United States.
     * @return The US state that the facility is located in
     */
    public String getState() {
        return state;
    }

    /**
     * Set the US state that the facility is located in
     * @param state The new state that the facility is located in
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * Get the zip code of the facility
     * @return The zip code of the facility
     */
    public String getZip() {
        return zip;
    }

    /**
     * Set the zip code of the facility
     * @param zip The new zip code of the facility
     */
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

    /**
     * Get the latitude of the facility
     * @return The latitude of the facility
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Set the latitude of the facility
     * @param latitude The new latitude of the facility
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Get the longitude of the facility
     * @return The longitude of the facility
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Set the longitude of the facility
     * @param longitude The new longitude of the facility
     */
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

    /**
     * Compare two facilities by distance to the user
     * @param another The other facility to compare to
     * @return A positive number if the facility is closer than the parameter.
     *         Negative if it is farther away. 0 if they have the same distance
     */
    @Override
    public int compareTo(@NonNull Facility another) {
        if(getDistance() < another.getDistance())
            return -1;
        if(getDistance() > another.getDistance())
            return 1;
        return 0;
    }

    public Bitmap getFacilityImage() {
        return facilityImage;
    }

    public void setFacilityImage(Bitmap facilityImage) {
        this.facilityImage = facilityImage;
    }
}
