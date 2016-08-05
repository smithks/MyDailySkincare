package com.smithkeegan.mydailyskincare.routine;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.smithkeegan.mydailyskincare.R;

/**
 * @author Keegan Smith
 * @since 8/5/2016
 * //TODO reuse a layout for main select screen for products and routines
 */
public class RoutineActivityMain extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_main);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.routine_activity_main, new RoutineFragmentMain());
        transaction.commit();
    }
}
