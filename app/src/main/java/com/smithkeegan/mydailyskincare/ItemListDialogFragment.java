package com.smithkeegan.mydailyskincare;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.smithkeegan.mydailyskincare.data.DiaryContract;
import com.smithkeegan.mydailyskincare.data.DiaryDbHelper;
import com.smithkeegan.mydailyskincare.ingredient.IngredientActivityDetail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * General class used to display and manipulate the relationship between two database tables with
 * a one to many relationship. This could be ingredients in a product, products in a routine, etc..
 * @author Keegan Smith
 * @since 7/12/2016
 */
//TODO would be nice to have ripple effect in listview
public class ItemListDialogFragment extends DialogFragment {

    //String constants to denote the data displayed in this fragment
    public static final String ITEM_ID = "Item_ID";
    public static final String DISPLAYED_DATA = "Displayed_data";
    public static final String INGREDIENTS = "Ingredients";
    public static final String PRODUCTS = "Products";

    private ListView listView;

    private DiaryDbHelper mDbHelper;
    private long mPrimaryItemID;
    private String mDisplayedData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_item_list_dialog,container,false);
        getDialog().setCanceledOnTouchOutside(false);

        mDbHelper = DiaryDbHelper.getInstance(getContext());

        listView = (ListView) v.findViewById(R.id.item_dialog_list_view);
        Button mSaveButton = (Button) v.findViewById(R.id.item_dialog_button_done);
        Button newItemButton = (Button) v.findViewById(R.id.item_dialog_button_new_item);

        Bundle args = getArguments();
        mDisplayedData = args.getString(DISPLAYED_DATA);
        mPrimaryItemID = args.getLong(ITEM_ID);

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemListDialogArrayAdapter adapter = (ItemListDialogArrayAdapter)listView.getAdapter();
                new SaveDataTask().execute(adapter.getItems(), mDisplayedData, mPrimaryItemID);
            }
        });

        if(mDisplayedData.equals(INGREDIENTS)){
            newItemButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(),IngredientActivityDetail.class);
                    intent.putExtra(IngredientActivityDetail.NEW_INGREDIENT,true);
                    startActivity(intent);
                }
            });
        }else if(mDisplayedData.equals(PRODUCTS)){

        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        Object[] taskArguments = {mDisplayedData, mPrimaryItemID};
        new FetchDataTask().execute(taskArguments);
    }

    /**
     * AsyncTask to fetch items from the database that this dialog fragment is displaying.
     */
    private class FetchDataTask extends AsyncTask<Object,Void,Cursor>{

        private String displayedData;
        private Long primaryItemID;

        @Override
        protected Cursor doInBackground(Object... params) {
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            Cursor result = null;

            //Load all items from the primary table first
            try {
                displayedData = (String) params[0];
                primaryItemID = (Long) params[1];
            }catch (ClassCastException exception){
                Log.e("DATABASE_ERROR","Error parsing query arguments " + exception);
            }

            //Example of this query at http://sqlfiddle.com/#!7/b38f9/5
            String[] columns = null;
            SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
            //Construct database query to return all ingredients and any products they may be attached to.
            if (displayedData.equals(INGREDIENTS)){
                queryBuilder.setTables(DiaryContract.Ingredient.TABLE_NAME + " LEFT JOIN "+ DiaryContract.ProductIngredient.TABLE_NAME +" ON " +
                        DiaryContract.Ingredient.TABLE_NAME + "." + DiaryContract.Ingredient._ID + " = " +
                        DiaryContract.ProductIngredient.TABLE_NAME + "." + DiaryContract.ProductIngredient.COLUMN_INGREDIENT_ID);
                columns = new String[] {DiaryContract.Ingredient._ID, DiaryContract.Ingredient.COLUMN_NAME,DiaryContract.ProductIngredient.COLUMN_PRODUCT_ID};
            }else if (displayedData.equals(PRODUCTS)){
                //Set table and get columns for products and productsroutines table
            }

            try {
                result = queryBuilder.query(db,columns,null,null,null,null,null);
            }catch (SQLiteException e){
                Log.e("DATABASE ERROR", "Error processing database request. " + e);
            }
            return result;
        }

        /*
          * Parses data from cursor and places result into an array adapter that is
          * attached to the listView.
         */
        @Override
        protected void onPostExecute(Cursor result) {
            ArrayList<ItemListDialogItem> itemList = new ArrayList<>();

            if(result!= null) {
                if (displayedData.equals(INGREDIENTS)) {
                    //Build array list of result from cursor.
                    if (result.moveToFirst()) {
                        ItemListDialogItem newItem = new ItemListDialogItem();
                        newItem.setId(result.getLong(result.getColumnIndex(DiaryContract.Ingredient._ID)));
                        newItem.setName(result.getString(result.getColumnIndex(DiaryContract.Ingredient.COLUMN_NAME)));
                        newItem.setLinkedId(result.getLong(result.getColumnIndex(DiaryContract.ProductIngredient.COLUMN_PRODUCT_ID)));
                        itemList.add(newItem);
                        while(result.moveToNext()){
                            newItem = new ItemListDialogItem();
                            newItem.setId(result.getLong(result.getColumnIndex(DiaryContract.Ingredient._ID)));
                            newItem.setName(result.getString(result.getColumnIndex(DiaryContract.Ingredient.COLUMN_NAME)));
                            newItem.setLinkedId(result.getLong(result.getColumnIndex(DiaryContract.ProductIngredient.COLUMN_PRODUCT_ID)));
                            itemList.add(newItem);
                        }
                    itemList = formatIngredientArray(itemList);
                    }
                } else if (displayedData.equals(PRODUCTS)) { //TODO account for products

                }

                ItemListDialogArrayAdapter arrayAdapter = new ItemListDialogArrayAdapter(getContext(),R.layout.item_list_dialog_item,itemList);
                listView.setAdapter(arrayAdapter);
            }
        }

        /*
          * Method to format the raw returned data from the cursor into the correct order and format
          * to display in the listview rows. Each ingredient will only appear in the list once, and
          * the isSelected field of the checkbox will be true if the linkedProductID returned form the cursor
          * matches the productID of the product this fragment was called from.
         */
        private ArrayList<ItemListDialogItem> formatIngredientArray(ArrayList<ItemListDialogItem> list){
            ArrayList<ItemListDialogItem> newList = new ArrayList<>();
            HashMap<String,Integer> entries = new HashMap<>(); //Hashmap of entry name and location in new arraylist
            int newListIndex = 0;
            for (int i = 0; i < list.size(); i++){
                ItemListDialogItem currItem = list.get(i);
                String name = currItem.getName();
                long linkedID = currItem.getLinkedId();

                //If the item is already in the new list, see if the item should be selected.
                if(entries.containsKey(name)){
                    if((long)currItem.getLinkedId() == primaryItemID){
                        newList.get(entries.get(name)).setInitialSelected(true);
                    }
                }else{
                    entries.put(name,newListIndex++);
                    if(linkedID == primaryItemID) currItem.setInitialSelected(true);
                    newList.add(currItem);
                }
            }

            return newList;
        }
    }

    /*
      * Custom array adapter class for interfacing with listview.
     */
    private class ItemListDialogArrayAdapter extends ArrayAdapter<ItemListDialogItem> {

        private ArrayList<ItemListDialogItem> itemList;

        public ItemListDialogArrayAdapter(Context context, int resource, List<ItemListDialogItem> objects) {
            super(context, resource, objects);
            itemList = new ArrayList<>();
            itemList.addAll(objects);
        }

        public ArrayList<ItemListDialogItem> getItems(){
            return itemList;
        }


        /*
          * Called by listview to retrieve a view to place in the row for this item.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent){

            IngredientViewHolder holder = null; //TODO allow products as well as ingredients? More general class?

            if(convertView == null){
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.item_list_dialog_item,null);

                holder = new IngredientViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.item_list_dialog_item_text);
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.item_list_dialog_check_box);
                convertView.setTag(holder); //Set this holder as this views tag

                //When the view is tapped, set the checkbox to checked and the selected field in the item.
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        IngredientViewHolder viewHolder = (IngredientViewHolder) v.getTag(); //Get holder from view's tag
                        ItemListDialogItem dialogItem = (ItemListDialogItem) viewHolder.checkBox.getTag(); //Get item from checkbox's tag
                        CheckBox checkBox = viewHolder.checkBox;
                        checkBox.setChecked(!checkBox.isChecked());
                        dialogItem.setFinalSelected(viewHolder.checkBox.isChecked());
                    }
                });
            }else{
                holder = (IngredientViewHolder) convertView.getTag();
            }

            ItemListDialogItem item = itemList.get(position);
            holder.name.setText(item.getName());
            holder.checkBox.setChecked(item.getFinalSelected());
            holder.checkBox.setTag(item); //Assign this item to the checkbox so its isSelected field can be modified with the checkbox

            return convertView;
        }


        //Helper class for setting tags to views for caching.
        private class IngredientViewHolder{
            TextView name;
            CheckBox checkBox;
        }
    }

    /*
      * AsyncTask for saving data when the user presses the done key or taps away
      * from the screen.
     */
    private class SaveDataTask extends AsyncTask<Object,Void,Long>{

        private String displayedData;
        private long primaryLinkedID;

        @Override
        protected Long doInBackground(Object... params) {
            ArrayList<ItemListDialogItem> selected = (ArrayList<ItemListDialogItem>) params[0];
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            displayedData = (String) params[1];
            primaryLinkedID = (long) params[2];
            boolean modified = false;
            Long result = (long) 1;

            for (ItemListDialogItem item: selected) {
                long primaryID = item.getId();
                long secondaryID = item.getLinkedId();
                boolean initialSelected = item.getInitialSelected();
                boolean finalSelected = item.getFinalSelected();

                //If entry was not originally selected but is now selected, add a new row to the linked table
                if (!initialSelected && finalSelected){
                    result = insertLinkedRow(db, primaryID);
                    modified = true;
                } //If entry was initially selcted but now is not, delete the entry from the linked table
                else if (initialSelected && !finalSelected){
                    result = deleteLinkedRow(db, primaryID);
                    modified = true;
                }
            }
            result = modified ? result : -2;

            return result;
        }

        @Override
        protected void onPostExecute(Long result){
            if (result == -1){
                Toast.makeText(getContext(),R.string.toast_save_failed,Toast.LENGTH_SHORT).show();
            } else if (result > -1){
                Toast.makeText(getContext(),R.string.toast_save_success,Toast.LENGTH_SHORT).show();
            }
            ((DialogClosedListener)getActivity()).onEditListDialogClosed();
            getDialog().dismiss();
        }


        //TODO support product-routine table
        private long insertLinkedRow(SQLiteDatabase db, long primaryID){
            Long result = (long) -1;
            if(displayedData.equals(INGREDIENTS)){
                ContentValues values = new ContentValues();
                values.put(DiaryContract.ProductIngredient.COLUMN_INGREDIENT_ID,primaryID);
                values.put(DiaryContract.ProductIngredient.COLUMN_PRODUCT_ID,primaryLinkedID);
                result = db.insert(DiaryContract.ProductIngredient.TABLE_NAME,null,values);
            }else if(displayedData.equals(PRODUCTS)){

            }
            return result;
        }

        //TODO support product-routine table
        private long deleteLinkedRow(SQLiteDatabase db, long primaryID){
            Long result = (long) -1;
            if(displayedData.equals(INGREDIENTS)){
                String where = DiaryContract.ProductIngredient.COLUMN_INGREDIENT_ID + " = ? AND "+ DiaryContract.ProductIngredient.COLUMN_PRODUCT_ID + " = ?";
                String[] whereArgs = {Long.toString(primaryID),Long.toString(primaryLinkedID)};
                result = (long) db.delete(DiaryContract.ProductIngredient.TABLE_NAME,where,whereArgs);
            }else if(displayedData.equals(PRODUCTS)){

            }
            return result;
        }
    }

    /**
     * Helper class for storing relevant information about the relationship
     * between two items in the ItemListDialogFragment.
     * @author Keegan Smith
     * @since 7/28/2016
     */
    public class ItemListDialogItem {

        private String mName;
        private Long mId;
        private Long mLinkedId;
        private boolean mInitialSelected;
        private boolean mFinalSelected;

        public ItemListDialogItem(){
            mInitialSelected = false;
            mInitialSelected = false;
            mName = null;
            mId = null;
            mLinkedId = null;
        }

        public boolean getInitialSelected() {
            return mInitialSelected;
        }

        public void setInitialSelected(boolean selected) {
            this.mInitialSelected = selected;
            mFinalSelected = selected;
        }

        public String getName() {
            return mName;
        }

        public Long getLinkedId() {
            return mLinkedId;
        }

        public Long getId() {
            return mId;
        }

        public void setName(String mName) {
            this.mName = mName;
        }

        public void setId(Long mId) {
            this.mId = mId;
        }

        public void setLinkedId(Long linkedId) {
            this.mLinkedId = linkedId;
        }

        public boolean getFinalSelected() {
            return mFinalSelected;
        }

        public void setFinalSelected(boolean mFinalSelected) {
            this.mFinalSelected = mFinalSelected;
        }
    }

}
