package com.smithkeegan.mydailyskincare.ingredient;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.smithkeegan.mydailyskincare.R;

/**
 * Activity class for the main screen of ingredients.
 * @author Keegan Smith
 * @since 5/6/2016
 */
public class IngredientActivityMain extends AppCompatActivity {

    public static final int INGREDIENT_FINISHED = 1;
    public static final String INGREDIENT_FINISHED_ID = "INGREDIENT_FINISHED_ID";

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_ingredient_main);

        if(savedInstance == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.ingredient_activity_main, new IngredientFragmentMain());
            transaction.commit();
        }
    }
}
