package be.ucll.se.demo.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class Contact {
    private String name;
    private String phone;
    private String email;

    public Contact() {
    }

    public Contact(String name, String phone, String email) {
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    // Nieuw
    public String getName() {
        return name;
    }

    // Nieuw
    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhoneNumber(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
