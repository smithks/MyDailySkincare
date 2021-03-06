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

import com.google.firebase.analytics.FirebaseAnalytics;
import com.smithkeegan.mydailyskincare.R;
import com.smithkeegan.mydailyskincare.analytics.MDSAnalytics;
import com.smithkeegan.mydailyskincare.data.DiaryContract;
import com.smithkeegan.mydailyskincare.data.DiaryDbHelper;

/**
 * Fragment class for the routine list screen.
 *
 * @author Keegan Smith
 * @since 8/5/2016
 */
public class RoutineFragmentMain extends Fragment {

    private DiaryDbHelper mDbHelper;
    private ListView mRoutinesList;
    private TextView mNoRoutinesTextView;
    private Button mNewRoutineButton;

    private FirebaseAnalytics firebaseAnalytics;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_routine_main, container, false);

        firebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        mDbHelper = DiaryDbHelper.getInstance(getContext());

        mRoutinesList = (ListView) rootView.findViewById(R.id.routine_main_list_view);
        mNoRoutinesTextView = (TextView) rootView.findViewById(R.id.routine_main_no_routines_text);
        mNewRoutineButton = (Button) rootView.findViewById(R.id.routine_main_new_button);

        setButtonListener();

        return rootView;
    }

    /**
     * Set listener for the new routine button.
     */
    private void setButtonListener() {
        mNewRoutineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logRoutineFirebaseEvent();
                Intent intent = new Intent(getContext(), RoutineActivityDetail.class);
                intent.putExtra(RoutineActivityDetail.NEW_ROUTINE, true);
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

    private void showLayout(View view) {
        mRoutinesList.setVisibility(View.INVISIBLE);
        mNoRoutinesTextView.setVisibility(View.INVISIBLE);

        view.setVisibility(View.VISIBLE);
    }

    /**
     * Logs a firebase event to Firebase Analytics.
     */
    private void logRoutineFirebaseEvent(){
        Bundle analyticsBundle = new Bundle();
        analyticsBundle.putString(MDSAnalytics.PARAM_REQUEST_ORIGIN,MDSAnalytics.VALUE_ROUTINE_ORIGIN_ROUTINE_LIST);
        firebaseAnalytics.logEvent(MDSAnalytics.EVENT_ROUTINE_OPENED,analyticsBundle);
    }
    private void refreshRoutineList() {
        new FetchRoutinesTask().execute();
    }

    private class FetchRoutinesTask extends AsyncTask<Void, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Void... params) {
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            String[] columns = {DiaryContract.Routine._ID, DiaryContract.Routine.COLUMN_NAME, DiaryContract.Routine.COLUMN_TIME};
            String sortOrder = DiaryContract.Routine.COLUMN_NAME + " DESC";
            return db.query(DiaryContract.Routine.TABLE_NAME, columns, null, null, null, null, sortOrder);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            String[] fromColumns = {DiaryContract.Routine.COLUMN_NAME, DiaryContract.Routine.COLUMN_TIME};
            int[] toViews = {R.id.routine_listview_name, R.id.routine_listView_time};
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(getContext(), R.layout.listview_item_routine_main, cursor, fromColumns, toViews, 0);
            adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                    if (columnIndex == cursor.getColumnIndex(DiaryContract.Routine.COLUMN_NAME)) {
                        TextView nameView = (TextView) view;
                        nameView.setText(cursor.getString(cursor.getColumnIndex(DiaryContract.Routine.COLUMN_NAME)));
                        return true;
                    } else if (columnIndex == cursor.getColumnIndex(DiaryContract.Routine.COLUMN_TIME)) {
                        TextView timeView = (TextView) view;
                        timeView.setText(cursor.getString(cursor.getColumnIndex(DiaryContract.Routine.COLUMN_TIME)));
                        return true;
                    }
                    return false;
                }
            });
            mRoutinesList.setAdapter(adapter);
            //Set listener for the list view cells. Open detail activity on press
            mRoutinesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    logRoutineFirebaseEvent();
                    Intent intent = new Intent(getContext(), RoutineActivityDetail.class);
                    intent.putExtra(RoutineActivityDetail.NEW_ROUTINE, false);
                    intent.putExtra(RoutineActivityDetail.ENTRY_ID, id);
                    startActivity(intent);
                }
            });
            if (mRoutinesList.getAdapter() != null && mRoutinesList.getAdapter().getCount() > 0) {
                showLayout(mRoutinesList);
            } else {
                showLayout(mNoRoutinesTextView);
            }
        }
    }
}
