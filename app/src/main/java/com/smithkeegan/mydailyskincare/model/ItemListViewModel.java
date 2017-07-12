package com.smithkeegan.mydailyskincare.model;

import android.content.Context;

import com.smithkeegan.mydailyskincare.data.DiaryContract;
import com.smithkeegan.mydailyskincare.data.ListItem;

import java.util.List;

/**
 * View model for fragments that display a list of items to the user. Exposes and observable
 * data object for the currently visible view to pull from.
 * Created by keegansmith on 7/12/17.
 */

public class ItemListViewModel implements ItemListSubscriber{

    private MyDailyModel model;
    private Context appContext; //Only pass the applicationContext, not activity context

    public enum RequestType {
        INGREDIENTS,
        PRODUCTS,
        ROUTINES
    }

    public ItemListViewModel(Context appContext){
        this.appContext = appContext;
        model = new MyDailyModel(appContext);
    }

    /**
     * Requests a list of items from the data model.
     * @param itemType
     */
    public void requestList(RequestType itemType){
        String tableName = null;
        String[] columns = null;
        String where = null;
        switch (itemType){
            case INGREDIENTS:
                tableName = DiaryContract.Ingredient.TABLE_NAME;
                columns = new String[] {DiaryContract.Ingredient._ID, DiaryContract.Ingredient.COLUMN_NAME};
        }

        model.getListOfItems(tableName,columns,where);
    }

    @Override
    public void onListReceived(List<ListItem> items) {

    }

}
