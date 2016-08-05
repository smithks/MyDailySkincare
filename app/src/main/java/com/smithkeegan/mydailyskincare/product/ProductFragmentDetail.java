package com.smithkeegan.mydailyskincare.product;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.smithkeegan.mydailyskincare.ItemListDialogFragment;
import com.smithkeegan.mydailyskincare.R;
import com.smithkeegan.mydailyskincare.data.DiaryContract;
import com.smithkeegan.mydailyskincare.data.DiaryDbHelper;

import java.util.ArrayList;

/**
 * @author Keegan Smith
 * @since 5/19/2016
 *
 * //TODO: on clicking ingredients in this fragment, open the ingredient detail activity
 * //TODO need default type
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
    private Button mEditIngredientsButton;
    private ListView mIngredientsList;

    private Long mProductId;
    private String mInitialName;
    private String mInitialBrand;
    private String mInitialType;
    private boolean mInitialLoadComplete;
    private boolean mIsNewProduct;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        View rootView = inflater.inflate(R.layout.fragment_product_detail, container, false);

        fetchViews(rootView);

        Bundle args = getArguments();
        Boolean mNewEntry = args.getBoolean(ProductActivityDetail.NEW_PRODUCT, true);
        mProductId = args.getLong(ProductActivityDetail.ENTRY_ID,-1);

        if (mNewEntry || mProductId < 0) { //New entry or error loading
            new SaveProductPlaceholderTask().execute();
        } else{ //Load data from existing entry
            new InitialLoadProductTask().execute(mProductId);
        }

        setListeners();

        return rootView;
    }

    /**
     * Refreshes data in the ingredients list if this is not the initial
     * load of the fragment.
     */
    @Override
    public void onResume(){
        super.onResume();
        if (mInitialLoadComplete){
            refreshIngredients();
        }
    }

    //Returns whether this enties values have changed since the form has been opened.
    public boolean entryHasChanged(){
        String currentName = mNameEditText.getText().toString().trim();
        String currentBrand = mBrandEditText.getText().toString().trim();
        String currentType = mTypeSpinner.getSelectedItem().toString();
        return (!mInitialName.equals(currentName) || (!mInitialBrand.equals(currentBrand)) || (!mInitialType.equals(currentType)));
    }

    //Sets the intial member values of this form for comparison when exiting.
    public void setInitialValues(){
        mInitialName = mNameEditText.getText().toString().trim();
        mInitialBrand = mBrandEditText.getText().toString().trim();
        mInitialType = mTypeSpinner.getSelectedItem().toString();
    }

    public void refreshIngredients(){
        mIngredientsList.setAdapter(null);
        new LoadProductIngredientsTask().execute(mProductId);
    }

    public void saveCurrentProduct(){
        String productName = mNameEditText.getText().toString().trim();
        String productBrand = mBrandEditText.getText().toString().trim();
        String productType = mTypeSpinner.getSelectedItem().toString();
        String[] params = {productName, productBrand, productType};
        new SaveProductTask().execute(params);
    }

    /*
      * Method called by parent activity when the user presses the home button or the physical back button.
      * Asks the user if they want to save changes if there are changes to save.
     */
    public void onBackButtonPressed(){
        if (entryHasChanged() || (mIsNewProduct && entryHasChanged())) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Save changes made to this product?")
                    .setTitle("Save changes?")
                    .setIcon(R.drawable.ic_warning_black_24dp)
                    .setPositiveButton(R.string.save_button_string, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveCurrentProduct();
                        }
                    })
                    .setNegativeButton(R.string.no_string, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(mIsNewProduct) new DeleteProductTask().execute(false);
                            dialog.dismiss();
                            getActivity().finish();
                        }
                    }).show();
        } else if (mIsNewProduct) {
            new DeleteProductTask().execute(false);
        } else {
            getActivity().finish();
        }
    }

    private void fetchViews(View rootView){
        mDbHelper = DiaryDbHelper.getInstance(getContext());
        mNameEditText = (EditText) rootView.findViewById(R.id.product_name_edit);
        mBrandEditText = (EditText) rootView.findViewById(R.id.product_brand_edit);
        mSaveButton = (Button) rootView.findViewById(R.id.product_save_button);
        mDeleteButton = (Button) rootView.findViewById(R.id.product_delete_button);
        mEditIngredientsButton = (Button) rootView.findViewById(R.id.product_edit_ingredients);
        mIngredientsList = (ListView) rootView.findViewById(R.id.product_ingredient_list);

        mTypeSpinner = (Spinner) rootView.findViewById(R.id.product_type_spinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getContext(),R.array.product_types_array,R.layout.spinner_layout);
        spinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        mTypeSpinner.setAdapter(spinnerAdapter);

        mProgressLayout = rootView.findViewById(R.id.product_loading_layout);
        mDetailLayout = rootView.findViewById(R.id.product_fragment_detail_layout);
        showLoadingLayout();
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

    //Sets listeners for the buttons
    private void setListeners(){
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.alert_delete_product_message)
                        .setTitle(R.string.alert_delete_product_title)
                        .setIcon(R.drawable.ic_warning_black_24dp)
                        .setPositiveButton(R.string.alert_delete_confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new DeleteProductTask().execute(true);
                            }
                        })
                        .setNegativeButton(R.string.cancel_string, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(entryHasChanged())
                    saveCurrentProduct();
                else
                    Toast.makeText(getContext(),R.string.no_changes_string, Toast.LENGTH_SHORT).show();
            }
        });

        mEditIngredientsButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                showEditDialog();
            }
        });

        mIngredientsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showEditDialog();
            }
        });
    }

    private void showEditDialog(){
        Bundle args = new Bundle();
        args.putString(ItemListDialogFragment.DISPLAYED_DATA,ItemListDialogFragment.INGREDIENTS);
        args.putLong(ItemListDialogFragment.ITEM_ID,mProductId);
        DialogFragment fragment = new ItemListDialogFragment();
        fragment.setArguments(args);
        fragment.show(getFragmentManager(),"dialog");
    }

    /**
     * Task to load a product and its ingredients from the database. Used only on initial load of
     * an existing product.
     */
    private class InitialLoadProductTask extends AsyncTask<Long,Void,Cursor[]>{

        @Override
        protected Cursor[] doInBackground(Long... params) {
            Cursor[] cursors = new Cursor[2]; //2 cursors, first from Products, second from ProductIngredients
            SQLiteDatabase db = mDbHelper.getReadableDatabase();

            //Fetch product information from product table
            String[] productColumns = {DiaryContract.Product.COLUMN_NAME, DiaryContract.Product.COLUMN_BRAND, DiaryContract.Product.COLUMN_TYPE};
            String productWhere = DiaryContract.Product._ID + " = "+params[0];
            cursors[0] = db.query(DiaryContract.Product.TABLE_NAME,productColumns,productWhere,null,null,null,null);

            //Fetch IDs of ingredients in this product from ProductIngredient table
            String[] productIngredientColumns = {DiaryContract.ProductIngredient.COLUMN_INGREDIENT_ID};
            String productIngredientWhere = DiaryContract.ProductIngredient.COLUMN_PRODUCT_ID + " = "+params[0];
            Cursor productIngredients = db.query(DiaryContract.ProductIngredient.TABLE_NAME,productIngredientColumns,productIngredientWhere,null,null,null,null);
            ArrayList<String> ingredients = new ArrayList<>();

            String[] ingredientColumns = {DiaryContract.Ingredient._ID,DiaryContract.Ingredient.COLUMN_NAME};
            String ingredientWhere = "";
            //Build where clause
            if (productIngredients!= null) {
                if (productIngredients.moveToFirst()){
                    ingredientWhere = DiaryContract.Ingredient._ID + " = " + Integer.toString(productIngredients.getInt(productIngredients.getColumnIndex(DiaryContract.ProductIngredient.COLUMN_INGREDIENT_ID)));
                    while(productIngredients.moveToNext()) {
                        int ingredientID = productIngredients.getInt(productIngredients.getColumnIndex(DiaryContract.ProductIngredient.COLUMN_INGREDIENT_ID));
                        ingredientWhere += " OR " + DiaryContract.Ingredient._ID + " = " + Integer.toString(ingredientID);
                    }
                }
            }

            //Fetch Names of Ingredients in product from Ingredients table using IDs
            if (ingredientWhere.length() > 0) {
                cursors[1] = db.query(DiaryContract.Ingredient.TABLE_NAME,ingredientColumns,ingredientWhere,null,null,null,null);
            }
            return cursors;
        }

        /*
        Populates the fields with data from both the Product and Ingredient table
         */
        @Override
        protected void onPostExecute(final Cursor[] result){
            Cursor productCursor = result[0];
            Cursor ingredientCursor = result[1];
            if (result[0] != null) {
                if (productCursor.moveToFirst()) { //Populate data from product
                    String name = productCursor.getString(productCursor.getColumnIndex(DiaryContract.Product.COLUMN_NAME));
                    String brand = productCursor.getString(productCursor.getColumnIndex(DiaryContract.Product.COLUMN_BRAND));

                    mNameEditText.setText(name);
                    mBrandEditText.setText(brand);
                }
            }
            if (result[1] != null) {
                if (ingredientCursor.moveToFirst()) { //Populate data from ingredients
                    String[] fromColumns = {DiaryContract.Ingredient.COLUMN_NAME};
                    int[] toViews = {R.id.detail_listview_item};
                    SimpleCursorAdapter ingredientAdapter = new SimpleCursorAdapter(getContext(), R.layout.detail_listview_item, ingredientCursor, fromColumns, toViews, 0);
                    mIngredientsList.setAdapter(ingredientAdapter);
                }
            }
            hideLoadingLayout();
            setInitialValues();
            mInitialLoadComplete = true;
        }
    }

    /**
     * Pulls values stored in the productIngredients table to populate the ingredients
     * list for this product. Called on fragment resume after the productIngredient table has
     * been modified from the ingredient list edit button.
     */
    private class LoadProductIngredientsTask extends AsyncTask<Long,Void,Cursor>{

        @Override
        protected Cursor doInBackground(Long... params) {
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            Cursor result = null;

            //Fetch IDs of ingredients in this product from ProductIngredient table
            String[] productIngredientColumns = {DiaryContract.ProductIngredient.COLUMN_INGREDIENT_ID};
            String productIngredientWhere = DiaryContract.ProductIngredient.COLUMN_PRODUCT_ID + " = "+params[0];
            Cursor productIngredients = db.query(DiaryContract.ProductIngredient.TABLE_NAME,productIngredientColumns,productIngredientWhere,null,null,null,null);

            //Build where clause for Ingredients table query from cursor result.
            String[] ingredientColumns = {DiaryContract.Ingredient._ID,DiaryContract.Ingredient.COLUMN_NAME};
            String ingredientWhere = "";
            if (productIngredients != null) {
                if (productIngredients.moveToFirst()) {
                    ingredientWhere = DiaryContract.Ingredient._ID + " = " + Integer.toString(productIngredients.getInt(productIngredients.getColumnIndex(DiaryContract.ProductIngredient.COLUMN_INGREDIENT_ID)));
                    while(productIngredients.moveToNext()){
                        int ingredientID = productIngredients.getInt(productIngredients.getColumnIndex(DiaryContract.ProductIngredient.COLUMN_INGREDIENT_ID));
                        ingredientWhere += " OR " + DiaryContract.Ingredient._ID + " = " + Integer.toString(ingredientID);
                    }
                }
            }

            //Fetch Names of Ingredients in product from Ingredients table
            if (ingredientWhere.length() > 0) {
                result = db.query(DiaryContract.Ingredient.TABLE_NAME,ingredientColumns,ingredientWhere,null,null,null,null);
            }

            return result;
        }

        /*
        Sets the adapter for the ingredients list if this product has ingredients
         */
        @Override
        protected void onPostExecute(Cursor result){
            if (result != null) {
                String[] fromColumns = {DiaryContract.Ingredient.COLUMN_NAME};
                int[] toViews = {R.id.detail_listview_item};
                SimpleCursorAdapter ingredientAdapter = new SimpleCursorAdapter(getContext(), R.layout.detail_listview_item, result, fromColumns, toViews, 0);
                mIngredientsList.setAdapter(ingredientAdapter);
            }
        }
    }

    /**
     * Task to save the product information to the product table. Saving to productIngredients is
     * unnecessary as that information is saved in the edit ingredients fragment of this product.
     */
    private class SaveProductTask extends AsyncTask<String,Void,Long>{

        @Override
        protected Long doInBackground(String... params) {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(DiaryContract.Product.COLUMN_NAME,params[0]);
            values.put(DiaryContract.Product.COLUMN_BRAND,params[1]);
            values.put(DiaryContract.Product.COLUMN_TYPE,params[2]);

            String where = DiaryContract.Product._ID + " = ?";
            String[] whereArgs = {mProductId.toString()};
            int rows = db.update(DiaryContract.Product.TABLE_NAME,values,where,whereArgs);

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
     * Task to save a product and its ingredients to the database.
     */
    private class SaveProductPlaceholderTask extends AsyncTask<Void,Void,Long>{

        @Override
        protected Long doInBackground(Void... params) {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DiaryContract.Product.COLUMN_NAME,"PlaceholderProduct");
            return db.insert(DiaryContract.Product.TABLE_NAME,null,values);
        }

        @Override
        protected void onPostExecute(final Long result){
            if (result == -1){
                Toast.makeText(getContext(),R.string.toast_create_failed,Toast.LENGTH_SHORT).show();
            }else {
                mProductId = result;
            }
            hideLoadingLayout();
            setInitialValues();
            mIsNewProduct = true;
            mDeleteButton.setVisibility(View.GONE);
            mInitialLoadComplete = true;
        }
    }

    /**
     * Task to delete a product and its ingredients from the database.
     * Called when user taps delete button or if this is a new entry and product
     * information has not been saved on activity destroy.
     */
    private class DeleteProductTask extends AsyncTask<Boolean,Void,Integer>{

        private boolean showToast;

        @Override
        protected Integer doInBackground(Boolean... params) {
            showToast = params[0];
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            String where = DiaryContract.Ingredient._ID + " = ?";
            String[] whereArgs = {mProductId.toString()};
            return db.delete(DiaryContract.Product.TABLE_NAME, where, whereArgs);
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
