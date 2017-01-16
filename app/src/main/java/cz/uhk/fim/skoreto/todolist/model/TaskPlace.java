package cz.uhk.fim.skoreto.todolist.model;

/**
 * Trida reprezentujici misto.
 * Created by Tomas.
 */

public class TaskPlace {

    private int id;

    private double latitude;

    private double longitude;

    private String address;

    private int radius;

    public TaskPlace() {
    }

    public TaskPlace(double latitude, double longitude, String address, int radius) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.radius = radius;
    }

    public TaskPlace(int id, double latitude, double longitude, String address, int radius) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}
