package com.thm.gr_application.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "car")
public class Car {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int mId;

    @ColumnInfo(name = "seat_number")
    private int seatNumber;

    @ColumnInfo(name = "license_plate")
    private String mLicensePlate;

    public Car(int seatNumber, String licensePlate) {
        this.seatNumber = seatNumber;
        mLicensePlate = licensePlate;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getLicensePlate() {
        return mLicensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        mLicensePlate = licensePlate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Car car = (Car) o;
        return mId == car.mId &&
                seatNumber == car.seatNumber &&
                Objects.equals(mLicensePlate, car.mLicensePlate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mId, seatNumber, mLicensePlate);
    }
}
