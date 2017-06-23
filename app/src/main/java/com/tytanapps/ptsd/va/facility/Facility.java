package com.tytanapps.ptsd.va.facility;

import android.support.annotation.NonNull;

import com.tytanapps.ptsd.va.Searchable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A facility is a VA facility that offers PTSD programs
 */
public class Facility implements Comparable<Facility>, Serializable, Searchable {

    private String facIntraneturl;
    private String city;
    private String fax;
    private String zip;
    private String facName;
    private String typeDesc;
    private String regName;
    private Integer facId;
    private String stationid;
    private Double longitude;
    private Integer showmap;
    private String url;
    private String state;
    private String imageUrl;
    private String divName;
    private String address;
    private Double latitude;
    private Integer showphoto;
    private String facInterneturl;
    private String phoneNumber;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    private double distanceToUser;


    public Facility() {
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    /**
     * Compare two facilities by distance to the user
     * @param another The other facility to compare to
     * @return A positive number if the facility is closer than the parameter.
     *         Negative if it is farther away. 0 if they have the same distance
     */
    @Override
    public int compareTo(@NonNull Facility another) {
        if (getDistanceToUser() < another.getDistanceToUser())
            return -1;
        if (getDistanceToUser() > another.getDistanceToUser())
            return 1;
        return 0;
    }

    public String getFullAddress() {
        return address + " " + city + " " + state;
    }


    @Override
    public boolean search(String text) {
        if (text == null || text.isEmpty())
            return true;
        text = text.toLowerCase();
        return facName.toLowerCase().contains(text) ||
                phoneNumber.toLowerCase().contains(text) ||
                getFullAddress().toLowerCase().contains(text);
    }

    // Generated Getters/setters/hash/equals/tostring


    public String getFacIntraneturl() {
        return facIntraneturl;
    }

    public void setFacIntraneturl(String facIntraneturl) {
        this.facIntraneturl = facIntraneturl;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getFacName() {
        return facName;
    }

    public void setFacName(String facName) {
        this.facName = facName;
    }

    public String getTypeDesc() {
        return typeDesc;
    }

    public void setTypeDesc(String typeDesc) {
        this.typeDesc = typeDesc;
    }

    public String getRegName() {
        return regName;
    }

    public void setRegName(String regName) {
        this.regName = regName;
    }

    public Integer getFacId() {
        return facId;
    }

    public void setFacId(Integer facId) {
        this.facId = facId;
    }

    public String getStationid() {
        return stationid;
    }

    public void setStationid(String stationid) {
        this.stationid = stationid;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getShowmap() {
        return showmap;
    }

    public void setShowmap(Integer showmap) {
        this.showmap = showmap;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDivName() {
        return divName;
    }

    public void setDivName(String divName) {
        this.divName = divName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Integer getShowphoto() {
        return showphoto;
    }

    public void setShowphoto(Integer showphoto) {
        this.showphoto = showphoto;
    }

    public String getFacInterneturl() {
        return facInterneturl;
    }

    public void setFacInterneturl(String facInterneturl) {
        this.facInterneturl = facInterneturl;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public double getDistanceToUser() {
        return distanceToUser;
    }

    public void setDistanceToUser(double distanceToUser) {
        this.distanceToUser = distanceToUser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Facility facility = (Facility) o;

        if (facIntraneturl != null ? !facIntraneturl.equals(facility.facIntraneturl) : facility.facIntraneturl != null)
            return false;
        if (city != null ? !city.equals(facility.city) : facility.city != null) return false;
        if (fax != null ? !fax.equals(facility.fax) : facility.fax != null) return false;
        if (zip != null ? !zip.equals(facility.zip) : facility.zip != null) return false;
        if (facName != null ? !facName.equals(facility.facName) : facility.facName != null)
            return false;
        if (typeDesc != null ? !typeDesc.equals(facility.typeDesc) : facility.typeDesc != null)
            return false;
        if (regName != null ? !regName.equals(facility.regName) : facility.regName != null)
            return false;
        if (facId != null ? !facId.equals(facility.facId) : facility.facId != null) return false;
        if (stationid != null ? !stationid.equals(facility.stationid) : facility.stationid != null)
            return false;
        if (longitude != null ? !longitude.equals(facility.longitude) : facility.longitude != null)
            return false;
        if (showmap != null ? !showmap.equals(facility.showmap) : facility.showmap != null)
            return false;
        if (url != null ? !url.equals(facility.url) : facility.url != null) return false;
        if (state != null ? !state.equals(facility.state) : facility.state != null) return false;
        if (imageUrl != null ? !imageUrl.equals(facility.imageUrl) : facility.imageUrl != null)
            return false;
        if (divName != null ? !divName.equals(facility.divName) : facility.divName != null)
            return false;
        if (address != null ? !address.equals(facility.address) : facility.address != null)
            return false;
        if (latitude != null ? !latitude.equals(facility.latitude) : facility.latitude != null)
            return false;
        if (showphoto != null ? !showphoto.equals(facility.showphoto) : facility.showphoto != null)
            return false;
        if (facInterneturl != null ? !facInterneturl.equals(facility.facInterneturl) : facility.facInterneturl != null)
            return false;
        if (phoneNumber != null ? !phoneNumber.equals(facility.phoneNumber) : facility.phoneNumber != null)
            return false;
        return additionalProperties != null ? additionalProperties.equals(facility.additionalProperties) : facility.additionalProperties == null;

    }

    @Override
    public int hashCode() {
        int result = facIntraneturl != null ? facIntraneturl.hashCode() : 0;
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (fax != null ? fax.hashCode() : 0);
        result = 31 * result + (zip != null ? zip.hashCode() : 0);
        result = 31 * result + (facName != null ? facName.hashCode() : 0);
        result = 31 * result + (typeDesc != null ? typeDesc.hashCode() : 0);
        result = 31 * result + (regName != null ? regName.hashCode() : 0);
        result = 31 * result + (facId != null ? facId.hashCode() : 0);
        result = 31 * result + (stationid != null ? stationid.hashCode() : 0);
        result = 31 * result + (longitude != null ? longitude.hashCode() : 0);
        result = 31 * result + (showmap != null ? showmap.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (imageUrl != null ? imageUrl.hashCode() : 0);
        result = 31 * result + (divName != null ? divName.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (latitude != null ? latitude.hashCode() : 0);
        result = 31 * result + (showphoto != null ? showphoto.hashCode() : 0);
        result = 31 * result + (facInterneturl != null ? facInterneturl.hashCode() : 0);
        result = 31 * result + (phoneNumber != null ? phoneNumber.hashCode() : 0);
        result = 31 * result + (additionalProperties != null ? additionalProperties.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Facility{" + "facIntraneturl='" + facIntraneturl + '\'' + ", city='" + city + '\'' + ", fax='" + fax + '\'' + ", zip='" + zip + '\'' + ", facName='" + facName + '\'' + ", typeDesc='" + typeDesc + '\'' + ", regName='" + regName + '\'' + ", facId=" + facId + ", stationid='" + stationid + '\'' + ", longitude=" + longitude + ", showmap=" + showmap + ", url='" + url + '\'' + ", state='" + state + '\'' + ", imageUrl='" + imageUrl + '\'' + ", divName='" + divName + '\'' + ", address='" + address + '\'' + ", latitude=" + latitude + ", showphoto=" + showphoto + ", facInterneturl='" + facInterneturl + '\'' + ", phoneNumber='" + phoneNumber + '\'' + ", additionalProperties=" + additionalProperties + '}';
    }
}
