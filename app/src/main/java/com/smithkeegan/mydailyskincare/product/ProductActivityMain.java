package com.smithkeegan.mydailyskincare.product;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.smithkeegan.mydailyskincare.R;

/**
 * @author Keegan Smith
 * @since 5/19/2016
 */
public class ProductActivityMain extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_product_main);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.product_activity_main, new ProductFragmentMain());
        transaction.commit();
    }
}
