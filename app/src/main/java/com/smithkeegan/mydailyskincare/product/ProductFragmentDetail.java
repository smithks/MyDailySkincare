package com.smithkeegan.mydailyskincare.product;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.smithkeegan.mydailyskincare.R;
import com.smithkeegan.mydailyskincare.data.DiaryContract;
import com.smithkeegan.mydailyskincare.data.DiaryDbHelper;

/**
 * @author Keegan Smith
 * @since 5/19/2016
 */
public class ProductFragmentDetail extends Fragment {

    private DiaryDbHelper mDbHelper;

    private View mProgressLayout;
    private View mDetailLayout;
    private EditText mNameEditText;
    private EditText mBrandEditText;
    private Spinner mTypeSpinner;
    private Button mSaveButton;
    private Button mDeleteButton;

    private Boolean mNewEntry;
    private Long mExistingId;
    private String mInitialName;
    private String mInitialBrand;
    //TODO add field for product type

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        View rootView = inflater.inflate(R.layout.fragment_product_detail, container, false);

        mDbHelper = DiaryDbHelper.getInstance(getContext());
        mNameEditText = (EditText) rootView.findViewById(R.id.product_name_edit);
        mBrandEditText = (EditText) rootView.findViewById(R.id.product_brand_edit);
        mSaveButton = (Button) rootView.findViewById(R.id.product_save_button);
        mDeleteButton = (Button) rootView.findViewById(R.id.product_delete_button);
        mTypeSpinner = (Spinner) rootView.findViewById(R.id.product_type_spinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getContext(),R.array.product_types_array,R.layout.spinner_layout);
        spinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        mTypeSpinner.setAdapter(spinnerAdapter);

        mProgressLayout = rootView.findViewById(R.id.product_loading_layout);
        mDetailLayout = rootView.findViewById(R.id.product_fragment_detail_layout);
        showLoadingLayout();

        Bundle args = getArguments();
        mNewEntry = args.getBoolean(ProductActivityDetail.NEW_PRODUCT,true);
        mExistingId = args.getLong(ProductActivityDetail.ENTRY_ID,-1);

        if (mNewEntry || mExistingId < 0) { //New entry or error loading
            //TODO on new entry, save to databse to get id to use for storing product-ingredient relationship
            mInitialName = mNameEditText.getText().toString();
            mInitialBrand = mBrandEditText.getText().toString();
        } else{ //Load data from existing entry
            new LoadProductTask().execute(mExistingId);
        }
        return rootView;
    }

    private void showLoadingLayout(){
        mProgressLayout.setVisibility(View.VISIBLE);
        mDetailLayout.setVisibility(View.INVISIBLE);
    }

    private void hideLoadingLayout(){
        mProgressLayout.setVisibility(View.INVISIBLE);
        mDetailLayout.setVisibility(View.VISIBLE);
    }


    /**
     * Task to load a product and its ingredients from the database.
     */
    //TODO load from product_ingredients table as well to fill ingredients list view
    private class LoadProductTask extends AsyncTask<Long,Void,Cursor[]>{

        @Override
        protected Cursor[] doInBackground(Long... params) {
            Cursor[] cursors = new Cursor[2]; //2 cursors, first from Products, second from ProductIngredients
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            String[] columns = {DiaryContract.Product.COLUMN_NAME, DiaryContract.Product.COLUMN_BRAND, DiaryContract.Product.COLUMN_TYPE};
            //TODO load from product_ingredients table as well to fill ingredients list view
            //TODO place returned cursor in cursor array
            String where = DiaryContract.Product._ID + " = "+params[0];
            cursors[0] = db.query(DiaryContract.Product.TABLE_NAME,columns,where,null,null,null,null);
            return cursors;
        }

        @Override
        protected void onPostExecute(final Cursor[] result){
            Cursor productCursor = result[0];
            if(productCursor.moveToFirst()){
                String name = productCursor.getString(productCursor.getColumnIndex(DiaryContract.Product.COLUMN_NAME));
                String brand = productCursor.getString(productCursor.getColumnIndex(DiaryContract.Product.COLUMN_BRAND));

                mNameEditText.setText(name);
                mBrandEditText.setText(brand);
                hideLoadingLayout();
            }
        }
    }

    /**
     * Task to save a product and its ingredients to the database.
     */
    private class SaveProductTask extends AsyncTask<String,Void,Long>{

        @Override
        protected Long doInBackground(String... params) {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            return null;
        }

        @Override
        protected void onPostExecute(final Long result){

        }
    }

    /**
     * Task to delete a product and its ingredients from the database.
     */
    private class DeleteProductTask extends AsyncTask<Void,Void,Integer>{

        @Override
        protected Integer doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(final Integer result){

        }
    }
}
