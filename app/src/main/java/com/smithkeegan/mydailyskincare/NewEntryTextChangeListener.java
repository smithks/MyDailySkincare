package com.smithkeegan.mydailyskincare;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Listens for changes to an EditText field for a new entry and changes the text color to black.
 * @author Keegan Smith
 * @since 5/19/2016
 */
public class NewEntryTextChangeListener implements TextWatcher {

    Context context;
    EditText parentView;
    boolean changed;
    boolean isNew;

    public NewEntryTextChangeListener(Context context, EditText parentView, boolean isNew){
        this.context = context;
        this.parentView = parentView;
        this.isNew = isNew;
        changed = false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (isNew && !changed){
            parentView.setTextColor(ContextCompat.getColor(context,R.color.black));
            changed = true;
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
