package com.smithkeegan.mydailyskincare.customClasses;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

import com.smithkeegan.mydailyskincare.ui.CalendarActivityMain;
import com.smithkeegan.mydailyskincare.R;

import java.util.Calendar;
import java.util.Date;

/**
 * Dialog that holds a date picker object that is used to pick a date to scroll the calendar
 * in the parent activity to.
 * @author Keegan Smith
 * @since 9/22/2016
 */

public class DatePickerDialogFragment extends DialogFragment{

    private DatePicker mDatePicker;
    private Button mCancelButton;
    private Button mOkButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.calendar_date_picker,container,false);

        mDatePicker = (DatePicker) view.findViewById(R.id.date_picker_dialog_picker);

        mCancelButton = (Button) view.findViewById(R.id.date_picker_dialog_cancel_button);
        mOkButton = (Button) view.findViewById(R.id.date_picker_dialog_ok_button);

        setListeners();

        return view;
    }

    /**
     * Sets button listeners.
     */
    private void setListeners(){
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendScrollToDate();
                getDialog().dismiss();
            }
        });
    }

    /**
     * Called when user press the ok button. Grabs the current date of the date picker
     * and passes it to the parent activity.
     */
    private void sendScrollToDate(){
        int day = mDatePicker.getDayOfMonth();
        int month = mDatePicker.getMonth();
        int year = mDatePicker.getYear();

        Calendar calendar = Calendar.getInstance();

        calendar.set(year,month,day,0,0,0);

        Date date = calendar.getTime();
        ((CalendarActivityMain) getActivity()).scrollToDate(date);
    }
}
