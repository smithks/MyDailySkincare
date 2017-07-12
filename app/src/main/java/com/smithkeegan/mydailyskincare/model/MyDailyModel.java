package com.smithkeegan.mydailyskincare.model;

import android.content.Context;
import android.database.Cursor;
import android.view.GestureDetector;

import com.smithkeegan.mydailyskincare.data.DatabaseRepository;
import com.smithkeegan.mydailyskincare.data.DiaryContract;
import com.smithkeegan.mydailyskincare.data.DiaryDbHelper;
import com.smithkeegan.mydailyskincare.data.ListItem;

import java.util.List;

/**
 * Model class for the data of MyDailySkincare. Performs data requests and transforms that data into
 * usable format by the app and returns it to the requesting entity.
 * Created by keegansmith on 7/12/17.
 */

public class MyDailyModel implements DataSubscriber {

    DatabaseRepository databaseRepository;
    ItemListSubscriber subscriber;

    public MyDailyModel(Context appContext){
        databaseRepository = DatabaseRepository.getDatabaseRepository(appContext);
    }

    public void getListOfItems(String tableName, String[] columns, String where){
        databaseRepository.register(this); //Register with the repository to receive data
        databaseRepository.getData(tableName,columns,where);
    }

    @Override
    public void onDataRetrieved(Cursor data) {
        databaseRepository.unregister(this); //Stop listening for data
        //Process data
        data.moveToFirst();
        do {
            String name = data.getString(data.getColumnIndex(DiaryContract.Ingredient.COLUMN_NAME));
        }while (data.moveToNext());
    }
}
