package com.smithkeegan.mydailyskincare;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;
import com.smithkeegan.mydailyskincare.data.DiaryDbHelper;
import com.smithkeegan.mydailyskincare.diaryEntry.DiaryEntryActivityMain;
import com.smithkeegan.mydailyskincare.ingredient.IngredientActivityMain;
import com.smithkeegan.mydailyskincare.product.ProductActivityMain;
import com.smithkeegan.mydailyskincare.routine.RoutineActivityMain;

import java.util.Calendar;
import java.util.Date;

/**
 * Main calendar activity. Displays a Caldroid calendar and handles actions
 * taken on that calendar view.
 * TODO: toolbar buttons to scroll to a date
 * @author Keegan Smith
 * @since 5/3/2016
 */
//TODO move save and delete buttons in detail fragments to toolbar
    //TODO handle orientation change in detail fragments through onSaveInstanceState and onRestoreInstanceState
public class CalendarActivityMain extends AppCompatActivity {

    public static final String APPTAG = "MyDailySkincare";

    private CaldroidFragment mCaldroidFragment;
    public final static String INTENT_DATE = "Date"; //Key for intent value

    private String[] mDrawerStrings;
    private ActionBarDrawerToggle mDrawerToggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_calendar_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        initializeDrawer();
        initializeCalendar();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration config){
        super.onConfigurationChanged(config);
        mDrawerToggle.onConfigurationChanged(config);
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

        if(mDrawerToggle.onOptionsItemSelected(item))
            return true;

        //TODO Remove this database menu action
        if (id == R.id.action_view_db) {
            Intent intent = new Intent(this,AndroidDatabaseManager.class);
            startActivity(intent);
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    /**
     * Initializes the drawer, setting adapters and click listeners.
     */
    private void initializeDrawer(){
        mDrawerStrings = getResources().getStringArray(R.array.drawer_strings);
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView drawerList = (ListView)findViewById(R.id.drawer);

        drawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.listview_item_calendar_drawer, mDrawerStrings));
        drawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, (Toolbar) findViewById(R.id.toolbar), R.string.drawer_open, R.string.drawer_close);

    }

    /**
     * Listener class for items in the drawer
     */
    class DrawerItemClickListener implements ListView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /**
     * Starts appropriate activity based on item pressed in drawer.
     * @param position item pressed in drawer
     */
    private void selectItem(int position){
        switch(position){
            case 0: //Todays diary entry
                break;
            case 1: //Routines
                Intent routineIntent = new Intent(this, RoutineActivityMain.class);
                startActivity(routineIntent);
                break;
            case 2: //Products
                Intent productIntent = new Intent(this, ProductActivityMain.class);
                startActivity(productIntent);
                break;
            case 3: //Ingredients
                Intent ingredientIntent = new Intent(this,IngredientActivityMain.class);
                startActivity(ingredientIntent);
                break;
            case 4: //Analytics
                break;
            case 5: //Settings
                break;
            case 6: //TODO REMOVE TESTING BLOCK
                DiaryDbHelper helper = DiaryDbHelper.getInstance(this);
                helper.dropTables(helper.getWritableDatabase());
                break;
        }
    }

    /**
     * Initializes the calendar on the main activity.
     * Uses a modified caldroid calendar (https://github.com/roomorama/Caldroid).
     * TODO: better way to highlight today
     * TODO: show select animation when selecting days with skin condition color
     */
    private void initializeCalendar(){

        mCaldroidFragment = new CaldroidFragment();
        mCaldroidFragment.setCaldroidListener(listener);
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH,cal.get(Calendar.MONTH)+1);
        args.putInt(CaldroidFragment.YEAR,cal.get(Calendar.YEAR));
        mCaldroidFragment.setArguments(args);

        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendar, mCaldroidFragment);
        t.commit();

        testColors();
    }

    /**
     * Listener for the calendar.
     */
    final CaldroidListener listener = new CaldroidListener() {
        @Override
        public void onSelectDate(Date date, View view) {
            Intent intent = new Intent(getApplicationContext(),DiaryEntryActivityMain.class);
            intent.putExtra(INTENT_DATE,date.getTime());
            startActivity(intent);
        }

        @Override
        public void onCaldroidViewCreated(){
            Button leftButton = mCaldroidFragment.getLeftArrowButton();
            Button rightButton = mCaldroidFragment.getRightArrowButton();

            leftButton.setBackgroundResource(R.drawable.ic_keyboard_arrow_left_black_24dp);
            rightButton.setBackgroundResource(R.drawable.ic_keyboard_arrow_right_black_24dp);

            mCaldroidFragment.setTextColorForDate(R.color.todayText,Calendar.getInstance().getTime());

            /* //Creating custom weekday strings
            List<String> weekdays = new ArrayList<>();
            weekdays.add("S");
            weekdays.add("M");
            weekdays.add("T");
            weekdays.add("W");
            weekdays.add("T");
            weekdays.add("F");
            weekdays.add("S");
            mCaldroidFragment.getWeekdayGridView().setAdapter(new WeekdayArrayAdapter(getApplicationContext(),R.layout.grid_weekday_textfield,weekdays,R.style.AppTheme));
            */
            mCaldroidFragment.refreshView();
        }
    };

    //TODO: remove
    private void testColors(){
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        ColorDrawable excellent = new ColorDrawable(ContextCompat.getColor(this,R.color.excellent));
        mCaldroidFragment.setBackgroundDrawableForDate(excellent,calendar.getTime());
        calendar.set(Calendar.DAY_OF_MONTH,3);
        mCaldroidFragment.setBackgroundDrawableForDate(new ColorDrawable(ContextCompat.getColor(this,R.color.veryGood)),calendar.getTime());
        calendar.set(Calendar.DAY_OF_MONTH,4);
        mCaldroidFragment.setBackgroundDrawableForDate(new ColorDrawable(ContextCompat.getColor(this,R.color.good)),calendar.getTime());
        calendar.set(Calendar.DAY_OF_MONTH,5);
        mCaldroidFragment.setBackgroundDrawableForDate(new ColorDrawable(ContextCompat.getColor(this,R.color.fair)),calendar.getTime());
        calendar.set(Calendar.DAY_OF_MONTH,6);
        mCaldroidFragment.setBackgroundDrawableForDate(new ColorDrawable(ContextCompat.getColor(this,R.color.poor)),calendar.getTime());
        calendar.set(Calendar.DAY_OF_MONTH,7);
        mCaldroidFragment.setBackgroundDrawableForDate(new ColorDrawable(ContextCompat.getColor(this,R.color.veryPoor)),calendar.getTime());
        calendar.set(Calendar.DAY_OF_MONTH,8);
        mCaldroidFragment.setBackgroundDrawableForDate(new ColorDrawable(ContextCompat.getColor(this,R.color.severe)),calendar.getTime());
        mCaldroidFragment.refreshView();
    }
}
