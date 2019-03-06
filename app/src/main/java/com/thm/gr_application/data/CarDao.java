package com.thm.gr_application.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.thm.gr_application.model.Car;

import java.util.List;

import io.reactivex.Single;


@Dao
public interface CarDao {

    @Query("SELECT * FROM car")
    Single<List<Car>> getAll();

    @Insert
    void insert(Car car);

    @Query("DELETE FROM car WHERE license_plate = :plate")
    int delete(String plate);
}
