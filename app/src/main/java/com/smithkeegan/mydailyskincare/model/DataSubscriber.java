package com.smithkeegan.mydailyskincare.model;

import android.database.Cursor;

/**
 * Created by keegansmith on 7/12/17.
 */

public interface DataSubscriber {

    public void onDataRetrieved(Cursor data);
}
