package com.smithkeegan.mydailyskincare.product;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
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
 * Fragment class for the product list screen.
 * //TODO wrap all database access in try catch
 * @author Keegan Smith
 * @since 5/19/2016
 */
public class ProductFragmentMain extends Fragment{

    private DiaryDbHelper mDbHelper;
    private ListView mProductsList;
    private TextView mNoProductsTextView;
    private Button mNewProductButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        View rootView = inflater.inflate(R.layout.fragment_product_main, container,false);

        mDbHelper = DiaryDbHelper.getInstance(getContext());
        mProductsList = (ListView) rootView.findViewById(R.id.product_main_list_view);
        mNoProductsTextView = (TextView) rootView.findViewById(R.id.product_main_no_products_text);
        mNewProductButton = (Button) rootView.findViewById(R.id.product_main_new_button);
        setButtonListener();
        return rootView;
    }

    private void setButtonListener(){
        mNewProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),ProductActivityDetail.class);
                intent.putExtra(ProductActivityDetail.NEW_PRODUCT,true);
                startActivityForResult(intent, ProductActivityMain.PRODUCT_FINISHED);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        refreshProductList();
    }

    private void showLayout(View view){
        mProductsList.setVisibility(View.INVISIBLE);
        mNoProductsTextView.setVisibility(View.INVISIBLE);

        view.setVisibility(View.VISIBLE);
    }

    /**
     * Refreshes the list of products by calling the fetch product task.
     */
    private void refreshProductList(){
        new FetchProductsTask().execute();
    }

    /**
     * Background process to fetch and populate the listview of ingredients.
     */
    private class FetchProductsTask extends AsyncTask<Void,Void,Cursor> {

        int highlightId;

        public FetchProductsTask(){
            highlightId = -1;
        }

        //Constructor used if an entry retrieved should be highlighted on fetching.
        public FetchProductsTask(int highlightId){
            this.highlightId =  highlightId;
        }

        @Override
        protected Cursor doInBackground(Void... params) {

            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            String[] columns = {DiaryContract.Product._ID, DiaryContract.Product.COLUMN_BRAND,DiaryContract.Product.COLUMN_NAME, DiaryContract.Product.COLUMN_TYPE};
            String sortOrder = DiaryContract.Product.COLUMN_NAME + " DESC";
            return db.query(DiaryContract.Product.TABLE_NAME,columns,null,null,null,null,sortOrder);
        }

        @Override
        protected void onPostExecute(final Cursor result){
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
            mProductsList.setAdapter(adapter);
            mProductsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                /*
                 * Called when a user clicks on an entry in the ingredient listview. Opens the ingredient detail
                 * for that ingredient.
                 */
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getContext(),ProductActivityDetail.class);
                    intent.putExtra(ProductActivityDetail.NEW_PRODUCT,false); //Not a new ingredient
                    intent.putExtra(ProductActivityDetail.ENTRY_ID,id); //ID of ingredient
                    startActivity(intent); //TODO use startActivityForResult if highlighting
                }
            });
            //TODO highlight edited field on return from detail activity.
            /*if (highlightId > -1) {
                int highlightPosition = -1;
                boolean done = false;
                result.moveToFirst();
                do {
                    if (highlightId == result.getInt(result.getColumnIndex(DiaryContract.Ingredient._ID))){
                        highlightPosition = result.getPosition();
                        done = true;
                    }
                } while (result.moveToNext() || !done);
                if (highlightPosition > -1){
                    mIngredientsList.setSelection(highlightPosition); //Find way to highlight selected row
                }
            }*/
            if (mProductsList.getAdapter() != null && mProductsList.getAdapter().getCount() > 0){
                showLayout(mProductsList);
            }else {
                showLayout(mNoProductsTextView);
            }
        }
    }
}
