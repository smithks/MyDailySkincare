package com.smithkeegan.mydailyskincare.routine;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
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
import android.widget.TextView;
import android.widget.Toast;

import com.smithkeegan.mydailyskincare.customClasses.ItemListDialogFragment;
import com.smithkeegan.mydailyskincare.R;
import com.smithkeegan.mydailyskincare.data.DiaryContract;
import com.smithkeegan.mydailyskincare.data.DiaryDbHelper;
import com.smithkeegan.mydailyskincare.product.ProductActivityDetail;

/**
 * Fragment class for the product detail screen. Handles actions to manipulate
 * information about a proudct and making those updates to the database.
 * @author Keegan Smith
 * @since 8/5/2016
 */
public class RoutineFragmentDetail extends Fragment {

    private EditText mNameEditText;
    private RadioGroup mTimeRadioGroup;
    private ListView mProductsListView;
    private EditText mCommentEditText;
    private Button mEditProductsButton;

    private View mProgressLayout;
    private View mDetailLayout;

    private DiaryDbHelper mDbHelper;
    private String mInitialName;
    private String mInitialTime;
    private String mInitialComment;
    private boolean mInitialLoadComplete;
    private boolean mIsNewRoutine;
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

        setListeners();

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
        mTimeRadioGroup = (RadioGroup) rootView.findViewById(R.id.routine_radio_group);
        mProductsListView = (ListView) rootView.findViewById(R.id.routine_product_list_view);
        mCommentEditText = (EditText) rootView.findViewById(R.id.routine_comment_edit);

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
        return (!mInitialName.equals(currName) || !mInitialTime.equals(currTime) || !mInitialComment.equals(currComment));
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
     * Checks if fields are valid and if there have been any changes before saving
     * the current entry.
     */
    private void saveCurrentRoutine(){
        if(mNameEditText.getText().toString().trim().length() == 0) {
            Toast.makeText(getContext(), R.string.toast_enter_valid_name, Toast.LENGTH_SHORT).show();
        }else {
            if(entryHasChanged()) {
                String name = mNameEditText.getText().toString().trim();
                String time = ((RadioButton) mTimeRadioGroup.findViewById(mTimeRadioGroup.getCheckedRadioButtonId())).getText().toString();
                String comment = mCommentEditText.getText().toString().trim();
                String[] params = {name, time, comment};
                new SaveRoutineTask().execute(params);
            }else{
                Toast.makeText(getContext(), R.string.toast_save_success, Toast.LENGTH_SHORT).show();
                getActivity().finish();
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
            saveCurrentRoutine();
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
            String[] routineColumns = {DiaryContract.Routine.COLUMN_NAME, DiaryContract.Routine.COLUMN_TIME, DiaryContract.Routine.COLUMN_COMMENT};
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

                    mNameEditText.setText(name);
                    if(time != null)
                        setSelectedRadioButton(time);
                    else  //Default saved time value if one was not saved.
                        setSelectedRadioButton(getString(R.string.routine_radio_AM));
                    mCommentEditText.setText(comment);
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
    }
}
