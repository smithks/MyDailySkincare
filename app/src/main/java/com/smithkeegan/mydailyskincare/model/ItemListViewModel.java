package com.smithkeegan.mydailyskincare.model;

import android.content.Context;
import android.support.annotation.WorkerThread;

import com.smithkeegan.mydailyskincare.data.DiaryContract;
import com.smithkeegan.mydailyskincare.data.ListItem;

import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.subjects.PublishSubject;

/**
 * View model for fragments that display a list of items to the user. Exposes an observable
 * data object for the view to pull from.
 * Created by keegansmith on 7/12/17.
 */

public class ItemListViewModel {

    private MyDailyModel model;

    public enum RequestType {
        INGREDIENTS,
        PRODUCTS,
        ROUTINES
    }

    public ItemListViewModel(Context appContext){
        model = new MyDailyModel(appContext);
    }

    public Single<List<ListItem>> getItemObservable(final RequestType requestType){
        return Single.fromCallable(new Callable<List<ListItem>>() {
            @Override
            public List<ListItem> call() throws Exception {
                return requestList(requestType);
            }
        });
    }

    /**
     * Requests a list of items from the data model.
     * @param itemType the type of request the caller needs
     */
    @WorkerThread
    public List<ListItem> requestList(RequestType itemType){
        String tableName = null;
        String[] columns = null;
        String where = null;
        switch (itemType){
            case INGREDIENTS:
                tableName = DiaryContract.Ingredient.TABLE_NAME;
                columns = new String[] {DiaryContract.Ingredient._ID, DiaryContract.Ingredient.COLUMN_NAME};
        }
        return model.getListOfItems(tableName,columns,where);
    }
}
