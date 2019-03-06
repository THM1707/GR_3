package com.thm.gr_application.data;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
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
