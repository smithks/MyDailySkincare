package com.smithkeegan.mydailyskincare.analytics;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smithkeegan.mydailyskincare.R;

/**
 * @author Keegan Smith
 * @since 10/18/2016
 */

public class AnalyticsFragmentMain extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_analytics_main,container,false);


        return rootView;
    }
}
