package com.smithkeegan.mydailyskincare.routine;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.smithkeegan.mydailyskincare.R;

/**
 * Activity class for the product list screen.
 * @author Keegan Smith
 * @since 8/5/2016
 */
public class RoutineActivityMain extends AppCompatActivity {

    public static final String ROUTINE_FINISHED_ID = "ROUTINE_FINISHED_ID";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_main);

        if(savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.routine_activity_main, new RoutineFragmentMain());
            transaction.commit();
        }
    }
}
