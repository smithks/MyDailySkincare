package com.smithkeegan.mydailyskincare.core;

import com.smithkeegan.mydailyskincare.core.model.Ingredient;

/**
 * Created by keegansmith on 7/19/17.
 */

public interface DetailView {

    public void updateView(Ingredient ingredient);
    public void displaySaveAlert();
    public void finish();
}
