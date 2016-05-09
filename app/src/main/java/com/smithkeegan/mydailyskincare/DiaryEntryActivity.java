package com.smithkeegan.mydailyskincare;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

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
public class DiaryEntryActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_diary_entry);

        Intent intent = getIntent();
        Date date = new Date(intent.getLongExtra(MainActivity.INTENT_DATE,0));
        DateFormat df = DateFormat.getDateInstance();
        setTitle(df.format(date)+" Entry");

    }
}
