package com.bookmyshow.model;

import java.util.Objects;

/**
 * Represents a physical venue where events are held.
 */
public class Venue {

    private int id;
    private String name;
    private String address;
    private String city;
    private int totalCapacity;

    // --- Constructors ---

    public Venue() {}

    public Venue(int id, String name, String address, String city, int totalCapacity) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.city = city;
        this.totalCapacity = totalCapacity;
    }

    // --- Getters ---

    public int getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public int getTotalCapacity() { return totalCapacity; }

    // --- Setters ---

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setCity(String city) { this.city = city; }
    public void setTotalCapacity(int totalCapacity) { this.totalCapacity = totalCapacity; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Venue)) return false;
        Venue venue = (Venue) o;
        return id == venue.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Venue{id=" + id + ", name='" + name + "', city='" + city + "'}";
    }
}
