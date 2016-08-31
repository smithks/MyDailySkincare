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
import android.widget.Toast;

import com.smithkeegan.mydailyskincare.R;
import com.smithkeegan.mydailyskincare.data.DiaryContract;
import com.smithkeegan.mydailyskincare.data.DiaryDbHelper;

import java.util.Date;

/**
 * @author Keegan Smith
 * @since 8/26/2016
 */
public class DiaryEntryFragmentMain extends Fragment {

    private DiaryDbHelper mDbHelper;
    private Date mDate;
    long mEpochTime; //Number of milliseconds since January 1, 1970 00:00:00.00. Value stored in database for this date.

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_diary_entry_main,container,false);

        Bundle bundle = getArguments();

        mDate = new Date(bundle.getLong(DiaryEntryActivityMain.DATE_EXTRA));
        mEpochTime = mDate.getTime(); //Set time long

        mDbHelper = DiaryDbHelper.getInstance(getContext());


        Button testButton = (Button)rootView.findViewById(R.id.diary_entry_test_button);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(),Long.toString(mDate.getTime()),Toast.LENGTH_SHORT).show();
            }
        });

        new LoadDiaryEntryTask().execute(mEpochTime);

        return rootView;
    }

    /**
     * Background task used to fetch data from the database corresponding to this date.
     * If there is no existing entry for this date then one is created and the corresponding
     * ID is returned.
     */
    private class LoadDiaryEntryTask extends AsyncTask<Long,Void,Cursor>{

        @Override
        protected Cursor doInBackground(Long... params) {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            long newID = 0; //default zero, sqlite database begins index at 1
            long epochTime = params[0];
            String[] columns = {DiaryContract.DiaryEntry._ID,
                    DiaryContract.DiaryEntry.COLUMN_DATE};
//                    DiaryContract.DiaryEntry.COLUMN_PHOTO,
//                    DiaryContract.DiaryEntry.COLUMN_GENERAL_CONDITION,
//                    DiaryContract.DiaryEntry.COLUMN_FOREHEAD_CONDITION,
//                    DiaryContract.DiaryEntry.COLUMN_CHEEK_CONDITION,
//                    DiaryContract.DiaryEntry.COLUMN_CHIN_CONDITION,
//                    DiaryContract.DiaryEntry.COLUMN_NOSE_CONDITION,
//                    DiaryContract.DiaryEntry.COLUMN_LIPS_CONDITION,
//                    DiaryContract.DiaryEntry.COLUMN_DIET,
//                    DiaryContract.DiaryEntry.COLUMN_EXERCISE,
//                    DiaryContract.DiaryEntry.COLUMN_HYGIENE,
//                    DiaryContract.DiaryEntry.COLUMN_WATER_INTAKE,
//                    DiaryContract.DiaryEntry.COLUMN_ON_PERIOD};

            String selection = DiaryContract.DiaryEntry.COLUMN_DATE +" = "+epochTime; // TODO add date

            Cursor rows = db.query(DiaryContract.DiaryEntry.TABLE_NAME,columns,selection,null,null,null,null);

            if(rows != null && rows.getCount() == 0){ //No entry found for this date, create a new one.
                ContentValues values = new ContentValues();
                values.put(DiaryContract.DiaryEntry.COLUMN_DATE,epochTime);
                newID = db.insert(DiaryContract.DiaryEntry.TABLE_NAME,null,values);
            }
            return rows;
        }

        @Override
        protected void onPostExecute(Cursor result) {
            if(result != null){
                if(result.moveToFirst()){
                    long id = result.getLong(result.getColumnIndex(DiaryContract.DiaryEntry._ID));
                    long date = result.getLong(result.getColumnIndex(DiaryContract.DiaryEntry.COLUMN_DATE));
                }
            }
        }
    }
}
