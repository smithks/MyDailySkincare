package com.smithkeegan.mydailyskincare.routine;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smithkeegan.mydailyskincare.R;

/**
 * @author Keegan Smith
 * @since 8/5/2016
 */
public class RoutineFragmentDetail extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_routine_detail,container,false);

        return rootView;
    }
}
