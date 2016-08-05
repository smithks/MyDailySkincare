package com.smithkeegan.mydailyskincare.product;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.smithkeegan.mydailyskincare.DialogClosedListener;
import com.smithkeegan.mydailyskincare.R;

/**
 * @author Keegan Smith
 * @since 5/19/2016
 */
public class ProductActivityDetail extends AppCompatActivity implements DialogClosedListener{

    public final static String NEW_PRODUCT = "NEW_PRODUCT";
    public final static String ENTRY_ID = "ID";

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_product_detail);

        ProductFragmentDetail fragmentDetail = new ProductFragmentDetail();
        //Set flags to send up to fragment. Existing product id or new status.
        //TODO: check for and send existing product to/from intent/bundle
        Bundle bundle = new Bundle();
        Intent thisIntent = getIntent();
        if (thisIntent.hasExtra(NEW_PRODUCT)){
            //Check new product flag and entry id flag. Error occurred if new_product is false and entry_id is -1. Open new product.
            if (thisIntent.getBooleanExtra(NEW_PRODUCT,true) || thisIntent.getLongExtra(ENTRY_ID,-1) < 0){
                bundle.putBoolean(NEW_PRODUCT,true);
            }else{
                bundle.putBoolean(NEW_PRODUCT,false);
                bundle.putLong(ENTRY_ID,thisIntent.getLongExtra(ENTRY_ID,-1));
            }
            fragmentDetail.setArguments(bundle);
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.product_activity_detail,fragmentDetail);
        transaction.commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                ((ProductFragmentDetail)getSupportFragmentManager().findFragmentById(R.id.product_activity_detail)).onBackButtonPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        ((ProductFragmentDetail)getSupportFragmentManager().findFragmentById(R.id.product_activity_detail)).onBackButtonPressed();
    }

    @Override
    public void onEditListDialogClosed() {
        FragmentManager manager = getSupportFragmentManager();
        ProductFragmentDetail fragment = (ProductFragmentDetail) manager.findFragmentById(R.id.product_activity_detail);
        fragment.refreshIngredients();
    }
}