package com.smithkeegan.mydailyskincare.routine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.smithkeegan.mydailyskincare.customClasses.DialogClosedListener;
import com.smithkeegan.mydailyskincare.R;

/**
 * Activity for the routine detail screen. Makes callbacks to corresponding fragment.
 * @author Keegan Smith
 * @since 8/5/2016
 */
public class RoutineActivityDetail extends AppCompatActivity implements DialogClosedListener {

    public final static String NEW_ROUTINE = "New Routine";
    public final static String ENTRY_ID = "ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.routine_activity_title);
        setContentView(R.layout.activity_routine_detail);

        if (savedInstanceState == null) {
            RoutineFragmentDetail fragmentDetail = new RoutineFragmentDetail();

            Bundle bundle = new Bundle();
            Intent thisIntent = getIntent();

            if (thisIntent.hasExtra(NEW_ROUTINE)) {
                //Check new routine flag and entry id flag. Error occurred if new_routine is false and entry_id is -1. Open new routine..
                if (thisIntent.getBooleanExtra(NEW_ROUTINE, true) || thisIntent.getLongExtra(ENTRY_ID, -1) < 0) {
                    bundle.putBoolean(NEW_ROUTINE, true);
                } else {
                    bundle.putBoolean(NEW_ROUTINE, false);
                    bundle.putLong(ENTRY_ID, thisIntent.getLongExtra(ENTRY_ID, -1));
                }
                fragmentDetail.setArguments(bundle);
            }

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.routine_activity_detail, fragmentDetail);
            transaction.commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home: //Handle the user pressing the navigate up button.
                ((RoutineFragmentDetail) getSupportFragmentManager().findFragmentById(R.id.routine_activity_detail)).onBackButtonPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() { //Handle the user pressing the back button
        ((RoutineFragmentDetail) getSupportFragmentManager().findFragmentById(R.id.routine_activity_detail)).onBackButtonPressed();
    }

    @Override
    public void onEditListDialogClosed() { //Refresh product list when returning from edit dialog
        ((RoutineFragmentDetail) getSupportFragmentManager().findFragmentById(R.id.routine_activity_detail)).refreshProducts();
    }
}
