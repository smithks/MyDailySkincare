package com.smithkeegan.mydailyskincare.routine;

import android.content.Intent;
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
import android.widget.ListView;

import com.smithkeegan.mydailyskincare.R;
import com.smithkeegan.mydailyskincare.data.DiaryContract;
import com.smithkeegan.mydailyskincare.data.DiaryDbHelper;

/**
 * @author Keegan Smith
 * @since 8/5/2016
 */
public class RoutineFragmentMain extends Fragment {

    private DiaryDbHelper mDbHelper;
    private ListView mRoutinesList;
    private Button mNewRoutineButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_routine_main, container, false);

        mDbHelper = DiaryDbHelper.getInstance(getContext());

        mRoutinesList = (ListView) rootView.findViewById(R.id.routine_main_list_view);
        mNewRoutineButton = (Button) rootView.findViewById(R.id.routine_main_new_button);

        setButtonListener();

        return rootView;
    }

    private void setButtonListener(){
        mNewRoutineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),RoutineActivityDetail.class);
                intent.putExtra(RoutineActivityDetail.NEW_ROUTINE,true);
                startActivity(intent);
                //TODO use startActivity for result if highlighting
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshRoutineList();
    }

    private void refreshRoutineList(){

    }

    private class FetchRoutinesTask extends AsyncTask<Void,Void,Cursor>{

        @Override
        protected Cursor doInBackground(Void... params) {
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            String[] columns = {DiaryContract.Routine._ID, DiaryContract.Routine.COLUMN_NAME, DiaryContract.Routine.COLUMN_TIME};
            String sortOrder = DiaryContract.Routine.COLUMN_TIME + "DESC";
            return db.query(DiaryContract.Routine.TABLE_NAME,columns,null,null,null,null,sortOrder);
        }
    }
}
