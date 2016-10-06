package com.smithkeegan.mydailyskincare.diaryEntry;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.smithkeegan.mydailyskincare.CalendarActivityMain;
import com.smithkeegan.mydailyskincare.R;
import com.smithkeegan.mydailyskincare.customClasses.DialogClosedListener;

import java.text.DateFormat;
import java.util.Date;

/**
 * Activity that holds a diary entry.
 * @author Keegan Smith
 * @since 5/3/2016
 * TODO: return to selected date when finished
 * TODO: load data from database based on date
 * TODO: don't show year in title if current year
 */
public class DiaryEntryActivityMain extends AppCompatActivity implements DialogClosedListener {

    public static final String DATE_EXTRA = "DATE_EXTRA";

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_diary_entry_main);

        Intent intent = getIntent();
        Date date = new Date(intent.getLongExtra(CalendarActivityMain.INTENT_DATE, 0));
        DateFormat df = DateFormat.getDateInstance();
        setTitle(df.format(date));

        if (savedInstance == null) {
            //Place date in bundle and send to fragment
            Bundle bundle = new Bundle();
            bundle.putLong(DATE_EXTRA, date.getTime());
            DiaryEntryFragmentMain fragment = new DiaryEntryFragmentMain();
            fragment.setArguments(bundle);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.diary_entry_main, fragment);
            transaction.commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: //Handle user pressing navigate up button.
                ((DiaryEntryFragmentMain) getSupportFragmentManager().findFragmentById(R.id.diary_entry_main)).onBackButtonPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        //Handle user pressing back button
        ((DiaryEntryFragmentMain) getSupportFragmentManager().findFragmentById(R.id.diary_entry_main)).onBackButtonPressed();
    }

    @Override
    public void onEditListDialogClosed() {
        //On return from item list dialog
        ((DiaryEntryFragmentMain) getSupportFragmentManager().findFragmentById(R.id.diary_entry_main)).refreshRoutinesList();
    }
}
