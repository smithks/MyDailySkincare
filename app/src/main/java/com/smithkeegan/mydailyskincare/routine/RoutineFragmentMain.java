package com.smithkeegan.mydailyskincare.routine;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

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
        new FetchRoutinesTask().execute();
    }

    private class FetchRoutinesTask extends AsyncTask<Void,Void,Cursor>{

        @Override
        protected Cursor doInBackground(Void... params) {
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            String[] columns = {DiaryContract.Routine._ID, DiaryContract.Routine.COLUMN_NAME, DiaryContract.Routine.COLUMN_TIME};
            String sortOrder = DiaryContract.Routine.COLUMN_NAME + " DESC";
            return db.query(DiaryContract.Routine.TABLE_NAME,columns,null,null,null,null,sortOrder);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            String[] fromColumns = {DiaryContract.Routine.COLUMN_NAME, DiaryContract.Routine.COLUMN_TIME};
            int[] toViews = {R.id.routine_listview_name, R.id.routine_listView_time};
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(getContext(),R.layout.routine_listview_item,cursor,fromColumns,toViews,0);
            adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                    if(columnIndex == cursor.getColumnIndex(DiaryContract.Routine.COLUMN_NAME)){
                        TextView nameView = (TextView) view;
                        nameView.setText(cursor.getString(cursor.getColumnIndex(DiaryContract.Routine.COLUMN_NAME)));
                        return true;
                    }
                    else if(columnIndex == cursor.getColumnIndex(DiaryContract.Routine.COLUMN_TIME)){
                        TextView timeView = (TextView) view;
                        timeView.setText(cursor.getString(cursor.getColumnIndex(DiaryContract.Routine.COLUMN_TIME)));
                        return true;
                    }
                    return false;
                }
            });
            mRoutinesList.setAdapter(adapter);
            mRoutinesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getContext(),RoutineActivityDetail.class);
                    intent.putExtra(RoutineActivityDetail.NEW_ROUTINE,false);
                    intent.putExtra(RoutineActivityDetail.ENTRY_ID,id);
                    startActivity(intent);
                }
            });
        }
    }
}
