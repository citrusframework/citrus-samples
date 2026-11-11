package org.apache.camel.demo.model;

public class ShippingAddress {

    private String fullName;
    private String streetAddress;

    public ShippingAddress() {
    }

    public ShippingAddress(String fullName, String streetAddress) {
        this.fullName = fullName;
        this.streetAddress = streetAddress;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getFullAddress() {
        return String.format("%s, %s", fullName, streetAddress);
    }
}
