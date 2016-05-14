package com.smithkeegan.mydailyskincare;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smithkeegan.mydailyskincare.Data.DiaryContract;
import com.smithkeegan.mydailyskincare.Data.DiaryDbHelper;

/**
 * Contains the users ingredients displayed in a list view.
 * @author Keegan Smith
 * @since 5/10/2016
 */
public class IngredientMainFragment extends Fragment {

    DiaryDbHelper dbHelper = DiaryDbHelper.getInstance(getContext());

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstance){
        View rootView = inflater.inflate(R.layout.fragment_ingredient_main, container, false);

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

    private class FetchIngredientsTask extends AsyncTask<Void,Void,Cursor>{

        @Override
        protected Cursor doInBackground(Void... params) {

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            String[] columns = {DiaryContract.Ingredient._ID,DiaryContract.Ingredient.COLUMN_NAME};
            String sortOrder = DiaryContract.Ingredient.COLUMN_NAME + " DESC";
            return db.query(DiaryContract.Ingredient.TABLE_NAME,columns,null,null,null,null,sortOrder);
        }

        @Override
        protected void onPostExecute(final Cursor result){

        }
    }
}
