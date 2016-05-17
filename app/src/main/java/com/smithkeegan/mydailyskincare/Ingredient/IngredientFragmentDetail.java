package com.smithkeegan.mydailyskincare.Ingredient;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smithkeegan.mydailyskincare.Data.DiaryDbHelper;
import com.smithkeegan.mydailyskincare.R;

/**
 * @author Keegan Smith
 * @since 5/10/2016
 */
public class IngredientFragmentDetail extends Fragment {

    DiaryDbHelper dbHelper = DiaryDbHelper.getInstance(getContext());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        return inflater.inflate(R.layout.fragment_ingredient_detail,container,false);
    }
}
