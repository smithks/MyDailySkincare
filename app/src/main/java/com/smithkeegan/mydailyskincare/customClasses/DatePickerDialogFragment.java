package com.smithkeegan.mydailyskincare.customClasses;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smithkeegan.mydailyskincare.R;

/**
 * @author Keegan Smith
 * @since 9/22/2016
 */

public class DatePickerDialogFragment extends DialogFragment{

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.calendar_date_picker,container,false);

        return view;
    }
}
