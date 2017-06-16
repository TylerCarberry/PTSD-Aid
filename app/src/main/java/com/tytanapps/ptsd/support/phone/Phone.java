package com.tytanapps.ptsd.support.phone;


import android.support.annotation.DrawableRes;
import android.support.annotation.Keep;

@Keep
class Phone {

    private String name;
    private String description;
    private String iconUrl;
    private @DrawableRes int iconRes;
    private String phoneNumber;
    private int order;
    private boolean veteranOnly;

    public Phone() {
    }

    public Phone(String name, String description, String iconUrl, int iconRes, String phoneNumber, int order, boolean veteranOnly) {
        this.name = name;
        this.description = description;
        this.iconUrl = iconUrl;
        this.iconRes = iconRes;
        this.phoneNumber = phoneNumber;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phone_number) {
        this.phoneNumber = phone_number;
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

        Phone phone = (Phone) o;

        if (iconRes != phone.iconRes) return false;
        if (order != phone.order) return false;
        if (veteranOnly != phone.veteranOnly) return false;
        if (name != null ? !name.equals(phone.name) : phone.name != null) return false;
        if (description != null ? !description.equals(phone.description) : phone.description != null)
            return false;
        if (iconUrl != null ? !iconUrl.equals(phone.iconUrl) : phone.iconUrl != null)
            return false;
        return phoneNumber != null ? phoneNumber.equals(phone.phoneNumber) : phone.phoneNumber == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (iconUrl != null ? iconUrl.hashCode() : 0);
        result = 31 * result + iconRes;
        result = 31 * result + (phoneNumber != null ? phoneNumber.hashCode() : 0);
        result = 31 * result + order;
        result = 31 * result + (veteranOnly ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Phone{" + "name='" + name + '\'' + ", description='" + description + '\'' + ", iconUrl='" + iconUrl + '\'' + ", iconRes=" + iconRes + ", phoneNumber='" + phoneNumber + '\'' + ", order=" + order + ", veteranOnly=" + veteranOnly + '}';
    }
}
