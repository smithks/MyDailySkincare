package com.smithkeegan.mydailyskincare.ingredient;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.smithkeegan.mydailyskincare.R;
import com.smithkeegan.mydailyskincare.data.DiaryContract;
import com.smithkeegan.mydailyskincare.data.DiaryDbHelper;

/**
 * @author Keegan Smith
 * @since 5/10/2016
 * TODO return to calling location, either add ingredients to product or ingredients listpage
 * TODO disable button if no changes have taken place
 */
public class IngredientFragmentDetail extends Fragment {

    private DiaryDbHelper dbHelper;
    private Boolean mNewEntry;
    private Boolean mEntryChanged;
    private EditText mIngredientName;
    private EditText mComment;
    private CheckBox mSkinIrritant;
    private Button mButtonDelete;
    private Button mButtonSave;

    String mInitalName;
    boolean mInitialCheck;
    String mInitialComment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        View rootView =  inflater.inflate(R.layout.fragment_ingredient_detail,container,false);
        dbHelper = DiaryDbHelper.getInstance(getContext());

        mIngredientName = (EditText) rootView.findViewById(R.id.ingredient_name_edit);
        mComment = (EditText) rootView.findViewById(R.id.ingredient_comment_edit);
        mSkinIrritant = (CheckBox) rootView.findViewById(R.id.ingredient_irritant_checkbox);
        mButtonDelete = (Button) rootView.findViewById(R.id.ingredient_delete_button);
        mButtonSave = (Button) rootView.findViewById(R.id.ingredient_save_button);


        Bundle args = getArguments();
        mNewEntry = args.getBoolean(IngredientActivityDetail.NEW_INGREDIENT,true);

        if (mNewEntry) {
            mInitalName = mIngredientName.getText().toString();
            mInitialCheck = mSkinIrritant.isChecked();
            mInitialComment = mComment.getText().toString();
        } else{
            //TODO Load from db
        }

        mEntryChanged = false;
        setButtonListeners();
        return rootView;
    }

    /**
     *Sets listeners for delete and save buttons.
     */
    private void setButtonListeners(){
        mButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mIngredientName.getText().toString().trim();
                boolean isIrritant = mSkinIrritant.isChecked();
                String irritant = isIrritant ? "1" : "0";
                String comment = mComment.getText().toString().trim();
                mEntryChanged = (!name.equals(mInitalName)) || (!isIrritant == mInitialCheck) || (!comment.equals(mInitialComment));
                if (mEntryChanged){
                    String[] params = {name,irritant,comment};
                    new SaveIngredientTask().execute(params);
                }else{
                    Toast.makeText(getContext(),"No changes to save!", Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    private class SaveIngredientTask extends AsyncTask<String,Void,Long>{

        @Override
        protected Long doInBackground(String... params) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(DiaryContract.Ingredient.COLUMN_NAME,params[0]);
            values.put(DiaryContract.Ingredient.COLUMN_IRRITANT,Integer.parseInt(params[1]));
            values.put(DiaryContract.Ingredient.COLUMN_COMMENT,params[2]);

            return db.insert(DiaryContract.Ingredient.TABLE_NAME,null,values);
        }

        @Override
        protected void onPostExecute(final Long param){
            if (param == -1){
                Toast.makeText(getContext(),"Error storing value",Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getContext(),"Store successful. ID: "+param,Toast.LENGTH_SHORT).show();
            }
        }
    }
}
