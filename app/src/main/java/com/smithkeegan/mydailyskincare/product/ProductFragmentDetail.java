package com.smithkeegan.mydailyskincare.product;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.smithkeegan.mydailyskincare.R;
import com.smithkeegan.mydailyskincare.customClasses.ItemListDialogFragment;
import com.smithkeegan.mydailyskincare.data.DiaryContract;
import com.smithkeegan.mydailyskincare.data.DiaryDbHelper;
import com.smithkeegan.mydailyskincare.ingredient.IngredientActivityDetail;

/**
 * Fragment class of the detail screen for a product. Performs actions to manipulate a product.
 * @author Keegan Smith
 * @since 5/19/2016
 *
 * //TODO prompts for which field is required when attempting to save
 */
public class ProductFragmentDetail extends Fragment {

    private DiaryDbHelper mDbHelper;

    private View mProgressLayout;
    private View mDetailLayout;
    private EditText mNameEditText;
    private EditText mBrandEditText;
    private Spinner mTypeSpinner;
    private ArrayAdapter<CharSequence> mSpinnerAdapter;
    private Button mEditIngredientsButton;
    private ListView mIngredientsList;
    private TextView mNoIngredientsText;

    private Long mProductId;
    private String mInitialName;
    private String mInitialBrand;
    private String mInitialType;
    private boolean mInitialLoadComplete;
    private boolean mIsNewProduct;
    private boolean mIngredientsListModified;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        View rootView = inflater.inflate(R.layout.fragment_product_detail, container, false);

        mDbHelper = DiaryDbHelper.getInstance(getContext());

        fetchViews(rootView);

        mIngredientsListModified = false;

        if (savedInstance == null) {
            Bundle args = getArguments();
            mIsNewProduct = args.getBoolean(ProductActivityDetail.NEW_PRODUCT, true);
            mProductId = args.getLong(ProductActivityDetail.ENTRY_ID, -1);

            showLoadingLayout();

            if (mIsNewProduct || mProductId < 0) { //New entry or error loading
                new SaveProductPlaceholderTask().execute();
            } else { //Load data from existing entry
                new InitialLoadProductTask().execute(mProductId);
            }
        }else{
            restoreSavedInstance(savedInstance);
        }

        setListeners();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_item_detail,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_action_save:
                saveCurrentProduct();
                return true;
            case R.id.menu_action_delete:
                deleteCurrentProduct();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    /**
     * Saves the current fields when the activity is destroyed by a system process to be restored
     * later.
     * @param outState bundle that will contain this fragments current fields.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(ProductState.PRODUCT_ID,mProductId);
        outState.putBoolean(ProductState.NEW_PRODUCT,mIsNewProduct);
        outState.putStringArray(ProductState.PRODUCT_NAME,new String[]{mInitialName,mNameEditText.getText().toString().trim()});
        outState.putStringArray(ProductState.PRODUCT_BRAND,new String[]{mInitialBrand,mBrandEditText.getText().toString().trim()});
        outState.putStringArray(ProductState.PRODUCT_TYPE,new String[]{mInitialType,mTypeSpinner.getSelectedItem().toString()});

        super.onSaveInstanceState(outState);
    }

    /**
     * Restores fields of this fragment from the restored instance state.
     * @param savedInstance the restored state
     */
    public void restoreSavedInstance(Bundle savedInstance){
        mProductId = savedInstance.getLong(ProductState.PRODUCT_ID);
        mIsNewProduct = savedInstance.getBoolean(ProductState.NEW_PRODUCT);

        String[] names = savedInstance.getStringArray(ProductState.PRODUCT_NAME);
        if (names != null){
            mInitialName = names[0];
            mNameEditText.setText(names[1]);
        }

        String[] brands = savedInstance.getStringArray(ProductState.PRODUCT_BRAND);
        if (brands != null){
            mInitialBrand = brands[0];
            mBrandEditText.setText(brands[1]);
        }

        String[] types = savedInstance.getStringArray(ProductState.PRODUCT_TYPE);
        if (types != null){
            mInitialType = types[0];
            int pos = mSpinnerAdapter.getPosition(types[1]);
            mTypeSpinner.setSelection(pos);
        }

        mInitialLoadComplete = true;
    }

    /**
     * Returns whether this enties values have changed since the form has been opened.
     * @return true if this entry has changed
     */
    public boolean entryHasChanged(){
        String currentName = mNameEditText.getText().toString().trim();
        String currentBrand = mBrandEditText.getText().toString().trim();
        String currentType = "";
        if(mTypeSpinner.getSelectedItem() != null) {
            currentType = mTypeSpinner.getSelectedItem().toString();
        }
        return (!mInitialName.equals(currentName) || (!mInitialBrand.equals(currentBrand)) || (!mInitialType.equals(currentType)) || mIngredientsListModified);
    }

    private void showLayout(View view){
        mIngredientsList.setVisibility(View.INVISIBLE);
        mNoIngredientsText.setVisibility(View.INVISIBLE);

        view.setVisibility(View.VISIBLE);
    }

    /**
     * Checks if the required name field is a valid value.
     * @return true if the name field is valid
     */
    public boolean checkNameField(){
        boolean valid = true;
        String currentName = mNameEditText.getText().toString().trim();
        if (currentName.length() == 0)
            valid = false;
        return valid;
    }

    /**
     * Sets the intial member values of this form for comparison when exiting.
     */
    public void setInitialValues(){
        mInitialName = mNameEditText.getText().toString().trim();
        mInitialBrand = mBrandEditText.getText().toString().trim();
        if(mTypeSpinner.getSelectedItem() != null){
            mInitialType = mTypeSpinner.getSelectedItem().toString();
        }else {
            mInitialType = getResources().getString(R.string.product_types_none);
        }
        if (mIngredientsList.getAdapter() != null && mIngredientsList.getCount() > 0){
            showLayout(mIngredientsList);
        }else {
            showLayout(mNoIngredientsText);
        }
    }

    /**
     * Refresh the ingredients list of this product
     */
    public void refreshIngredients(){
        mIngredientsList.setAdapter(null);
        new LoadProductIngredientsTask().execute(mProductId);
    }

    /**
     * Called by parent activity when edit dialog is closed.
     * @param listModified true if the list of ingredients was modified.
     */
    public void onEditDialogClosed(boolean listModified){
        if (listModified){
            refreshIngredients();
            mIngredientsListModified = true;
        }
    }

    /**
     * Save the current product based on its current status.
     */
    public void saveCurrentProduct(){
        if(!checkNameField()) {
            Toast.makeText(getContext(), R.string.toast_enter_valid_name, Toast.LENGTH_SHORT).show();
        }else {
            if(entryHasChanged()) {
                String productName = mNameEditText.getText().toString().trim();
                String productBrand = mBrandEditText.getText().toString().trim();
                String productType = mTypeSpinner.getSelectedItem().toString();
                String[] params = {productName, productBrand, productType};
                new SaveProductTask().execute(params);
            }else{
                getActivity().finish();
                Toast.makeText(getContext(),R.string.toast_save_success,Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Deletes this product. Called when the user preses the delete button.
     */
    private void deleteCurrentProduct(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.product_delete_alert_dialog_message)
                .setTitle(R.string.product_delete_alert_dialog__title)
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

    /**
     * Method called by parent activity when the user presses the home button or the physical back button.
     * Asks the user if they want to save changes if there are changes to save.
     */
    public void onBackButtonPressed(){
        if (entryHasChanged()) {
            if (mIsNewProduct){ //Ask user if this is a new entry and changes have been made.
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.product_back_alert_dialog_message)
                        .setTitle(R.string.product_back_alert_dialog_title)
                        .setPositiveButton(R.string.save_button_string, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                saveCurrentProduct();
                            }
                        })
                        .setNegativeButton(R.string.no_string, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new DeleteProductTask().execute(false);
                            }
                        }).setNeutralButton(R.string.cancel_string, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
            }else {
                saveCurrentProduct();
            }
        } else if (mIsNewProduct) {
            new DeleteProductTask().execute(false);
        } else {
            getActivity().finish();
        }
    }

    /**
     * Fetches the views belonging to this fragment.
     * @param rootView the rootview of this fragment
     */
    private void fetchViews(View rootView){
        mNameEditText = (EditText) rootView.findViewById(R.id.product_name_edit);
        mBrandEditText = (EditText) rootView.findViewById(R.id.product_brand_edit);
        mEditIngredientsButton = (Button) rootView.findViewById(R.id.product_edit_ingredients);
        mIngredientsList = (ListView) rootView.findViewById(R.id.product_ingredient_list);
        mNoIngredientsText = (TextView) rootView.findViewById(R.id.product_ingredient_empty_text);

        mTypeSpinner = (Spinner) rootView.findViewById(R.id.product_type_spinner);
        mSpinnerAdapter = ArrayAdapter.createFromResource(getContext(),R.array.product_types_array,R.layout.product_spinner_layout);
        mSpinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        mTypeSpinner.setAdapter(mSpinnerAdapter);

        mProgressLayout = rootView.findViewById(R.id.product_loading_layout);
        mDetailLayout = rootView.findViewById(R.id.product_fragment_detail_layout);
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
     * Sets listeners to appropriate views in this fragment.
     */
    private void setListeners(){
        mEditIngredientsButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                showEditDialog();
            }
        });

        mIngredientsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), IngredientActivityDetail.class);
                intent.putExtra(IngredientActivityDetail.NEW_INGREDIENT,false);
                intent.putExtra(IngredientActivityDetail.ENTRY_ID,id);
                startActivity(intent);
            }
        });

        mTypeSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                View view = getActivity().getCurrentFocus();
                InputMethodManager methodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (view != null) {
                    methodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    view.clearFocus();
                }
                return false;
            }
        });
    }

    /**
     * Called when the edit ingredients button is pressed. Opens the item list dialog for this product.
     */
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
                productIngredients.close();
            }

            //Fetch Names of Ingredients in product from Ingredients table using IDs
            if (ingredientWhere.length() > 0) {
                cursors[1] = db.query(DiaryContract.Ingredient.TABLE_NAME,ingredientColumns,ingredientWhere,null,null,null, DiaryContract.Ingredient.COLUMN_NAME + " ASC");
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
                    String type = productCursor.getString(productCursor.getColumnIndex(DiaryContract.Product.COLUMN_TYPE));

                    mNameEditText.setText(name);
                    mBrandEditText.setText(brand);
                    int pos = mSpinnerAdapter.getPosition(type);
                    mTypeSpinner.setSelection(pos);
                }
            }
            if (result[1] != null) {
                if (ingredientCursor.moveToFirst()) { //Populate data from ingredients
                    String[] fromColumns = {DiaryContract.Ingredient.COLUMN_NAME};
                    int[] toViews = {R.id.product_detail_ingredient_listview_item};
                    SimpleCursorAdapter ingredientAdapter = new SimpleCursorAdapter(getContext(), R.layout.listview_item_product_detail_ingredients, ingredientCursor, fromColumns, toViews, 0);
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
                productIngredients.close();
            }

            //Fetch Names of Ingredients in product from Ingredients table
            if (ingredientWhere.length() > 0) {
                result = db.query(DiaryContract.Ingredient.TABLE_NAME,ingredientColumns,ingredientWhere,null,null,null, DiaryContract.Ingredient.COLUMN_NAME + " ASC");
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
                int[] toViews = {R.id.product_detail_ingredient_listview_item};
                SimpleCursorAdapter ingredientAdapter = new SimpleCursorAdapter(getContext(), R.layout.listview_item_product_detail_ingredients, result, fromColumns, toViews, 0);
                mIngredientsList.setAdapter(ingredientAdapter);
            }
            if (mIngredientsList.getAdapter() != null && mIngredientsList.getAdapter().getCount() > 0){
                showLayout(mIngredientsList);
            }else {
                showLayout(mNoIngredientsText);
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
                Intent intent = new Intent();
                intent.putExtra(ProductActivityMain.PRODUCT_FINISHED_ID,mProductId);
                getActivity().setResult(Activity.RESULT_OK,intent);
                Toast.makeText(getContext(), R.string.toast_save_success, Toast.LENGTH_SHORT).show();
            }
            getActivity().finish();
        }
    }

    /**
     * Task to save a placeholder product so there is a product ID available to link ingredients to on initial creation.
     */
    private class SaveProductPlaceholderTask extends AsyncTask<Void,Void,Long>{

        @Override
        protected Long doInBackground(Void... params) {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DiaryContract.Product.COLUMN_NAME,"PlaceholderProduct");
            values.put(DiaryContract.Product.COLUMN_TYPE,getResources().getString(R.string.product_types_none));
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
            String where = DiaryContract.Product._ID + " = ?";
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

    /**
     * Class to hold keys to be used when saving and restoring instance state.
     */
    protected static class ProductState {
        protected static final String PRODUCT_ID = "PRODUCT_ID";
        protected static final String NEW_PRODUCT = "NEW_PRODUCT";
        protected static final String PRODUCT_NAME = "PRODUCT_NAME";
        protected static final String PRODUCT_BRAND = "PRODUCT_BRAND";
        protected static final String PRODUCT_TYPE = "PRODUCT_TYPE";
    }
}
