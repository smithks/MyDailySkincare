package com.smithkeegan.mydailyskincare;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;
import com.smithkeegan.mydailyskincare.data.DiaryContract;
import com.smithkeegan.mydailyskincare.data.DiaryDbHelper;
import com.smithkeegan.mydailyskincare.diaryEntry.DiaryEntryActivityMain;
import com.smithkeegan.mydailyskincare.ingredient.IngredientActivityMain;
import com.smithkeegan.mydailyskincare.product.ProductActivityMain;
import com.smithkeegan.mydailyskincare.routine.RoutineActivityMain;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    public final static String INTENT_DATE = "Date"; //Key for intent value
    public final static String INTENT_DATE_DELETED = "DateDeleted"; //Key for activity result on entry deletion
    public final static int CODE_DATE_RETURN = 1;

    private Context mContext;
    private CaldroidFragment mCaldroidFragment;
    private DiaryDbHelper mDbHelper;

    private DrawerLayout mDrawerLayout;
    private String[] mDrawerStrings;
    private ActionBarDrawerToggle mDrawerToggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = DiaryDbHelper.getInstance(this);
        mContext = this;

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

    @Override
    protected void onResume() {
        super.onResume();
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawer(GravityCompat.START,false);
        }
    }

    /**
     * When returning from the DiaryEntryActivity, refresh the data for the date that
     * was just changed.
     * @param resultCode CODE_DATE_RETURN if the result we are looking for
     * @param data intent containing the date that needs to be refreshed
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == CODE_DATE_RETURN){
                if (data.hasExtra(INTENT_DATE)){
                    long date = data.getLongExtra(INTENT_DATE,-1);
                    if(date > -1){
                        long fullRefresh = 0;
                        Long[] args = {date,date,fullRefresh}; //Set begin and end to the same date to only fetch the single date, do not full refresh
                        new FetchCalendarDataTask().execute(args);
                    }
                }else if (data.hasExtra(INTENT_DATE_DELETED)){ //If the entry was deleted, clear the corresponding calendar date
                    long date = data.getLongExtra(INTENT_DATE_DELETED,-1);
                    mCaldroidFragment.clearBackgroundDrawableForDate(new Date(date));
                    mCaldroidFragment.refreshView();
                }
            }
        }
    }

    /**
     * Initializes the drawer, setting adapters and click listeners.
     */
    private void initializeDrawer(){
        mDrawerStrings = getResources().getStringArray(R.array.drawer_strings);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView drawerList = (ListView)findViewById(R.id.drawer);

        drawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.listview_item_calendar_drawer, mDrawerStrings));
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, (Toolbar) findViewById(R.id.toolbar), R.string.drawer_open, R.string.drawer_close);

    }

    /**
     * Starts appropriate activity based on item pressed in drawer.
     * @param position item pressed in drawer
     */
    private void selectItem(int position){
        switch(position){
            case 0: //Go to today's diary entry
                Calendar calendar = Calendar.getInstance(); //Fetch todays entry by creating a date corresponding to today at time 00:00.000
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE,0);
                calendar.set(Calendar.SECOND,0);
                calendar.set(Calendar.MILLISECOND,0);
                Date todaysDate = calendar.getTime();
                Intent intent = new Intent(this,DiaryEntryActivityMain.class);
                intent.putExtra(INTENT_DATE,todaysDate.getTime());
                startActivityForResult(intent,CODE_DATE_RETURN);
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
        args.putBoolean(CaldroidFragment.SHOW_NAVIGATION_ARROWS,false);
        mCaldroidFragment.setArguments(args);

        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendar, mCaldroidFragment);
        t.commit();

        //testColors();
    }

    /**
     * Listener for the calendar.
     */
    final CaldroidListener listener = new CaldroidListener() {

        @Override
        public void onSelectDate(Date date, View view) {
            Intent intent = new Intent(getApplicationContext(),DiaryEntryActivityMain.class);
            intent.putExtra(INTENT_DATE,date.getTime());
            startActivityForResult(intent,CODE_DATE_RETURN);
        }

        @Override
        public void onCaldroidViewCreated(){

        }

        /**
         * When the month is changed, load that months data from the database.
         * @param month the new month
         * @param year the new year
         */
        @Override
        public void onChangeMonth(int month, int year) {
            Calendar calendar = Calendar.getInstance(); //Get the epochtime for the first date of this month.
            calendar.set(year,month-1,1,0,0,0);
            calendar.set(Calendar.MILLISECOND,0);

            calendar.add(Calendar.DAY_OF_MONTH, -7); //Include the previous months final week
            long beginEpoch = calendar.getTimeInMillis();

            calendar.add(Calendar.DAY_OF_MONTH, 7); //Return to first day of this month.
            calendar.roll(Calendar.DAY_OF_MONTH,false); //Roll the day_of_month constant back a day to get the final day of this month.
            calendar.add(Calendar.DAY_OF_MONTH,14); //Include the following months first two weeks
            long endEpoch = calendar.getTimeInMillis();

            long fullRefresh = 1;
            Long[] args = {beginEpoch,endEpoch,fullRefresh};
            new FetchCalendarDataTask().execute(args);
        }
    };

    /**
     * Async Task to fetch data for the given range of dates. Populates the displayed dates with this data.
     */
    private class FetchCalendarDataTask extends AsyncTask<Long,Void,Cursor>{

        private boolean mFullRefresh;

        /**
         * Querys the DiaryEntry table for information about the specified dates in the range.
         * @param params params[0] is the begin date, params[1] is the end date, params[2] denotes whether the range should be cleared
         * @return cursor containing database data
         */
        @Override
        protected Cursor doInBackground(Long... params) {
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            long beginEpoch = params[0];
            long endEpoch = params[1];

            //params[2] contains the fullRefresh flag, when set, all calendar dates are cleared before being populated.
            if (params[2] == 1)
                mFullRefresh = true;
            else
                mFullRefresh = false;

            String columns[] = {DiaryContract.DiaryEntry._ID, DiaryContract.DiaryEntry.COLUMN_DATE, DiaryContract.DiaryEntry.COLUMN_GENERAL_CONDITION};
            String selection = DiaryContract.DiaryEntry.COLUMN_DATE + " >= ? AND "+ DiaryContract.DiaryEntry.COLUMN_DATE+ " <= ?";
            String[] selectionArgs = {Long.toString(beginEpoch),Long.toString(endEpoch)};
            return db.query(DiaryContract.DiaryEntry.TABLE_NAME,columns,selection,selectionArgs,null,null,null);
        }

        /**
         * Populates the displayed dates in the calendar with the returned information.
         * @param result data returned from query in the form of a cursor
         */
        @Override
        protected void onPostExecute(Cursor result) {
            if(result != null && result.moveToFirst()){
                Map<Date, Drawable> backgroundMap = new HashMap<>();
                do{
                    long dateEpoch = result.getLong(result.getColumnIndex(DiaryContract.DiaryEntry.COLUMN_DATE));
                    int dateCondition = result.getInt(result.getColumnIndex(DiaryContract.DiaryEntry.COLUMN_GENERAL_CONDITION));
                    Date date = new Date(dateEpoch);
                    Drawable background = getConditionBackground(dateCondition);

                    if(mFullRefresh) //Store date and background into map if doing full refresh.
                        backgroundMap.put(date, background);
                    else //Otherwise set the single dates
                        mCaldroidFragment.setBackgroundDrawableForDate(getConditionBackground(dateCondition),date);
                }while(result.moveToNext());

                if(mFullRefresh) //Full refresh using the map
                    mCaldroidFragment.setBackgroundDrawableForDates(backgroundMap);
                mCaldroidFragment.refreshView();
            }
        }

        /**
         * Helper method to grab a background drawable to set the background for a date.
         * @param condition the condition value for this date
         * @return a new colorDrawable to use as this dates background.
         */
        public ColorDrawable getConditionBackground(int condition){
            int color;
            switch (condition){
                case 0:
                    color = R.color.severe;
                    break;
                case 1:
                    color = R.color.veryPoor;
                    break;
                case 2:
                    color = R.color.poor;
                    break;
                case 4:
                    color = R.color.good;
                    break;
                case 5:
                    color = R.color.veryGood;
                    break;
                case 6:
                    color = R.color.excellent;
                    break;
                default: //Default or condition = 3
                    color = R.color.fair;
                    break;
            }
            return new ColorDrawable(ContextCompat.getColor(mContext,color));
        }
    }
}
