package com.smithkeegan.mydailyskincare.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Fetches data from database and returns the raw data to the requesting entity.
 * Created by keegansmith on 7/12/17.
 */

public class DatabaseRepository {

    private DiaryDbHelper databaseHelper;
    private static DatabaseRepository repository;

    public static synchronized DatabaseRepository getDatabaseRepository(Context appContext){
        if (repository == null){
            repository = new DatabaseRepository(appContext);
        }

        return repository;
    }

    private DatabaseRepository(Context appContext){
        databaseHelper = DiaryDbHelper.getInstance(appContext);
    }

    public Cursor getData(String table, String[] columns, String where){
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        return db.query(table,columns,where,null,null,null,null);
    }

}
