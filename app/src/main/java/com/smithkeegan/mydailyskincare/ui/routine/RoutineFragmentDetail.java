package com.smithkeegan.mydailyskincare.ui.routine;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smithkeegan.mydailyskincare.R;
import com.smithkeegan.mydailyskincare.util.ItemListDialogFragment;
import com.smithkeegan.mydailyskincare.data.DiaryContract;
import com.smithkeegan.mydailyskincare.data.DiaryDbHelper;
import com.smithkeegan.mydailyskincare.ui.product.ProductActivityDetail;

/**
 * Fragment class for the product detail screen. Handles actions to manipulate
 * information about a proudct and making those updates to the database.
 * @author Keegan Smith
 * @since 8/5/2016
 */
public class RoutineFragmentDetail extends Fragment {

    private static final int SELECTED = 0;
    private static final int END_CELL = 1;

    private EditText mNameEditText;
    private RadioGroup mTimeRadioGroup;
    private RadioGroup mFrequencyRadioGroup;
    private TableLayout mFrequencyTable;
    private TextView[] mFrequencyTableColumns;
    private ListView mProductsListView;
    private TextView mNoProductsTextView;
    private EditText mCommentEditText;
    private Button mEditProductsButton;

    private View mProgressLayout;
    private View mDetailLayout;

    private DiaryDbHelper mDbHelper;
    private String mInitialName;
    private String mInitialTime;
    private String mInitialFrequency;
    private String mInitialComment;
    private boolean mInitialLoadComplete;
    private boolean mIsNewRoutine;
    private boolean mProductsListModified;
    private Long mRoutineID;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_routine_detail,container,false);

        mDbHelper = DiaryDbHelper.getInstance(getContext());
        fetchViews(rootView);

        mProductsListModified = false;

        if (savedInstanceState == null) {
            Bundle args = getArguments();
            mIsNewRoutine = args.getBoolean(RoutineActivityDetail.NEW_ROUTINE, true);
            mRoutineID = args.getLong(RoutineActivityDetail.ENTRY_ID, -1);

            showLoadingLayout();

            if (mIsNewRoutine || mRoutineID < 0) { //New entry or error loading
                new SaveRoutinePlaceholderTask().execute();
            } else { //Existing entry
                new InitialLoadRoutineTask().execute(mRoutineID);
            }
        }else{
            restoreSavedInstance(savedInstanceState);
        }

        if (mIsNewRoutine){
            getActivity().setTitle(R.string.routine_activity_title_new);
        }else {
            getActivity().setTitle(R.string.routine_activity_title);
        }

        setListeners();
        if (!PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getResources().getString(R.string.preference_routine_demo_seen),false)){
            showDemo();
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mInitialLoadComplete) refreshProducts();
    }

    /**
     * Fetches views belonging to this fragment.
     * @param rootView the fragment's rootview
     */
    public void fetchViews(View rootView){
        mNameEditText = (EditText) rootView.findViewById(R.id.routine_name_edit);
        mTimeRadioGroup = (RadioGroup) rootView.findViewById(R.id.routine_time_radio_group);
        mProductsListView = (ListView) rootView.findViewById(R.id.routine_product_list_view);
        mNoProductsTextView = (TextView) rootView.findViewById(R.id.routine_no_products_text);
        mCommentEditText = (EditText) rootView.findViewById(R.id.routine_comment_edit);
        mFrequencyRadioGroup = (RadioGroup) rootView.findViewById(R.id.routine_frequency_radio_group);

        mFrequencyTable = (TableLayout) rootView.findViewById(R.id.routine_frequency_table);
        mFrequencyTableColumns = new TextView[7];
        mFrequencyTableColumns[0] = (TextView) rootView.findViewById(R.id.routine_frequency_text_sunday);
        mFrequencyTableColumns[1] = (TextView) rootView.findViewById(R.id.routine_frequency_text_monday);
        mFrequencyTableColumns[2] = (TextView) rootView.findViewById(R.id.routine_frequency_text_tuesday);
        mFrequencyTableColumns[3] = (TextView) rootView.findViewById(R.id.routine_frequency_text_wednesday);
        mFrequencyTableColumns[4] = (TextView) rootView.findViewById(R.id.routine_frequency_text_thursday);
        mFrequencyTableColumns[5] = (TextView) rootView.findViewById(R.id.routine_frequency_text_friday);
        mFrequencyTableColumns[6] = (TextView) rootView.findViewById(R.id.routine_frequency_text_saturday);

        //Set the default value for the cell tags which indicate if the cell has been selected
        for (TextView cell : mFrequencyTableColumns){
           cell.setTag(false);
        }

        mEditProductsButton = (Button) rootView.findViewById(R.id.routine_edit_products);

        mProgressLayout = rootView.findViewById(R.id.routine_loading_layout);
        mDetailLayout = rootView.findViewById(R.id.routine_fragment_detail_layout);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_item_detail,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_action_save:
                saveCurrentRoutine();
                return true;
            case R.id.menu_action_delete:
                deleteCurrentRoutine();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Saves the current fields when the activity is destroyed by a system process to be restored
     * later.
     * @param outState bundle that will contain this fragments current fields.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(RoutineState.ROUTINE_ID,mRoutineID);
        outState.putBoolean(RoutineState.NEW_ROUTINE,mIsNewRoutine);
        outState.putStringArray(RoutineState.ROUTINE_NAME,new String[]{mInitialName,mNameEditText.getText().toString().trim()});
        outState.putStringArray(RoutineState.ROUTINE_TIME,new String[]{mInitialTime,((RadioButton) mTimeRadioGroup.findViewById(mTimeRadioGroup.getCheckedRadioButtonId())).getText().toString()});
        outState.putStringArray(RoutineState.ROUTINE_COMMENT,new String[]{mInitialComment,mCommentEditText.getText().toString().trim()});
        outState.putStringArray(RoutineState.ROUTINE_FREQUENCY,new String[]{mInitialFrequency,getFrequencyString()});

        super.onSaveInstanceState(outState);
    }

    /**
     * Restores fields of this fragment from the restored instance state.
     * @param savedInstance the restored state
     */
    public void restoreSavedInstance(Bundle savedInstance){
        mRoutineID = savedInstance.getLong(RoutineState.ROUTINE_ID);
        mIsNewRoutine = savedInstance.getBoolean(RoutineState.NEW_ROUTINE);

        String[] names = savedInstance.getStringArray(RoutineState.ROUTINE_NAME);
        if (names != null){
            mInitialName = names[0];
            mNameEditText.setText(names[1]);
        }

        String[] time = savedInstance.getStringArray(RoutineState.ROUTINE_TIME);
        if (time != null){
            mInitialTime = time[0];
            setSelectedRadioButton(time[1]);
        }

        String[] comment = savedInstance.getStringArray(RoutineState.ROUTINE_COMMENT);
        if (comment != null){
            mInitialComment = comment[0];
            mCommentEditText.setText(comment[1]);
        }

        String[] frequency = savedInstance.getStringArray(RoutineState.ROUTINE_FREQUENCY);
        if (frequency != null){
            mInitialFrequency = frequency[0];
            setFrequencyBlock(frequency[1]);
        }

        mInitialLoadComplete = true;
    }

    /**
     * Refreshes the list of products belonging to this routine.
     */
    public void refreshProducts(){
        mProductsListView.setAdapter(null);
        new LoadRoutineProductsTask().execute(mRoutineID);
    }

    /**
     * Called by parent activity on products edit dialog closed. Refreshes
     * products list if products has been modified.
     */
    public void onEditDialogClosed(boolean listModified){
        if (listModified){
            refreshProducts();
            mProductsListModified = true;
        }
    }

    /**
     * Hides the layout of this fragment and displays the loading icon
     */
    private void showLoadingLayout(){
        mProgressLayout.setVisibility(View.VISIBLE);
        mDetailLayout.setVisibility(View.INVISIBLE);
    }

    /**
     * Hides the loading icon and shows the fragments layout
     */
    private void hideLoadingLayout(){
        mProgressLayout.setVisibility(View.INVISIBLE);
        mDetailLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Sets initial values for checking changes when exiting.
     */
    public void setInitialMemberValues(){
        mInitialName = mNameEditText.getText().toString().trim();
        mInitialTime = ((RadioButton)mTimeRadioGroup.findViewById(mTimeRadioGroup.getCheckedRadioButtonId())).getText().toString();
        mInitialComment = mCommentEditText.getText().toString().trim();
        mInitialFrequency = getFrequencyString();

        if (mProductsListView.getAdapter() != null && mProductsListView.getCount() > 0){
            showLayout(mProductsListView);
        }else {
            showLayout(mNoProductsTextView);
        }
    }

    private void showLayout(View view){
        mProductsListView.setVisibility(View.INVISIBLE);
        mNoProductsTextView.setVisibility(View.INVISIBLE);

        view.setVisibility(View.VISIBLE);
    }

    /**
     * Returns true if the fields in this fragment have changed since creation
     * @return true if the fields of this fragment have been changed
     */
    public boolean entryHasChanged(){
        String currName = mNameEditText.getText().toString().trim();
        int id = mTimeRadioGroup.getCheckedRadioButtonId();
        RadioButton selectedButton = (RadioButton) mTimeRadioGroup.findViewById(id);
        String currTime = selectedButton.getText().toString();
        String currComment = mCommentEditText.getText().toString().trim();
        String currFrequency = getFrequencyString();
        return (!mInitialName.equals(currName) || !mInitialTime.equals(currTime) || !mInitialComment.equals(currComment) || !mInitialFrequency.equals(currFrequency)|| mProductsListModified);
    }

    /**
     * Sets listeners of appropriate views in this fragment.
     */
    public void setListeners(){
        mEditProductsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProductsEditDialog();
            }
        });

        mProductsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), ProductActivityDetail.class);
                intent.putExtra(ProductActivityDetail.NEW_PRODUCT,false);
                intent.putExtra(ProductActivityDetail.ENTRY_ID,id);
                startActivity(intent);
            }
        });

        //Assign a cell listener to each cell in the weekday table
        for (TextView cell : mFrequencyTableColumns){
            cell.setOnClickListener(new TableCellOnClickListener());
        }


        //Show or hide frequency table based on which radio button is selected
        mFrequencyRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.routine_radio_button_specified){
                    mFrequencyTable.setVisibility(View.VISIBLE);
                }else{
                    mFrequencyTable.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * Listener class for cells in the weekday frequency table.
     */
    private class TableCellOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            boolean selected = (boolean) v.getTag();
            if (selected){ //If currently selected, set to unselected
                if (v.getId() == R.id.routine_frequency_text_saturday){
                    v.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.table_cell_unselected_end_background));
                }else {
                    v.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.table_cell_unselected_background));
                }
                ((TextView)v).setTextColor(ContextCompat.getColor(getContext(),R.color.newText));
                v.setTag(false);
            }else{ //If currently unselected, set to selected.
                if (v.getId() == R.id.routine_frequency_text_saturday){
                    v.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.table_cell_selected_end_background));
                }else{
                    v.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.table_cell_selected_background));
                }
                ((TextView)v).setTextColor(ContextCompat.getColor(getContext(),R.color.white));
                v.setTag(true);
            }
        }
    }

    /**
     * Loads the product list detail dialog when the user presses the edit
     * list button.
     */
    private void showProductsEditDialog(){
        Bundle args = new Bundle();
        args.putString(ItemListDialogFragment.DISPLAYED_DATA,ItemListDialogFragment.PRODUCTS);
        args.putLong(ItemListDialogFragment.ITEM_ID,mRoutineID);
        DialogFragment fragment = new ItemListDialogFragment();
        fragment.setArguments(args);
        fragment.show(getFragmentManager(),"dialog");
    }

    /**
     * Sets the member radio button to the selection passed into the method.
     * @param selectionText the text to match to a radio button option.
     */
    private void setSelectedRadioButton(String selectionText){
        for (int i = 0; i < mTimeRadioGroup.getChildCount(); i++){
            RadioButton button = (RadioButton) mTimeRadioGroup.getChildAt(i);
            if (selectionText.equals(button.getText().toString())){
                button.setChecked(true);
            }
        }
    }

    /**
     * Shows the routine demo dialog the first time this fragment is loaded.
     */
    private void showDemo(){
        final Dialog demoDialog = new Dialog(getContext(),android.R.style.Theme_Translucent_NoTitleBar);
        demoDialog.setContentView(R.layout.demo_layout);

        final TextView demoText = (TextView) demoDialog.findViewById(R.id.demo_layout_text_view);
        final Button demoButton = (Button) demoDialog.findViewById(R.id.demo_layout_button_next_done);

        demoText.setText(getResources().getString(R.string.routine_demo_text_first));
        demoButton.setTag(1); //Track current text with this buttons tag
        demoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int demoState = (int) demoButton.getTag();
                switch (demoState){
                    case 1:
                        demoText.setText(getResources().getString(R.string.routine_demo_text_second));
                        demoButton.setText(getResources().getString(R.string.main_demo_get_started));
                        demoButton.setTag(2);
                        break;
                    case 2:
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(getResources().getString(R.string.preference_routine_demo_seen),true).apply();
                        demoDialog.dismiss();
                        break;
                }
            }
        });
        demoDialog.show();
    }

    /**
     * Checks if fields are valid and if there have been any changes before saving
     * the current entry.
     */
    private void saveCurrentRoutine(){
        if(mNameEditText.getText().toString().trim().length() == 0) {
            Toast.makeText(getContext(), R.string.toast_enter_valid_name, Toast.LENGTH_SHORT).show();
        } else if ((mFrequencyRadioGroup.getCheckedRadioButtonId() == R.id.routine_radio_button_specified) && getSelectedDays().length() == 0){
            Toast.makeText(getContext(),R.string.routine_toast_select_days,Toast.LENGTH_LONG).show();
        }
        else {
            if(entryHasChanged()) {
                String name = mNameEditText.getText().toString().trim();
                String time = ((RadioButton) mTimeRadioGroup.findViewById(mTimeRadioGroup.getCheckedRadioButtonId())).getText().toString();
                String comment = mCommentEditText.getText().toString().trim();
                String frequency = getFrequencyString();

                String[] params = {name, time, comment,frequency};
                new SaveRoutineTask().execute(params);
            }else{
                Toast.makeText(getContext(), R.string.toast_save_success, Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
    }

    /**
     * Gets the current value of the frequency of this routine.
     * @return formatted string representation of this frequency.
     */
    private String getFrequencyString(){
        String frequency;
        int radioID = mFrequencyRadioGroup.getCheckedRadioButtonId();
        if (radioID == R.id.routine_radio_button_needed || radioID == R.id.routine_radio_button_daily){
            frequency = ((RadioButton) mFrequencyRadioGroup.findViewById(radioID)).getText().toString();
        }else{ //Otherwise get selected days
            frequency = getSelectedDays();
        }
        return frequency;
    }

    /**
     * Returns a constructed string of days that are selected in the weekday table.
     * @return formated string of selected days
     */
    private String getSelectedDays(){
        String days = "";
        for (TextView cell : mFrequencyTableColumns){
            if ((boolean)cell.getTag()){
                if (days.length() == 0) {
                    days += cell.getText().toString();
                }else {
                    days += "-"+cell.getText().toString();
                }
            }
        }
        return days;
    }

    /**
     * Sets the selected frequency radio button and selected days on the frequency table.
     * @param frequency string value that was stored in routine table
     */
    private void setFrequencyBlock(String frequency){
        if (frequency.equals(((TextView)mFrequencyRadioGroup.findViewById(R.id.routine_radio_button_needed)).getText().toString())){
            mFrequencyRadioGroup.check(mFrequencyRadioGroup.findViewById(R.id.routine_radio_button_needed).getId());
        }else if (frequency.equals(((TextView)mFrequencyRadioGroup.findViewById(R.id.routine_radio_button_daily)).getText().toString())){
            mFrequencyRadioGroup.check(mFrequencyRadioGroup.findViewById(R.id.routine_radio_button_daily).getId());
        }else{ //Otherwise select "on selected days" and populate the correct days
            mFrequencyRadioGroup.check(mFrequencyRadioGroup.findViewById(R.id.routine_radio_button_specified).getId());
            mFrequencyTable.setVisibility(View.VISIBLE);
            if (frequency.length() > 0) {
                String[] days = frequency.split("-");
                for (int i = 0; i < days.length; i++){
                    String day = days[i];
                    for (TextView cell : mFrequencyTableColumns){
                        if (day.equals(cell.getText().toString())){
                            if (cell.getId() == R.id.routine_frequency_text_saturday){
                                cell.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.table_cell_selected_end_background));
                            }else{
                                cell.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.table_cell_selected_background));
                            }
                            cell.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
                            cell.setTag(true);
                        }
                    }
                }
            }
        }
    }

    /**
     * Deletes this routine entry. Called when the user presses the delete button.
     */
    private void deleteCurrentRoutine(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.routine_delete_alert_dialog_title)
                .setMessage(R.string.routine_delete_alert_dialog_message)
                .setIcon(R.drawable.ic_warning_black_24dp)
                .setPositiveButton(R.string.alert_delete_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new DeleteRoutineTask().execute(true);
                    }
                })
                .setNegativeButton(R.string.cancel_string, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    /**
     * Method called by parent activity when the user presses the home button or the physical back button.
     * Asks the user if they want to save changes if there are changes to save.
     */
    public void onBackButtonPressed(){
        if (entryHasChanged()) {
            if (mIsNewRoutine) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.routine_back_alert_dialog_message)
                        .setTitle(R.string.routine_back_alert_dialog_title)
                        .setPositiveButton(R.string.save_button_string, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                saveCurrentRoutine();
                            }
                        })
                        .setNegativeButton(R.string.no_string, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new DeleteRoutineTask().execute(false);
                            }
                        })
                        .setNeutralButton(R.string.cancel_string, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            } else {
                saveCurrentRoutine();
            }
        } else if (mIsNewRoutine) {
            new DeleteRoutineTask().execute(false);
        } else {
            getActivity().finish();
        }
    }


    /**
     * Task to load a routine and its products from the database. Used only on initial load of
     * an existing routine.
     */
    private class InitialLoadRoutineTask extends AsyncTask<Long,Void,Cursor[]> {

        @Override
        protected Cursor[] doInBackground(Long... params) {
            Cursor[] cursors = new Cursor[2]; //2 cursors, first from Routine, second from RoutineProduct
            SQLiteDatabase db = mDbHelper.getReadableDatabase();

            //Fetch routine information from routine table
            String[] routineColumns = {DiaryContract.Routine.COLUMN_NAME, DiaryContract.Routine.COLUMN_TIME, DiaryContract.Routine.COLUMN_COMMENT, DiaryContract.Routine.COLUMN_FREQUENCY};
            String routineWhere = DiaryContract.Routine._ID + " = "+params[0];
            cursors[0] = db.query(DiaryContract.Routine.TABLE_NAME,routineColumns,routineWhere,null,null,null,null);

            //Fetch IDs of products in this routine from RoutineProduct table
            String[] routineProductColumns = {DiaryContract.RoutineProduct.COLUMN_PRODUCT_ID};
            String routineProductWhere = DiaryContract.RoutineProduct.COLUMN_ROUTINE_ID + " = "+params[0];
            Cursor routineProducts = db.query(DiaryContract.RoutineProduct.TABLE_NAME,routineProductColumns,routineProductWhere,null,null,null,null);

            String[] productColumns = {DiaryContract.Product._ID,DiaryContract.Product.COLUMN_NAME,DiaryContract.Product.COLUMN_BRAND, DiaryContract.Product.COLUMN_TYPE};
            String productWhere = "";
            //Build where clause
            if (routineProducts!= null) {
                if (routineProducts.moveToFirst()){
                    productWhere = DiaryContract.Product._ID + " = " + Integer.toString(routineProducts.getInt(routineProducts.getColumnIndex(DiaryContract.RoutineProduct.COLUMN_PRODUCT_ID)));
                    while(routineProducts.moveToNext()) {
                        int productID = routineProducts.getInt(routineProducts.getColumnIndex(DiaryContract.RoutineProduct.COLUMN_PRODUCT_ID));
                        productWhere += " OR " + DiaryContract.Product._ID + " = " + Integer.toString(productID);
                    }
                }
                routineProducts.close();
            }

            //Fetch Names of products in this routine table using IDs
            if (productWhere.length() > 0) {
                cursors[1] = db.query(DiaryContract.Product.TABLE_NAME,productColumns,productWhere,null,null,null, DiaryContract.Product.COLUMN_NAME + " ASC");
            }
            return cursors;
        }

        /**
         * Populates the fields of this fragment with data from both the Routine and Product table
         */
        @Override
        protected void onPostExecute(final Cursor[] result){
            Cursor routineCursor = result[0];
            Cursor productCursor = result[1];
            if (result[0] != null) {
                if (routineCursor.moveToFirst()) { //Populate data from routine
                    String name = routineCursor.getString(routineCursor.getColumnIndex(DiaryContract.Routine.COLUMN_NAME));
                    String time = routineCursor.getString(routineCursor.getColumnIndex(DiaryContract.Routine.COLUMN_TIME));
                    String comment = routineCursor.getString(routineCursor.getColumnIndex(DiaryContract.Routine.COLUMN_COMMENT));
                    String frequency = routineCursor.getString(routineCursor.getColumnIndex(DiaryContract.Routine.COLUMN_FREQUENCY));

                    mNameEditText.setText(name);
                    if(time != null)
                        setSelectedRadioButton(time);
                    else  //Default saved time value if one was not saved.
                        setSelectedRadioButton(getString(R.string.routine_radio_AM));
                    mCommentEditText.setText(comment);

                    if (frequency != null) {
                        setFrequencyBlock(frequency);
                    }
                }
            }
            if (result[1] != null) {
                if (productCursor.moveToFirst()) { //Populate data from products
                    String[] fromColumns = {DiaryContract.Product.COLUMN_NAME, DiaryContract.Product.COLUMN_BRAND, DiaryContract.Product.COLUMN_TYPE};
                    int[] toViews = {R.id.product_listview_name,R.id.product_listview_brand,R.id.product_listview_type};
                    SimpleCursorAdapter adapter = new SimpleCursorAdapter(getContext(),R.layout.listview_item_product_main,productCursor,fromColumns,toViews,0);
                    adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder(){

                        /**
                         * Custom handling of textviews being populated from adapter
                         * @param view The view corresponding to this cursor entry
                         * @param cursor The cursor
                         * @param columnIndex Current column index
                         * @return true if this method was used, false otherwise
                         */
                        @Override
                        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                            if (columnIndex == cursor.getColumnIndex(DiaryContract.Product.COLUMN_NAME)){
                                TextView nameView = (TextView) view;
                                String name = cursor.getString(cursor.getColumnIndex(DiaryContract.Product.COLUMN_NAME));
                                nameView.setText(name);
                                return true;
                            }else if (columnIndex == cursor.getColumnIndex(DiaryContract.Product.COLUMN_BRAND)){
                                TextView brandView = (TextView) view;
                                String brand = cursor.getString(cursor.getColumnIndex(DiaryContract.Product.COLUMN_BRAND));
                                brandView.setText(brand);
                                return true;
                            }else if (columnIndex == cursor.getColumnIndex(DiaryContract.Product.COLUMN_TYPE)){
                                TextView typeView = (TextView) view;
                                String type = cursor.getString(cursor.getColumnIndex(DiaryContract.Product.COLUMN_TYPE));
                                typeView.setText(type);
                                return true;
                            }
                            return false;
                        }
                    });
                    mProductsListView.setAdapter(adapter);
                }
            }
            hideLoadingLayout();
            setInitialMemberValues();
            mInitialLoadComplete = true;
        }
    }

    /**
     * Pulls values stored in the routineProducts table to populate the product
     * list for this routine. Called on fragment resume after the routineProduct table has
     * been modified from the product list edit button.
     */
    private class LoadRoutineProductsTask extends AsyncTask<Long,Void,Cursor>{

        @Override
        protected Cursor doInBackground(Long... params) {
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            Cursor result = null;

            //Fetch IDs of products in this routine from the routineProducts table
            String[] routineProductsColumns = {DiaryContract.RoutineProduct.COLUMN_PRODUCT_ID};
            String routineProductsWhere = DiaryContract.RoutineProduct.COLUMN_ROUTINE_ID + " = "+params[0];
            Cursor routineProducts = db.query(DiaryContract.RoutineProduct.TABLE_NAME,routineProductsColumns,routineProductsWhere,null,null,null,null);

            //Build where clause for Product table query from cursor result.
            String[] productColumns = {DiaryContract.Product._ID,DiaryContract.Product.COLUMN_NAME,DiaryContract.Product.COLUMN_BRAND, DiaryContract.Product.COLUMN_TYPE};
            String productWhere = "";

            //Build where clause
            if (routineProducts!= null) {
                if (routineProducts.moveToFirst()){
                    productWhere = DiaryContract.Product._ID + " = " + Integer.toString(routineProducts.getInt(routineProducts.getColumnIndex(DiaryContract.RoutineProduct.COLUMN_PRODUCT_ID)));
                    while(routineProducts.moveToNext()) {
                        int productID = routineProducts.getInt(routineProducts.getColumnIndex(DiaryContract.RoutineProduct.COLUMN_PRODUCT_ID));
                        productWhere += " OR " + DiaryContract.Product._ID + " = " + Integer.toString(productID);
                    }
                }
                routineProducts.close();
            }

            //Fetch product information for this routine from Products table using IDs
            if (productWhere.length() > 0) {
                result = db.query(DiaryContract.Product.TABLE_NAME,productColumns,productWhere,null,null,null, DiaryContract.Product.COLUMN_NAME + " ASC");
            }
            return result;
        }

        /*
        Sets the adapter for the products list if this routine has products
         */
        @Override
        protected void onPostExecute(Cursor result){
            if (result != null) {
                if (result.moveToFirst()) { //Populate data from products
                    String[] fromColumns = {DiaryContract.Product.COLUMN_NAME, DiaryContract.Product.COLUMN_BRAND, DiaryContract.Product.COLUMN_TYPE};
                    int[] toViews = {R.id.product_listview_name,R.id.product_listview_brand,R.id.product_listview_type};
                    SimpleCursorAdapter adapter = new SimpleCursorAdapter(getContext(),R.layout.listview_item_product_main,result,fromColumns,toViews,0);
                    adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder(){

                        /**
                         * Custom handling of textviews being populated from adapter
                         * @param view The view corresponding to this cursor entry
                         * @param cursor The cursor
                         * @param columnIndex Current column index
                         * @return true if this method was used, false otherwise
                         */
                        @Override
                        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                            if (columnIndex == cursor.getColumnIndex(DiaryContract.Product.COLUMN_NAME)){
                                TextView nameView = (TextView) view;
                                String name = cursor.getString(cursor.getColumnIndex(DiaryContract.Product.COLUMN_NAME));
                                nameView.setText(name);
                                return true;
                            }else if (columnIndex == cursor.getColumnIndex(DiaryContract.Product.COLUMN_BRAND)){
                                TextView brandView = (TextView) view;
                                String brand = cursor.getString(cursor.getColumnIndex(DiaryContract.Product.COLUMN_BRAND));
                                brandView.setText(brand);
                                return true;
                            }else if (columnIndex == cursor.getColumnIndex(DiaryContract.Product.COLUMN_TYPE)){
                                TextView typeView = (TextView) view;
                                String type = cursor.getString(cursor.getColumnIndex(DiaryContract.Product.COLUMN_TYPE));
                                typeView.setText(type);
                                return true;
                            }
                            return false;
                        }
                    });
                    mProductsListView.setAdapter(adapter);
                }
            }

            if (mProductsListView.getAdapter() != null && mProductsListView.getAdapter().getCount() > 0){
                showLayout(mProductsListView);
            }else {
                showLayout(mNoProductsTextView);
            }
        }
    }

    /**
     * Task to save the routine information to the routine table. Saving to routineProduct is
     * unnecessary as that information is saved in the edit products fragment of this routine.
     */
    private class SaveRoutineTask extends AsyncTask<String,Void,Long>{

        @Override
        protected Long doInBackground(String... params) {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(DiaryContract.Routine.COLUMN_NAME,params[0]);
            values.put(DiaryContract.Routine.COLUMN_TIME,params[1]);
            values.put(DiaryContract.Routine.COLUMN_COMMENT,params[2]);
            values.put(DiaryContract.Routine.COLUMN_FREQUENCY,params[3]);

            String where = DiaryContract.Routine._ID + " = ?";
            String[] whereArgs = {mRoutineID.toString()};
            int rows = db.update(DiaryContract.Routine.TABLE_NAME,values,where,whereArgs);

            return Long.valueOf(rows == 0? -1 : 1); //return -1 if no rows affected, error storing
        }

        @Override
        protected void onPostExecute(Long result){
            if (result == -1){
                Toast.makeText(getContext(),R.string.toast_save_failed,Toast.LENGTH_SHORT).show();
            }else {
                //Pass the id of the new routine down to calling activity
                Intent intent = new Intent();
                intent.putExtra(RoutineActivityMain.ROUTINE_FINISHED_ID,mRoutineID);
                getActivity().setResult(Activity.RESULT_OK,intent);
                Toast.makeText(getContext(), R.string.toast_save_success, Toast.LENGTH_SHORT).show();
            }
            getActivity().finish();
        }
    }

    /**
     * Called when a new routine is created, creates a placeholder routine to save to the Routine
     * database in order to have an ID to link to the RoutineProducts table when adding products to the new
     * routine.
     */
    private class SaveRoutinePlaceholderTask extends AsyncTask<Void,Void,Long>{

        @Override
        protected Long doInBackground(Void... params) {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DiaryContract.Routine.COLUMN_NAME,"PlaceholderRoutine");
            values.put(DiaryContract.Routine.COLUMN_TIME,getResources().getString(R.string.routine_radio_AM));
            values.put(DiaryContract.Routine.COLUMN_FREQUENCY,getResources().getString(R.string.routine_radio_frequency_needed));
            return db.insert(DiaryContract.Routine.TABLE_NAME,null,values);
        }

        @Override
        protected void onPostExecute(final Long result){
            if (result == -1){
                Toast.makeText(getContext(),R.string.toast_create_failed,Toast.LENGTH_SHORT).show();
            }else {
                mRoutineID = result;
            }
            mIsNewRoutine = true;
            hideLoadingLayout();
            mTimeRadioGroup.check(R.id.routine_radio_button_am);  //Set default time to AM
            mFrequencyRadioGroup.check(R.id.routine_radio_button_needed); //Default value of frequency is as needed
            setInitialMemberValues();
            mInitialLoadComplete = true;
        }
    }

    /**
     * Task to delete a routine and its linked products in RoutineProduct from the database.
     * Called when user taps delete button or if this is a new entry and routine
     * information has not been saved on activity destroy.
     */
    private class DeleteRoutineTask extends AsyncTask<Boolean,Void,Integer>{

        private boolean showToast;

        @Override
        protected Integer doInBackground(Boolean... params) {
            showToast = params[0];
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            String where = DiaryContract.Routine._ID + " = ?";
            String[] whereArgs = {mRoutineID.toString()};
            return db.delete(DiaryContract.Routine.TABLE_NAME, where, whereArgs);
        }

        @Override
        protected void onPostExecute(final Integer result){
            if(result == 1){
                if(showToast) Toast.makeText(getContext(),R.string.toast_delete_successful,Toast.LENGTH_SHORT).show();
            }else{
                if(showToast) Toast.makeText(getContext(),R.string.toast_delete_failed,Toast.LENGTH_SHORT).show();
            }
            getActivity().finish();
        }
    }

    /**
     * Class to hold static strings used as keys when saving or restoring instance state.
     */
    protected static class RoutineState{
        protected static final String ROUTINE_ID = "ROUTINE_ID";
        protected static final String NEW_ROUTINE = "NEW_ROUTINE";
        protected static final String ROUTINE_NAME = "ROUTINE_NAME";
        protected static final String ROUTINE_TIME = "ROUTINE_TIME";
        protected static final String ROUTINE_COMMENT = "ROUTINE_COMMENT";
        protected static final String ROUTINE_FREQUENCY = "ROUTINE_FREQUENCY";
    }
}
