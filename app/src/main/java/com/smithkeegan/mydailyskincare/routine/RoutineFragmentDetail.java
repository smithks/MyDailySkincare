package com.smithkeegan.mydailyskincare.routine;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.smithkeegan.mydailyskincare.R;
import com.smithkeegan.mydailyskincare.data.DiaryContract;
import com.smithkeegan.mydailyskincare.data.DiaryDbHelper;

/**
 * @author Keegan Smith
 * @since 8/5/2016
 */
public class RoutineFragmentDetail extends Fragment {

    private EditText mNameEditText;
    private RadioGroup mTimeRadioGroup;
    private ListView mProductsListView;
    private EditText mCommentEditText;
    private Button mEditProductsButton;
    private Button mDeleteButton;
    private Button mSaveButton;

    private View mProgressLayout;
    private View mDetailLayout;

    private DiaryDbHelper mDbHelper;
    private String mInitialName;
    private String mInitialTime;
    private String mInitialComment;
    private Boolean mInitialLoadComplete;
    private Boolean mIsNewRoutine;
    private Long mRoutineID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_routine_detail,container,false);

        mDbHelper = DiaryDbHelper.getInstance(getContext());

        fetchViews(rootView);

        setListeners();

        return rootView;
    }

    public void fetchViews(View rootView){
        mNameEditText = (EditText) rootView.findViewById(R.id.routine_name_edit);
        mTimeRadioGroup = (RadioGroup) rootView.findViewById(R.id.routine_radio_group);
        mProductsListView = (ListView) rootView.findViewById(R.id.routine_product_list_view);
        mCommentEditText = (EditText) rootView.findViewById(R.id.routine_comment_edit);

        mEditProductsButton = (Button) rootView.findViewById(R.id.routine_edit_products);
        mDeleteButton = (Button) rootView.findViewById(R.id.routine_delete_button);
        mSaveButton = (Button) rootView.findViewById(R.id.routine_save_button);

        mProgressLayout = (View) rootView.findViewById(R.id.routine_loading_layout);
        mDetailLayout = (View) rootView.findViewById(R.id.routine_fragment_detail_layout);
    }

    //Hides the layout of this fragment and displays the loading icon
    private void showLoadingLayout(){
        mProgressLayout.setVisibility(View.VISIBLE);
        mDetailLayout.setVisibility(View.INVISIBLE);
    }

    //Hides the loading icon and shows the fragments layout
    private void hideLoadingLayout(){
        mProgressLayout.setVisibility(View.INVISIBLE);
        mDetailLayout.setVisibility(View.VISIBLE);
    }

    //Sets initial values for checking changes when exiting.
    public void setInitialValues(){
        mInitialName = mNameEditText.getText().toString().trim();
        //TODO get time
        mInitialComment = mCommentEditText.getText().toString().trim();
    }

    public void setListeners(){

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

            //Fetch routine information from product table
            String[] routineColumns = {DiaryContract.Routine.COLUMN_NAME, DiaryContract.Routine.COLUMN_TIME};
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
                cursors[1] = db.query(DiaryContract.Product.TABLE_NAME,productColumns,productWhere,null,null,null,null);
            }
            return cursors;
        }

        /*
        Populates the fields of this fragment with data from both the Routine and Product table
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
                    //TODO set time checked
                    mCommentEditText.setText(comment);
                }
            }
            if (result[1] != null) {
                if (productCursor.moveToFirst()) { //Populate data from products
                    String[] fromColumns = {DiaryContract.Product.COLUMN_NAME, DiaryContract.Product.COLUMN_BRAND, DiaryContract.Product.COLUMN_TYPE};
                    int[] toViews = {R.id.product_listview_name,R.id.product_listview_brand,R.id.product_listview_type};
                    SimpleCursorAdapter adapter = new SimpleCursorAdapter(getContext(),R.layout.product_listview_item,productCursor,fromColumns,toViews,0);
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
            setInitialValues();
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
                result = db.query(DiaryContract.Product.TABLE_NAME,productColumns,productWhere,null,null,null,null);
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
                    SimpleCursorAdapter adapter = new SimpleCursorAdapter(getContext(),R.layout.product_listview_item,result,fromColumns,toViews,0);
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
            hideLoadingLayout();
            setInitialValues();
            mIsNewRoutine = true;
            mDeleteButton.setVisibility(View.GONE);
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
}
