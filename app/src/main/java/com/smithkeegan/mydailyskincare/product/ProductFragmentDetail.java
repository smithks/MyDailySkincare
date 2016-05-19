package com.smithkeegan.mydailyskincare.product;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smithkeegan.mydailyskincare.R;

/**
 * @author Keegan Smith
 * @since 5/19/2016
 */
public class ProductFragmentDetail extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        View rootView = inflater.inflate(R.layout.fragment_product_detail, container, false);
        return rootView;
    }
}
