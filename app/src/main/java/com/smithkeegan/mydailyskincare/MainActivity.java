package com.smithkeegan.mydailyskincare;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.util.Calendar;
import java.util.Date;

/**
 * TODO: toolbar buttons to scroll to a date
 * @author Keegan Smith
 * @since 5/3/2016
 */
public class MainActivity extends AppCompatActivity {

    private CaldroidFragment caldroidFragment;
    public final static String DATE = "Date"; //Key for intent value


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        initializeCalendar();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_view_db) {
            Intent intent = new Intent(this,AndroidDatabaseManager.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Initializes the calendar on the main activity.
     * Uses a modified caldroid calendar (https://github.com/roomorama/Caldroid).
     * TODO: better way to highlight today
     * TODO: show select animation when selecting days with skin condition color
     * TODO: only
     */
    private void initializeCalendar(){

        caldroidFragment = new CaldroidFragment();
        caldroidFragment.setCaldroidListener(listener);
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH,cal.get(Calendar.MONTH)+1);
        args.putInt(CaldroidFragment.YEAR,cal.get(Calendar.YEAR));
        caldroidFragment.setArguments(args);

        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendar,caldroidFragment);
        t.commit();

        testColors();
    }

    /**
     * Listener for the calendar.
     */
    final CaldroidListener listener = new CaldroidListener() {
        @Override
        public void onSelectDate(Date date, View view) {
            Intent intent = new Intent(getApplicationContext(),DiaryEntryActivity.class);
            intent.putExtra(DATE,date.getTime());
            startActivity(intent);
        }

        @Override
        public void onCaldroidViewCreated(){
            Button leftButton = caldroidFragment.getLeftArrowButton();
            Button rightButton = caldroidFragment.getRightArrowButton();

            leftButton.setBackgroundResource(R.drawable.ic_keyboard_arrow_left_black_24dp);
            rightButton.setBackgroundResource(R.drawable.ic_keyboard_arrow_right_black_24dp);

            caldroidFragment.setTextColorForDate(R.color.todayText,Calendar.getInstance().getTime());

            /* //Creating custom weekday strings
            List<String> weekdays = new ArrayList<>();
            weekdays.add("S");
            weekdays.add("M");
            weekdays.add("T");
            weekdays.add("W");
            weekdays.add("T");
            weekdays.add("F");
            weekdays.add("S");
            caldroidFragment.getWeekdayGridView().setAdapter(new WeekdayArrayAdapter(getApplicationContext(),R.layout.grid_weekday_textfield,weekdays,R.style.AppTheme));
            */
            caldroidFragment.refreshView();
        }
    };

    private void testColors(){
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        ColorDrawable excellent = new ColorDrawable(ContextCompat.getColor(this,R.color.excellent));
        caldroidFragment.setBackgroundDrawableForDate(excellent,calendar.getTime());
        calendar.set(Calendar.DAY_OF_MONTH,3);
        caldroidFragment.setBackgroundDrawableForDate(new ColorDrawable(ContextCompat.getColor(this,R.color.veryGood)),calendar.getTime());
        calendar.set(Calendar.DAY_OF_MONTH,4);
        caldroidFragment.setBackgroundDrawableForDate(new ColorDrawable(ContextCompat.getColor(this,R.color.good)),calendar.getTime());
        calendar.set(Calendar.DAY_OF_MONTH,5);
        caldroidFragment.setBackgroundDrawableForDate(new ColorDrawable(ContextCompat.getColor(this,R.color.fair)),calendar.getTime());
        calendar.set(Calendar.DAY_OF_MONTH,6);
        caldroidFragment.setBackgroundDrawableForDate(new ColorDrawable(ContextCompat.getColor(this,R.color.poor)),calendar.getTime());
        calendar.set(Calendar.DAY_OF_MONTH,7);
        caldroidFragment.setBackgroundDrawableForDate(new ColorDrawable(ContextCompat.getColor(this,R.color.veryPoor)),calendar.getTime());
        calendar.set(Calendar.DAY_OF_MONTH,8);
        caldroidFragment.setBackgroundDrawableForDate(new ColorDrawable(ContextCompat.getColor(this,R.color.terrible)),calendar.getTime());
        caldroidFragment.refreshView();
    }
}
