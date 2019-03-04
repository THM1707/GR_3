package com.thm.gr_application.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.thm.gr_application.model.Car;

@Database(entities = {Car.class}, version = 1, exportSchema = false)
public abstract class CarDatabase extends RoomDatabase {
    public abstract CarDao getCarDao();

    private static volatile CarDatabase INSTANCE;

    public static CarDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (CarDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), CarDatabase.class, "car").build();
                }
            }
        }
        return INSTANCE;
    }
}
