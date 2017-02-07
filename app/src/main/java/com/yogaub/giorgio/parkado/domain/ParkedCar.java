package com.yogaub.giorgio.parkado.domain;

import com.google.gson.Gson;

/**
 * Created by yogaub on 07/02/17.
 *
 * This class represents a parked car. It is different from a parking object since it specifies
 * which of the user's car is involved.
 */

public class ParkedCar {

    private int carType;
    private double lastLat;
    private double lastLong;
    private boolean parked;
    private int carId;

    public ParkedCar(int carType, double lastLat, double lastLong, boolean parked, int carId) {
        this.carType = carType;
        this.lastLat = lastLat;
        this.lastLong = lastLong;
        this.parked = parked;
        this.carId = carId;
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

    public int getCarId() {
        return carId;
    }

    public void setCarId(int carId) {
        this.carId = carId;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}
