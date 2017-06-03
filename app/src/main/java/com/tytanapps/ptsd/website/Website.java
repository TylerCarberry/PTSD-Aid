package com.tytanapps.ptsd.website;


import android.support.annotation.DrawableRes;

class Website {

    private String name;
    private String description;
    private String iconUrl;
    private @DrawableRes int iconRes;
    private String url;
    private int order;
    private boolean veteranOnly;

    public Website() {
    }

    public Website(String name, String description, String iconUrl, int iconRes, String url, int order, boolean veteranOnly) {
        this.name = name;
        this.description = description;
        this.iconUrl = iconUrl;
        this.iconRes = iconRes;
        this.url = url;
        this.order = order;
        this.veteranOnly = veteranOnly;
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

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String icon_url) {
        this.iconUrl = icon_url;
    }

    public int getIconRes() {
        return iconRes;
    }

    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isVeteranOnly() {
        return veteranOnly;
    }

    public void setVeteranOnly(boolean veteran_only) {
        this.veteranOnly = veteran_only;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Website website = (Website) o;

        if (iconRes != website.iconRes) return false;
        if (order != website.order) return false;
        if (veteranOnly != website.veteranOnly) return false;
        if (name != null ? !name.equals(website.name) : website.name != null) return false;
        if (description != null ? !description.equals(website.description) : website.description != null)
            return false;
        if (iconUrl != null ? !iconUrl.equals(website.iconUrl) : website.iconUrl != null)
            return false;
        return url != null ? url.equals(website.url) : website.url == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (iconUrl != null ? iconUrl.hashCode() : 0);
        result = 31 * result + iconRes;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + order;
        result = 31 * result + (veteranOnly ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Website{" + "name='" + name + '\'' + ", description='" + description + '\'' + ", iconUrl='" + iconUrl + '\'' + ", iconRes=" + iconRes + ", url='" + url + '\'' + ", order=" + order + ", veteranOnly=" + veteranOnly + '}';
    }
}


