package com.smithkeegan.mydailyskincare.diaryEntry;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.smithkeegan.mydailyskincare.R;
import com.smithkeegan.mydailyskincare.customClasses.DiaryEntrySeekBar;
import com.smithkeegan.mydailyskincare.data.DiaryContract;
import com.smithkeegan.mydailyskincare.data.DiaryDbHelper;

import java.util.Date;

/**
 * @author Keegan Smith
 * @since 8/26/2016
 */
public class DiaryEntryFragmentMain extends Fragment {

    public static final int CODE_PHOTO_REQUEST = 1;
    public static final int CODE_PHOTO_PERMISSION_REQUEST = 1;

    private DiaryDbHelper mDbHelper;

    private DiaryEntrySeekBar mSeekBarGeneralCondition;
    private TextView mTextViewGeneralCondition;

    private Date mDate;
    private long mEpochTime; //Number of milliseconds since January 1, 1970 00:00:00.00. Value stored in database for this date.

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
        mDate = new Date(bundle.getLong(DiaryEntryActivityMain.DATE_EXTRA));
        mEpochTime = mDate.getTime(); //Set time long
        setConditionArrays();

        View rootView = inflater.inflate(R.layout.fragment_diary_entry_main,container,false);
        setMemberViews(rootView);
        setListeners();

        Button testButton = (Button)rootView.findViewById(R.id.diary_entry_test_button);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double steps = 100 / 6.0;
                Toast.makeText(getContext(),String.valueOf(steps),Toast.LENGTH_SHORT).show();
            }
        });

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
                return true;
            case R.id.menu_action_delete:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Retrieves the views for this fragment.
     * @param rootView the rootview of this fragment
     */
    private void setMemberViews(View rootView){
        mSeekBarGeneralCondition = (DiaryEntrySeekBar) rootView.findViewById(R.id.diary_entry_seek_bar_general_condition);
        mTextViewGeneralCondition = (TextView) rootView.findViewById(R.id.diary_entry_condition_general_text);

        mSeekBarGeneralCondition.setDefaultStep();
        updateSliderLabel(mTextViewGeneralCondition, 3);
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
        mSeekBarGeneralCondition.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) { //Do not recalculate if progress set programmatically
                    int step = mSeekBarGeneralCondition.getNearestStep(progress);
                    mSeekBarGeneralCondition.setProgressToStep(step);
                    updateSliderLabel(mTextViewGeneralCondition, step);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
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
     * Background task used to fetch data from the database corresponding to this date.
     * If there is no existing entry for this date then one is created.
     */
    private class LoadDiaryEntryTask extends AsyncTask<Long,Void,Cursor>{

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
                db.insert(DiaryContract.DiaryEntry.TABLE_NAME,null,values);
            }

            return rows;
        }

        @Override
        protected void onPostExecute(Cursor result) {
            if(result != null && result.moveToFirst()){ //Row was returned from query, an entry for this date exists
                long id = result.getLong(result.getColumnIndex(DiaryContract.DiaryEntry._ID));
                long date = result.getLong(result.getColumnIndex(DiaryContract.DiaryEntry.COLUMN_DATE));
            }
        }
    }
}
