package com.smithkeegan.mydailyskincare.product;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.smithkeegan.mydailyskincare.R;
import com.smithkeegan.mydailyskincare.data.DiaryContract;
import com.smithkeegan.mydailyskincare.data.DiaryDbHelper;

/**
 * @author Keegan Smith
 * @since 5/19/2016
 */
public class ProductFragmentDetail extends Fragment {

    private DiaryDbHelper mDbHelper;

    private EditText mNameEditText;
    private EditText mBrandEditText;

    private Boolean mNewEntry;
    private Long mExistingId;
    private String mInitialName;
    private String mInitialBrand;
    //TODO add type

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        View rootView = inflater.inflate(R.layout.fragment_product_detail, container, false);

        mDbHelper = DiaryDbHelper.getInstance(getContext());
        mNameEditText = (EditText) rootView.findViewById(R.id.product_name_edit);
        mBrandEditText = (EditText) rootView.findViewById(R.id.product_brand_edit);

        Bundle args = getArguments();
        mNewEntry = args.getBoolean(ProductActivityDetail.NEW_PRODUCT,true);
        mExistingId = args.getLong(ProductActivityDetail.ENTRY_ID,-1);

        if (mNewEntry || mExistingId < 0) { //New entry or error loading
            mNameEditText.setTextColor(ContextCompat.getColor(getContext(),R.color.newText));
            mInitialName = mNameEditText.getText().toString();
            mInitialBrand = mBrandEditText.getText().toString();
        } else{ //Load data from existing entry
            new LoadProductTask().execute(mExistingId);
        }
        return rootView;
    }



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
            }
        }
    }
}
