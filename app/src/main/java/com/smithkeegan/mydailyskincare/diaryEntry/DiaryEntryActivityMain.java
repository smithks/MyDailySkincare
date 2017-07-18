package com.smithkeegan.mydailyskincare.diaryEntry;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.smithkeegan.mydailyskincare.R;
import com.smithkeegan.mydailyskincare.customClasses.DialogClosedListener;

import java.util.Calendar;
import java.util.Date;

/**
 * Activity that holds a diary entry.
 * @author Keegan Smith
 * @since 5/3/2016
 */
public class DiaryEntryActivityMain extends AppCompatActivity implements DialogClosedListener {

    public static final String DATE_EXTRA = "DATE_EXTRA";

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_diary_entry_main);

        Intent intent = getIntent();
        Date date = new Date(intent.getLongExtra(DATE_EXTRA, 0));

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        calendar.setTime(date);

        String dayOfWeek = getDayOfWeek(date);
        String month = getMonth(date);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int year = calendar.get(Calendar.YEAR);

        String activityTitle = dayOfWeek+", "+month+" "+dayOfMonth;
        if (year != currentYear){ //Show year only if this diary entry is not from the current year.
            activityTitle += ", "+year;
        }
        setTitle(activityTitle);



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

    /**
     * Returns the current day of the week.
     * @param date date object to find day of the week from
     * @return the current day of the week.
     */
    private String getDayOfWeek(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String dayOfWeek = "";
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        switch (day){
            case Calendar.SUNDAY:
                dayOfWeek = "Sun";
                break;
            case Calendar.MONDAY:
                dayOfWeek = "Mon";
                break;
            case Calendar.TUESDAY:
                dayOfWeek = "Tue";
                break;
            case Calendar.WEDNESDAY:
                dayOfWeek = "Wed";
                break;
            case Calendar.THURSDAY:
                dayOfWeek = "Thu";
                break;
            case Calendar.FRIDAY:
                dayOfWeek = "Fri";
                break;
            case Calendar.SATURDAY:
                dayOfWeek = "Sat";
                break;
        }

        return dayOfWeek;
    }

    /**
     * Returns the month given a date object.
     * @param date date object to find month of
     * @return the month of the given date object
     */
    private String getMonth(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String month = "";
        int dateMonth = calendar.get(Calendar.MONTH);

        switch (dateMonth){
            case Calendar.JANUARY:
                month = "Jan";
                break;
            case Calendar.FEBRUARY:
                month = "Feb";
                break;
            case Calendar.MARCH:
                month = "Mar";
                break;
            case Calendar.APRIL:
                month = "Apr";
                break;
            case Calendar.MAY:
                month = "May";
                break;
            case Calendar.JUNE:
                month = "June";
                break;
            case Calendar.JULY:
                month = "July";
                break;
            case Calendar.AUGUST:
                month = "Aug";
                break;
            case Calendar.SEPTEMBER:
                month = "Sept";
                break;
            case Calendar.OCTOBER:
                month = "Oct";
                break;
            case Calendar.NOVEMBER:
                month = "Nov";
                break;
            case Calendar.DECEMBER:
                month = "Dec";
                break;
        }

        return month;
    }

    @Override
    public void onBackPressed() {
        //Handle user pressing back button
        ((DiaryEntryFragmentMain) getSupportFragmentManager().findFragmentById(R.id.diary_entry_main)).onBackButtonPressed();
    }

    @Override
    public void onEditListDialogClosed(boolean listModified) {
        //On return from item list dialog
        ((DiaryEntryFragmentMain) getSupportFragmentManager().findFragmentById(R.id.diary_entry_main)).onEditDialogClosed(listModified);
    }
}
