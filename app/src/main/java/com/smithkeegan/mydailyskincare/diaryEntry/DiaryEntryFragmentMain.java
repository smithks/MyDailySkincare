package com.smithkeegan.mydailyskincare.diaryEntry;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.smithkeegan.mydailyskincare.CalendarActivityMain;
import com.smithkeegan.mydailyskincare.R;
import com.smithkeegan.mydailyskincare.data.DiaryContract;
import com.smithkeegan.mydailyskincare.data.DiaryDbHelper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Keegan Smith
 * @since 8/26/2016
 */
public class DiaryEntryFragmentMain extends Fragment {

    private static final int CODE_PHOTO_REQUEST = 1;

    private DiaryDbHelper mDbHelper;
    private Date mDate;
    private long mEpochTime; //Number of milliseconds since January 1, 1970 00:00:00.00. Value stored in database for this date.
    private String mPhotoPath;


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

    private void takePhoto(){
        Intent systemPhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(systemPhotoIntent.resolveActivity(getActivity().getPackageManager()) != null){ //If there is a valid camera app
            File photofile = null;
            try {
                photofile = createImageFile();
            } catch (IOException e){
                Log.e(CalendarActivityMain.APPTAG,"Error creating imagefile. " + e.getMessage());
            }
            if(photofile != null){
                Uri photoURI = FileProvider.getUriForFile(getContext(),"com.smithkeegan.mydailyskincare.fileprovider",photofile);

                systemPhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoURI);
                startActivityForResult(systemPhotoIntent,CODE_PHOTO_REQUEST);
            }
        }
    }

    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_"+timeStamp+"_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg",storageDir);

        mPhotoPath = "file:"+image.getAbsolutePath();
        return image;
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
