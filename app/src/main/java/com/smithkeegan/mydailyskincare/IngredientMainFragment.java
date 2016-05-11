package com.smithkeegan.mydailyskincare;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author Keegan Smith
 * @since 5/10/2016
 */
public class IngredientMainFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstance){
        return inflater.inflate(R.layout.fragment_ingredient_main, container, false);
    }
}
