package com.smithkeegan.mydailyskincare.analytics;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.smithkeegan.mydailyskincare.R;
import com.smithkeegan.mydailyskincare.data.DiaryContract;

import java.util.ArrayList;
import java.util.Stack;

/**
 * @author Keegan Smith
 * @since 10/18/2016
 */

public class AnalyticsFragmentMain extends Fragment {

    private static final String SAVE_KEY_STATE_STACK = "SAVE_KEY_STATE_STACK";
    private static final String SAVE_KEY_TEXT_QUERY_STACK = "SAVE_KEY_TEXT_QUERY_STACK";

    private TextView mQueryTextView;
    private GridView mButtonGridView;

    private Stack<String> mStateStack;
    private Stack<Spannable> mQueryStringStack;
    private DatabaseQueryFields mQueryBuilder;

    //TODO way to keep up with curent database string

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_analytics_main, container, false);

        initializeMemberViews(rootView);

        //Restore member values if saved instance state is not null, initialize default otherwise.
        if (savedInstanceState != null) {
            restoreSavedState(savedInstanceState);
        } else {
            initializeMemberValues();
        }

        updateGridAndQuery();

        return rootView;
    }

    /**
     * Initializes the views in this fragment.
     * @param rootView the rootview of the fragment
     */
    private void initializeMemberViews(View rootView) {
        mQueryTextView = (TextView) rootView.findViewById(R.id.analytics_query_text_view);
        mButtonGridView = (GridView) rootView.findViewById(R.id.analytics_grid_view);
    }

    /**
     * Initializes member values of this fragment.
     */
    private void initializeMemberValues() {
        mStateStack = new Stack<>();
        mStateStack.push(GridStackStates.STATE_MAIN);

        mQueryStringStack = new Stack<>();
        mQueryStringStack.push(new SpannableString(getString(R.string.analytics_query_show_me)));

        mQueryBuilder = new DatabaseQueryFields();
    }

    /**
     * Restores member values from the passed in saved state instance.
     * @param savedInstanceState bundle saved when fragment was saved
     */
    private void restoreSavedState(Bundle savedInstanceState) {
        mStateStack = (Stack<String>) savedInstanceState.getSerializable(SAVE_KEY_STATE_STACK);
        mQueryStringStack = (Stack<Spannable>) savedInstanceState.getSerializable(SAVE_KEY_TEXT_QUERY_STACK);

    }

    /**
     * Saves member values into a bundle for restoring when the fragment is resumed.
     * @param outState bundle containing member values for restoring
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(SAVE_KEY_STATE_STACK, mStateStack);
        outState.putSerializable(SAVE_KEY_TEXT_QUERY_STACK, mQueryStringStack);

        super.onSaveInstanceState(outState);
    }

    /**
     * Uses the query string stack to build the string that will be displayed at the top of the screen.
     */
    private void updateTextViewQueryString() {
        Stack<Spannable> stackCopy = (Stack<Spannable>) mQueryStringStack.clone();
        ArrayList<Spannable> querySpannables = new ArrayList<>();
        //Fill an array with the current query strings
        while (!stackCopy.isEmpty()) {
            querySpannables.add(stackCopy.pop());
        }

        //Go backwards through this array building the query string
        SpannableStringBuilder queryString = new SpannableStringBuilder();
        for (int i = querySpannables.size() - 1; i >= 0; i--) {
            if (i != querySpannables.size() - 1) {
                queryString.append(" "); //append a space on all iterations other than the first
            }
            queryString.append(querySpannables.get(i));
        }

        mQueryTextView.setText(queryString, TextView.BufferType.SPANNABLE);
    }

    /**
     * Checks the current status of the stateStack and updates the gridview accordingly.
     */
    private void updateGridAndQuery() {
        String state = mStateStack.peek();

        String[] buttonStrings = checkStackState(state);
        //Update the displayed query string
        updateTextViewQueryString();

        //Update the displayed buttons in the grid view
        if (buttonStrings != null) {
            ArrayList<Button> buttons = new ArrayList<>();

            //Build arraylist of buttons to pass to gridview adapter
            for (String title : buttonStrings) {
                buttons.add(getGridButton(title));
            }

            mButtonGridView.setAdapter(new ButtonGridViewAdapter(buttons));
        }
    }

    /**
     * Creates and returns a new button to place in the gridview.
     * @param buttonText the new buttons text
     * @return the new button
     */
    private Button getGridButton(String buttonText) {
        AnalyticsButton choiceButton = new AnalyticsButton(getContext());
        choiceButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 250));

        Drawable drawable = DrawableCompat.wrap(choiceButton.getBackground());
        DrawableCompat.setTint(drawable, ContextCompat.getColor(getContext(), R.color.colorPrimary));
        choiceButton.setBackground(drawable);

        choiceButton.setText(buttonText);
        choiceButton.setTextColor(ContextCompat.getColor(getContext(), R.color.white));

        setButtonProperties(choiceButton); //Set the linked string to push onto the stack and other button properties
        choiceButton.setOnClickListener(getGridButtonListener()); //Set the buttons listener

        return choiceButton;
    }

    /**
     * Checks the current state of the stack and updates the query textview stack accordingly.
     * Fetches the strings to use as button titles based on the top of the state stack.
     * @param state the top of the state stack
     * @return the list of strings to use as button text
     */
    private String[] checkStackState(String state) {
        //TODO create a state for each group of buttons or listview of options
        String[] buttonStrings = null;
        Spannable newQuerySpannable = null;
        switch (state) {
            case GridStackStates.STATE_MAIN: //1st/Top level
                buttonStrings = getResources().getStringArray(R.array.analytics_main_button_strings);
                mQueryStringStack.clear(); //Clear the query string if at top level
                newQuerySpannable = new SpannableString(getString(R.string.analytics_query_show_me));
                break;
            case GridStackStates.STATE_DAYS: //2nd level
                buttonStrings = getResources().getStringArray(R.array.analytics_days_buttons_strings);
                newQuerySpannable = new SpannableString(getString(R.string.analytics_query_where_my));
                break;
            case GridStackStates.STATE_DAYS_CONDITION: //3rd level
                buttonStrings = getResources().getStringArray(R.array.analytics_days_conditions);
                newQuerySpannable = new SpannableString(getString(R.string.analytics_days_was));
                break;
            case GridStackStates.STATE_ROUTINES: //2nd level
                buttonStrings = getResources().getStringArray(R.array.analytics_routine_buttons_strings);
                newQuerySpannable = new SpannableString(getString(R.string.analytics_routine_applied));
                break;
            case GridStackStates.STATE_ROUTINES_DAYS: //3rd level
                buttonStrings = getResources().getStringArray(R.array.analytics_routine_day_buttons);
                break;
            case GridStackStates.STATE_FETCH_DATA: //final level //TODO fetch data from database and replace grid.
                String table = mQueryBuilder.TABLE;
                String where = mQueryBuilder.WHERE;
                String whereArgs = mQueryBuilder.WHERE_ARG;
                break;
        }

        //Update the query string if needed.
        if (newQuerySpannable != null) {
            mQueryStringStack.push(newQuerySpannable);
        }

        return buttonStrings;
    }

    /**
     * Sets analytics properties of this button.
     * @param button the button to set
     */
    private void setButtonProperties(AnalyticsButton button) {
        String linkedState;

        String buttonTitle = button.getText().toString();
        //TODO add button types and query data to all buttons
        if (buttonTitle.equals(getString(R.string.analytics_button_days))) {                    //2nd level
            linkedState = GridStackStates.STATE_DAYS;
            button.setButtonType(AnalyticsButton.BUTTON_TABLE);
            button.setQueryData(DiaryContract.DiaryEntry.TABLE_NAME);
        } else if (buttonTitle.equals(getString(R.string.analytics_button_overall_condition))) { //3rd level
            linkedState = GridStackStates.STATE_DAYS_CONDITION;
            button.setButtonType(AnalyticsButton.BUTTON_WHERE);
            button.setQueryData(DiaryContract.DiaryEntry.COLUMN_OVERALL_CONDITION);
        } else if (buttonTitle.equals(getString(R.string.analytics_button_forehead_condition))) { //3rd level
            linkedState = GridStackStates.STATE_DAYS_CONDITION;

        } else if (buttonTitle.equals(getString(R.string.analytics_button_nose_condition))) {     //3rd level
            linkedState = GridStackStates.STATE_DAYS_CONDITION;

        } else if (buttonTitle.equals(getString(R.string.analytics_button_cheek_condition))) {   //3rd level
            linkedState = GridStackStates.STATE_DAYS_CONDITION;

        } else if (buttonTitle.equals(getString(R.string.analytics_button_lips_condition))) {   //3rd level
            linkedState = GridStackStates.STATE_DAYS_CONDITION;

        } else if (buttonTitle.equals(getString(R.string.analytics_button_chin_condition))) {   //3rd level
            linkedState = GridStackStates.STATE_DAYS_CONDITION;

        } else if (buttonTitle.equals(getString(R.string.diary_entry_condition_excellent))){     //4th level
            linkedState = GridStackStates.STATE_FETCH_DATA;
            button.setButtonType(AnalyticsButton.BUTTON_WHERE_ARG);
            button.setQueryData(Integer.toString(6));
        } else if (buttonTitle.equals(getString(R.string.diary_entry_condition_very_good))){     //4th level
            linkedState = GridStackStates.STATE_FETCH_DATA;
            button.setButtonType(AnalyticsButton.BUTTON_WHERE_ARG);
            button.setQueryData(Integer.toString(5));
        }else if ( buttonTitle.equals(getString(R.string.diary_entry_condition_good))){         //4th level
            linkedState = GridStackStates.STATE_FETCH_DATA;
            button.setButtonType(AnalyticsButton.BUTTON_WHERE_ARG);
            button.setQueryData(Integer.toString(4));
        }else if ( buttonTitle.equals(getString(R.string.diary_entry_condition_fair))){         //4th level
            linkedState = GridStackStates.STATE_FETCH_DATA;
            button.setButtonType(AnalyticsButton.BUTTON_WHERE_ARG);
            button.setQueryData(Integer.toString(3));
        }else if ( buttonTitle.equals(getString(R.string.diary_entry_condition_poor))) {        //4th level
            linkedState = GridStackStates.STATE_FETCH_DATA;
            button.setButtonType(AnalyticsButton.BUTTON_WHERE_ARG);
            button.setQueryData(Integer.toString(2));
        }else if ( buttonTitle.equals(getString(R.string.diary_entry_condition_very_poor))){    //4th level
            linkedState = GridStackStates.STATE_FETCH_DATA;
            button.setButtonType(AnalyticsButton.BUTTON_WHERE_ARG);
            button.setQueryData(Integer.toString(1));
        }else if ( buttonTitle.equals(getString(R.string.diary_entry_condition_severe))) {      //4th level
            linkedState = GridStackStates.STATE_FETCH_DATA;
            button.setButtonType(AnalyticsButton.BUTTON_WHERE_ARG);
            button.setQueryData(Integer.toString(0));
        } else if (buttonTitle.equals(getString(R.string.analytics_button_routines))) {        //2nd level
            linkedState = GridStackStates.STATE_ROUTINES;
        } else if (buttonTitle.equals(getString(R.string.analytics_routine_on_specified))) {   //3rd level
            linkedState = GridStackStates.STATE_ROUTINES_DAYS;
        } else if (buttonTitle.equals(getString(R.string.analytics_button_products))) {        //2nd level
            linkedState = GridStackStates.STATE_PRODUCTS;
        } else if (buttonTitle.equals(getString(R.string.analytics_button_ingredients))) {     //2nd level
            linkedState = GridStackStates.STATE_INGREDIENTS;
        } else { //Return to main if unexpected button pressed
            linkedState = GridStackStates.STATE_MAIN;
        }

        button.setLinkedStackString(linkedState);
    }

    /**
     * Creates and returns a button listener for a grid button.
     * @return listener for a grid button
     */
    private View.OnClickListener getGridButtonListener() {
        View.OnClickListener buttonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //When a button is pressed, push that buttons linked state string onto the stack and then update the stack
                AnalyticsButton thisButton = (AnalyticsButton) v;

                switch (thisButton.getButtonType()){
                    case AnalyticsButton.BUTTON_TABLE:
                        mQueryBuilder.TABLE = thisButton.getQueryData();
                        break;
                    case AnalyticsButton.BUTTON_WHERE:
                        mQueryBuilder.WHERE = thisButton.getQueryData();
                        break;
                    case AnalyticsButton.BUTTON_WHERE_ARG:
                        mQueryBuilder.WHERE_ARG = thisButton.getQueryData();
                        break;
                    default: //No button type assigned
                        break;
                }

                Spannable newQueryString = new SpannableString(thisButton.getText());
                newQueryString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.colorAccent)), 0, newQueryString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                mQueryStringStack.push(newQueryString); //Push the new colored spannable onto the query stack

                mStateStack.push(thisButton.getLinkedStackString()); //Push the new state onto the stack
                updateGridAndQuery(); //Update the grid and query based on new stack
            }
        };
        return buttonListener;
    }

    /**
     * Custom adapter used to populate the buttons grid view with buttons.
     */
    private class ButtonGridViewAdapter extends BaseAdapter {

        ArrayList<Button> mGridViewButtons;

        public ButtonGridViewAdapter(ArrayList<Button> buttons) {
            mGridViewButtons = buttons;
        }

        @Override
        public int getCount() {
            return mGridViewButtons.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Button button;

            if (convertView == null) {
                button = mGridViewButtons.get(position);
            } else {
                button = (Button) convertView;
            }

            return button;
        }
    }

    /**
     * Helper class that contains the possible states that can be pushed onto the state stack.
     */
    private static class GridStackStates {
        public static final String STATE_MAIN = "STATE_MAIN";

        public static final String STATE_DAYS = "STATE_DAYS";
        public static final String STATE_DAYS_CONDITION = "STATE_DAYS_CONDITION";

        public static final String STATE_ROUTINES = "STATE_ROUTINES";
        public static final String STATE_ROUTINES_DAYS = "STATE_ROUTINES_DAYS";

        public static final String STATE_PRODUCTS = "STATE_PRODUCTS";
        public static final String STATE_INGREDIENTS = "STATE_INGREDIENTS";

        public static final String STATE_FETCH_DATA = "STATE_FETCH_DATA";
    }

    /**
     * Custom button class that contains additional fields for storing the linked stack value
     * that is added to the stack when this button is pressed as well as button identifiers.
     */
    private class AnalyticsButton extends Button {
        public static final String BUTTON_TABLE = "BUTTON_TABLE";
        public static final String BUTTON_WHERE = "BUTTON_WHERE";
        public static final String BUTTON_WHERE_ARG = "BUTTON_WHERE_ARG";

        private String mLinkedStackString;
        private String mQueryData;
        private String mButtonType;

        public AnalyticsButton(Context context) {
            super(context);
            mButtonType = "";
        }

        public AnalyticsButton(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public AnalyticsButton(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @TargetApi(21)
        public AnalyticsButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        public void setLinkedStackString(String linkedString) {
            mLinkedStackString = linkedString;
        }

        public String getLinkedStackString() {
            return mLinkedStackString;
        }

        public void setQueryData(String data) {
            mQueryData = data;
        }

        public String getQueryData() {
            return mQueryData;
        }

        public void setButtonType(String type) {
            mButtonType = type;
        }

        public String getButtonType() {
            return mButtonType;
        }

    }

    /**
     * Object to hold the fields that will be used in this database access
     */
    private class DatabaseQueryFields {
        public String TABLE;
        public String WHERE;
        public String WHERE_ARG;

        DatabaseQueryFields() {
            TABLE = "";
            WHERE = "";
            WHERE_ARG = "";
        }
    }
}
