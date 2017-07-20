package com.smithkeegan.mydailyskincare;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TypefaceSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;
import com.smithkeegan.mydailyskincare.analytics.AnalyticsActivityMain;
import com.smithkeegan.mydailyskincare.analytics.MDSAnalytics;
import com.smithkeegan.mydailyskincare.customClasses.DatePickerDialogFragment;
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

import static com.smithkeegan.mydailyskincare.R.id.calendar;

/**
 * Main calendar activity. Displays a Caldroid calendar and handles actions
 * taken on that calendar view.
 * @author Keegan Smith
 * @since 5/3/2016
 */
public class CalendarActivityMain extends AppCompatActivity {

    public static final String APPTAG = "MyDailySkincare";

    public final static String INTENT_DATE = "Date"; //Key for intent value
    public final static String INTENT_DATE_DELETED = "DateDeleted"; //Key for activity result on entry deletion
    public final static int CODE_DATE_RETURN = 1;
    private final static String SAVED_STATE_MONTH = "SAVED_STATE_MONTH";
    private final static String SAVED_STATE_YEAR = "SAVED_STATE_YEAR";

    private Context mContext;
    private CaldroidFragment mCaldroidFragment;
    private DiaryDbHelper mDbHelper;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mDrawerToggle;

    private FirebaseAnalytics firebaseAnalytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = DiaryDbHelper.getInstance(this);
        mContext = this;

        firebaseAnalytics = FirebaseAnalytics.getInstance(mContext);

        setContentView(R.layout.activity_calendar_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeDrawer();
        initializeCalendar(savedInstanceState);

        //Show demo if this is the first launch
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getResources().getString(R.string.preference_main_demo_seen),false)){
            showDemo();
        }
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
        int id = item.getItemId();

        if(mDrawerToggle.onOptionsItemSelected(item))
            return true;

        switch (id){
            case R.id.action_go_to_date:
                logFirebaseEvent(MDSAnalytics.EVENT_SCROLL_TO_DATE_OPENED,null);
                DialogFragment datePicker = new DatePickerDialogFragment();
                datePicker.show(getSupportFragmentManager(), "date picker");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Logs a firebase event to Firebase Analytics.
     * @param event the event name
     * @param extras the extra params if any
     */
    private void logFirebaseEvent(String event,@Nullable Bundle extras){
        firebaseAnalytics.logEvent(event,extras);
    }

    /**
     * Called when the user has selected a date to scroll to in the scroll to date dialog.
     * @param date the date to scroll to
     */
    public void scrollToDate(Date date){
        mCaldroidFragment.moveToDate(date);

        //Log the use of scroll to date to firebase
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String dateID = (calendar.get(Calendar.MONTH)+1)+"/"+calendar.get(Calendar.DAY_OF_MONTH)+"/"+calendar.get(Calendar.YEAR);
        Bundle analyticsBundle = new Bundle();
        analyticsBundle.putString(MDSAnalytics.PARAM_DATE,dateID);
        logFirebaseEvent(MDSAnalytics.EVENT_SCROLL_TO_DATE_USED,analyticsBundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawer(GravityCompat.START,false);
        }
        //Uncheck any checked items.
        if (mNavigationView != null){
            for (int i = 0; i < mNavigationView.getMenu().size(); i++){
                mNavigationView.getMenu().getItem(i).setChecked(false);
            }
        }

    }

    /**
     * Close the drawer on back press if it is open.
     */
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }

    /**
     * Saves the current month to be scrolled to on instance restore.
     * @param outState bundle containing the month to scroll to
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(SAVED_STATE_MONTH,mCaldroidFragment.getMonth());
        outState.putInt(SAVED_STATE_YEAR,mCaldroidFragment.getYear());
        super.onSaveInstanceState(outState);
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
                    long dateEpoch = data.getLongExtra(INTENT_DATE_DELETED,-1);
                    Date calendarDate = new Date(dateEpoch); //If clearing todays date, set it to todays background drawable, otherwise just clear the cell
                    if (calendarDate.compareTo(getTodayCalendarDate().getTime()) == 0){
                        mCaldroidFragment.setBackgroundDrawableForDate(ContextCompat.getDrawable(mContext,R.drawable.calendar_cell_background_today),calendarDate);
                    }else {
                        mCaldroidFragment.clearBackgroundDrawableForDate(calendarDate);
                    }
                    mCaldroidFragment.refreshView();
                }
            }
        }
    }

    /**
     * Initializes the drawer, setting adapters and click listeners.
     */
    private void initializeDrawer(){
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
                switch (item.getItemId()){
                    case R.id.navigation_today:
                        Bundle analyticsBundle = new Bundle();
                        analyticsBundle.putString(MDSAnalytics.PARAM_REQUEST_ORIGIN,MDSAnalytics.VALUE_DIARY_ENTRY_ORIGIN_DRAWER);
                        logFirebaseEvent(MDSAnalytics.EVENT_DIARY_ENTRY_OPENED,analyticsBundle);
                        Date todaysDate = getTodayCalendarDate().getTime();
                        Intent intent = new Intent(mContext,DiaryEntryActivityMain.class);
                        intent.putExtra(DiaryEntryActivityMain.DATE_EXTRA,todaysDate.getTime());
                        startActivityForResult(intent,CODE_DATE_RETURN);
                        return true;
                    case R.id.navigation_routines:
                        Intent routineIntent = new Intent(mContext, RoutineActivityMain.class);
                        startActivity(routineIntent);
                        return true;
                    case R.id.navigation_products:
                        Intent productIntent = new Intent(mContext, ProductActivityMain.class);
                        startActivity(productIntent);
                        return true;
                    case R.id.navigation_ingredients:
                        Intent ingredientIntent = new Intent(mContext, IngredientActivityMain.class);
                        startActivity(ingredientIntent);
                        return true;
                    case R.id.navigation_analytics:
                        Intent analyticsIntent = new Intent(mContext, AnalyticsActivityMain.class);
                        startActivity(analyticsIntent);
                        return true;
                    default:
                        return false;
                }
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, (Toolbar) findViewById(R.id.toolbar), R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        //Set the height of the navigation header based on the size of this screen.
        RelativeLayout navHeader = (RelativeLayout) mNavigationView.getHeaderView(0);
        if (navHeader != null) {
            int newHeight = navHeader.getLayoutParams().height;
            int identifier = getResources().getIdentifier("status_bar_height", "dimen", "android"); //Get the status bar id
            if (identifier > 0) {
                newHeight = newHeight + getResources().getDimensionPixelSize(identifier); //Add height to existing height.
            }
            navHeader.getLayoutParams().height = newHeight;
        }

        //Set the font and text size of menu items
        for (int i = 0; i < mNavigationView.getMenu().size(); i++) {
            Spannable newTitle = new SpannableString(mNavigationView.getMenu().getItem(i).getTitle());
            newTitle.setSpan(new TypefaceSpan(""), 0, newTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); //Use the default typeface to set the menu items font to default. This is a bit hacky
            mNavigationView.getMenu().getItem(i).setTitle(newTitle);
        }

    }

    /**
     * Initializes the calendar on the main activity.
     * Uses a modified caldroid calendar (https://github.com/roomorama/Caldroid).
     * TODO: show select animation when selecting days with skin condition color
     */
    private void initializeCalendar(Bundle savedInstanceState){
        Calendar todayCalendar = Calendar.getInstance();
        int month = todayCalendar.get(Calendar.MONTH)+1;
        int year = todayCalendar.get(Calendar.YEAR);

        //Scroll to the saved month from the saved instance state if one exists
        if (savedInstanceState != null && savedInstanceState.containsKey(SAVED_STATE_MONTH) && savedInstanceState.containsKey(SAVED_STATE_YEAR)){
            month = savedInstanceState.getInt(SAVED_STATE_MONTH);
            year = savedInstanceState.getInt(SAVED_STATE_YEAR);
        }

        mCaldroidFragment = new CaldroidFragment();
        mCaldroidFragment.setCaldroidListener(listener);
        Bundle args = new Bundle();
        args.putInt(CaldroidFragment.MONTH,month);
        args.putInt(CaldroidFragment.YEAR,year);
        args.putBoolean(CaldroidFragment.SHOW_NAVIGATION_ARROWS,false);
        mCaldroidFragment.setArguments(args);

        todayCalendar = getTodayCalendarDate();
        mCaldroidFragment.setBackgroundDrawableForDate(ContextCompat.getDrawable(mContext,R.drawable.calendar_cell_background_today),todayCalendar.getTime());

        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(calendar, mCaldroidFragment);
        t.commit();
    }

    /**
     * Shows the welcome demo on the first launch.
     */
    private void showDemo(){
        final Dialog dialog = new Dialog(this,android.R.style.Theme_Translucent_NoTitleBar);
        dialog.setContentView(R.layout.demo_layout);

        //Set preference value of main demo seen.
        final TextView dialogText = (TextView) dialog.findViewById(R.id.demo_layout_text_view);
        final Button dialogButton = (Button) dialog.findViewById(R.id.demo_layout_button_next_done);
        dialogButton.setTag(1); //Use the view's tag to track the current displayed text phase
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPhase = (int) dialogButton.getTag();
                switch (currentPhase){
                    case 1:
                        dialogButton.setTag(2);
                        dialogText.setText(getResources().getString(R.string.main_demo_text_second));
                        break;
                    case 2:
                        dialogButton.setTag(3);
                        dialogText.setText(getResources().getString(R.string.main_demo_text_third));
                        dialogButton.setText(getResources().getString(R.string.main_demo_get_started));
                        break;
                    case 3:
                        PreferenceManager.getDefaultSharedPreferences(CalendarActivityMain.this).edit().putBoolean(getResources().getString(R.string.preference_main_demo_seen),true).apply();
                        dialog.dismiss();
                        break;
                }

            }
        });
        dialogText.setText(getResources().getString(R.string.main_demo_text_first));
        dialog.show();
    }

    /**
     * Listener for the calendar.
     */
    final CaldroidListener listener = new CaldroidListener() {

        @Override
        public void onSelectDate(Date date, View view) {
            Bundle analyticsBundle = new Bundle();
            analyticsBundle.putString(MDSAnalytics.PARAM_REQUEST_ORIGIN,MDSAnalytics.VALUE_DIARY_ENTRY_ORIGIN_CALENDAR);
            logFirebaseEvent(MDSAnalytics.EVENT_DIARY_ENTRY_OPENED,analyticsBundle);
            Intent intent = new Intent(getApplicationContext(),DiaryEntryActivityMain.class);
            intent.putExtra(DiaryEntryActivityMain.DATE_EXTRA,date.getTime());
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
     * Creates and returns a calendar object that is set to today's date.
     * @return a calendar object representing today's date
     */
    private Calendar getTodayCalendarDate(){
        Calendar todayDate = Calendar.getInstance();
        todayDate.set(Calendar.HOUR_OF_DAY,0);
        todayDate.set(Calendar.MINUTE,0);
        todayDate.set(Calendar.SECOND,0);
        todayDate.set(Calendar.MILLISECOND,0);
        return todayDate;
    }

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

            String columns[] = {DiaryContract.DiaryEntry._ID, DiaryContract.DiaryEntry.COLUMN_DATE, DiaryContract.DiaryEntry.COLUMN_OVERALL_CONDITION};
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
                    int dateCondition = result.getInt(result.getColumnIndex(DiaryContract.DiaryEntry.COLUMN_OVERALL_CONDITION));
                    Date date = new Date(dateEpoch);
                    Drawable background = getConditionBackground(dateCondition,date);

                    if(mFullRefresh) { //Store date and background into map if doing full refresh.
                        backgroundMap.put(date, background);
                    }else { //Otherwise set the single dates
                        mCaldroidFragment.setBackgroundDrawableForDate(getConditionBackground(dateCondition, date), date);
                    }
                }while(result.moveToNext());

                if(mFullRefresh) { //Full refresh using the map
                    //If there is no background in the map for todays date, add the default background for todays date
                    if (!backgroundMap.containsKey(getTodayCalendarDate().getTime())){
                        backgroundMap.put(getTodayCalendarDate().getTime(),ContextCompat.getDrawable(mContext,R.drawable.calendar_cell_background_today));
                    }
                    mCaldroidFragment.setBackgroundDrawableForDates(backgroundMap);
                }
                mCaldroidFragment.refreshView();
            }
        }

        /**
         * Helper method to grab a background drawable to set the background for a date.
         * @param condition the condition value for this date
         * @param date the date of the calendar cell to fetch background for
         * @return a new drawable to use as this dates background.
         */
        public Drawable getConditionBackground(int condition, Date date){
            int colorID;
            int drawableID;
            Drawable backgroundDrawable;
            boolean isToday = false;
            //See if the calendar date is todays date.
            Calendar calendarDate = Calendar.getInstance();
            calendarDate.setTime(date);

            if (calendarDate.compareTo(getTodayCalendarDate()) == 0){
                isToday = true;
            }

            switch (condition){
                case 0:
                    drawableID = R.drawable.calendar_cell_background_today_severe;
                    colorID = R.color.severe;
                    break;
                case 1:
                    drawableID = R.drawable.calendar_cell_background_today_very_poor;
                    colorID = R.color.veryPoor;
                    break;
                case 2:
                    drawableID = R.drawable.calendar_cell_background_today_poor;
                    colorID = R.color.poor;
                    break;
                case 4:
                    drawableID = R.drawable.calendar_cell_background_today_good;
                    colorID = R.color.good;
                    break;
                case 5:
                    drawableID = R.drawable.calendar_cell_background_today_very_good;
                    colorID = R.color.veryGood;
                    break;
                case 6:
                    drawableID = R.drawable.calendar_cell_background_today_excellent;
                    colorID = R.color.excellent;
                    break;
                default: //Default or condition = 3
                    drawableID = R.drawable.calendar_cell_background_today_fair;
                    colorID = R.color.fair;
                    break;
            }

            //Fetch the drawable that includes the today selector if the date is today.
            if (isToday){
                backgroundDrawable = ContextCompat.getDrawable(mContext,drawableID);
            }else{
                backgroundDrawable = new ColorDrawable(ContextCompat.getColor(mContext,colorID));
            }
            return backgroundDrawable;
        }
    }
}
