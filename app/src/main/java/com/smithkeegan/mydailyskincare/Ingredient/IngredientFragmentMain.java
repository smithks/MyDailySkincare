package com.smithkeegan.mydailyskincare.Ingredient;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.smithkeegan.mydailyskincare.Data.DiaryContract;
import com.smithkeegan.mydailyskincare.Data.DiaryDbHelper;
import com.smithkeegan.mydailyskincare.R;

/**
 * Contains the users ingredients displayed in a list view.
 * @author Keegan Smith
 * @since 5/10/2016
 */
public class IngredientFragmentMain extends Fragment {

    DiaryDbHelper mDbHelper = DiaryDbHelper.getInstance(getContext());
    FragmentManager mFragmentManager;
    Button mNewIngredientButton;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstance){
        View rootView = inflater.inflate(R.layout.fragment_ingredient_main, container, false);

        mFragmentManager = getActivity().getSupportFragmentManager();
        mNewIngredientButton = (Button) rootView.findViewById(R.id.ingredient_main_new_button);
        setButtonListener();
/*
        ListView listView = (ListView)rootView.findViewById(R.id.ingredient_main_list_view);
        ArrayList<String> list = new ArrayList<>();
        list.add("Merp");
        list.add("Derp");
        list.add("herp");

        ArrayAdapter adapter = new ArrayAdapter(getActivity(),R.layout.ingredient_listview_item,list);
        listView.setAdapter(adapter); */
        return rootView;
    }

    public void setButtonListener(){
        mNewIngredientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                transaction.replace(R.id.ingredient_activity_main,new IngredientFragmentDetail());
                transaction.commit();
            }
        });
    }

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

        }
    }
}
