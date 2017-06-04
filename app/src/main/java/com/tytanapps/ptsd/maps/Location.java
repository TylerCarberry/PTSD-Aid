
package com.tytanapps.ptsd.maps;

import java.util.HashMap;
import java.util.Map;

public class Location {

    private Double lat;
    private Double lng;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     */
    public Location() {
    }

    public Location(Double lat, Double lng) {
        super();
        this.lat = lat;
        this.lng = lng;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
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

        Location location = (Location) o;

        if (lat != null ? !lat.equals(location.lat) : location.lat != null) return false;
        if (lng != null ? !lng.equals(location.lng) : location.lng != null) return false;
        return additionalProperties != null ? additionalProperties.equals(location.additionalProperties) : location.additionalProperties == null;

    }

    @Override
    public int hashCode() {
        int result = lat != null ? lat.hashCode() : 0;
        result = 31 * result + (lng != null ? lng.hashCode() : 0);
        result = 31 * result + (additionalProperties != null ? additionalProperties.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Location{" + "lat=" + lat + ", lng=" + lng + ", additionalProperties=" + additionalProperties + '}';
    }
}
