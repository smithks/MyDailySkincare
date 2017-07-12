package com.smithkeegan.mydailyskincare.ui.ingredient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.smithkeegan.mydailyskincare.R;

/**
 * Activity class for the detail screen of ingredients.
 * @author Keegan Smith
 * @since 5/16/2016
 */
public class IngredientActivityDetail extends AppCompatActivity {

    public final static String NEW_INGREDIENT = "NEW_INGREDIENT"; //Key to new ingredient indicator value
    public final static String ENTRY_ID = "ID";  //Key to ingredient id value passed to ingredient detail fragment.

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.activity_ingredient_detail);

        if (savedInstance == null) { //Create a new fragment if one isn't being restored
            IngredientFragmentDetail fragmentDetail = new IngredientFragmentDetail();
            //Set flags to send up to fragment. Existing ingredient id and new ingredient status.
            Bundle bundle = new Bundle();
            Intent thisIntent = getIntent();
            if (thisIntent.hasExtra(NEW_INGREDIENT)) {
                //Check new ingredient flag and entry id flag. Error occurred if new_ingredient is false and entry_id is -1. Open new ingredient.
                if (thisIntent.getBooleanExtra(NEW_INGREDIENT, true) || thisIntent.getLongExtra(ENTRY_ID, -1) < 0) {
                    bundle.putBoolean(NEW_INGREDIENT, true);
                } else {
                    bundle.putBoolean(NEW_INGREDIENT, false);
                    bundle.putLong(ENTRY_ID, thisIntent.getLongExtra(ENTRY_ID, -1));
                }
                fragmentDetail.setArguments(bundle);
            }


            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.ingredient_activity_detail, fragmentDetail);
            transaction.commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: //Call to fragment when navigate up button is pressed.
                ((IngredientFragmentDetail) getSupportFragmentManager().findFragmentById(R.id.ingredient_activity_detail)).onBackButtonPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //Call to fragment when back button is pressed.
        ((IngredientFragmentDetail) getSupportFragmentManager().findFragmentById(R.id.ingredient_activity_detail)).onBackButtonPressed();
    }
}
