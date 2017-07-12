package com.smithkeegan.mydailyskincare.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;

import com.smithkeegan.mydailyskincare.model.DataSubscriber;
import com.squareup.sqlbrite2.BriteDatabase;
import com.squareup.sqlbrite2.SqlBrite;
import com.squareup.sqlbrite2.SqlBrite.Query;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Fetches data from database and returns the raw data to the requesting entity.
 * Created by keegansmith on 7/12/17.
 */

public class DatabaseRepository {

    private BriteDatabase briteDatabase;
    private static DatabaseRepository repository;
    private ArrayList<DataSubscriber> subscribers;

    public static synchronized DatabaseRepository getDatabaseRepository(Context appContext){
        if (repository == null){
            repository = new DatabaseRepository(appContext);
        }

        return repository;
    }

    private DatabaseRepository(Context appContext){
        SqlBrite sqlBrite = new SqlBrite.Builder().build();
        subscribers = new ArrayList<>();
        briteDatabase = sqlBrite.wrapDatabaseHelper(DiaryDbHelper.getInstance(appContext), Schedulers.io());
    }

    public void register(DataSubscriber subscriber){
        if (!subscribers.contains(subscriber)) {
            subscribers.add(subscriber);
        }
    }

    public void unregister(DataSubscriber subscriber){
        if (subscribers.contains(subscriber)){
            subscribers.remove(subscriber);
        }
    }

    public void getData(String table, String[] columns, String where){
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(table);
        String query = builder.buildQuery(columns,where,null,null,null,null);
        final Observable<Query> items = briteDatabase.createQuery(table,query);
        items.subscribe(new Consumer<Query>() {
            @Override
            public void accept(@NonNull Query query) throws Exception {
                Cursor result = query.run();
                alertSubscribers(result);
            }
        });
    }

    private void alertSubscribers(Cursor cursor){
        for (DataSubscriber subscriber : subscribers){
            subscriber.onDataRetrieved(cursor);
        }
    }

}
