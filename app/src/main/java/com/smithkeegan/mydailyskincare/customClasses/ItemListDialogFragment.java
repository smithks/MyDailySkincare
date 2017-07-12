package com.smithkeegan.mydailyskincare.customClasses;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.smithkeegan.mydailyskincare.R;
import com.smithkeegan.mydailyskincare.data.DiaryContract;
import com.smithkeegan.mydailyskincare.data.DiaryDbHelper;
import com.smithkeegan.mydailyskincare.ui.ingredient.IngredientActivityDetail;
import com.smithkeegan.mydailyskincare.ui.ingredient.IngredientActivityMain;
import com.smithkeegan.mydailyskincare.ui.product.ProductActivityDetail;
import com.smithkeegan.mydailyskincare.ui.product.ProductActivityMain;
import com.smithkeegan.mydailyskincare.ui.routine.RoutineActivityDetail;
import com.smithkeegan.mydailyskincare.ui.routine.RoutineActivityMain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * General class used to display and manipulate the relationship between two database tables with
 * a one to many relationship. This could be ingredients in a product, products in a routine, etc..
 * @author Keegan Smith
 * @since 7/12/2016
 */
public class ItemListDialogFragment extends DialogFragment {

    //String constants to denote the data displayed in this fragment
    public static final String ITEM_ID = "Item_ID";
    public static final String DISPLAYED_DATA = "Displayed_data";
    public static final String INGREDIENTS = "Ingredients";
    public static final String PRODUCTS = "Products";
    public static final String ROUTINES = "Routines";
    public static final int NEW_ITEM_ID_REQUEST = 1;

    private ListView mListView;
    private TextView mEmptyTextView;

    private DiaryDbHelper mDbHelper;
    private long mPrimaryItemID;
    private String mDisplayedData;
    private long mNewItemID;

    private boolean mListModified;

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) { //Set dimensions of this dialog.
            Point size = new Point();
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            display.getSize(size);
            if (size.x > size.y) { //Phone is in landscape orientation
                dialog.getWindow().setLayout(size.x, WindowManager.LayoutParams.WRAP_CONTENT);
            } else { //Phone is in portrait orientation
                dialog.getWindow().setLayout(size.x, WindowManager.LayoutParams.WRAP_CONTENT);
            }

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_item_list_dialog, container, false);
        getDialog().setCanceledOnTouchOutside(false);

        //Listen for back button touch
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP && !event.isCanceled()) {
                    saveCurrentItems(false);
                    ((DialogClosedListener) getActivity()).onEditListDialogClosed(mListModified);
                }
                return false;
            }
        });

        mDbHelper = DiaryDbHelper.getInstance(getContext());

        mListView = (ListView) v.findViewById(R.id.item_dialog_list_view);
        mEmptyTextView = (TextView) v.findViewById(R.id.item_dialog_no_entries_text);
        Button mSaveButton = (Button) v.findViewById(R.id.item_dialog_button_done);
        Button newItemButton = (Button) v.findViewById(R.id.item_dialog_button_new_item);
        TextView titleView = (TextView) v.findViewById(R.id.item_dialog_title);

        Bundle args = getArguments();
        mDisplayedData = args.getString(DISPLAYED_DATA);
        mPrimaryItemID = args.getLong(ITEM_ID);
        mListModified = false;

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCurrentItems(true);
            }
        });


        //Set the behavior of the new item button based on which fragment this dialog was called from
        if (mDisplayedData.equals(INGREDIENTS)) {
            titleView.setText(R.string.item_list_select_ingredients);
            newItemButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveCurrentItems(false);
                    mNewItemID = 0; //Set newitemid to default 0
                    Intent intent = new Intent(getContext(), IngredientActivityDetail.class);
                    intent.putExtra(IngredientActivityDetail.NEW_INGREDIENT, true);
                    startActivityForResult(intent, NEW_ITEM_ID_REQUEST);
                }
            });
        } else if (mDisplayedData.equals(PRODUCTS)) {
            titleView.setText(R.string.item_list_select_products);
            newItemButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveCurrentItems(false);
                    mNewItemID = 0; //Set new item id to default
                    Intent intent = new Intent(getContext(), ProductActivityDetail.class);
                    intent.putExtra(ProductActivityDetail.NEW_PRODUCT, true);
                    startActivityForResult(intent, NEW_ITEM_ID_REQUEST);
                }
            });
        } else if (mDisplayedData.equals(ROUTINES)) {
            titleView.setText(R.string.item_list_select_routines);
            newItemButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveCurrentItems(false);
                    mNewItemID = 0; //set new item id to default
                    Intent intent = new Intent(getContext(), RoutineActivityDetail.class);
                    intent.putExtra(RoutineActivityDetail.NEW_ROUTINE, true);
                    startActivityForResult(intent, NEW_ITEM_ID_REQUEST);
                }
            });
        }

        return v;
    }

    /**
     * Shows only the passed in view and sets other views to invisible.
     * @param view the view to show
     */
    private void showLayout(View view) {
        mEmptyTextView.setVisibility(View.INVISIBLE);
        mListView.setVisibility(View.INVISIBLE);

        view.setVisibility(View.VISIBLE);
    }

    /**
     * Saves the current items to the database.
     * @param closeOnFinish Whether the dialog should be closed when the save is completed.
     */
    private void saveCurrentItems(boolean closeOnFinish) {
        updateListModified();
        ItemListDialogArrayAdapter adapter = (ItemListDialogArrayAdapter) mListView.getAdapter();
        if (adapter != null) { //There are records to save
            new SaveDataTask().execute(adapter.getItems(), mDisplayedData, mPrimaryItemID, closeOnFinish);
        }else if (closeOnFinish){ //Done pressed with empty list
            ((DialogClosedListener) getActivity()).onEditListDialogClosed(mListModified);
            getDialog().dismiss();
        }
    }

    /**
     * Set the id of the new item when returning from new item creation.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEW_ITEM_ID_REQUEST) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                if (mDisplayedData.equals(INGREDIENTS)) {
                    if (data.hasExtra(IngredientActivityMain.INGREDIENT_FINISHED_ID)) {
                        mNewItemID = data.getExtras().getLong(IngredientActivityMain.INGREDIENT_FINISHED_ID);
                    }
                } else if (mDisplayedData.equals(PRODUCTS)) {
                    if (data.hasExtra(ProductActivityMain.PRODUCT_FINISHED_ID)) {
                        mNewItemID = data.getExtras().getLong(ProductActivityMain.PRODUCT_FINISHED_ID);
                    }
                } else if (mDisplayedData.equals(ROUTINES)) {
                    if (data.hasExtra(RoutineActivityMain.ROUTINE_FINISHED_ID)) {
                        mNewItemID = data.getExtras().getLong(RoutineActivityMain.ROUTINE_FINISHED_ID);
                    }
                }
            }
        }
    }

    /**
     * Refresh the list of items on resume.
     */
    @Override
    public void onResume() {
        super.onResume();

        Object[] taskArguments = {mDisplayedData, mPrimaryItemID};
        mListView.setAdapter(null); //Clear listview adapter
        new FetchDataTask().execute(taskArguments);
    }

    /**
     * Checks the initial and current values of rows in the listview to see if the list
     * has been modified.
     */
    private void updateListModified() {
        ItemListDialogArrayAdapter arrayAdapter = (ItemListDialogArrayAdapter) mListView.getAdapter();
        if (arrayAdapter != null) {
            ArrayList<ItemListDialogItem> items = arrayAdapter.getItems();

            for (ItemListDialogItem entry : items) {
                if (entry.getInitialSelected() != entry.getFinalSelected()) {
                    mListModified = true;
                }
            }
        }
    }

    /**
     * AsyncTask to fetch items from the database that this dialog fragment is displaying.
     */
    private class FetchDataTask extends AsyncTask<Object, Void, Cursor> {

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
            } catch (ClassCastException exception) {
                Log.e("DATABASE_ERROR", "Error parsing query arguments " + exception);
            }

            //Example of this query at http://sqlfiddle.com/#!7/b38f9/5
            String[] columns = null;
            String sortOrder = null;
            SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
            //Construct database query to return all ingredients and any products they may be attached to.
            if (displayedData.equals(INGREDIENTS)) {
                queryBuilder.setTables(DiaryContract.Ingredient.TABLE_NAME + " LEFT JOIN " + DiaryContract.ProductIngredient.TABLE_NAME + " ON " +
                        DiaryContract.Ingredient.TABLE_NAME + "." + DiaryContract.Ingredient._ID + " = " +
                        DiaryContract.ProductIngredient.TABLE_NAME + "." + DiaryContract.ProductIngredient.COLUMN_INGREDIENT_ID);
                columns = new String[]{DiaryContract.Ingredient._ID, DiaryContract.Ingredient.COLUMN_NAME, DiaryContract.ProductIngredient.COLUMN_PRODUCT_ID};
                sortOrder = DiaryContract.Ingredient.COLUMN_NAME + " ASC";
            } else if (displayedData.equals(PRODUCTS)) {
                //Set table and get columns for products and productsroutines table
                queryBuilder.setTables(DiaryContract.Product.TABLE_NAME + " LEFT JOIN " + DiaryContract.RoutineProduct.TABLE_NAME + " ON " +
                        DiaryContract.Product.TABLE_NAME + "." + DiaryContract.Product._ID + " = " +
                        DiaryContract.RoutineProduct.TABLE_NAME + "." + DiaryContract.RoutineProduct.COLUMN_PRODUCT_ID);
                columns = new String[]{DiaryContract.Product._ID, DiaryContract.Product.COLUMN_NAME, DiaryContract.Product.COLUMN_BRAND, DiaryContract.Product.COLUMN_TYPE, DiaryContract.RoutineProduct.COLUMN_ROUTINE_ID};
                sortOrder = DiaryContract.Product.COLUMN_NAME + " ASC";
            } else if (mDisplayedData.equals(ROUTINES)) {
                queryBuilder.setTables(DiaryContract.Routine.TABLE_NAME + " LEFT JOIN " + DiaryContract.DiaryEntryRoutine.TABLE_NAME + " ON " +
                        DiaryContract.Routine.TABLE_NAME + "." + DiaryContract.Routine._ID + " = " +
                        DiaryContract.DiaryEntryRoutine.TABLE_NAME + "." + DiaryContract.DiaryEntryRoutine.COLUMN_ROUTINE_ID);
                columns = new String[]{DiaryContract.Routine._ID, DiaryContract.Routine.COLUMN_NAME, DiaryContract.Routine.COLUMN_TIME, DiaryContract.DiaryEntryRoutine.COLUMN_DIARY_ENTRY_ID};
                sortOrder = DiaryContract.Routine.COLUMN_NAME + " ASC";
            }

            try {
                result = queryBuilder.query(db, columns, null, null, null, null, sortOrder);
            } catch (SQLiteException e) {
                Log.e("DATABASE ERROR", "Error processing database request. " + e);
            }
            return result;
        }

        /*
          * Parses data from cursor and places result into an array adapter that is
          * attached to the mListView.
         */
        @Override
        protected void onPostExecute(Cursor result) {
            ArrayList<ItemListDialogItem> itemList = new ArrayList<>();

            if (result != null) {
                if (result.getCount() > 0) { //Entries found, populate listview
                    if (displayedData.equals(INGREDIENTS)) {
                        //Build array list of result from cursor.
                        if (result.moveToFirst()) {
                            ItemListDialogItem item = new ItemListDialogItem();
                            item.setId(result.getLong(result.getColumnIndex(DiaryContract.Ingredient._ID)));
                            item.setName(result.getString(result.getColumnIndex(DiaryContract.Ingredient.COLUMN_NAME)));
                            item.setLinkedId(result.getLong(result.getColumnIndex(DiaryContract.ProductIngredient.COLUMN_PRODUCT_ID)));
                            if (mNewItemID != 0 && mNewItemID == item.getId())
                                item.setFinalSelected(true); //Set this item to automatically selected since it was just created
                            itemList.add(item);
                            while (result.moveToNext()) {
                                item = new ItemListDialogItem();
                                item.setId(result.getLong(result.getColumnIndex(DiaryContract.Ingredient._ID)));
                                item.setName(result.getString(result.getColumnIndex(DiaryContract.Ingredient.COLUMN_NAME)));
                                item.setLinkedId(result.getLong(result.getColumnIndex(DiaryContract.ProductIngredient.COLUMN_PRODUCT_ID)));
                                if (mNewItemID != 0 && mNewItemID == item.getId())
                                    item.setFinalSelected(true); //Set this item to automatically selected since it was just created
                                itemList.add(item);
                            }
                            itemList = formatItemArray(itemList);
                        }
                    } else if (displayedData.equals(PRODUCTS)) {
                        if (result.moveToFirst()) {
                            ItemListDialogItem item = new ItemListDialogItem();
                            item.setId(result.getLong(result.getColumnIndex(DiaryContract.Product._ID)));
                            item.setName(result.getString(result.getColumnIndex(DiaryContract.Product.COLUMN_NAME)));
                            item.setExtraField1(result.getString(result.getColumnIndex(DiaryContract.Product.COLUMN_BRAND)));
                            item.setExtraField2(result.getString(result.getColumnIndex(DiaryContract.Product.COLUMN_TYPE)));
                            item.setLinkedId(result.getLong(result.getColumnIndex(DiaryContract.RoutineProduct.COLUMN_ROUTINE_ID)));
                            if (mNewItemID != 0 && mNewItemID == item.getId())
                                item.setFinalSelected(true); //Set this item to automatically selected since it was just created
                            itemList.add(item);
                            while (result.moveToNext()) {
                                item = new ItemListDialogItem();
                                item.setId(result.getLong(result.getColumnIndex(DiaryContract.Product._ID)));
                                item.setName(result.getString(result.getColumnIndex(DiaryContract.Product.COLUMN_NAME)));
                                item.setExtraField1(result.getString(result.getColumnIndex(DiaryContract.Product.COLUMN_BRAND)));
                                item.setExtraField2(result.getString(result.getColumnIndex(DiaryContract.Product.COLUMN_TYPE)));
                                item.setLinkedId(result.getLong(result.getColumnIndex(DiaryContract.RoutineProduct.COLUMN_ROUTINE_ID)));
                                if (mNewItemID != 0 && mNewItemID == item.getId())
                                    item.setFinalSelected(true); //Set this item to automatically selected since it was just created
                                itemList.add(item);
                            }
                            itemList = formatItemArray(itemList);
                        }
                    } else if (displayedData.equals(ROUTINES)) {
                        if (result.moveToFirst()) {
                            ItemListDialogItem item = new ItemListDialogItem();
                            item.setId(result.getLong(result.getColumnIndex(DiaryContract.Routine._ID)));
                            item.setName(result.getString(result.getColumnIndex(DiaryContract.Routine.COLUMN_NAME)));
                            item.setExtraField1(result.getString(result.getColumnIndex(DiaryContract.Routine.COLUMN_TIME)));
                            item.setLinkedId(result.getLong(result.getColumnIndex(DiaryContract.DiaryEntryRoutine.COLUMN_DIARY_ENTRY_ID)));
                            if (mNewItemID != 0 && mNewItemID == item.getId())
                                item.setFinalSelected(true); //Item was just created, automatically select it
                            itemList.add(item);
                            while (result.moveToNext()) {
                                item = new ItemListDialogItem();
                                item.setId(result.getLong(result.getColumnIndex(DiaryContract.Routine._ID)));
                                item.setName(result.getString(result.getColumnIndex(DiaryContract.Routine.COLUMN_NAME)));
                                item.setExtraField1(result.getString(result.getColumnIndex(DiaryContract.Routine.COLUMN_TIME)));
                                item.setLinkedId(result.getLong(result.getColumnIndex(DiaryContract.DiaryEntryRoutine.COLUMN_DIARY_ENTRY_ID)));
                                if (mNewItemID != 0 && mNewItemID == item.getId())
                                    item.setFinalSelected(true); //Item was just created, automatically select it
                                itemList.add(item);
                            }
                            itemList = formatItemArray(itemList);
                        }
                    }

                    ItemListDialogArrayAdapter arrayAdapter = new ItemListDialogArrayAdapter(getContext(), R.layout.listview_item_item_list_dialog, itemList);
                    mListView.setAdapter(arrayAdapter);
                    showLayout(mListView);
                } else {
                    if (displayedData.equals(INGREDIENTS)) {
                        String newText = mEmptyTextView.getText().toString() + " " + getResources().getString(R.string.item_list_no_entries_ingredient);
                        mEmptyTextView.setText(newText);
                    } else if (displayedData.equals(PRODUCTS)) {
                        String newText = mEmptyTextView.getText().toString() + " " + getResources().getString(R.string.item_list_no_entries_products);
                        mEmptyTextView.setText(newText);
                    } else if (displayedData.equals(ROUTINES)) {
                        String newText = mEmptyTextView.getText().toString() + " " + getResources().getString(R.string.item_list_no_entries_routines);
                        mEmptyTextView.setText(newText);
                    }
                    showLayout(mEmptyTextView);
                }

            }
        }

        /**
         * Method to format the raw returned data from the cursor into the correct order and format
         * to display in the listview rows. Each item will only appear in the list once, and
         * the isSelected field of the checkbox will be true if the linkedID returned from the cursor
         * matches the primaryItemID of the item this fragment was called from.
         * @param list The raw data from cursor
         * @return The formatted list of items.
         */
        private ArrayList<ItemListDialogItem> formatItemArray(ArrayList<ItemListDialogItem> list) {
            ArrayList<ItemListDialogItem> newList = new ArrayList<>();
            HashMap<String, Integer> entries = new HashMap<>(); //Hashmap of entry name and location in new arraylist
            int newListIndex = 0;
            for (int i = 0; i < list.size(); i++) {
                ItemListDialogItem currItem = list.get(i);
                String name = currItem.getName();
                long linkedID = currItem.getLinkedId();

                //If the item is already in the new list, see if the item should be selected.
                if (entries.containsKey(name)) {
                    if (linkedID == primaryItemID) {
                        newList.get(entries.get(name)).setInitialSelected(true);
                    }
                } else {
                    entries.put(name, newListIndex++);
                    if (linkedID == primaryItemID) currItem.setInitialSelected(true);
                    newList.add(currItem);
                }
            }
            return newList;
        }
    }

    /**
     * Custom array adapter class for interfacing with listview.
     */
    private class ItemListDialogArrayAdapter extends ArrayAdapter<ItemListDialogItem> {

        private ArrayList<ItemListDialogItem> itemList;

        public ItemListDialogArrayAdapter(Context context, int resource, List<ItemListDialogItem> objects) {
            super(context, resource, objects);
            itemList = new ArrayList<>();
            itemList.addAll(objects);
        }

        public ArrayList<ItemListDialogItem> getItems() {
            return itemList;
        }


        /**
         * Called by listview to retrieve a view to place in the row for this item.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            IngredientViewHolder holder = null;

            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.listview_item_item_list_dialog, null);

                holder = new IngredientViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.item_list_dialog_item_text);
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.item_list_dialog_check_box);
                convertView.setTag(holder); //Set this holder as this views tag

                holder.checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ItemListDialogItem dialogItem = (ItemListDialogItem) v.getTag();
                        CheckBox checkBox = (CheckBox) v;
                        dialogItem.setFinalSelected(checkBox.isChecked());
                    }
                });

                //When the view is tapped, set the checkbox status and the selected field of the item based on selection.
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
            } else {
                holder = (IngredientViewHolder) convertView.getTag();
            }

            ItemListDialogItem item = itemList.get(position);
            holder.name.setText(item.getName());
            holder.checkBox.setChecked(item.getFinalSelected());
            holder.checkBox.setTag(item); //Assign this item to the checkbox so its isSelected field can be modified with the checkbox

            return convertView;
        }


        /**
         * Helper class for setting tags to views for caching.
         */
        private class IngredientViewHolder {
            TextView name;
            CheckBox checkBox;
        }
    }

    /**
     * AsyncTask for saving data when the user is finished with this dialog.
     */
    private class SaveDataTask extends AsyncTask<Object, Void, Long> {

        private String displayedData;
        private long primaryLinkedID;
        private boolean closeOnFinish;

        @Override
        protected Long doInBackground(Object... params) {
            ArrayList<ItemListDialogItem> selected = (ArrayList<ItemListDialogItem>) params[0];
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            displayedData = (String) params[1];
            primaryLinkedID = (long) params[2];
            closeOnFinish = (boolean) params[3];
            Long result = (long) 1;

            for (ItemListDialogItem item : selected) {
                long primaryID = item.getId();
                boolean initialSelected = item.getInitialSelected();
                boolean finalSelected = item.getFinalSelected();

                //If entry was not originally selected but is now selected, add a new row to the linked table
                if (!initialSelected && finalSelected) {
                    result = insertLinkedRow(db, primaryID);
                } //If entry was initially selcted but now is not, delete the entry from the linked table
                else if (initialSelected && !finalSelected) {
                    result = deleteLinkedRow(db, primaryID);
                }
            }
            result = mListModified ? result : -2;

            return result;
        }

        @Override
        protected void onPostExecute(Long result) {
            if (result == -1) {
                Toast.makeText(getContext(), R.string.toast_save_failed, Toast.LENGTH_SHORT).show();
            }
            if (closeOnFinish) {
                ((DialogClosedListener) getActivity()).onEditListDialogClosed(mListModified);
                getDialog().dismiss();
            }
        }

        /**
         * Adds a new entry to the given database.
         * @param db        The database to insert into
         * @param primaryID The id of the primary item to insert
         * @return the id of the inserted record
         */
        private long insertLinkedRow(SQLiteDatabase db, long primaryID) {
            Long result = (long) -1;
            if (displayedData.equals(INGREDIENTS)) {
                ContentValues values = new ContentValues();
                values.put(DiaryContract.ProductIngredient.COLUMN_INGREDIENT_ID, primaryID);
                values.put(DiaryContract.ProductIngredient.COLUMN_PRODUCT_ID, primaryLinkedID);
                result = db.insert(DiaryContract.ProductIngredient.TABLE_NAME, null, values);
            } else if (displayedData.equals(PRODUCTS)) {
                ContentValues values = new ContentValues();
                values.put(DiaryContract.RoutineProduct.COLUMN_PRODUCT_ID, primaryID);
                values.put(DiaryContract.RoutineProduct.COLUMN_ROUTINE_ID, primaryLinkedID);
                result = db.insert(DiaryContract.RoutineProduct.TABLE_NAME, null, values);
            } else if (displayedData.equals(ROUTINES)) {
                ContentValues values = new ContentValues();
                values.put(DiaryContract.DiaryEntryRoutine.COLUMN_ROUTINE_ID, primaryID);
                values.put(DiaryContract.DiaryEntryRoutine.COLUMN_DIARY_ENTRY_ID, primaryLinkedID);
                result = db.insert(DiaryContract.DiaryEntryRoutine.TABLE_NAME, null, values);
            }
            return result;
        }

        /**
         * Deletes the given record from the given databse.
         * @param db        The databse to remove the record from.
         * @param primaryID The primary item id of the record to remove
         * @return The number of rows affected by this deletion, will always be 1
         */
        private long deleteLinkedRow(SQLiteDatabase db, long primaryID) {
            Long result = (long) -1;
            if (displayedData.equals(INGREDIENTS)) {
                String where = DiaryContract.ProductIngredient.COLUMN_INGREDIENT_ID + " = ? AND " + DiaryContract.ProductIngredient.COLUMN_PRODUCT_ID + " = ?";
                String[] whereArgs = {Long.toString(primaryID), Long.toString(primaryLinkedID)};
                result = (long) db.delete(DiaryContract.ProductIngredient.TABLE_NAME, where, whereArgs);
            } else if (displayedData.equals(PRODUCTS)) {
                String where = DiaryContract.RoutineProduct.COLUMN_PRODUCT_ID + " = ? AND " + DiaryContract.RoutineProduct.COLUMN_ROUTINE_ID + " = ?";
                String[] whereArgs = {Long.toString(primaryID), Long.toString(primaryLinkedID)};
                result = (long) db.delete(DiaryContract.RoutineProduct.TABLE_NAME, where, whereArgs);
            } else if (displayedData.equals(ROUTINES)) {
                String where = DiaryContract.DiaryEntryRoutine.COLUMN_ROUTINE_ID + " = ? AND " + DiaryContract.DiaryEntryRoutine.COLUMN_DIARY_ENTRY_ID + " = ?";
                String[] whereArgs = {Long.toString(primaryID), Long.toString(primaryLinkedID)};
                result = (long) db.delete(DiaryContract.DiaryEntryRoutine.TABLE_NAME, where, whereArgs);
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
        private String mExtraField1; //Ingredient: N/A. Product: Brand. Routine: Time.
        private String mExtraField2; //Ingredient: N/A. Product: Type. Routine: N/A.
        private boolean mInitialSelected;
        private boolean mFinalSelected;

        public ItemListDialogItem() {
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

        public String getExtraField1() {
            return mExtraField1;
        }

        public void setExtraField1(String mExtraField1) {
            this.mExtraField1 = mExtraField1;
        }

        public String getExtraField2() {
            return mExtraField2;
        }

        public void setExtraField2(String mExtraField2) {
            this.mExtraField2 = mExtraField2;
        }
    }

}
