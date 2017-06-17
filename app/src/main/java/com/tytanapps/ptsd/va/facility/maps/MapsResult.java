package com.tytanapps.ptsd.va.facility.maps;

import java.util.HashMap;
import java.util.Map;

public class MapsResult {

    private String url;
    private String status;
    private Boolean isStreetView;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     *
     */
    public MapsResult() {
    }

    public MapsResult(String url, String status, Boolean isStreetView) {
        super();
        this.url = url;
        this.status = status;
        this.isStreetView = isStreetView;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsStreetView() {
        return isStreetView;
    }

    public void setIsStreetView(Boolean isStreetView) {
        this.isStreetView = isStreetView;
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

        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (isStreetView != null ? !isStreetView.equals(that.isStreetView) : that.isStreetView != null)
            return false;
        return additionalProperties != null ? additionalProperties.equals(that.additionalProperties) : that.additionalProperties == null;

    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (isStreetView != null ? isStreetView.hashCode() : 0);
        result = 31 * result + (additionalProperties != null ? additionalProperties.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MapsResult{" + "url='" + url + '\'' + ", status='" + status + '\'' + ", isStreetView=" + isStreetView + ", additionalProperties=" + additionalProperties + '}';
    }
}