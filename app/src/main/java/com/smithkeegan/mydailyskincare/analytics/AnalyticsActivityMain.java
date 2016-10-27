package com.smithkeegan.mydailyskincare.analytics;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.smithkeegan.mydailyskincare.R;

/**
 * @author Keegan Smith
 * @since 10/18/2016
 */

public class AnalyticsActivityMain extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics_main);

        setTitle(R.string.analytics_activity_title);

        if (savedInstanceState == null){
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.analytics_activity_main,new AnalyticsFragmentMain());
            transaction.commit();
        }
    }

    @Override
    public void onBackPressed() {
        boolean handled = ((AnalyticsFragmentMain) getSupportFragmentManager().findFragmentById(R.id.analytics_activity_main)).backButtonPressed();
        if (!handled) { //Handle the back press normally if not caught by fragment.
            super.onBackPressed();
        }
    }
}
