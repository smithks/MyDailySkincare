package com.smithkeegan.mydailyskincare.product;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.smithkeegan.mydailyskincare.R;

/**
 * @author Keegan Smith
 * @since 5/19/2016
 */
public class ProductActivityDetail extends AppCompatActivity{

    public final static String NEW_PRODUCT = "NEW_PRODUCT";
    public final static String ENTRY_ID = "ID";

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_product_detail);
    }
}
