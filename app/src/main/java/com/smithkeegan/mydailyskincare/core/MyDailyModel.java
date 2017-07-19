package com.smithkeegan.mydailyskincare.core;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.WorkerThread;

import com.smithkeegan.mydailyskincare.data.DatabaseRepository;
import com.smithkeegan.mydailyskincare.core.model.MDSItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class for the data of MyDailySkincare. Performs data requests and transforms that data into
 * usable format by the app and returns it to the requesting entity.
 * Created by keegansmith on 7/12/17.
 */

public class MyDailyModel {

    private DatabaseRepository databaseRepository;

    public MyDailyModel(Context appContext){
        databaseRepository = DatabaseRepository.getDatabaseRepository(appContext);
    }

    @WorkerThread
    public List<MDSItem> getListOfItems(String tableName, String[] columns, String where){
        Cursor result = databaseRepository.getData(tableName,columns,where);
        return formatListData(result);
    }

    /**
     * Parses the data from the cursor into view friendly format.
     * @param data the data to parse
     * @return a list of listitems
     */
    private List<MDSItem> formatListData(Cursor data){
        List<MDSItem> items = new ArrayList<>();
        int columnCount = data.getColumnCount();
        if (data.getCount() > 0) {
            data.moveToFirst();
            do {
                MDSItem newItem = new MDSItem();
                newItem.setId(data.getLong(data.getColumnIndex(BaseColumns._ID)));
                for (int i = 0; i < columnCount; i++) {
                    if (i != data.getColumnIndex(BaseColumns._ID)) {
                        newItem.getExtras().put(data.getColumnName(i), data.getString(i));
                    }
                }
                items.add(newItem);
            } while (data.moveToNext());
        }
        return items;
    }
}
