package com.smithkeegan.mydailyskincare.diaryEntry;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.smithkeegan.mydailyskincare.CalendarActivityMain;
import com.smithkeegan.mydailyskincare.R;
import com.smithkeegan.mydailyskincare.customClasses.DiaryEntrySeekBar;
import com.smithkeegan.mydailyskincare.customClasses.ItemListDialogFragment;
import com.smithkeegan.mydailyskincare.data.DiaryContract;
import com.smithkeegan.mydailyskincare.data.DiaryDbHelper;
import com.smithkeegan.mydailyskincare.routine.RoutineActivityDetail;

import java.util.Date;

/**
 * @author Keegan Smith
 * @since 8/26/2016
 */
public class DiaryEntryFragmentMain extends Fragment {

    private DiaryDbHelper mDbHelper;

    private View mLoadingView;
    private View mEntryDetailView;

    private RelativeLayout mShowMoreLayout;
    private RelativeLayout mAdditionalConditionsLayout;
    private RelativeLayout mRoutinesLayout;

    private TextView mTextViewGeneralCondition;
    private TextView mTextViewForeheadCondition;
    private TextView mTextViewNoseCondition;
    private TextView mTextViewCheeksCondition;
    private TextView mTextViewLipsCondition;
    private TextView mTextViewChinCondition;

    private DiaryEntrySeekBar mSeekBarGeneralCondition;
    private DiaryEntrySeekBar mSeekBarForeheadCondition;
    private DiaryEntrySeekBar mSeekBarNoseCondition;
    private DiaryEntrySeekBar mSeekBarCheeksCondition;
    private DiaryEntrySeekBar mSeekBarLipsCondition;
    private DiaryEntrySeekBar mSeekBarChinCondition;

    private ListView mRoutinesListView;
    private Button mAddRemoveRoutinesButton;

    private EntryFieldCollection mInitialFieldValues;
    private EntryFieldCollection mCurrentFieldValues;

    private long mDiaryEntryID;
    private long mEpochTime; //Number of milliseconds since January 1, 1970 00:00:00.00. Value stored in database for this date.
    private boolean mNewEntry;
    private boolean mAdditionalConditionsShown;
    private boolean mInitalLoadFinished;

    private String[] mConditionStrings;
    private int[] mConditionColorIds;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDbHelper = DiaryDbHelper.getInstance(getContext());

        Bundle bundle = getArguments();
        Date date = new Date(bundle.getLong(DiaryEntryActivityMain.DATE_EXTRA));
        mEpochTime = date.getTime(); //Set time long
        setConditionArrays();

        View rootView = inflater.inflate(R.layout.fragment_diary_entry_main,container,false);
        setMemberViews(rootView);
        setListeners();

        mAdditionalConditionsShown = false;
        mInitalLoadFinished = false;

        mInitialFieldValues = new EntryFieldCollection();
        mCurrentFieldValues = new EntryFieldCollection();
        mNewEntry = false;

        showLoadingScreen();
        new LoadDiaryEntryTask().execute(mEpochTime);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_item_detail,menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_action_save:
                saveCurrentDiaryEntry();
                return true;
            case R.id.menu_action_delete:
                deleteCurrentDiaryEntry(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Refresh routines list on activity resume.
     */
    @Override
    public void onResume() {
        super.onResume();
        if(mInitalLoadFinished){
            refreshRoutinesList();
        }

    }

    /**
     * Retrieves the views for this fragment.
     * @param rootView the rootview of this fragment
     */
    private void setMemberViews(View rootView){
        mLoadingView = rootView.findViewById(R.id.diary_entry_loading_layout);
        mEntryDetailView = rootView.findViewById(R.id.diary_entry_detail_layout);

        mShowMoreLayout = (RelativeLayout) rootView.findViewById(R.id.diary_entry_show_more_layout);
        mAdditionalConditionsLayout = (RelativeLayout) rootView.findViewById(R.id.diary_entry_additional_conditions_layout);
        mRoutinesLayout = (RelativeLayout) rootView.findViewById(R.id.diary_entry_routines_layout);

        mTextViewGeneralCondition = (TextView) rootView.findViewById(R.id.diary_entry_general_condition_text);
        mTextViewForeheadCondition = (TextView) rootView.findViewById(R.id.diary_entry_forehead_condition_text);
        mTextViewNoseCondition = (TextView) rootView.findViewById(R.id.diary_entry_nose_condition_text);
        mTextViewCheeksCondition = (TextView) rootView.findViewById(R.id.diary_entry_cheeks_condition_text);
        mTextViewLipsCondition = (TextView) rootView.findViewById(R.id.diary_entry_lips_condition_text);
        mTextViewChinCondition = (TextView) rootView.findViewById(R.id.diary_entry_chin_condition_text);

        mSeekBarGeneralCondition = (DiaryEntrySeekBar) rootView.findViewById(R.id.diary_entry_general_condition_seek_bar);
        mSeekBarForeheadCondition = (DiaryEntrySeekBar) rootView.findViewById(R.id.diary_entry_forehead_condition_seek_bar);
        mSeekBarNoseCondition = (DiaryEntrySeekBar) rootView.findViewById(R.id.diary_entry_nose_condition_seek_bar);
        mSeekBarCheeksCondition = (DiaryEntrySeekBar) rootView.findViewById(R.id.diary_entry_cheeks_condition_seek_bar);
        mSeekBarLipsCondition = (DiaryEntrySeekBar) rootView.findViewById(R.id.diary_entry_lips_condition_seek_bar);
        mSeekBarChinCondition = (DiaryEntrySeekBar) rootView.findViewById(R.id.diary_entry_chin_condition_seek_bar);

        mRoutinesListView = (ListView) rootView.findViewById(R.id.diary_entry_routines_listview);
        mAddRemoveRoutinesButton = (Button) rootView.findViewById(R.id.diary_entry_add_remove_routine_button);

        //Set the default value of sliders, will be changed on load from database.
        updateSliderLabel(mTextViewGeneralCondition, 3);
        updateSliderLabel(mTextViewForeheadCondition, 3);
        updateSliderLabel(mTextViewNoseCondition, 3);
        updateSliderLabel(mTextViewCheeksCondition, 3);
        updateSliderLabel(mTextViewLipsCondition, 3);
        updateSliderLabel(mTextViewChinCondition, 3);

        mSeekBarGeneralCondition.setDefaultStep();
        mSeekBarForeheadCondition.setDefaultStep();
        mSeekBarNoseCondition.setDefaultStep();
        mSeekBarCheeksCondition.setDefaultStep();
        mSeekBarLipsCondition.setDefaultStep();
        mSeekBarChinCondition.setDefaultStep();
    }

    /*
    Fetches properties to be used with the condition label's from xml resources.
     */
    private void setConditionArrays(){
        mConditionStrings = new String[7];
        mConditionStrings[0] = getResources().getString(R.string.severe);
        mConditionStrings[1] = getResources().getString(R.string.very_poor);
        mConditionStrings[2] = getResources().getString(R.string.poor);
        mConditionStrings[3] = getResources().getString(R.string.fair);
        mConditionStrings[4] = getResources().getString(R.string.good);
        mConditionStrings[5] = getResources().getString(R.string.very_good);
        mConditionStrings[6] = getResources().getString(R.string.excellent);

        mConditionColorIds = new int[7];
        mConditionColorIds[0] = ContextCompat.getColor(getContext(),R.color.severe);
        mConditionColorIds[1] = ContextCompat.getColor(getContext(),R.color.veryPoor);
        mConditionColorIds[2] = ContextCompat.getColor(getContext(),R.color.poor);
        mConditionColorIds[3] = ContextCompat.getColor(getContext(),R.color.fair);
        mConditionColorIds[4] = ContextCompat.getColor(getContext(),R.color.good);
        mConditionColorIds[5] = ContextCompat.getColor(getContext(),R.color.veryGood);
        mConditionColorIds[6] = ContextCompat.getColor(getContext(),R.color.excellent);
    }

    /*
    Sets the listeners of views in this fragment.
     */
    private void setListeners(){
        mSeekBarGeneralCondition.setOnSeekBarChangeListener(new DiaryEntrySeekBarChangeListener());
        mSeekBarForeheadCondition.setOnSeekBarChangeListener(new DiaryEntrySeekBarChangeListener());
        mSeekBarNoseCondition.setOnSeekBarChangeListener(new DiaryEntrySeekBarChangeListener());
        mSeekBarCheeksCondition.setOnSeekBarChangeListener(new DiaryEntrySeekBarChangeListener());
        mSeekBarLipsCondition.setOnSeekBarChangeListener(new DiaryEntrySeekBarChangeListener());
        mSeekBarChinCondition.setOnSeekBarChangeListener(new DiaryEntrySeekBarChangeListener());

        mShowMoreLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAdditionalConditions();
            }
        });

        ((ImageButton)mShowMoreLayout.findViewById(R.id.diary_entry_show_more_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAdditionalConditions();
            }
        });

        mAddRemoveRoutinesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString(ItemListDialogFragment.DISPLAYED_DATA,ItemListDialogFragment.ROUTINES);
                args.putLong(ItemListDialogFragment.ITEM_ID,mDiaryEntryID);
                DialogFragment fragment = new ItemListDialogFragment();
                fragment.setArguments(args);
                fragment.show(getFragmentManager(),"dialog");
            }
        });

        mRoutinesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), RoutineActivityDetail.class);
                intent.putExtra(RoutineActivityDetail.NEW_ROUTINE,false);
                intent.putExtra(RoutineActivityDetail.ENTRY_ID,id);
                startActivity(intent);
            }
        });
    }


    /**
     * Listener for this diary entries seek bars. Updates the corresponding textView and value for this
     * seekbar.
     */
    private class DiaryEntrySeekBarChangeListener implements SeekBar.OnSeekBarChangeListener{

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser) { //Only change if the user changed the progress
                DiaryEntrySeekBar diaryEntrySeekBar = ((DiaryEntrySeekBar) seekBar);
                int step = diaryEntrySeekBar.getNearestStep(progress);
                switch (diaryEntrySeekBar.getId()){ //Set the text, color and current value for this seek bar's corresponding views
                    case R.id.diary_entry_general_condition_seek_bar:
                        updateConditionBlock(mTextViewGeneralCondition, mSeekBarGeneralCondition, step);
                        mCurrentFieldValues.generalCondition = step;
                        break;
                    case R.id.diary_entry_forehead_condition_seek_bar:
                        updateConditionBlock(mTextViewForeheadCondition, mSeekBarForeheadCondition, step);
                        mCurrentFieldValues.foreheadCondition = step;
                        break;
                    case R.id.diary_entry_nose_condition_seek_bar:
                        updateConditionBlock(mTextViewNoseCondition, mSeekBarNoseCondition, step);
                        mCurrentFieldValues.noseCondition = step;
                        break;
                    case R.id.diary_entry_cheeks_condition_seek_bar:
                        updateConditionBlock(mTextViewCheeksCondition, mSeekBarCheeksCondition, step);
                        mCurrentFieldValues.cheeksCondition = step;
                        break;
                    case R.id.diary_entry_lips_condition_seek_bar:
                        updateConditionBlock(mTextViewLipsCondition, mSeekBarLipsCondition, step);
                        mCurrentFieldValues.lipsCondition = step;
                        break;
                    case R.id.diary_entry_chin_condition_seek_bar:
                        updateConditionBlock(mTextViewChinCondition, mSeekBarChinCondition, step);
                        mCurrentFieldValues.chinCondition = step;
                        break;
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    /**
     * Shows the loading screen while hiding the detail layout.
     */
    private void showLoadingScreen(){
        mLoadingView.setVisibility(View.VISIBLE);
        mEntryDetailView.setVisibility(View.INVISIBLE);
    }

    /**
     * Hides the loading screen and shows the detail layout.
     */
    private void hideLoadingScreen(){
        mLoadingView.setVisibility(View.INVISIBLE);
        mEntryDetailView.setVisibility(View.VISIBLE);
    }

    /**
     * Called when the users toggles the show more conditions button. Displays
     * additional conditions if they are hidden, hides them if they are shown.
     */
    private void toggleAdditionalConditions(){

        if(mAdditionalConditionsShown){
            mAdditionalConditionsLayout.setVisibility(View.INVISIBLE);
            ((ImageButton)mShowMoreLayout.findViewById(R.id.diary_entry_show_more_button)).setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.ic_add_circle_black_24dp));
            //Move the layout below additional conditions up
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.BELOW, mShowMoreLayout.getId());
            mRoutinesLayout.setLayoutParams(layoutParams);
            mAdditionalConditionsShown = false;
        }else{
            mAdditionalConditionsLayout.setVisibility(View.VISIBLE);
            ((ImageButton)mShowMoreLayout.findViewById(R.id.diary_entry_show_more_button)).setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.ic_remove_circle_black_24dp));
            //Move the layout below the additional conditions down
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.BELOW,mAdditionalConditionsLayout.getId());
            mRoutinesLayout.setLayoutParams(layoutParams);
            mAdditionalConditionsShown = true;
        }
    }

    /**
     *  Updates the passed in textView with the appropriate label and background color.
     * @param view the view to update
     * @param step the step of the slider to set the text and color too
     */
    public void updateSliderLabel(TextView view, int step){
        if(step < mConditionStrings.length){
            view.setText(mConditionStrings[step]);
            view.setBackgroundColor(mConditionColorIds[step]);
        }
    }

    /**
     * Sets a condition block to the provided step.
     * @param view The textview to update
     * @param seekbar The seekbar to update
     * @param step The step to set to.
     */
    private void updateConditionBlock(TextView view, DiaryEntrySeekBar seekbar, long step){
        updateSliderLabel(view,(int)step);
        seekbar.setProgressToStep((int)step);
    }

    /**
     * Checks if the current values of any field is different from its value at creation.
     * @return true if this entry has changed
     */
    private boolean entryHasChanged(){
        if(mInitialFieldValues.generalCondition != mCurrentFieldValues.generalCondition
                || mInitialFieldValues.foreheadCondition != mCurrentFieldValues.foreheadCondition
                || mInitialFieldValues.noseCondition != mCurrentFieldValues.noseCondition
                || mInitialFieldValues.cheeksCondition != mCurrentFieldValues.cheeksCondition
                || mInitialFieldValues.chinCondition != mCurrentFieldValues.chinCondition){
            return true;
        }
        return false;
    }

    /**
     * Called when the user presses the back button or the navigate up button.
     * Called by parent activity.
     */
    public void backButtonPressed(){
        if(entryHasChanged() || mNewEntry) {
            final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
            if(mNewEntry){
                dialog.setTitle(R.string.diary_entry_back_alert_save_new_entry_title);
                dialog.setMessage(R.string.diary_entry_back_alert_save_new_entry_message);
            }else{
                dialog.setTitle(R.string.diary_entry_back_alert_dialog_title);
                dialog.setMessage(R.string.diary_entry_back_alert_dialog_message);
            }
            dialog.setPositiveButton(R.string.save_button_string, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveCurrentDiaryEntry();
                        }
                    }).setNegativeButton(R.string.no_string, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(mNewEntry) deleteCurrentDiaryEntry(false); //If this is a new entry, delete it
                            dialog.dismiss();
                            getActivity().finish();
                        }
                    }).setNeutralButton(R.string.cancel_string, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        }else{
            getActivity().finish();
        }
    }

    /**
     * Refreshes the listview containing this diary entries routines.
     */
    public void refreshRoutinesList(){
        Long[] args = {mDiaryEntryID};
        new LoadDiaryEntryRoutinesTask().execute(args);
    }


    /**
     * Calls to the saveDiaryEntry async task to save this diary entry.
     */
    private void saveCurrentDiaryEntry(){
        Long[] args = {mEpochTime,
                mCurrentFieldValues.generalCondition,
                mCurrentFieldValues.foreheadCondition,
                mCurrentFieldValues.noseCondition,
                mCurrentFieldValues.cheeksCondition,
                mCurrentFieldValues.lipsCondition,
                mCurrentFieldValues.chinCondition};
        new SaveDiaryEntryTask().execute(args);
    }

    /**
     * Calls to deleteDiaryEntry task to delete this entry.
     */
    private void deleteCurrentDiaryEntry(boolean askUser){
        if(askUser) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.diary_entry_delete_alert_dialog_message)
                    .setTitle(R.string.diary_entry_delete_alert_dialog_title)
                    .setIcon(R.drawable.ic_warning_black_24dp)
                    .setPositiveButton(R.string.alert_delete_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Boolean[] args = {true};
                            new DeleteDiaryEntryTask().execute(args);
                        }
                    })
                    .setNegativeButton(R.string.cancel_string, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        }else{
            Boolean[] args = {false};
            new DeleteDiaryEntryTask().execute(args);
        }
    }

    /**
     * Background task used to fetch data from the database corresponding to this date.
     * If there is no existing entry for this date then one is created.
     */
    private class LoadDiaryEntryTask extends AsyncTask<Long,Void,Cursor>{

        /**
         * @param params params[0] contains the date to fetch from database
         * @return cursor holding database query results
         */
        @Override
        protected Cursor doInBackground(Long... params) {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            long epochTime = params[0];
            String[] columns = {DiaryContract.DiaryEntry._ID, //Columns to retrieve
                    DiaryContract.DiaryEntry.COLUMN_DATE,
                    DiaryContract.DiaryEntry.COLUMN_PHOTO,
                    DiaryContract.DiaryEntry.COLUMN_GENERAL_CONDITION,
                    DiaryContract.DiaryEntry.COLUMN_FOREHEAD_CONDITION,
                    DiaryContract.DiaryEntry.COLUMN_CHEEK_CONDITION,
                    DiaryContract.DiaryEntry.COLUMN_CHIN_CONDITION,
                    DiaryContract.DiaryEntry.COLUMN_NOSE_CONDITION,
                    DiaryContract.DiaryEntry.COLUMN_LIPS_CONDITION,
                    DiaryContract.DiaryEntry.COLUMN_DIET,
                    DiaryContract.DiaryEntry.COLUMN_EXERCISE,
                    DiaryContract.DiaryEntry.COLUMN_HYGIENE,
                    DiaryContract.DiaryEntry.COLUMN_WATER_INTAKE,
                    DiaryContract.DiaryEntry.COLUMN_ON_PERIOD};

            String selection = DiaryContract.DiaryEntry.COLUMN_DATE +" = "+epochTime; // TODO add date

            Cursor rows = db.query(DiaryContract.DiaryEntry.TABLE_NAME,columns,selection,null,null,null,null);

            if(rows != null && rows.getCount() == 0){ //No entry found for this date, create a new one.
                ContentValues values = new ContentValues();
                values.put(DiaryContract.DiaryEntry.COLUMN_DATE,epochTime);
                mDiaryEntryID = db.insert(DiaryContract.DiaryEntry.TABLE_NAME,null,values); //Set returned row ID to this diary entry's ID
            }

            return rows;
        }

        /**
         * Set values of views and fields based on result returned form query.
         * @param result cursor that contains this entry from the Diary Entry table
         */
        @Override
        protected void onPostExecute(Cursor result) {
            if(result != null && result.moveToFirst()){ //Row was returned from query, an entry for this date exists
                mDiaryEntryID = result.getLong(result.getColumnIndex(DiaryContract.DiaryEntry._ID));

                long generalStep = result.getLong(result.getColumnIndex(DiaryContract.DiaryEntry.COLUMN_GENERAL_CONDITION));
                updateConditionBlock(mTextViewGeneralCondition,mSeekBarGeneralCondition,generalStep);
                mInitialFieldValues.generalCondition = generalStep;
                mCurrentFieldValues.generalCondition = generalStep;

                long foreheadStep = result.getLong(result.getColumnIndex(DiaryContract.DiaryEntry.COLUMN_FOREHEAD_CONDITION));
                updateConditionBlock(mTextViewForeheadCondition, mSeekBarForeheadCondition, foreheadStep);
                mInitialFieldValues.foreheadCondition = foreheadStep;
                mCurrentFieldValues.foreheadCondition = foreheadStep;

                long noseStep = result.getLong(result.getColumnIndex(DiaryContract.DiaryEntry.COLUMN_NOSE_CONDITION));
                updateConditionBlock(mTextViewNoseCondition, mSeekBarNoseCondition, noseStep);
                mInitialFieldValues.noseCondition = noseStep;
                mCurrentFieldValues.noseCondition = noseStep;

                long cheeksStep = result.getLong(result.getColumnIndex(DiaryContract.DiaryEntry.COLUMN_CHEEK_CONDITION));
                updateConditionBlock(mTextViewCheeksCondition, mSeekBarCheeksCondition, cheeksStep);
                mInitialFieldValues.cheeksCondition = cheeksStep;
                mCurrentFieldValues.cheeksCondition = cheeksStep;

                long lipsStep = result.getLong(result.getColumnIndex(DiaryContract.DiaryEntry.COLUMN_LIPS_CONDITION));
                updateConditionBlock(mTextViewLipsCondition, mSeekBarLipsCondition, lipsStep);
                mInitialFieldValues.lipsCondition = lipsStep;
                mCurrentFieldValues.lipsCondition = lipsStep;

                long chinStep = result.getLong(result.getColumnIndex(DiaryContract.DiaryEntry.COLUMN_CHIN_CONDITION));
                updateConditionBlock(mTextViewChinCondition, mSeekBarChinCondition, chinStep);
                mInitialFieldValues.chinCondition = chinStep;
                mCurrentFieldValues.chinCondition = chinStep;
            }else{ //A new entry was created, default values have already been set
                mNewEntry = true;
            }
            refreshRoutinesList();
            hideLoadingScreen();
            mInitalLoadFinished = true;
        }
    }

    /**
     * AsyncTask to populate the routines listview for this diary entry.
     */
    private class LoadDiaryEntryRoutinesTask extends AsyncTask<Long,Void,Cursor>{

        /**
         * Grabs this diary entry's routines from the DiaryEntryRoutines table and
         * returns them via a cursor.
         * @param params params[0] - this diary entry's _ID
         * @return cursor object containing routines
         */
        @Override
        protected Cursor doInBackground(Long... params) {
            SQLiteDatabase db = mDbHelper.getReadableDatabase();

            long diaryEntryID = params[0];
            Cursor routineCursor = null;

            String[] columns = {DiaryContract.DiaryEntryRoutine.COLUMN_ROUTINE_ID};
            String where = DiaryContract.DiaryEntryRoutine.COLUMN_DIARY_ENTRY_ID + " = " + Long.toString(diaryEntryID);
            Cursor routineIDsCursor = db.query(DiaryContract.DiaryEntryRoutine.TABLE_NAME,columns,where,null,null,null,null);

            String[] routineColumns = {DiaryContract.Routine._ID, DiaryContract.Routine.COLUMN_NAME, DiaryContract.Routine.COLUMN_TIME};
            String routineWhere = "";
            if(routineIDsCursor.moveToFirst()){
                do {
                    long routineID = routineIDsCursor.getLong(routineIDsCursor.getColumnIndex(DiaryContract.DiaryEntryRoutine.COLUMN_ROUTINE_ID));
                    if (routineWhere.length() > 0) { //Append OR to every ID past the first.
                        routineWhere = routineWhere + " OR ";
                    }
                    routineWhere = routineWhere +DiaryContract.Routine._ID + " = " + routineID;
                }while(routineIDsCursor.moveToNext());

                String routineOrderBy = DiaryContract.Routine.COLUMN_NAME + " ASC";
                routineCursor = db.query(DiaryContract.Routine.TABLE_NAME,routineColumns,routineWhere,null,null,null,routineOrderBy);
            }

            return routineCursor;
        }

        /**
         * Populates the listview with the results from the table query
         * @param cursor contains rows to insert into listview
         */
        @Override
        protected void onPostExecute(Cursor cursor) {
            mRoutinesListView.setAdapter(null);//Clear current adapter
            if(cursor != null){
                String [] fromColumns = {DiaryContract.Routine.COLUMN_NAME, DiaryContract.Routine.COLUMN_TIME};
                int[] toViews = {R.id.routine_listview_name,R.id.routine_listView_time};
                SimpleCursorAdapter adapter = new SimpleCursorAdapter(getContext(),R.layout.listview_item_routine_main,cursor,fromColumns,toViews,0);
                adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                    @Override
                    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                        if (columnIndex == cursor.getColumnIndex(DiaryContract.Routine.COLUMN_NAME)) {
                            TextView nameView = (TextView) view;
                            nameView.setText(cursor.getString(cursor.getColumnIndex(DiaryContract.Routine.COLUMN_NAME)));
                            return true;
                        }else if (columnIndex == cursor.getColumnIndex(DiaryContract.Routine.COLUMN_TIME)){
                            TextView timeView = (TextView) view;
                            timeView.setText(cursor.getString(cursor.getColumnIndex(DiaryContract.Routine.COLUMN_TIME)));
                            return true;
                        }
                        return false;
                    }
                });
                mRoutinesListView.setAdapter(adapter);
            }
        }
    }


    /**
     * AsyncTask for saving this diary entry to the database. Called when the user explicitly presses the save button or when the
     * user chooses to save changes when leaving the fragment.
     */
    private class SaveDiaryEntryTask extends AsyncTask<Long,Void,Long>{

        /**
         * @param params params[0] - todays date as milliseconds from epoch
         *               params[1] - general condition
         *               params[2] - forehead condition
         *               params[3] - nose condition
         *               params[4] - cheeks condition
         *               params[5] - lips condition
         *               params[6] - chin condition
         */
        @Override
        protected Long doInBackground(Long... params) {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(DiaryContract.DiaryEntry.COLUMN_DATE,params[0]);
            values.put(DiaryContract.DiaryEntry.COLUMN_GENERAL_CONDITION,params[1]);
            values.put(DiaryContract.DiaryEntry.COLUMN_FOREHEAD_CONDITION, params[2]);
            values.put(DiaryContract.DiaryEntry.COLUMN_NOSE_CONDITION, params[3]);
            values.put(DiaryContract.DiaryEntry.COLUMN_CHEEK_CONDITION, params[4]);
            values.put(DiaryContract.DiaryEntry.COLUMN_LIPS_CONDITION, params[5]);
            values.put(DiaryContract.DiaryEntry.COLUMN_CHIN_CONDITION, params[6]);

            String selection = DiaryContract.DiaryEntry.COLUMN_DATE + " = " + mEpochTime;
            return (long) db.update(DiaryContract.DiaryEntry.TABLE_NAME,values,selection,null);
        }

        @Override
        protected void onPostExecute(Long result) {
            if(result == -1){
                Toast.makeText(getContext(),R.string.toast_save_failed, Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getContext(),R.string.toast_save_success,Toast.LENGTH_SHORT).show();
            }
            Intent returnIntent = new Intent();
            returnIntent.putExtra(CalendarActivityMain.INTENT_DATE,mEpochTime);
            getActivity().setResult(Activity.RESULT_OK,returnIntent);
            getActivity().finish();
        }
    }

    /**
     * AsyncTask for deleting a diary entry.
     */
    private class DeleteDiaryEntryTask extends AsyncTask<Boolean,Void,Integer>{

        private boolean showToast;

        /**
         * @param params params[0] contains a boolean value denoting whether a toast should be displayed on delete.
         *               (no toast is shown when automatically deleting a new entry on back press).
         */
        @Override
        protected Integer doInBackground(Boolean... params) {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            showToast = params[0];

            String where = DiaryContract.DiaryEntry.COLUMN_DATE + " = ?";
            String[] whereArgs = {Long.toString(mEpochTime)};
            return db.delete(DiaryContract.DiaryEntry.TABLE_NAME,where,whereArgs);
        }

        @Override
        protected void onPostExecute(Integer result) {
            if(result == 0) {
                if (showToast)
                    Toast.makeText(getContext(), R.string.toast_delete_failed, Toast.LENGTH_SHORT).show();
            } else {
                if (showToast)
                    Toast.makeText(getContext(), R.string.toast_delete_successful, Toast.LENGTH_SHORT).show();
            }

            //Return to calendar activity with delete flag set
            Intent returnIntent = new Intent();
            returnIntent.putExtra(CalendarActivityMain.INTENT_DATE_DELETED,mEpochTime);
            getActivity().setResult(Activity.RESULT_OK,returnIntent);
            getActivity().finish();
        }
    }

    /**
     * Helper class used to hold values of fields in this fragment for comparison.
     */
    private class EntryFieldCollection{
        long generalCondition;
        long foreheadCondition;
        long noseCondition;
        long cheeksCondition;
        long lipsCondition;
        long chinCondition;

        EntryFieldCollection(){
            generalCondition = 3;
            foreheadCondition = 3;
            noseCondition = 3;
            cheeksCondition = 3;
            lipsCondition = 3;
            chinCondition = 3;
        }
    }
}
