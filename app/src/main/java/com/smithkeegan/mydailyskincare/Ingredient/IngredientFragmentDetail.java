package com.smithkeegan.mydailyskincare.ingredient;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.smithkeegan.mydailyskincare.NewEntryTextChangeListener;
import com.smithkeegan.mydailyskincare.R;
import com.smithkeegan.mydailyskincare.data.DiaryContract;
import com.smithkeegan.mydailyskincare.data.DiaryDbHelper;

/**
 * @author Keegan Smith
 * @since 5/10/2016
 * TODO return to calling location, either add ingredients to product or ingredients listpage
 * TODO disable button if no changes have taken place, listeners for each field, change "update" text to "save"
 */
public class IngredientFragmentDetail extends Fragment {

    private DiaryDbHelper mDbHelper;
    private Boolean mNewEntry;
    private Boolean mEntryChanged;
    private EditText mNameEditText;
    private EditText mCommentEditText;
    private CheckBox mIrritantCheckbox;
    private Button mButtonDelete;
    private Button mButtonSave;

    private String mInitialName;
    private boolean mInitialCheck;
    private String mInitialComment;
    private Long mExistingId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        View rootView =  inflater.inflate(R.layout.fragment_ingredient_detail,container,false);
        mDbHelper = DiaryDbHelper.getInstance(getContext());

        mNameEditText = (EditText) rootView.findViewById(R.id.ingredient_name_edit);
        mCommentEditText = (EditText) rootView.findViewById(R.id.ingredient_comment_edit);
        mIrritantCheckbox = (CheckBox) rootView.findViewById(R.id.ingredient_irritant_checkbox);
        mButtonDelete = (Button) rootView.findViewById(R.id.ingredient_delete_button);
        mButtonSave = (Button) rootView.findViewById(R.id.ingredient_save_button);


        Bundle args = getArguments();
        mNewEntry = args.getBoolean(IngredientActivityDetail.NEW_INGREDIENT,true);
        mExistingId = args.getLong(IngredientActivityDetail.ENTRY_ID,-1);

        if (mNewEntry || mExistingId < 0) { //New entry or error loading
            mNameEditText.setTextColor(ContextCompat.getColor(getContext(),R.color.newText));
            mCommentEditText.setTextColor(ContextCompat.getColor(getContext(),R.color.newText));
            mInitialName = mNameEditText.getText().toString();
            mInitialCheck = mIrritantCheckbox.isChecked();
            mInitialComment = mCommentEditText.getText().toString();
            mButtonDelete.setVisibility(View.GONE);
        } else{ //Load data from existing entry
            new LoadIngredientTask().execute(mExistingId);
        }

        mEntryChanged = false;
        setListeners();
        return rootView;
    }

    /**
     *Sets listeners for delete and save buttons.
     */
    private void setListeners(){
        mButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.alert_delete_message)
                        .setTitle(R.string.alert_delete_title)
                        .setIcon(R.drawable.ic_warning_black_24dp)
                        .setPositiveButton(R.string.alert_delete_confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new DeleteIngredientTask().execute();
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

        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mNameEditText.getText().toString().trim();
                boolean isIrritant = mIrritantCheckbox.isChecked();
                String irritant = isIrritant ? "1" : "0";
                String comment = mCommentEditText.getText().toString().trim();
                mEntryChanged = (!name.equals(mInitialName)) || (!isIrritant == mInitialCheck) || (!comment.equals(mInitialComment));
                if (mEntryChanged){
                    String[] params = {name,irritant,comment};
                    new SaveIngredientTask().execute(params);
                }else{
                    Toast.makeText(getContext(),R.string.no_changes_string, Toast.LENGTH_SHORT).show();
                }
            }
        });

        mNameEditText.addTextChangedListener(new NewEntryTextChangeListener(getContext(), mNameEditText,mNewEntry));
        mCommentEditText.addTextChangedListener(new NewEntryTextChangeListener(getContext(), mCommentEditText,mNewEntry));
    }

    /**
     * Task to save a new ingredient or save changes to an existing ingredient.
     * Performed as a background task.
     */
    private class SaveIngredientTask extends AsyncTask<String,Void,Long>{

        @Override
        protected Long doInBackground(String... params) {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(DiaryContract.Ingredient.COLUMN_NAME,params[0]);
            values.put(DiaryContract.Ingredient.COLUMN_IRRITANT,Integer.parseInt(params[1]));
            values.put(DiaryContract.Ingredient.COLUMN_COMMENT,params[2]);
            if (mNewEntry)
                return db.insert(DiaryContract.Ingredient.TABLE_NAME,null,values);
            else{
                String where = DiaryContract.Ingredient._ID + " = ?";
                String[] whereArg = {mExistingId.toString()};
                int rows =  db.update(DiaryContract.Ingredient.TABLE_NAME,values,where,whereArg);
                return Long.valueOf(rows == 0? -1 : 1); //return -1 if no rows affected, error storing
            }

        }

        @Override
        protected void onPostExecute(final Long param){
            if (param == -1){
                Toast.makeText(getContext(),R.string.toast_save_failed,Toast.LENGTH_SHORT).show();
            }else {
                if (mNewEntry)
                    Toast.makeText(getContext(),R.string.toast_save_success,Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getContext(),R.string.toast_update_success,Toast.LENGTH_SHORT).show();
            }
            //TODO uncomment if using highlight
            //Intent intent = new Intent();
            //intent.putExtra(IngredientActivityMain.INGREDIENT_FINISHED_ID,param);
            //getActivity().setResult(AppCompatActivity.RESULT_OK,intent);
            getActivity().finish();
        }
    }

    /**
     * Task to delete the currently open ingredient.
     */
    private class DeleteIngredientTask extends AsyncTask<Void,Void,Integer>{

        @Override
        protected Integer doInBackground(Void... params) {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            String where = DiaryContract.Ingredient._ID + " = ?";
            String[] whereArgs = {mExistingId.toString()};
            return db.delete(DiaryContract.Ingredient.TABLE_NAME,where,whereArgs);
        }

        @Override
        protected void onPostExecute(final Integer param){
            if(param == 1){
                Toast.makeText(getContext(),R.string.toast_delete_successful,Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getContext(),R.string.toast_delete_failed,Toast.LENGTH_SHORT).show();
            }
            getActivity().finish();
        }
    }


    /**
     * Task to load ingredient data from database on fragment load. Performed in background.
     */
    private class LoadIngredientTask extends AsyncTask<Long,Void,Cursor>{

        @Override
        protected Cursor doInBackground(Long... params) {
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            String[] columns = {DiaryContract.Ingredient.COLUMN_NAME,DiaryContract.Ingredient.COLUMN_IRRITANT, DiaryContract.Ingredient.COLUMN_COMMENT};
            String where = DiaryContract.Ingredient._ID + " = "+params[0];
            return db.query(DiaryContract.Ingredient.TABLE_NAME,columns,where,null,null,null,null);
        }

        @Override
        protected void onPostExecute(final Cursor result){
            if(result.moveToFirst()) {
                String name = result.getString(result.getColumnIndex(DiaryContract.Ingredient.COLUMN_NAME));
                int irritantInt = result.getInt(result.getColumnIndex(DiaryContract.Ingredient.COLUMN_IRRITANT));
                boolean isIrritant = irritantInt == 1; //boolean stored as int (1 == true, 0 == false
                String comment = result.getString(result.getColumnIndex(DiaryContract.Ingredient.COLUMN_COMMENT));

                mNameEditText.setText(name);
                mInitialName = name;

                mIrritantCheckbox.setChecked(isIrritant);
                mInitialCheck = isIrritant;

                mCommentEditText.setText(comment);
                mInitialComment = comment;
            }
        }
    }
}
