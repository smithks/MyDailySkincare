package com.smithkeegan.mydailyskincare.core;

import android.content.Context;
import android.support.annotation.WorkerThread;

import com.smithkeegan.mydailyskincare.core.model.Ingredient;
import com.smithkeegan.mydailyskincare.data.DiaryContract;
import com.smithkeegan.mydailyskincare.core.model.MDSItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Single;

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

    public Single<List<MDSItem>> getItemObservable(final RequestType requestType){
        return Single.fromCallable(new Callable<List<MDSItem>>() {
            @Override
            public List<MDSItem> call() throws Exception {
                return requestList(requestType);
            }
        });
    }

    /**
     * Requests a list of items from the data model.
     * @param itemType the types of items the caller needs
     */
    @WorkerThread
    public List<MDSItem> requestList(RequestType itemType){
        String tableName = null;
        String[] columns = null;
        String where = null;
        switch (itemType){
            case INGREDIENTS:
                tableName = DiaryContract.Ingredient.TABLE_NAME;
        }
        List<MDSItem> items = model.getListOfItems(tableName,columns,where);
        switch (itemType){
            case INGREDIENTS:
                items = formatIngredientsList(items);
        }
        return items;
    }

    /**
     * Takes a list of MDS items and populates the relevant information into new Ingredient items.
     * @param items the list of raw MDS items
     * @return a new list of formatted Ingredients
     */
    private List<MDSItem> formatIngredientsList(List<MDSItem> items){
        List<MDSItem> ingredients = new ArrayList<MDSItem>();
        for (MDSItem item : items){
            Ingredient ingredient = new Ingredient();
            ingredient.setId(item.getId());
            //TODO: set initialIngredient itemtype?
            ingredient.setName(item.getExtras().get(DiaryContract.Ingredient.COLUMN_NAME));
            ingredient.setIrritant(Integer.valueOf(item.getExtras().get(DiaryContract.Ingredient.COLUMN_IRRITANT)) == 1);
            ingredient.setComment(item.getExtras().get(DiaryContract.Ingredient.COLUMN_COMMENT));
            ingredients.add(ingredient);
        }
        return ingredients;
    }
}
