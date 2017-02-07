package com.yogaub.giorgio.parkado.domain;

import com.google.gson.Gson;

/**
 * Created by yogaub on 07/02/17.
 *
 * This class represents a Parking object. Used to compute the available nearby.
 */

public class Parking {

    private int carType;
    private double lastLat;
    private double lastLong;
    private boolean parked;

    public Parking(int carType, double lastLat, double lastLong, boolean parked) {
        this.carType = carType;
        this.lastLat = lastLat;
        this.lastLong = lastLong;
        this.parked = parked;
    }

    public Parking(ParkedCar parkedCar) {
        this.carType = parkedCar.getCarType();
        this.lastLat = parkedCar.getLastLat();
        this.lastLong = parkedCar.getLastLong();
        this.parked = parkedCar.isParked();
    }

    public int getCarType() {
        return carType;
    }

    public void setCarType(int carType) {
        this.carType = carType;
    }

    public double getLastLat() {
        return lastLat;
    }

    public void setLastLat(double lastLat) {
        this.lastLat = lastLat;
    }

    public double getLastLong() {
        return lastLong;
    }

    public void setLastLong(double lastLong) {
        this.lastLong = lastLong;
    }

    public boolean isParked() {
        return parked;
    }

    public void setParked(boolean parked) {
        this.parked = parked;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}
