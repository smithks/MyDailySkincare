package com.smithkeegan.mydailyskincare;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * @author Keegan Smith
 * @since 5/10/2016
 */
public class IngredientMainFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstance){
        View rootView = inflater.inflate(R.layout.fragment_ingredient_main, container, false);


        ListView listView = (ListView)rootView.findViewById(R.id.ingredient_main_list_view);
        ArrayList<String> list = new ArrayList<>();
        list.add("Merp");
        list.add("Derp");
        list.add("herp");

        ArrayAdapter adapter = new ArrayAdapter(getActivity(),R.layout.ingredient_listview_item,list);
        listView.setAdapter(adapter);
        return rootView;
    }
}
