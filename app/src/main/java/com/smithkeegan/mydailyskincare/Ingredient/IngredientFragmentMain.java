package com.smithkeegan.mydailyskincare.ingredient;

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

import com.smithkeegan.mydailyskincare.R;
import com.smithkeegan.mydailyskincare.data.DiaryContract;
import com.smithkeegan.mydailyskincare.data.DiaryDbHelper;

/**
 * Contains the users ingredients displayed in a list view.
 * @author Keegan Smith
 * @since 5/10/2016
 */
public class IngredientFragmentMain extends Fragment {

    DiaryDbHelper mDbHelper;
    ListView mIngredientsList;
    Button mNewIngredientButton;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstance){
        View rootView = inflater.inflate(R.layout.fragment_ingredient_main, container, false);

        mDbHelper = DiaryDbHelper.getInstance(getContext());
        mNewIngredientButton = (Button) rootView.findViewById(R.id.ingredient_main_new_button);
        setButtonListener();

        mIngredientsList = (ListView)rootView.findViewById(R.id.ingredient_main_list_view);

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        refreshListView();
    }

    private void refreshListView(){
        new FetchIngredientsTask().execute();
    }

    /**
     * Sets listener for the new ingredient button.
     */
    public void setButtonListener(){
        mNewIngredientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),IngredientActivityDetail.class);
                intent.putExtra(IngredientActivityDetail.NEW_INGREDIENT,true);
                startActivity(intent);
            }
        });
    }


    /**
     * Background process to fetch and populate the listview of ingredients.
     */
    private class FetchIngredientsTask extends AsyncTask<Void,Void,Cursor>{

        @Override
        protected Cursor doInBackground(Void... params) {

            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            String[] columns = {DiaryContract.Ingredient._ID,DiaryContract.Ingredient.COLUMN_NAME};
            String sortOrder = DiaryContract.Ingredient.COLUMN_NAME + " DESC";
            return db.query(DiaryContract.Ingredient.TABLE_NAME,columns,null,null,null,null,sortOrder);
        }

        @Override
        protected void onPostExecute(final Cursor result){
            String[] fromColumns = {DiaryContract.Ingredient.COLUMN_NAME};
            int[] toViews = {R.id.ingredient_list_view_item};
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(getContext(),R.layout.ingredient_listview_item,result,fromColumns,toViews,SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
            mIngredientsList.setAdapter(adapter);
            mIngredientsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                /*
                 * Called when a user clicks on an entry in the ingredient listview. Opens the ingredient detail
                 * for that ingredient.
                 */
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getContext(),IngredientActivityDetail.class);
                    intent.putExtra(IngredientActivityDetail.NEW_INGREDIENT,false); //Not a new ingredient
                    intent.putExtra(IngredientActivityDetail.ENTRY_ID,id); //ID of ingredient
                    startActivity(intent);
                }
            });
        }
    }
}
