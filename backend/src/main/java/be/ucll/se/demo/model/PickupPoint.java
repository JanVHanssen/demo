package be.ucll.se.demo.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class PickupPoint {
    private String street;
    private String number;
    private String postal;
    private String city;

    public PickupPoint() {
    }

    public PickupPoint(String street, String number, String postal, String city) {
        this.street = street;
        this.number = number;
        this.postal = postal;
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPostal() {
        return postal;
    }

    public void setPostal(String postal) {
        this.postal = postal;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
