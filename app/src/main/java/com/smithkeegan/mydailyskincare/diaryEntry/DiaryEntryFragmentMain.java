package com.smithkeegan.mydailyskincare.diaryEntry;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smithkeegan.mydailyskincare.R;
import com.smithkeegan.mydailyskincare.data.DiaryContract;
import com.smithkeegan.mydailyskincare.data.DiaryDbHelper;

/**
 * @author Keegan Smith
 * @since 8/26/2016
 */
public class DiaryEntryFragmentMain extends Fragment {

    DiaryDbHelper mDbHelper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_diary_entry_main,container,false);

        mDbHelper = DiaryDbHelper.getInstance(getContext());

        return rootView;
    }

    /**
     * Background task used to fetch data from the database corresponding to this date.
     * If there is no existing entry for this date then one is created and the corresponding
     * ID is returned.
     */
    private class LoadDiaryEntryTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            String[] columns = {DiaryContract.DiaryEntry._ID,
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

            String selection = DiaryContract.DiaryEntry.COLUMN_DATE +" = "; // TODO add date

            Cursor rows = db.query(DiaryContract.DiaryEntry.TABLE_NAME,columns,selection,null,null,null,null);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

        }
    }
}
