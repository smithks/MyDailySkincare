package com.smithkeegan.mydailyskincare.routine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.smithkeegan.mydailyskincare.R;

/**
 * @author Keegan Smith
 * @since 8/5/2016
 */
public class RoutineActivityDetail extends AppCompatActivity {

    public final static String NEW_ROUTINE = "New Routine";
    public final static String ENTRY_ID = "ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_detail);

        RoutineFragmentDetail fragmentDetail = new RoutineFragmentDetail();

        Bundle bundle = new Bundle();
        Intent thisIntent = getIntent();

        if (thisIntent.hasExtra(NEW_ROUTINE)){
            //Check new routine flag and entry id flag. Error occurred if new_routine is false and entry_id is -1. Open new routine..
            if (thisIntent.getBooleanExtra(NEW_ROUTINE,true) || thisIntent.getLongExtra(ENTRY_ID,-1) < 0){
                bundle.putBoolean(NEW_ROUTINE,true);
            }else{
                bundle.putBoolean(NEW_ROUTINE,false);
                bundle.putLong(ENTRY_ID,thisIntent.getLongExtra(ENTRY_ID,-1));
            }
            fragmentDetail.setArguments(bundle);
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.routine_activity_detail,fragmentDetail);
        transaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                ((RoutineFragmentDetail)getSupportFragmentManager().findFragmentById(R.id.routine_activity_detail)).onBackButtonPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        ((RoutineFragmentDetail)getSupportFragmentManager().findFragmentById(R.id.routine_activity_detail)).onBackButtonPressed();
    }
}
