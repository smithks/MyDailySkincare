package com.smithkeegan.mydailyskincare.diaryEntry;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
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


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mDbHelper = DiaryDbHelper.getInstance(getContext());

        Bundle bundle = getArguments();
        mDate = new Date(bundle.getLong(DiaryEntryActivityMain.DATE_EXTRA));
        mEpochTime = mDate.getTime(); //Set time long
        setConditionStringArray();

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


    private void setMemberViews(View rootView){
        mSeekBarGeneralCondition = (DiaryEntrySeekBar) rootView.findViewById(R.id.diary_entry_seek_bar_general_condition);
        mTextViewGeneralCondition = (TextView) rootView.findViewById(R.id.diary_entry_condition_general_text);

        mSeekBarGeneralCondition.setDefaultStep();
        mTextViewGeneralCondition.setText(mConditionStrings[3]);
    }

    private void setConditionStringArray(){
        mConditionStrings = new String[7];
        mConditionStrings[0] = getResources().getString(R.string.terrible);
        mConditionStrings[1] = getResources().getString(R.string.very_poor);
        mConditionStrings[2] = getResources().getString(R.string.poor);
        mConditionStrings[3] = getResources().getString(R.string.fair);
        mConditionStrings[4] = getResources().getString(R.string.good);
        mConditionStrings[5] = getResources().getString(R.string.very_good);
        mConditionStrings[6] = getResources().getString(R.string.excellent);
    }

    private void setListeners(){
        mSeekBarGeneralCondition.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) { //Do not recalculate if progress set programmatically
                    int step = mSeekBarGeneralCondition.getNearestStep(progress);
                    mSeekBarGeneralCondition.setProgressToStep(step);
                    if (step < mConditionStrings.length)
                        mTextViewGeneralCondition.setText(mConditionStrings[step]);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
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
