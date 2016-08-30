package com.smithkeegan.mydailyskincare.diaryEntry;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.smithkeegan.mydailyskincare.CalendarActivityMain;
import com.smithkeegan.mydailyskincare.R;

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
public class DiaryEntryActivityMain extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_diary_entry_main);

        Intent intent = getIntent();
        Date date = new Date(intent.getLongExtra(CalendarActivityMain.INTENT_DATE,0));
        DateFormat df = DateFormat.getDateInstance();
        setTitle(df.format(date));

        if(savedInstance == null){
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.diary_entry_layout, new DiaryEntryFragmentMain());
            transaction.commit();
        }

    }
}
