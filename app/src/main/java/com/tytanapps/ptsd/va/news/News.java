package com.tytanapps.ptsd.va.news;

import android.support.annotation.NonNull;

import com.tytanapps.ptsd.va.Searchable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class News implements Serializable, Searchable, Comparable<News> {

    private String press_title;
    private String press_text;
    private String press_date;
    private Integer press_id;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 1167190026290410427L;
    
    public News() {
    }
    
    public News(String press_title, String press_text, String press_date, Integer press_id) {
        super();
        this.press_title = press_title;
        this.press_text = press_text;
        this.press_date = press_date;
        this.press_id = press_id;
    }

    public String getPressTitle() {
        return press_title;
    }

    public void setPressTitle(String press_title) {
        this.press_title = press_title;
    }

    public String getPressText() {
        return press_text;
    }

    public void setPressText(String press_text) {
        this.press_text = press_text;
    }

    public String getPressDate() {
        return press_date;
    }

    public void setPressDate(String press_date) {
        this.press_date = press_date;
    }

    public Integer getPressId() {
        return press_id;
    }

    public void setPressId(Integer press_id) {
        this.press_id = press_id;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    /**
     * Compare the press_id's of two News
     * The most recent one (News with the highest press_id) comes first
     * @param another The news article to compare to
     * @return -1 if this has a greater press_id, 0 if equal, 1 if less
     */
    @Override
    public int compareTo(@NonNull News another) {
        return 0-((Integer)this.getPressId()).compareTo(another.getPressId());
    }

    public boolean search(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty())
            return true;
        searchTerm = searchTerm.toLowerCase();
        return  press_title.toLowerCase().contains(searchTerm) ||
                press_text.toLowerCase().contains(searchTerm) ||
                press_date.toLowerCase().contains(searchTerm);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        News news = (News) o;

        if (press_title != null ? !press_title.equals(news.press_title) : news.press_title != null)
            return false;
        if (press_text != null ? !press_text.equals(news.press_text) : news.press_text != null)
            return false;
        if (press_date != null ? !press_date.equals(news.press_date) : news.press_date != null)
            return false;
        if (press_id != null ? !press_id.equals(news.press_id) : news.press_id != null) return false;
        return additionalProperties != null ? additionalProperties.equals(news.additionalProperties) : news.additionalProperties == null;

    }

    @Override
    public int hashCode() {
        int result = press_title != null ? press_title.hashCode() : 0;
        result = 31 * result + (press_text != null ? press_text.hashCode() : 0);
        result = 31 * result + (press_date != null ? press_date.hashCode() : 0);
        result = 31 * result + (press_id != null ? press_id.hashCode() : 0);
        result = 31 * result + (additionalProperties != null ? additionalProperties.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "News{" + "press_title='" + press_title + '\'' + ", press_text='" + press_text + '\'' + ", press_date='" + press_date + '\'' + ", press_id=" + press_id + ", additionalProperties=" + additionalProperties + '}';
    }
}