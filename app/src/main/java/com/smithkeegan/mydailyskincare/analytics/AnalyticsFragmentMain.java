package com.smithkeegan.mydailyskincare.analytics;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.smithkeegan.mydailyskincare.R;

import java.util.ArrayList;
import java.util.Stack;

/**
 * @author Keegan Smith
 * @since 10/18/2016
 */

public class AnalyticsFragmentMain extends Fragment {

    private static final String STATE_KEY_STACK = "STATE_KEY_STACK";

    private TextView mQueryTextView;
    private GridView mButtonGridView;

    private Stack<String> mStateStack;
    private String mDatabaseQuery;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_analytics_main,container,false);

        initializeMemberViews(rootView);

        //Restore member values if saved instance state is not null, initialize default otherwise.
        if (savedInstanceState != null){
            restoreSavedState(savedInstanceState);
        }else{
            initializeMemberValues();
        }

        updateButtonGridView();

        return rootView;
    }

    /**
     * Initializes the views in this fragment.
     * @param rootView the rootview of the fragment
     */
    private void initializeMemberViews(View rootView){
        mQueryTextView = (TextView) rootView.findViewById(R.id.analytics_query_text_view);
        mButtonGridView = (GridView) rootView.findViewById(R.id.analytics_grid_view);
    }

    /**
     * Initializes member values of this fragment.
     */
    private void initializeMemberValues(){
        mStateStack = new Stack<>();
        mStateStack.push("Main");
        mDatabaseQuery = "";
    }

    /**
     * Restores member values from the passed in saved state instance.
     * @param savedInstanceState bundle saved when fragment was saved
     */
    private void restoreSavedState(Bundle savedInstanceState){
        mStateStack = (Stack<String>) savedInstanceState.getSerializable(STATE_KEY_STACK);
    }

    /**
     * Saves member values into a bundle for restoring when the fragment is resumed.
     * @param outState bundle containing member values for restoring
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(STATE_KEY_STACK,mStateStack);

        super.onSaveInstanceState(outState);
    }

    /**
     * Checks the current status of the stateStack and updates the gridview accordingly.
     */
    private void updateButtonGridView(){
        String state = mStateStack.peek();

        String[] buttonStrings = getButtonStrings(state);
        if (buttonStrings != null){
            ArrayList<Button> buttons = new ArrayList<>();

            //Build arraylist of buttons to pass to gridview adapter
            for (String title : buttonStrings){
                buttons.add(getGridButton(title,null));
            }

            mButtonGridView.setAdapter(new ButtonGridViewAdapter(buttons));
        }
    }

    /**
     * Fetches the strings to use as button titles based on the current state of the stack.
     * @param state the current state of the stack
     * @return the list of strings to use as button text
     */
    private String[] getButtonStrings(String state){
        String[] buttonStrings;
        switch (state){
            case "Main":
                buttonStrings = getResources().getStringArray(R.array.analytics_main_strings);
                break;
            default:
                buttonStrings = null;
                break;
        }
        return buttonStrings;
    }

    /**
     * Creates and returns a new button to place in the gridview.
     * @param buttonText the new buttons text
     * @param buttonListener the new buttons click listener
     * @return the new button
     */
    private Button getGridButton(String buttonText, View.OnClickListener buttonListener){
        Button choiceButton = new Button(getContext());
        choiceButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,300));
        Drawable drawable = DrawableCompat.wrap(choiceButton.getBackground());
        DrawableCompat.setTint(drawable, ContextCompat.getColor(getContext(),R.color.colorPrimary));
        choiceButton.setBackground(drawable);
        choiceButton.setText(buttonText);
        choiceButton.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
        choiceButton.setOnClickListener(buttonListener);
        return choiceButton;
    }

    /**
     * Creates and returns a button listener for a grid button.
     * @return listener for a grid button
     */
    private View.OnClickListener getButtonListener(){
        View.OnClickListener buttonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        };
        return buttonListener;
    }

    /**
     * Custom adapter used to populate the buttons grid view with buttons.
     */
    private class ButtonGridViewAdapter extends BaseAdapter{

        ArrayList<Button> mGridViewButtons;

        public ButtonGridViewAdapter(ArrayList<Button> buttons){
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

            if (convertView == null){
                button = mGridViewButtons.get(position);
            }else{
                button = (Button) convertView;
            }

            return button;
        }
    }
}
