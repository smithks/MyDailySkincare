package com.smithkeegan.mydailyskincare.analytics;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.smithkeegan.mydailyskincare.R;
import com.smithkeegan.mydailyskincare.customClasses.DatabaseQueryFieldCollection;
import com.smithkeegan.mydailyskincare.data.DiaryContract;
import com.smithkeegan.mydailyskincare.data.DiaryDbHelper;
import com.smithkeegan.mydailyskincare.diaryEntry.DiaryEntryActivityMain;
import com.smithkeegan.mydailyskincare.ingredient.IngredientActivityDetail;
import com.smithkeegan.mydailyskincare.routine.RoutineActivityDetail;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Stack;

/**
 * @author Keegan Smith
 * @since 10/18/2016
 */

public class AnalyticsFragmentMain extends Fragment {

    private static final String SAVE_KEY_STATE_STACK = "SAVE_KEY_STATE_STACK";
    private static final String SAVE_KEY_TEXT_QUERY_STACK = "SAVE_KEY_TEXT_QUERY_STACK";
    private static final String SAVE_KEY_QUERY_BUILDER = "SAVE_KEY_QUERY_BUILDER";
    private static final String SAVE_KEY_VISIBLE_LAYOUT = "SAVE_KEY_VISIBLE_LAYOUT";

    private TextView mQueryTextView;
    private GridView mButtonGridView;
    private View mLoadingView;
    private ListView mResultsListView;
    private TextView mResultsEmptyText;
    private Menu mMenu;

    private Stack<String> mStateStack;
    private Stack<Spannable> mQueryStringStack;
    private DatabaseQueryFieldCollection mQueryBuilder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mMenu = menu;
        inflater.inflate(R.menu.menu_analytics, menu);
        //Set the default state of this menu.
        if (mStateStack != null && mStateStack.size() > 0 && mStateStack.peek().equals(GridStackStates.STATE_MAIN)){
            setBackMenuVisibility(false);
        }else{
            setBackMenuVisibility(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_analytics_back:
                goBackOneLevel();
                return true;
            case R.id.menu_analytics_start_over:
                startOver();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Called to change the visibility of the options in the menu bar. The options will not be
     * visible if the analytics fragment is at the top level.
     * @param visible whether the option is visible or not
     */
    private void setBackMenuVisibility(boolean visible){
        if (mMenu != null) {
            MenuItem backMenuItem = mMenu.findItem(R.id.menu_analytics_back);
            MenuItem startOverBackItem = mMenu.findItem(R.id.menu_analytics_start_over);
            backMenuItem.setVisible(visible);
            startOverBackItem.setVisible(visible);
            getActivity().invalidateOptionsMenu();
        }
    }

    /**
     * Called when the user presses the back button within the analytics fragment. If top level then back proceeds normally, otherwise
     * the goBackOneLevel method is called to go up one level in the analytics.
     */
    public boolean backButtonPressed(){
        if (mStateStack != null && mStateStack.size() > 0 && !(mStateStack.peek().equals(GridStackStates.STATE_MAIN))){
            goBackOneLevel();
            return true;
        }else { //At top level, handle back button in activity
            return false;
        }
    }

    /**
     * Goes back one level of the analytics query.
     */
    private void goBackOneLevel() {
        if (mStateStack.size() > 1) {
            mStateStack.pop();
            if (mQueryStringStack.size() > 1) {
                mQueryStringStack.pop();
            }
            updateGridAndQuery();
        }
    }

    /**
     * Returns the analytics page to the default starting point.
     */
    private void startOver() {
        mStateStack.clear();
        mStateStack.push(GridStackStates.STATE_MAIN);
        updateGridAndQuery();
    }

    /**
     * Initializes the views in this fragment.
     * @param rootView the rootview of the fragment
     */
    private void initializeMemberViews(View rootView) {
        mQueryTextView = (TextView) rootView.findViewById(R.id.analytics_query_text_view);
        mButtonGridView = (GridView) rootView.findViewById(R.id.analytics_grid_view);
        mLoadingView = rootView.findViewById(R.id.analytics_loading_view);
        mResultsListView = (ListView) rootView.findViewById(R.id.analytics_results_list_view);
        mResultsEmptyText = (TextView) rootView.findViewById(R.id.analytics_list_view_empty_text);
    }

    /**
     * Initializes member values of this fragment.
     */
    private void initializeMemberValues() {
        mStateStack = new Stack<>();
        mStateStack.push(GridStackStates.STATE_MAIN);

        mQueryStringStack = new Stack<>();
        mQueryStringStack.push(new SpannableString(getString(R.string.analytics_query_show_me)));

        mQueryBuilder = new DatabaseQueryFieldCollection();

        showLayout(mButtonGridView);
    }

    /**
     * Restores member values from the passed in saved state instance.
     * @param savedInstanceState bundle saved when fragment was saved
     */
    private void restoreSavedState(Bundle savedInstanceState) {
        mStateStack = (Stack<String>) savedInstanceState.getSerializable(SAVE_KEY_STATE_STACK);
        mQueryStringStack = (Stack<Spannable>) savedInstanceState.getSerializable(SAVE_KEY_TEXT_QUERY_STACK);
        mQueryBuilder = savedInstanceState.getParcelable(SAVE_KEY_QUERY_BUILDER);

        //Get the currently visible layout based
        int currentLayout = savedInstanceState.getInt(SAVE_KEY_VISIBLE_LAYOUT);
        switch (currentLayout){
            case 1:
                showLayout(mLoadingView);
                break;
            case 2:
                showLayout(mResultsListView);
                break;
            case 3:
                showLayout(mResultsEmptyText);
                break;
            default:
                showLayout(mButtonGridView);
        }
    }

    /**
     * Saves member values into a bundle for restoring when the fragment is resumed.
     * @param outState bundle containing member values for restoring
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(SAVE_KEY_STATE_STACK, mStateStack);
        outState.putSerializable(SAVE_KEY_TEXT_QUERY_STACK, mQueryStringStack);
        outState.putParcelable(SAVE_KEY_QUERY_BUILDER,mQueryBuilder);

        //Save an int representing the currently visible layout.
        int currentLayout;
        if (mLoadingView.getVisibility() == View.VISIBLE){
            currentLayout = 1;
        } else if (mResultsListView.getVisibility() == View.VISIBLE){
            currentLayout = 2;
        } else if (mResultsEmptyText.getVisibility() == View.VISIBLE){
            currentLayout = 3;
        }else { //Default is button gridview layout.
            currentLayout = 0;
        }
        outState.putInt(SAVE_KEY_VISIBLE_LAYOUT,currentLayout);
        //TODO save query builder status and curent displayed layout?
        super.onSaveInstanceState(outState);
    }

    /**
     * Used to set the specified view to visible while hiding all other views.
     * @param view the view to show
     */
    private void showLayout(View view) {
        //Set all views to invisible
        mButtonGridView.setVisibility(View.INVISIBLE);
        mLoadingView.setVisibility(View.INVISIBLE);
        mResultsListView.setVisibility(View.INVISIBLE);
        mResultsEmptyText.setVisibility(View.INVISIBLE);

        //Set the passed in view to visible
        view.setVisibility(View.VISIBLE);
    }

    /**
     * Uses the query string stack to build the string that will be displayed at the top of the screen.
     */
    private void updateTextViewQueryString(boolean resultString) {
        Stack<Spannable> stackCopy = (Stack<Spannable>) mQueryStringStack.clone();
        ArrayList<Spannable> querySpannables = new ArrayList<>();
        //Fill an array with the current query strings
        while (!stackCopy.isEmpty()) {
            querySpannables.add(stackCopy.pop());
        }

        //Go backwards through this array building the query string
        SpannableStringBuilder queryString = new SpannableStringBuilder();
        int i;
        if (resultString) { //Skip "show me" string if displaying results data.
            i = querySpannables.size() - 2;
        } else {
            i = querySpannables.size() - 1;
        }
        boolean firstString = true;
        while (i >= 0) {
            Spannable currSpannable = querySpannables.get(i);
            if (firstString) { //Capitalize the first word of the string.
                Object[] spans = currSpannable.getSpans(0, currSpannable.length(), Object.class);
                Spannable newString = new SpannableString(Character.toString(currSpannable.charAt(0)).toUpperCase() + currSpannable.subSequence(1, currSpannable.length()));
                if (spans.length > 0) { //Restore span if one existed.
                    newString.setSpan(spans[0], currSpannable.getSpanStart(spans[0]), currSpannable.getSpanEnd(spans[0]), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    currSpannable = newString;
                }
                firstString = false;
            } else { //Add a space to every string but the first
                queryString.append(" ");
            }
            queryString.append(currSpannable);
            i--;
        }

        mQueryTextView.setText(queryString, TextView.BufferType.SPANNABLE);
    }

    /**
     * Checks the current status of the stateStack and updates the gridview accordingly.
     */
    private void updateGridAndQuery() {
        if (!mStateStack.isEmpty()) {
            String state = mStateStack.peek();

            String[] buttonStrings = checkStackState(state);

            //Update the displayed buttons in the grid view if buttonStrings array was populated
            if (buttonStrings != null) {
                ArrayList<Button> buttons = new ArrayList<>();

                //Build arraylist of buttons to pass to gridview adapter
                for (String title : buttonStrings) {
                    buttons.add(getGridButton(title));
                }

                mButtonGridView.setAdapter(new ButtonGridViewAdapter(buttons));
            }
        }
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
        //Spannable newQuerySpannable = null;
        boolean resultsState = false;
        boolean useMemberQuery = true;
        showLayout(mButtonGridView);
        setBackMenuVisibility(true); //Will be visible for all levels but the top
        switch (state) {
            case GridStackStates.STATE_MAIN: //1st/Top level
                buttonStrings = getResources().getStringArray(R.array.analytics_main_button_strings);
                mQueryStringStack.clear(); //Clear the query string if at top level
                Spannable newQuerySpannable = new SpannableString(getString(R.string.analytics_query_show_me));
                mQueryStringStack.push(newQuerySpannable);
                setBackMenuVisibility(false);
                break;
            case GridStackStates.STATE_DAYS: //2nd level
                buttonStrings = getResources().getStringArray(R.array.analytics_days_buttons_strings);
                mQueryBuilder.setColumns(new String[]{DiaryContract.DiaryEntry._ID, DiaryContract.DiaryEntry.COLUMN_DATE});
                mQueryBuilder.ORDER_BY = DiaryContract.DiaryEntry.COLUMN_DATE + " ASC";
                break;
            case GridStackStates.STATE_DAYS_CONDITION: //3rd level
                buttonStrings = getResources().getStringArray(R.array.analytics_days_conditions);
                break;
            case GridStackStates.STATE_DAYS_EXERCISE: //3rd level
                buttonStrings = getResources().getStringArray(R.array.analytics_days_exercise_strings);
                break;
            case GridStackStates.STATE_DAYS_DIET:  //3rd level
                buttonStrings = getResources().getStringArray(R.array.analytics_days_diet_strings);
                break;
            case GridStackStates.STATE_DAYS_HYGIENE: //3rd level
                buttonStrings = getResources().getStringArray(R.array.analytics_days_hygiene_strings);
                break;
            case GridStackStates.STATE_DAYS_WATER_INTAKE: //3rd level
                buttonStrings = getResources().getStringArray(R.array.analytics_days_water_strings);
                break;
            case GridStackStates.STATE_DAYS_ROUTINES:  //3rd level load all routines
                useMemberQuery = false;
                showLayout(mLoadingView);
                String[] routineColumns = {DiaryContract.Routine._ID, DiaryContract.Routine.COLUMN_NAME};
                String routineOrderBy = DiaryContract.Routine.COLUMN_NAME + " ASC";
                Object[] routineArgs = {useMemberQuery, DiaryContract.Routine.TABLE_NAME, routineColumns, null, null, routineOrderBy};
                new FetchFromDatabase().execute(routineArgs);
                break;


            case GridStackStates.STATE_ROUTINES: //2nd level
                buttonStrings = getResources().getStringArray(R.array.analytics_routine_buttons_strings);
                mQueryBuilder.setColumns(new String[]{DiaryContract.Routine._ID, DiaryContract.Routine.COLUMN_NAME});
                mQueryBuilder.ORDER_BY = DiaryContract.Routine.COLUMN_NAME + " ASC";
                break;
            case GridStackStates.STATE_ROUTINES_WEEKDAYS: //3rd level
                buttonStrings = getResources().getStringArray(R.array.analytics_routine_weekday_buttons);
                break;
            case GridStackStates.STATE_ROUTINES_PRODUCTS: //3rd level, load all products
                useMemberQuery = false;
                showLayout(mLoadingView);
                String[] productColumns = {DiaryContract.Product._ID, DiaryContract.Product.COLUMN_NAME};
                String productOrderBy = DiaryContract.Product.COLUMN_NAME + " ASC";
                Object[] productArgs = {useMemberQuery, DiaryContract.Product.TABLE_NAME, productColumns, null, null, productOrderBy};
                new FetchFromDatabase().execute(productArgs);
                break;


            case GridStackStates.STATE_PRODUCTS:
                buttonStrings = getResources().getStringArray(R.array.analytics_products_buttons);
                mQueryBuilder.setColumns(new String[]{DiaryContract.Product._ID, DiaryContract.Product.COLUMN_NAME});
                mQueryBuilder.ORDER_BY = DiaryContract.Product.COLUMN_NAME + " ASC";
                break;
            case GridStackStates.STATE_PRODUCTS_TYPES:
                buttonStrings = getResources().getStringArray(R.array.product_types_array);
                break;
            case GridStackStates.STATE_PRODUCTS_INGREDIENTS:
                useMemberQuery = false;
                showLayout(mLoadingView);
                String[] ingredientColumns = {DiaryContract.Ingredient._ID, DiaryContract.Ingredient.COLUMN_NAME};
                String ingredientOrderBy = DiaryContract.Ingredient.COLUMN_NAME + " ASC";
                Object[] ingredientArgs = {useMemberQuery, DiaryContract.Ingredient.TABLE_NAME, ingredientColumns, null, null, ingredientOrderBy};
                new FetchFromDatabase().execute(ingredientArgs);
                break;

            case GridStackStates.STATE_INGREDIENTS:
                buttonStrings = getResources().getStringArray(R.array.analytics_ingredients_buttons);
                mQueryBuilder.setColumns(new String[]{DiaryContract.Ingredient._ID, DiaryContract.Ingredient.COLUMN_NAME});
                mQueryBuilder.ORDER_BY = DiaryContract.Ingredient.COLUMN_NAME + " ASC";
                break;

            case GridStackStates.STATE_FETCH_DATA: //final level //TODO fetch data from database and replace grid.
                resultsState = true;
                showLayout(mLoadingView);
                new FetchFromDatabase().execute(useMemberQuery);
                break;
        }

        updateTextViewQueryString(resultsState);
        return buttonStrings;
    }

    /**
     * Creates and returns a new button to place in the gridview.
     * @param buttonText the new buttons text
     * @return the new button
     */
    private Button getGridButton(String buttonText) {
        AnalyticsButton choiceButton = new AnalyticsButton(getContext());
        choiceButton.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 250));

        Drawable drawable = DrawableCompat.wrap(choiceButton.getBackground());
        DrawableCompat.setTint(drawable, ContextCompat.getColor(getContext(), R.color.colorPrimary));
        choiceButton.setBackground(drawable);

        choiceButton.setText(buttonText);
        choiceButton.setTextColor(ContextCompat.getColor(getContext(), R.color.white));

        findButtonProperties(choiceButton); //Set the linked string to push onto the stack and other button properties
        choiceButton.setOnClickListener(getGridButtonListener()); //Set the buttons listener

        return choiceButton;
    }

    /**
     * Sets analytics properties of this button based on which button was passed in. Provides the bulk of individual
     * button settings so its a big one.
     * @param button the button to set
     */
    private void findButtonProperties(AnalyticsButton button) {
        String linkedState;

        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();

        String buttonTitle = button.getText().toString();
        stringBuilder.append(buttonTitle.toLowerCase());
        stringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.colorAccent)), 0, button.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        //Button for Days
        if (buttonTitle.equals(getString(R.string.analytics_button_days))) {                    //2nd level
            stringBuilder.append(" ").append(getResources().getString(R.string.analytics_query_where_my));
            setButtonProperties(button, GridStackStates.STATE_DAYS, AnalyticsButton.BUTTON_TABLE, DiaryContract.DiaryEntry.TABLE_NAME, stringBuilder, 0, 0);
        }

        //Buttons for Days -> Skin Conditions
        else if (buttonTitle.equals(getString(R.string.analytics_button_overall_condition))) { //3rd level
            stringBuilder.append(" ").append(getResources().getString(R.string.analytics_days_was));
            setButtonProperties(button, GridStackStates.STATE_DAYS_CONDITION, AnalyticsButton.BUTTON_WHERE, DiaryContract.DiaryEntry.COLUMN_OVERALL_CONDITION, stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getString(R.string.analytics_button_forehead_condition))) { //3rd level
            stringBuilder.append(" ").append(getResources().getString(R.string.analytics_days_was));
            setButtonProperties(button, GridStackStates.STATE_DAYS_CONDITION, AnalyticsButton.BUTTON_WHERE, DiaryContract.DiaryEntry.COLUMN_FOREHEAD_CONDITION, stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getString(R.string.analytics_button_nose_condition))) {     //3rd level
            stringBuilder.append(" ").append(getResources().getString(R.string.analytics_days_was));
            setButtonProperties(button, GridStackStates.STATE_DAYS_CONDITION, AnalyticsButton.BUTTON_WHERE, DiaryContract.DiaryEntry.COLUMN_NOSE_CONDITION, stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getString(R.string.analytics_button_cheek_condition))) {   //3rd level
            stringBuilder.append(" ").append(getResources().getString(R.string.analytics_days_was));
            setButtonProperties(button, GridStackStates.STATE_DAYS_CONDITION, AnalyticsButton.BUTTON_WHERE, DiaryContract.DiaryEntry.COLUMN_CHEEK_CONDITION, stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getString(R.string.analytics_button_lips_condition))) {   //3rd level
            stringBuilder.append(" ").append(getResources().getString(R.string.analytics_days_was));
            setButtonProperties(button, GridStackStates.STATE_DAYS_CONDITION, AnalyticsButton.BUTTON_WHERE, DiaryContract.DiaryEntry.COLUMN_LIPS_CONDITION, stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getString(R.string.analytics_button_chin_condition))) {   //3rd level
            stringBuilder.append(" ").append(getResources().getString(R.string.analytics_days_was));
            setButtonProperties(button, GridStackStates.STATE_DAYS_CONDITION, AnalyticsButton.BUTTON_WHERE, DiaryContract.DiaryEntry.COLUMN_CHIN_CONDITION, stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getString(R.string.diary_entry_condition_excellent))) {     //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, Integer.toString(6), stringBuilder, R.color.excellent, R.color.black);
        } else if (buttonTitle.equals(getString(R.string.diary_entry_condition_very_good))) {     //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, Integer.toString(5), stringBuilder, R.color.veryGood, R.color.black);
        } else if (buttonTitle.equals(getString(R.string.diary_entry_condition_good))) {         //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, Integer.toString(4), stringBuilder, R.color.good, R.color.black);
        } else if (buttonTitle.equals(getString(R.string.diary_entry_condition_fair))) {         //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, Integer.toString(3), stringBuilder, R.color.fair, R.color.black);
        } else if (buttonTitle.equals(getString(R.string.diary_entry_condition_poor))) {        //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, Integer.toString(2), stringBuilder, R.color.poor, R.color.black);
        } else if (buttonTitle.equals(getString(R.string.diary_entry_condition_very_poor))) {    //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, Integer.toString(1), stringBuilder, R.color.veryPoor, R.color.black);
        } else if (buttonTitle.equals(getString(R.string.diary_entry_condition_severe))) {      //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, Integer.toString(0), stringBuilder, R.color.severe, R.color.black);
        }

        //Button for Days -> Contains Selected Routines
        else if (buttonTitle.equals(getResources().getString(R.string.analytics_days_button_routines))) {  //3rd level
            stringBuilder.append(":");
            setButtonProperties(button, GridStackStates.STATE_DAYS_ROUTINES, null, null, stringBuilder, 0, 0);
        }

        //Buttons for Days -> Exercise
        else if (buttonTitle.equals(getResources().getString(R.string.analytics_button_exercise_level))) { //3rd level
            stringBuilder.append(" ").append(getResources().getString(R.string.analytics_days_was));
            setButtonProperties(button, GridStackStates.STATE_DAYS_EXERCISE, AnalyticsButton.BUTTON_WHERE, DiaryContract.DiaryEntry.COLUMN_EXERCISE, stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.diary_entry_slider_exercise_intense))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, Integer.toString(4), stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.diary_entry_slider_exercise_moderate))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, Integer.toString(3), stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.diary_entry_slider_exercise_light))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, Integer.toString(2), stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.diary_entry_slider_exercise_none))) { //4th level, shared with "none" from hygiene
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, Integer.toString(1), stringBuilder, 0, 0);
        }

        //Buttons for Days -> Diet
        else if (buttonTitle.equals(getResources().getString(R.string.analytics_button_diet))) { //3rd level
            stringBuilder.append(" ").append(getResources().getString(R.string.analytics_days_was));
            setButtonProperties(button, GridStackStates.STATE_DAYS_DIET, AnalyticsButton.BUTTON_WHERE, DiaryContract.DiaryEntry.COLUMN_DIET, stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.analytics_days_diet_excellent))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, Integer.toString(4), stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.analytics_days_diet_good))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, Integer.toString(3), stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.analytics_days_diet_fair))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, Integer.toString(2), stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.analytics_days_diet_poor))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, Integer.toString(1), stringBuilder, 0, 0);
        }

        //Buttons for Days -> Hygiene
        else if (buttonTitle.equals(getResources().getString(R.string.analytics_button_hygiene))) { //3rd level
            stringBuilder.append(" ").append(getResources().getString(R.string.analytics_days_was));
            setButtonProperties(button, GridStackStates.STATE_DAYS_HYGIENE, AnalyticsButton.BUTTON_WHERE, DiaryContract.DiaryEntry.COLUMN_HYGIENE, stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.diary_entry_slider_hygiene_body_and_hair))) {
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, Integer.toString(4), stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.diary_entry_slider_hygiene_hair_only))) {
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, Integer.toString(3), stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.diary_entry_slider_hygiene_body_only))) { //4th level, final hygiene shares "none" button with exercise
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, Integer.toString(2), stringBuilder, 0, 0);
        }

        //Buttons for Days -> Water Intake
        else if (buttonTitle.equals(getResources().getString(R.string.analytics_button_water_intake))) { //3rd level
            stringBuilder.append(" ").append(getResources().getString(R.string.analytics_days_was));
            setButtonProperties(button, GridStackStates.STATE_DAYS_WATER_INTAKE, AnalyticsButton.BUTTON_WHERE, DiaryContract.DiaryEntry.COLUMN_WATER_INTAKE, stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.diary_entry_slider_water_ten_plus))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, Integer.toString(4), stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.diary_entry_slider_water_seven_nine))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, Integer.toString(3), stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.diary_entry_slider_water_four_six))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, Integer.toString(2), stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.diary_entry_slider_water_one_three))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, Integer.toString(1), stringBuilder, 0, 0);
        }

        //Button for Days -> Period Active
        else if (buttonTitle.equals(getResources().getString(R.string.analytics_button_period_was_active))) { //3rd level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE, DiaryContract.DiaryEntry.COLUMN_ON_PERIOD, AnalyticsButton.BUTTON_WHERE_ARG, "1", stringBuilder, 0, 0);
        }

        //Buttons for Routines
        else if (buttonTitle.equals(getString(R.string.analytics_button_routines))) {        //2nd level
            stringBuilder.append(" ").append(getResources().getString(R.string.analytics_routine_that));
            setButtonProperties(button, GridStackStates.STATE_ROUTINES, AnalyticsButton.BUTTON_TABLE, DiaryContract.Routine.TABLE_NAME, stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.analytics_routine_am))) { //3rd level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE, DiaryContract.Routine.COLUMN_TIME, AnalyticsButton.BUTTON_WHERE_ARG, getResources().getString(R.string.routine_radio_AM), stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.analytics_routine_pm))) { //3rd level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE, DiaryContract.Routine.COLUMN_TIME, AnalyticsButton.BUTTON_WHERE_ARG, mQueryBuilder.WHERE_ARG = getResources().getString(R.string.routine_radio_PM), stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.analytics_routine_as_needed))) { //3rd level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE, DiaryContract.Routine.COLUMN_FREQUENCY, AnalyticsButton.BUTTON_WHERE_ARG, mQueryBuilder.WHERE_ARG = getResources().getString(R.string.routine_radio_frequency_needed), stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.analytics_routine_daily))) { //3rd level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE, DiaryContract.Routine.COLUMN_FREQUENCY, AnalyticsButton.BUTTON_WHERE_ARG, mQueryBuilder.WHERE_ARG = getResources().getString(R.string.routine_radio_frequency_daily), stringBuilder, 0, 0);
        }

        //Button for Routines -> On Specified Day
        else if (buttonTitle.equals(getString(R.string.analytics_routine_on_specified))) {   //3rd level
            setButtonProperties(button, GridStackStates.STATE_ROUTINES_WEEKDAYS, AnalyticsButton.BUTTON_WHERE, DiaryContract.Routine.COLUMN_FREQUENCY + " LIKE ?", stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.analytics_routine_day_sunday))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, "%" + getResources().getString(R.string.routine_frequency_sunday) + "%", stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.analytics_routine_day_monday))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, "%" + getResources().getString(R.string.routine_frequency_monday) + "%", stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.analytics_routine_day_tuesday))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, "%" + getResources().getString(R.string.routine_frequency_tuesday) + "%", stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.analytics_routine_day_wednesday))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, "%" + getResources().getString(R.string.routine_frequency_wednesday) + "%", stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.analytics_routine_day_thursday))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, "%" + getResources().getString(R.string.routine_frequency_thursday) + "%", stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.analytics_routine_day_friday))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, "%" + getResources().getString(R.string.routine_frequency_friday) + "%", stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.analytics_routine_day_saturday))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, "%" + getResources().getString(R.string.routine_frequency_saturday) + "%", stringBuilder, 0, 0);
        }

        //Button for Routines -> selected products
        else if (buttonTitle.equals(getResources().getString(R.string.analytics_routine_contains_product))) { //3rd level
            stringBuilder.append(":");
            setButtonProperties(button, GridStackStates.STATE_ROUTINES_PRODUCTS, null, null, stringBuilder, 0, 0);
        }

        //Button For Products
        else if (buttonTitle.equals(getString(R.string.analytics_button_products))) {        //2nd level
            setButtonProperties(button, GridStackStates.STATE_PRODUCTS, AnalyticsButton.BUTTON_TABLE, DiaryContract.Product.TABLE_NAME, stringBuilder, 0, 0);
        }

        //Buttons for Products -> Of type
        else if (buttonTitle.equals(getResources().getString(R.string.analytics_products_of_type))) {
            setButtonProperties(button, GridStackStates.STATE_PRODUCTS_TYPES, AnalyticsButton.BUTTON_WHERE, DiaryContract.Product.COLUMN_TYPE, stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.product_types_none))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, getResources().getString(R.string.product_types_none), stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.product_types_cleanser))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, getResources().getString(R.string.product_types_cleanser), stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.product_types_exfoliator))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, getResources().getString(R.string.product_types_exfoliator), stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.product_types_eye))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, getResources().getString(R.string.product_types_eye), stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.product_types_lip))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, getResources().getString(R.string.product_types_lip), stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.product_types_mask))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, getResources().getString(R.string.product_types_mask), stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.product_types_moisturizer))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, getResources().getString(R.string.product_types_moisturizer), stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.product_types_other))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, getResources().getString(R.string.product_types_other), stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.product_types_self))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, getResources().getString(R.string.product_types_self), stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.product_types_serum))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, getResources().getString(R.string.product_types_serum), stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.product_types_sun))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, getResources().getString(R.string.product_types_sun), stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.product_types_toner))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, getResources().getString(R.string.product_types_toner), stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.product_types_treatment))) { //4th level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE_ARG, getResources().getString(R.string.product_types_treatment), stringBuilder, 0, 0);
        }

        //Button for Product -> with ingredients
        else if (buttonTitle.equals(getResources().getString(R.string.analytics_products_with_ingredients))) {
            stringBuilder.append(":");
            setButtonProperties(button, GridStackStates.STATE_PRODUCTS_INGREDIENTS, null, null, stringBuilder, 0, 0);
        }

        //Button For Ingredients
        else if (buttonTitle.equals(getString(R.string.analytics_button_ingredients))) {     //2nd level
            stringBuilder.append(" ").append(getResources().getString(R.string.analytics_ingredients_that));
            setButtonProperties(button, GridStackStates.STATE_INGREDIENTS, AnalyticsButton.BUTTON_TABLE, DiaryContract.Ingredient.TABLE_NAME, stringBuilder, 0, 0);
        } else if (buttonTitle.equals(getResources().getString(R.string.analytics_ingredients_are_irritants))) { //3rd level
            setButtonProperties(button, GridStackStates.STATE_FETCH_DATA, AnalyticsButton.BUTTON_WHERE, DiaryContract.Ingredient.COLUMN_IRRITANT, AnalyticsButton.BUTTON_WHERE_ARG, Integer.toString(1), stringBuilder, 0, 0);
        }

        //Return to main if unexpected button pressed
        else {
            button.setLinkedStackString(GridStackStates.STATE_MAIN);
        }

    }

    /**
     * Sets parameters and attributes of the passed in button.
     * @param button          the button to modify
     * @param linkedState     the linked state of this button
     * @param buttonType      the button type of this button
     * @param buttonQueryData the query data of this button
     * @param buttonColor     the color to set the buttons background to
     * @param buttonTextColor the color to set the buttons text color to
     */
    private void setButtonProperties(AnalyticsButton button, String linkedState, String buttonType, String buttonQueryData, Spannable queryStackString, int buttonColor, int buttonTextColor) {
        if (linkedState != null) {
            button.setLinkedStackString(linkedState);
        }

        if (buttonType != null) {
            button.setButtonType(buttonType);
        }

        if (queryStackString != null) {
            button.setQueryStackString(queryStackString);
        }

        if (buttonQueryData != null) {
            button.setQueryData(buttonQueryData);
        }

        if (buttonColor != 0) {
            Drawable drawable = DrawableCompat.wrap(button.getBackground());
            DrawableCompat.setTint(drawable, ContextCompat.getColor(getContext(), buttonColor));
            button.setBackground(drawable);
        }

        if (buttonTextColor != 0) {
            button.setTextColor(ContextCompat.getColor(getContext(), buttonTextColor));
        }
    }

    /**
     * Sets parameters and attributes of the passed in button.
     * @param button          the button to modify
     * @param linkedState     the linked state of this button
     * @param buttonType      the button type of this button
     * @param buttonQueryData the query data of this button
     * @param secondaryType   the secondary type if needed
     * @param secondaryData   the secondary data if needed
     * @param buttonColor     the color to set the buttons background to
     * @param buttonTextColor the color to set the buttons text color to
     */
    private void setButtonProperties(AnalyticsButton button, String linkedState, String buttonType, String buttonQueryData, String secondaryType, String secondaryData, Spannable queryStackString, int buttonColor, int buttonTextColor) {
        setButtonProperties(button, linkedState, buttonType, buttonQueryData, queryStackString, buttonColor, buttonTextColor);

        if (secondaryType != null) {
            button.setSecondaryButtonType(secondaryType);
        }

        if (secondaryData != null) {
            button.setSecondaryQueryData(secondaryData);
        }
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

                switch (thisButton.getButtonType()) {
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

                //Fetch data from secondary button type if used
                switch (thisButton.getSecondaryButtonType()) {
                    case AnalyticsButton.BUTTON_TABLE:
                        mQueryBuilder.TABLE = thisButton.getSecondaryQueryData();
                        break;
                    case AnalyticsButton.BUTTON_WHERE:
                        mQueryBuilder.WHERE = thisButton.getSecondaryQueryData();
                        break;
                    case AnalyticsButton.BUTTON_WHERE_ARG:
                        mQueryBuilder.WHERE_ARG = thisButton.getSecondaryQueryData();
                        break;
                    default: //No button type assigned
                        break;
                }

                mQueryStringStack.push(thisButton.getQueryStackString()); //Push query string onto string stack
                mStateStack.push(thisButton.getLinkedStackString()); //Push the new state onto the stack
                updateGridAndQuery(); //Update the grid and query based on new stack
            }
        };
        return buttonListener;
    }

    /**
     * Uses the database query object to build a query to perform on the database. Populates the listview with this data.
     */
    private class FetchFromDatabase extends AsyncTask<Object, Void, Cursor> {

        String queryTable;

        @Override
        protected Cursor doInBackground(Object... params) {
            SQLiteDatabase db = DiaryDbHelper.getInstance(getContext()).getReadableDatabase();

            boolean useMemberQuery = true;
            if (params[0] != null) {
                useMemberQuery = (boolean) params[0];
            }

            String table, where, orderBy;
            String[] columns, whereArg;

            if (useMemberQuery) {
                table = mQueryBuilder.TABLE;
                columns = mQueryBuilder.COLUMNS;
                where = mQueryBuilder.WHERE;
                if (!(where.substring(where.length() - 1).equals("?"))) { //If the passed in query does not end in a where arg flag (?), add one
                    where = where + " = ?";
                }
                whereArg = new String[]{mQueryBuilder.WHERE_ARG};
                orderBy = mQueryBuilder.ORDER_BY;
            } else {
                table = (String) params[1];
                columns = (String[]) params[2];
                where = (String) params[3];
                whereArg = (String[]) params[4];
                orderBy = (String) params[5];
            }

            queryTable = table;
            //If using linked tables set table to join on linked tables before performing query.
            //the where variable will be null if attempting to load all records from a table
            if (where != null && table.equals(DiaryContract.DiaryEntry.TABLE_NAME) && where.equals(DiaryContract.Routine._ID + " = ?")) { //Searching days for routines
                columns = new String[]{DiaryContract.DiaryEntry._ID, DiaryContract.DiaryEntry.COLUMN_DATE};
                table = DiaryContract.DiaryEntry.TABLE_NAME + " JOIN " + DiaryContract.DiaryEntryRoutine.TABLE_NAME + " ON " +
                        DiaryContract.DiaryEntry._ID + " = " + DiaryContract.DiaryEntryRoutine.COLUMN_DIARY_ENTRY_ID;
                where = DiaryContract.DiaryEntryRoutine.COLUMN_ROUTINE_ID + " = ?";
            } else if (where != null && table.equals(DiaryContract.Routine.TABLE_NAME) && where.equals(DiaryContract.Product._ID + " = ?")) { //Searching routines for products
                columns = new String[]{DiaryContract.Routine._ID, DiaryContract.Routine.COLUMN_NAME};
                table = DiaryContract.Routine.TABLE_NAME + " JOIN " + DiaryContract.RoutineProduct.TABLE_NAME + " ON " +
                        DiaryContract.Routine._ID + " = " + DiaryContract.RoutineProduct.COLUMN_ROUTINE_ID;
                where = DiaryContract.RoutineProduct.COLUMN_PRODUCT_ID + " = ?";
            } else if (where != null && table.equals(DiaryContract.Product.TABLE_NAME) && where.equals(DiaryContract.Ingredient._ID + " = ?")) { //Searching products for ingredients
                columns = new String[]{DiaryContract.Product._ID, DiaryContract.Product.COLUMN_NAME};
                table = DiaryContract.Product.TABLE_NAME + " JOIN " + DiaryContract.ProductIngredient.TABLE_NAME + " ON " +
                        DiaryContract.Product._ID + " = " + DiaryContract.ProductIngredient.COLUMN_PRODUCT_ID;
                where = DiaryContract.ProductIngredient.COLUMN_INGREDIENT_ID + " = ?";
            }

            return db.query(table, columns, where, whereArg, null, null, orderBy);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {

            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    mResultsListView.setAdapter(null);
                    if (mStateStack.peek().equals(GridStackStates.STATE_FETCH_DATA)) { //Reached final state, query builder used
                        switch (mQueryBuilder.TABLE) {
                            case DiaryContract.DiaryEntry.TABLE_NAME: //If populating list of diary entries
                                populateDiaryEntryList(cursor);
                                break;
                            case DiaryContract.Routine.TABLE_NAME:
                                populateRoutineList(cursor, true);
                                break;
                            case DiaryContract.Product.TABLE_NAME:
                                populateProductList(cursor, true);
                                break;
                            case DiaryContract.Ingredient.TABLE_NAME:
                                populateIngredientList(cursor, true);
                                break;
                        }
                    } else { //Did not reach final state, intermittent load.
                        switch (queryTable) {
                            case DiaryContract.Routine.TABLE_NAME:
                                populateRoutineList(cursor, false);
                                break;
                            case DiaryContract.Product.TABLE_NAME:
                                populateProductList(cursor, false);
                                break;
                            case DiaryContract.Ingredient.TABLE_NAME:
                                populateIngredientList(cursor, false);
                                break;
                        }
                    }
                    showLayout(mResultsListView);
                } else {
                    showLayout(mResultsEmptyText);
                }
            }
        }

        /**
         * Called when the returned cursor contains diary entry rows.
         * @param cursor
         */
        private void populateDiaryEntryList(final Cursor cursor) {
            final HashMap<Long, Long> idDateMap = new HashMap<>();

            String[] fromColumns = {DiaryContract.DiaryEntry.COLUMN_DATE};
            int[] toViews = {R.id.analytics_list_view_diary_entry_date};
            SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(getContext(), R.layout.listview_item_analytics_diary_entry, cursor, fromColumns, toViews, 0);
            cursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                    if (columnIndex == cursor.getColumnIndex(DiaryContract.DiaryEntry.COLUMN_DATE)) {
                        TextView dateView = (TextView) view;
                        long dateID = cursor.getLong(cursor.getColumnIndex(DiaryContract.DiaryEntry._ID));
                        long epochTime = cursor.getLong(cursor.getColumnIndex(DiaryContract.DiaryEntry.COLUMN_DATE));
                        idDateMap.put(dateID, epochTime);
                        Date date = new Date(epochTime);
                        DateFormat df = DateFormat.getDateInstance();
                        dateView.setText(df.format(date));
                        return true;
                    }
                    return false;
                }
            });
            mResultsListView.setAdapter(cursorAdapter);
            mResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent diaryEntryIntent = new Intent(getContext(), DiaryEntryActivityMain.class);
                    long epochTime = idDateMap.get(id);
                    diaryEntryIntent.putExtra(DiaryEntryActivityMain.DATE_EXTRA, epochTime);
                    startActivity(diaryEntryIntent);
                }
            });
        }

        /**
         * Called when returned cursor contains routine rows and is an intermittent load.
         * @param cursor      query result cursor
         * @param finalResult true if the cursor contains final results, false if these are intermediate
         */
        private void populateRoutineList(final Cursor cursor, boolean finalResult) {
            String[] fromColumns = {DiaryContract.Routine.COLUMN_NAME};
            int[] toViews = {R.id.routine_listview_name};
            SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(getContext(), R.layout.listview_item_routine_main, cursor, fromColumns, toViews, 0);
            cursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                    if (columnIndex == cursor.getColumnIndex(DiaryContract.Routine.COLUMN_NAME)) {
                        TextView routineView = (TextView) view;
                        routineView.setText(cursor.getString(cursor.getColumnIndex(DiaryContract.Routine.COLUMN_NAME)));
                        return true;
                    }
                    return false;
                }
            });
            mResultsListView.setAdapter(cursorAdapter);
            //Set listener based on whether this is a final result or an intermediate result
            if (finalResult) {
                mResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(getContext(), RoutineActivityDetail.class);
                        intent.putExtra(RoutineActivityDetail.NEW_ROUTINE, false);
                        intent.putExtra(RoutineActivityDetail.ENTRY_ID, id);
                        startActivity(intent);
                    }
                });
            } else {
                mResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        TextView textView = (TextView) view.findViewById(R.id.routine_listview_name);
                        Spannable spannable = new SpannableString(textView.getText().toString().toLowerCase());
                        spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.colorAccent)), 0, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        mQueryStringStack.push(spannable);
                        mQueryBuilder.WHERE = DiaryContract.Routine._ID;
                        mQueryBuilder.WHERE_ARG = Long.toString(id);
                        mStateStack.push(GridStackStates.STATE_FETCH_DATA);
                        updateGridAndQuery();
                    }
                });
            }
        }

        /**
         * Populates the listview with products
         * @param cursor      the returned cursor
         * @param finalResult true if this was a final result, false if it was intermediate
         */
        private void populateProductList(Cursor cursor, boolean finalResult) {
            String[] fromColumns = {DiaryContract.Product.COLUMN_NAME};
            int[] toViews = {R.id.product_listview_name};
            SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(getContext(), R.layout.listview_item_product_main, cursor, fromColumns, toViews, 0);
            cursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                    if (columnIndex == cursor.getColumnIndex(DiaryContract.Product.COLUMN_NAME)) {
                        TextView nameView = (TextView) view.findViewById(R.id.product_listview_name);
                        nameView.setText(cursor.getString(cursor.getColumnIndex(DiaryContract.Product.COLUMN_NAME)));
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            mResultsListView.setAdapter(cursorAdapter);
            if (finalResult) {
                mResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(getContext(), IngredientActivityDetail.class);
                        intent.putExtra(IngredientActivityDetail.NEW_INGREDIENT, false);
                        intent.putExtra(IngredientActivityDetail.ENTRY_ID, id);
                        startActivity(intent);
                    }
                });
            } else {
                mResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        TextView textView = (TextView) view.findViewById(R.id.product_listview_name);
                        Spannable spannable = new SpannableString(textView.getText().toString().toLowerCase());
                        spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.colorAccent)), 0, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        mQueryStringStack.push(spannable);
                        mQueryBuilder.WHERE = DiaryContract.Product._ID;
                        mQueryBuilder.WHERE_ARG = Long.toString(id);
                        mStateStack.push(GridStackStates.STATE_FETCH_DATA);
                        updateGridAndQuery();
                    }
                });
            }
        }

        /**
         * Populates the listview with ingredients.
         * @param cursor      the returned cursor
         * @param finalResult true if this was a final result, false if it was intermediate
         */
        private void populateIngredientList(Cursor cursor, boolean finalResult) {
            String[] fromColumns = new String[]{DiaryContract.Ingredient.COLUMN_NAME};
            final int[] toViews = {R.id.ingredient_list_view_item};
            SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(getContext(), R.layout.listview_item_ingredient_main, cursor, fromColumns, toViews, 0);
            cursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                    if (columnIndex == cursor.getColumnIndex(DiaryContract.Ingredient.COLUMN_NAME)) {
                        TextView nameView = (TextView) view;
                        nameView.setText(cursor.getString(cursor.getColumnIndex(DiaryContract.Ingredient.COLUMN_NAME)));
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            mResultsListView.setAdapter(cursorAdapter);
            if (finalResult) {
                mResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(getContext(), IngredientActivityDetail.class);
                        intent.putExtra(IngredientActivityDetail.NEW_INGREDIENT, false);
                        intent.putExtra(IngredientActivityDetail.ENTRY_ID, id);
                        startActivity(intent);
                    }
                });
            } else {
                mResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        TextView textView = (TextView) view;
                        Spannable spannable = new SpannableString(textView.getText().toString().toLowerCase());
                        spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.colorAccent)), 0, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        mQueryStringStack.push(spannable);
                        mQueryBuilder.WHERE = DiaryContract.Ingredient._ID;
                        mQueryBuilder.WHERE_ARG = Long.toString(id);
                        mStateStack.push(GridStackStates.STATE_FETCH_DATA);
                        updateGridAndQuery();
                    }
                });
            }
        }
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
            return mGridViewButtons.get(position);
        }
    }

    /**
     * Helper class that contains the possible states that can be pushed onto the state stack.
     */
    private static class GridStackStates {
        public static final String STATE_MAIN = "STATE_MAIN";

        public static final String STATE_DAYS = "STATE_DAYS";
        public static final String STATE_DAYS_CONDITION = "STATE_DAYS_CONDITION";
        public static final String STATE_DAYS_EXERCISE = "STATE_DAYS_EXERCISE";
        public static final String STATE_DAYS_DIET = "STATE_DAYS_DIET";
        public static final String STATE_DAYS_HYGIENE = "STATE_DAYS_HYGIENE";
        public static final String STATE_DAYS_WATER_INTAKE = "STATE_DAYS_WATER_INTAKE";
        public static final String STATE_DAYS_ROUTINES = "STATE_DAYS_ROUTINES";

        public static final String STATE_ROUTINES = "STATE_ROUTINES";
        public static final String STATE_ROUTINES_WEEKDAYS = "STATE_ROUTINES_WEEKDAYS";
        public static final String STATE_ROUTINES_PRODUCTS = "STATE_ROUTINES_PRODUCTS";

        public static final String STATE_PRODUCTS = "STATE_PRODUCTS";
        public static final String STATE_PRODUCTS_TYPES = "STATE_PRODUCT_TYPES";
        public static final String STATE_PRODUCTS_INGREDIENTS = "STATE_PRODUCTS_INGREDIENTS";

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
        private Spannable mQueryStackString;
        private String mQueryData;
        private String mButtonType;
        private String mSecondaryButtonType;
        private String mSecondaryQueryData;

        public AnalyticsButton(Context context) {
            super(context);
            mButtonType = "";
            mSecondaryButtonType = "";
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

        public void setSecondaryButtonType(String type) {
            mSecondaryButtonType = type;
        }

        public String getSecondaryButtonType() {
            return mSecondaryButtonType;
        }

        public void setSecondaryQueryData(String data) {
            mSecondaryQueryData = data;
        }

        public String getSecondaryQueryData() {
            return mSecondaryQueryData;
        }

        public void setQueryStackString(Spannable query) {
            mQueryStackString = query;
        }

        public Spannable getQueryStackString() {
            return mQueryStackString;
        }

    }

}
