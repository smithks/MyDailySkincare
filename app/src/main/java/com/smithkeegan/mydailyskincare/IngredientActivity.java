package com.smithkeegan.mydailyskincare;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

/**
 * @author Keegan Smith
 * @since 5/6/2016
 */
public class IngredientActivity extends AppCompatActivity {

    FragmentManager mFragmentManager;

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_ingredient);
        mFragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();

        transaction.add(R.id.ingredient_activity_main,new IngredientMainFragment());
        transaction.commit();
    }
}
