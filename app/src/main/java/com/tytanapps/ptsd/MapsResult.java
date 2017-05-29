
package com.tytanapps.ptsd;

import java.util.HashMap;
import java.util.Map;

public class MapsResult {

    private String copyright;
    private String date;
    private Location location;
    private String panoId;
    private String status;
    private String errorMessage;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public MapsResult() {
    }

    /**
     * 
     * @param errorMessage
     * @param panoId
     * @param status
     * @param location
     * @param copyright
     * @param date
     */
    public MapsResult(String copyright, String date, Location location, String panoId, String status, String errorMessage) {
        super();
        this.copyright = copyright;
        this.date = date;
        this.location = location;
        this.panoId = panoId;
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public boolean isStreetViewAvailable() {
        return status != null && status.equalsIgnoreCase("OK");
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getPanoId() {
        return panoId;
    }

    public void setPanoId(String panoId) {
        this.panoId = panoId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MapsResult that = (MapsResult) o;

        if (copyright != null ? !copyright.equals(that.copyright) : that.copyright != null)
            return false;
        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        if (location != null ? !location.equals(that.location) : that.location != null)
            return false;
        if (panoId != null ? !panoId.equals(that.panoId) : that.panoId != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (errorMessage != null ? !errorMessage.equals(that.errorMessage) : that.errorMessage != null)
            return false;
        return additionalProperties != null ? additionalProperties.equals(that.additionalProperties) : that.additionalProperties == null;

    }

    @Override
    public int hashCode() {
        int result = copyright != null ? copyright.hashCode() : 0;
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (panoId != null ? panoId.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (errorMessage != null ? errorMessage.hashCode() : 0);
        result = 31 * result + (additionalProperties != null ? additionalProperties.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MapsResult{" + "copyright='" + copyright + '\'' + ", date='" + date + '\'' + ", location=" + location + ", panoId='" + panoId + '\'' + ", status='" + status + '\'' + ", errorMessage='" + errorMessage + '\'' + ", additionalProperties=" + additionalProperties + '}';
    }
}
