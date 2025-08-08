package be.ucll.se.demo.dto;

import be.ucll.se.demo.model.CarType;

public class CarDTO {
    private Long id;
    private String brand;
    private String model;
    private String licensePlate;
    private String ownerEmail;
    private CarType type;
    private int numberOfSeats;
    private int numberOfChildSeats;
    private boolean foldingRearSeat;
    private boolean towBar;
    private boolean availableForRent;

    // Default constructor
    public CarDTO() {
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public CarType getType() {
        return type;
    }

    public void setType(CarType type) {
        this.type = type;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public void setNumberOfSeats(int numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    public int getNumberOfChildSeats() {
        return numberOfChildSeats;
    }

    public void setNumberOfChildSeats(int numberOfChildSeats) {
        this.numberOfChildSeats = numberOfChildSeats;
    }

    public boolean isFoldingRearSeat() {
        return foldingRearSeat;
    }

    public void setFoldingRearSeat(boolean foldingRearSeat) {
        this.foldingRearSeat = foldingRearSeat;
    }

    public boolean isTowBar() {
        return towBar;
    }

    public void setTowBar(boolean towBar) {
        this.towBar = towBar;
    }

    public boolean isAvailableForRent() {
        return availableForRent;
    }

    public void setAvailableForRent(boolean availableForRent) {
        this.availableForRent = availableForRent;
    }
}