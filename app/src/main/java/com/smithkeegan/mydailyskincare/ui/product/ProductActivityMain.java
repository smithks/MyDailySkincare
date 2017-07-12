package com.smithkeegan.mydailyskincare.ui.product;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.smithkeegan.mydailyskincare.R;

/**
 * Activity class for the product main screen. Contains a list of all products.
 * @author Keegan Smith
 * @since 5/19/2016
 */
public class ProductActivityMain extends AppCompatActivity {

    public static final int PRODUCT_FINISHED = 1;
    public static final String PRODUCT_FINISHED_ID = "PRODUCT_FINISHED_ID";

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_product_main);

        if(savedInstance== null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.product_activity_main, new ProductFragmentMain());
            transaction.commit();
        }
    }
}
