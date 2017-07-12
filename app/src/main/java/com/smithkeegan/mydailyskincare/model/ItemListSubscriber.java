package com.smithkeegan.mydailyskincare.model;

import com.smithkeegan.mydailyskincare.data.ListItem;

import java.util.List;

/**
 * Created by keegansmith on 7/12/17.
 */

public interface ItemListSubscriber {

    public void onListReceived(List<ListItem> items);
}
