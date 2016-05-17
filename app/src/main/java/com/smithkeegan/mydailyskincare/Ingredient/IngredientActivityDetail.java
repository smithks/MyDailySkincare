package com.smithkeegan.mydailyskincare.ingredient;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.smithkeegan.mydailyskincare.R;

/**
 * @author Keegan Smith
 * @since 5/16/2016
 */
public class IngredientActivityDetail extends AppCompatActivity {

    public final static String NEW_INGREDIENT = "NEW_INGREDIENT";

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_ingredient_detail);



        IngredientFragmentDetail fragmentDetail = new IngredientFragmentDetail();
        //Set flags to send up to fragment. Existing ingredient id and new ingredient status.
        //TODO: check for and send existing ingredient to/from intent/bundle
        Bundle bundle = new Bundle();
        if (getIntent().hasExtra(NEW_INGREDIENT)){
            if (getIntent().getBooleanExtra(NEW_INGREDIENT,true)){
                bundle.putBoolean(NEW_INGREDIENT,true);
            }else{
                bundle.putBoolean(NEW_INGREDIENT,false);
            }
            fragmentDetail.setArguments(bundle);
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.ingredient_activity_detail,fragmentDetail);
        transaction.commit();
    }
}
