package com.smithkeegan.mydailyskincare.diaryEntry;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.smithkeegan.mydailyskincare.CalendarActivityMain;
import com.smithkeegan.mydailyskincare.R;
import com.smithkeegan.mydailyskincare.customClasses.DiaryEntryFieldCollection;
import com.smithkeegan.mydailyskincare.customClasses.DiaryEntrySeekBar;
import com.smithkeegan.mydailyskincare.customClasses.ItemListDialogFragment;
import com.smithkeegan.mydailyskincare.data.DiaryContract;
import com.smithkeegan.mydailyskincare.data.DiaryDbHelper;
import com.smithkeegan.mydailyskincare.routine.RoutineActivityDetail;

import java.util.Calendar;
import java.util.Date;

/**
 * Fragment class representing a Diary Entry corresponding to a date. Contains fields and views for
 * each slider or control within the Diary Entry.
 * @author Keegan Smith
 * @since 8/26/2016
 */
public class DiaryEntryFragmentMain extends Fragment {

    private DiaryDbHelper mDbHelper;

    private View mLoadingView;
    private View mEntryDetailView;

    private RelativeLayout mShowMoreLayout;
    private RelativeLayout mAdditionalConditionsLayout;
    private RelativeLayout mRoutinesLayout;
    private RelativeLayout mOnPeriodLayout;

    private TextView mTextViewOverallCondition;
    private TextView mTextViewForeheadCondition;
    private TextView mTextViewNoseCondition;
    private TextView mTextViewCheeksCondition;
    private TextView mTextViewLipsCondition;
    private TextView mTextViewChinCondition;
    private TextView mTextViewExercise;
    private TextView mTextViewDiet;
    private TextView mTextViewHygiene;
    private TextView mTextViewWaterIntake;

    private DiaryEntrySeekBar mSeekBarOverallCondition;
    private DiaryEntrySeekBar mSeekBarForeheadCondition;
    private DiaryEntrySeekBar mSeekBarNoseCondition;
    private DiaryEntrySeekBar mSeekBarCheeksCondition;
    private DiaryEntrySeekBar mSeekBarLipsCondition;
    private DiaryEntrySeekBar mSeekBarChinCondition;
    private DiaryEntrySeekBar mSeekBarExercise;
    private DiaryEntrySeekBar mSeekBarDiet;
    private DiaryEntrySeekBar mSeekBarHygiene;
    private DiaryEntrySeekBar mSeekBarWaterIntake;

    private ListView mRoutinesListView;
    private TextView mNoRoutinesTextView;
    private Button mAddRemoveRoutinesButton;
    private CheckBox mOnPeriodCheckBox;

    private DiaryEntryFieldCollection mInitialFieldValues;
    private DiaryEntryFieldCollection mCurrentFieldValues;

    private long mDiaryEntryID;
    private long mEpochTime; //Number of milliseconds since January 1, 1970 00:00:00.00. Value stored in database for this date.
    private boolean mNewEntry;
    private boolean mAdditionalConditionsShown;
    private boolean mInitialLoadFinished;

    private String[] mConditionStrings;
    private int[] mConditionColorIds;

    private String[] mExerciseStrings;
    private String[] mDietStrings;
    private String[] mHygieneStrings;
    private String[] mWaterIntakeStrings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDbHelper = DiaryDbHelper.getInstance(getContext());

        Bundle bundle = getArguments();
        Date date = new Date(bundle.getLong(DiaryEntryActivityMain.DATE_EXTRA));
        mEpochTime = date.getTime(); //Set time long
        setSliderArrays();

        View rootView = inflater.inflate(R.layout.fragment_diary_entry_main, container, false);

        setMemberViews(rootView);
        if (savedInstanceState == null) {
            showLoadingScreen();
            setDefaultMemberValues();
            new LoadDiaryEntryTask().execute(mEpochTime);
        } else {
            restoreSavedInstance(savedInstanceState);
        }

        setListeners();

        //Show demo if this is the first launch of this fragment
        if (!PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(getResources().getString(R.string.preference_diary_entry_demo_seen),false)){
            showDemo();
        }

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_item_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_save:
                saveCurrentDiaryEntry();
                return true;
            case R.id.menu_action_delete:
                deleteCurrentDiaryEntry(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Refresh routines list on activity resume.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (mInitialLoadFinished) {
            refreshRoutinesList();
        }
    }

    /**
     * Saves the current fields when the activity is destroyed by a system process to be restored
     * later.
     * @param outState bundle that will contain this fragments current fields.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(DiaryEntryState.DIARY_ENTRY_ID, mDiaryEntryID);
        outState.putLong(DiaryEntryState.DIARY_ENTRY_TIME, mEpochTime);
        outState.putBoolean(DiaryEntryState.NEW_DIARY_ENTRY, mNewEntry);
        outState.putBoolean(DiaryEntryState.ADDITIONAL_SHOWN, mAdditionalConditionsShown);
        outState.putParcelable(DiaryEntryState.INITIAL_FIELD_VALUES, mInitialFieldValues);
        outState.putParcelable(DiaryEntryState.CURRENT_FIELD_VALUES, mCurrentFieldValues);

        super.onSaveInstanceState(outState);
    }

    /**
     * Restores fields of this fragment from the restored instance state.
     * @param savedInstance the restored state
     */
    public void restoreSavedInstance(Bundle savedInstance) {
        mDiaryEntryID = savedInstance.getLong(DiaryEntryState.DIARY_ENTRY_ID);
        mEpochTime = savedInstance.getLong(DiaryEntryState.DIARY_ENTRY_TIME);
        mNewEntry = savedInstance.getBoolean(DiaryEntryState.NEW_DIARY_ENTRY);

        //toggleAdditionalConditions shows or hides the additional sliders based on the member boolean value
        //Set it to the inverse of the desiered setting before calling toggleAdditionalConditions.
        mAdditionalConditionsShown = !savedInstance.getBoolean(DiaryEntryState.ADDITIONAL_SHOWN);
        toggleAdditionalConditions();

        mInitialFieldValues = savedInstance.getParcelable(DiaryEntryState.INITIAL_FIELD_VALUES);
        mCurrentFieldValues = savedInstance.getParcelable(DiaryEntryState.CURRENT_FIELD_VALUES);

        updateFieldViews(mCurrentFieldValues);

        mInitialLoadFinished = true;
    }

    /**
     * Shows the welcome demo on the first launch.
     */
    private void showDemo(){
        final Dialog dialog = new Dialog(getContext(),android.R.style.Theme_Translucent_NoTitleBar);
        dialog.setContentView(R.layout.demo_layout);

        //Set preference value of main demo seen.
        final TextView dialogText = (TextView) dialog.findViewById(R.id.demo_layout_text_view);
        final Button dialogButton = (Button) dialog.findViewById(R.id.demo_layout_button_next_done);
        dialogButton.setTag(1); //Use the view's tag to track the current displayed text phase
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPhase = (int) dialogButton.getTag();
                switch (currentPhase){
                    case 1:
                        dialogButton.setTag(2);
                        dialogText.setText(getResources().getString(R.string.diary_entry_demo_text_second));
                        break;
                    case 2:
                        dialogButton.setTag(3);
                        dialogText.setText(getResources().getString(R.string.diary_entry_demo_text_third));
                        dialogButton.setText(getResources().getString(R.string.done_string));
                        break;
                    case 3:
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(getResources().getString(R.string.preference_diary_entry_demo_seen),true).apply();
                        dialog.dismiss();
                        break;
                }

            }
        });
        dialogText.setText(getResources().getString(R.string.diary_entry_demo_text_first));
        dialog.show();
    }

    /*
        Fetches properties to be used with the condition label's from xml resources.
        Could use string-arrays instead of loading manually here.
         */
    private void setSliderArrays() {
        mConditionStrings = new String[7];
        mConditionStrings[0] = getResources().getString(R.string.diary_entry_condition_severe);
        mConditionStrings[1] = getResources().getString(R.string.diary_entry_condition_very_poor);
        mConditionStrings[2] = getResources().getString(R.string.diary_entry_condition_poor);
        mConditionStrings[3] = getResources().getString(R.string.diary_entry_condition_fair);
        mConditionStrings[4] = getResources().getString(R.string.diary_entry_condition_good);
        mConditionStrings[5] = getResources().getString(R.string.diary_entry_condition_very_good);
        mConditionStrings[6] = getResources().getString(R.string.diary_entry_condition_excellent);

        mConditionColorIds = new int[7];
        mConditionColorIds[0] = ContextCompat.getColor(getContext(), R.color.severe);
        mConditionColorIds[1] = ContextCompat.getColor(getContext(), R.color.veryPoor);
        mConditionColorIds[2] = ContextCompat.getColor(getContext(), R.color.poor);
        mConditionColorIds[3] = ContextCompat.getColor(getContext(), R.color.fair);
        mConditionColorIds[4] = ContextCompat.getColor(getContext(), R.color.good);
        mConditionColorIds[5] = ContextCompat.getColor(getContext(), R.color.veryGood);
        mConditionColorIds[6] = ContextCompat.getColor(getContext(), R.color.excellent);

        mExerciseStrings = new String[5];
        mExerciseStrings[0] = getResources().getString(R.string.diary_entry_slider_not_specified);
        mExerciseStrings[1] = getResources().getString(R.string.diary_entry_slider_exercise_none);
        mExerciseStrings[2] = getResources().getString(R.string.diary_entry_slider_exercise_light);
        mExerciseStrings[3] = getResources().getString(R.string.diary_entry_slider_exercise_moderate);
        mExerciseStrings[4] = getResources().getString(R.string.diary_entry_slider_exercise_intense);

        mDietStrings = new String[5];
        mDietStrings[0] = getResources().getString(R.string.diary_entry_slider_not_specified);
        mDietStrings[1] = getResources().getString(R.string.diary_entry_condition_poor);
        mDietStrings[2] = getResources().getString(R.string.diary_entry_condition_fair);
        mDietStrings[3] = getResources().getString(R.string.diary_entry_condition_good);
        mDietStrings[4] = getResources().getString(R.string.diary_entry_condition_excellent);

        mHygieneStrings = new String[5];
        mHygieneStrings[0] = getResources().getString(R.string.diary_entry_slider_not_specified);
        mHygieneStrings[1] = getResources().getString(R.string.diary_entry_slider_exercise_none);
        mHygieneStrings[2] = getResources().getString(R.string.diary_entry_slider_hygiene_body_only);
        mHygieneStrings[3] = getResources().getString(R.string.diary_entry_slider_hygiene_hair_only);
        mHygieneStrings[4] = getResources().getString(R.string.diary_entry_slider_hygiene_body_and_hair);

        mWaterIntakeStrings = new String[5];
        mWaterIntakeStrings[0] = getResources().getString(R.string.diary_entry_slider_not_specified);
        //mWaterIntakeStrings[1] = getResources().getString(R.string.diary_entry_slider_exercise_none);
        mWaterIntakeStrings[1] = getResources().getString(R.string.diary_entry_slider_water_one_three);
        mWaterIntakeStrings[2] = getResources().getString(R.string.diary_entry_slider_water_four_six);
        mWaterIntakeStrings[3] = getResources().getString(R.string.diary_entry_slider_water_seven_nine);
        mWaterIntakeStrings[4] = getResources().getString(R.string.diary_entry_slider_water_ten_plus);
    }

    /**
     * Retrieves the views for this fragment.
     * @param rootView the rootview of this fragment
     */
    private void setMemberViews(View rootView) {
        mLoadingView = rootView.findViewById(R.id.diary_entry_loading_layout);
        mEntryDetailView = rootView.findViewById(R.id.diary_entry_detail_layout);

        mShowMoreLayout = (RelativeLayout) rootView.findViewById(R.id.diary_entry_show_more_layout);
        mAdditionalConditionsLayout = (RelativeLayout) rootView.findViewById(R.id.diary_entry_additional_conditions_layout);
        mRoutinesLayout = (RelativeLayout) rootView.findViewById(R.id.diary_entry_routines_layout);
        mOnPeriodLayout = (RelativeLayout) rootView.findViewById(R.id.diary_entry_lifestyle_period_layout);

        mTextViewOverallCondition = (TextView) rootView.findViewById(R.id.diary_entry_general_condition_text);
        mTextViewForeheadCondition = (TextView) rootView.findViewById(R.id.diary_entry_forehead_condition_text);
        mTextViewNoseCondition = (TextView) rootView.findViewById(R.id.diary_entry_nose_condition_text);
        mTextViewCheeksCondition = (TextView) rootView.findViewById(R.id.diary_entry_cheeks_condition_text);
        mTextViewLipsCondition = (TextView) rootView.findViewById(R.id.diary_entry_lips_condition_text);
        mTextViewChinCondition = (TextView) rootView.findViewById(R.id.diary_entry_chin_condition_text);
        mTextViewExercise = (TextView) rootView.findViewById(R.id.diary_entry_lifestyle_exercise_text);
        mTextViewDiet = (TextView) rootView.findViewById(R.id.diary_entry_lifestyle_diet_text);
        mTextViewHygiene = (TextView) rootView.findViewById(R.id.diary_entry_lifestyle_hygiene_text);
        mTextViewWaterIntake = (TextView) rootView.findViewById(R.id.diary_entry_lifestyle_water_text);

        mSeekBarOverallCondition = (DiaryEntrySeekBar) rootView.findViewById(R.id.diary_entry_general_condition_seek_bar);
        mSeekBarForeheadCondition = (DiaryEntrySeekBar) rootView.findViewById(R.id.diary_entry_forehead_condition_seek_bar);
        mSeekBarNoseCondition = (DiaryEntrySeekBar) rootView.findViewById(R.id.diary_entry_nose_condition_seek_bar);
        mSeekBarCheeksCondition = (DiaryEntrySeekBar) rootView.findViewById(R.id.diary_entry_cheeks_condition_seek_bar);
        mSeekBarLipsCondition = (DiaryEntrySeekBar) rootView.findViewById(R.id.diary_entry_lips_condition_seek_bar);
        mSeekBarChinCondition = (DiaryEntrySeekBar) rootView.findViewById(R.id.diary_entry_chin_condition_seek_bar);

        mSeekBarExercise = (DiaryEntrySeekBar) rootView.findViewById(R.id.diary_entry_lifestyle_exercise_seek_bar);
        mSeekBarDiet = (DiaryEntrySeekBar) rootView.findViewById(R.id.diary_entry_lifestyle_diet_seek_bar);
        mSeekBarHygiene = (DiaryEntrySeekBar) rootView.findViewById(R.id.diary_entry_lifestyle_hygiene_seek_bar);
        mSeekBarWaterIntake = (DiaryEntrySeekBar) rootView.findViewById(R.id.diary_entry_lifestyle_water_seek_bar);

        mRoutinesListView = (ListView) rootView.findViewById(R.id.diary_entry_routines_listview);
        mNoRoutinesTextView = (TextView) rootView.findViewById(R.id.diary_entry_no_routines_text);
        mAddRemoveRoutinesButton = (Button) rootView.findViewById(R.id.diary_entry_add_remove_routine_button);
        mOnPeriodCheckBox = (CheckBox) rootView.findViewById(R.id.diary_entry_lifestyle_period_check);
    }

    /**
     * Set the default values of fields of this diary entry.
     */
    private void setDefaultMemberValues() {
        //Set the default value of sliders, will be changed on load from database.
        updateSliderLabel(mTextViewOverallCondition, 3, mConditionStrings);
        updateSliderLabel(mTextViewForeheadCondition, 3, mConditionStrings);
        updateSliderLabel(mTextViewNoseCondition, 3, mConditionStrings);
        updateSliderLabel(mTextViewCheeksCondition, 3, mConditionStrings);
        updateSliderLabel(mTextViewLipsCondition, 3, mConditionStrings);
        updateSliderLabel(mTextViewChinCondition, 3, mConditionStrings);

        updateSliderLabel(mTextViewExercise, 0, mExerciseStrings);
        updateSliderLabel(mTextViewDiet, 0, mDietStrings);
        updateSliderLabel(mTextViewHygiene, 0, mHygieneStrings);
        updateSliderLabel(mTextViewWaterIntake, 0, mWaterIntakeStrings);

        mSeekBarOverallCondition.setDefaultStep();
        mSeekBarForeheadCondition.setDefaultStep();
        mSeekBarNoseCondition.setDefaultStep();
        mSeekBarCheeksCondition.setDefaultStep();
        mSeekBarLipsCondition.setDefaultStep();
        mSeekBarChinCondition.setDefaultStep();

        //Set the number of steps for lifestyle sliders
        mSeekBarExercise.setNumSteps(mExerciseStrings.length);
        mSeekBarDiet.setNumSteps(mDietStrings.length);
        mSeekBarHygiene.setNumSteps(mHygieneStrings.length);
        mSeekBarWaterIntake.setNumSteps(mWaterIntakeStrings.length);

        //Set default value of lifestyle sliders
        mSeekBarExercise.setProgressToStep(0);
        mSeekBarDiet.setProgressToStep(0);
        mSeekBarHygiene.setProgressToStep(0);
        mSeekBarWaterIntake.setProgressToStep(0);

        mAdditionalConditionsShown = false;
        mInitialLoadFinished = false;

        mInitialFieldValues = new DiaryEntryFieldCollection();
        mCurrentFieldValues = new DiaryEntryFieldCollection();
        mNewEntry = false;
    }

    private void showLayout(View view){
        mRoutinesListView.setVisibility(View.INVISIBLE);
        mNoRoutinesTextView.setVisibility(View.INVISIBLE);

        view.setVisibility(View.VISIBLE);
    }

    /*
    Sets the listeners of views in this fragment.
     */
    private void setListeners() {
        mSeekBarOverallCondition.setOnSeekBarChangeListener(new DiaryEntrySeekBarChangeListener());
        mSeekBarForeheadCondition.setOnSeekBarChangeListener(new DiaryEntrySeekBarChangeListener());
        mSeekBarNoseCondition.setOnSeekBarChangeListener(new DiaryEntrySeekBarChangeListener());
        mSeekBarCheeksCondition.setOnSeekBarChangeListener(new DiaryEntrySeekBarChangeListener());
        mSeekBarLipsCondition.setOnSeekBarChangeListener(new DiaryEntrySeekBarChangeListener());
        mSeekBarChinCondition.setOnSeekBarChangeListener(new DiaryEntrySeekBarChangeListener());

        mSeekBarExercise.setOnSeekBarChangeListener(new DiaryEntrySeekBarChangeListener());
        mSeekBarDiet.setOnSeekBarChangeListener(new DiaryEntrySeekBarChangeListener());
        mSeekBarHygiene.setOnSeekBarChangeListener(new DiaryEntrySeekBarChangeListener());
        mSeekBarWaterIntake.setOnSeekBarChangeListener(new DiaryEntrySeekBarChangeListener());

        mShowMoreLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAdditionalConditions();
            }
        });

        mShowMoreLayout.findViewById(R.id.diary_entry_show_more_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAdditionalConditions();
            }
        });

        mOnPeriodLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnPeriodCheckBox.setChecked(!mOnPeriodCheckBox.isChecked());
                toggleOnPeriodCheckbox();
            }
        });

        mOnPeriodCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleOnPeriodCheckbox();
            }
        });

        mAddRemoveRoutinesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString(ItemListDialogFragment.DISPLAYED_DATA, ItemListDialogFragment.ROUTINES);
                args.putLong(ItemListDialogFragment.ITEM_ID, mDiaryEntryID);
                DialogFragment fragment = new ItemListDialogFragment();
                fragment.setArguments(args);
                fragment.show(getFragmentManager(), "dialog");
            }
        });

        mRoutinesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), RoutineActivityDetail.class);
                intent.putExtra(RoutineActivityDetail.NEW_ROUTINE, false);
                intent.putExtra(RoutineActivityDetail.ENTRY_ID, id);
                startActivity(intent);
            }
        });
    }

    /**
     * Listener for this diary entries seek bars. Updates the corresponding textView and value for this
     * seekbar.
     */
    private class DiaryEntrySeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) { //Only change if the user changed the progress
                DiaryEntrySeekBar diaryEntrySeekBar = ((DiaryEntrySeekBar) seekBar);
                int step = diaryEntrySeekBar.getNearestStep(progress);
                switch (diaryEntrySeekBar.getId()) { //Set the text, color and current value for this seek bar's corresponding views
                    case R.id.diary_entry_general_condition_seek_bar:
                        updateConditionBlock(mTextViewOverallCondition, mSeekBarOverallCondition, step);
                        mCurrentFieldValues.overallCondition = step;
                        break;
                    case R.id.diary_entry_forehead_condition_seek_bar:
                        updateConditionBlock(mTextViewForeheadCondition, mSeekBarForeheadCondition, step);
                        mCurrentFieldValues.foreheadCondition = step;
                        break;
                    case R.id.diary_entry_nose_condition_seek_bar:
                        updateConditionBlock(mTextViewNoseCondition, mSeekBarNoseCondition, step);
                        mCurrentFieldValues.noseCondition = step;
                        break;
                    case R.id.diary_entry_cheeks_condition_seek_bar:
                        updateConditionBlock(mTextViewCheeksCondition, mSeekBarCheeksCondition, step);
                        mCurrentFieldValues.cheeksCondition = step;
                        break;
                    case R.id.diary_entry_lips_condition_seek_bar:
                        updateConditionBlock(mTextViewLipsCondition, mSeekBarLipsCondition, step);
                        mCurrentFieldValues.lipsCondition = step;
                        break;
                    case R.id.diary_entry_chin_condition_seek_bar:
                        updateConditionBlock(mTextViewChinCondition, mSeekBarChinCondition, step);
                        mCurrentFieldValues.chinCondition = step;
                        break;
                    case R.id.diary_entry_lifestyle_exercise_seek_bar:
                        updateSliderLabel(mTextViewExercise, step, mExerciseStrings);
                        diaryEntrySeekBar.setProgressToStep(step);
                        mCurrentFieldValues.exercise = step;
                        break;
                    case R.id.diary_entry_lifestyle_diet_seek_bar:
                        updateSliderLabel(mTextViewDiet, step, mDietStrings);
                        diaryEntrySeekBar.setProgressToStep(step);
                        mCurrentFieldValues.diet = step;
                        break;
                    case R.id.diary_entry_lifestyle_hygiene_seek_bar:
                        updateSliderLabel(mTextViewHygiene, step, mHygieneStrings);
                        diaryEntrySeekBar.setProgressToStep(step);
                        mCurrentFieldValues.hygiene = step;
                        break;
                    case R.id.diary_entry_lifestyle_water_seek_bar:
                        updateSliderLabel(mTextViewWaterIntake, step, mWaterIntakeStrings);
                        diaryEntrySeekBar.setProgressToStep(step);
                        mCurrentFieldValues.waterIntake = step;
                        break;
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

    /**
     * Returns the day of the week as a abbreviated string.
     * @return the day of the week: Sun,Mon,Tue,Wed,Thu,Fri,Sat
     */
    private String getDayOfWeek(){
        Calendar today = Calendar.getInstance();
        today.setTime(new Date(mEpochTime));
        int day = today.get(Calendar.DAY_OF_WEEK);

        String dayOfWeek = "";

        switch (day){
            case Calendar.SUNDAY:
                dayOfWeek = getResources().getString(R.string.routine_frequency_sunday);
                break;
            case Calendar.MONDAY:
                dayOfWeek = getResources().getString(R.string.routine_frequency_monday);
                break;
            case Calendar.TUESDAY:
                dayOfWeek = getResources().getString(R.string.routine_frequency_tuesday);
                break;
            case Calendar.WEDNESDAY:
                dayOfWeek = getResources().getString(R.string.routine_frequency_wednesday);
                break;
            case Calendar.THURSDAY:
                dayOfWeek = getResources().getString(R.string.routine_frequency_thursday);
                break;
            case Calendar.FRIDAY:
                dayOfWeek = getResources().getString(R.string.routine_frequency_friday);
                break;
            case Calendar.SATURDAY:
                dayOfWeek = getResources().getString(R.string.routine_frequency_saturday);
                break;
        }

        return dayOfWeek;
    }

    /**
     * Shows the loading screen while hiding the detail layout.
     */
    private void showLoadingScreen() {
        mLoadingView.setVisibility(View.VISIBLE);
        mEntryDetailView.setVisibility(View.INVISIBLE);
    }

    /**
     * Hides the loading screen and shows the detail layout.
     */
    private void hideLoadingScreen() {
        mLoadingView.setVisibility(View.INVISIBLE);
        mEntryDetailView.setVisibility(View.VISIBLE);
    }

    /**
     * Called when the users toggles the show more conditions button. Displays
     * additional conditions if they are hidden, hides them if they are shown.
     */
    private void toggleAdditionalConditions() {

        if (mAdditionalConditionsShown) {
            mAdditionalConditionsLayout.setVisibility(View.INVISIBLE);
            ((ImageButton) mShowMoreLayout.findViewById(R.id.diary_entry_show_more_button)).setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_add_circle_black_24dp));
            //Move the layout below additional conditions up
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.BELOW, mShowMoreLayout.getId());
            mRoutinesLayout.setLayoutParams(layoutParams);
            mAdditionalConditionsShown = false;
        } else {
            mAdditionalConditionsLayout.setVisibility(View.VISIBLE);
            ((ImageButton) mShowMoreLayout.findViewById(R.id.diary_entry_show_more_button)).setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_remove_circle_black_24dp));
            //Move the layout below the additional conditions down
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.BELOW, mAdditionalConditionsLayout.getId());
            mRoutinesLayout.setLayoutParams(layoutParams);
            mAdditionalConditionsShown = true;
        }
    }

    /**
     * Sets the views of this fragment to correspond with the passed in collection of values.
     * @param values values for each view
     */
    private void updateFieldViews(DiaryEntryFieldCollection values) {
        updateConditionBlock(mTextViewOverallCondition, mSeekBarOverallCondition, values.overallCondition);
        updateConditionBlock(mTextViewForeheadCondition, mSeekBarForeheadCondition, values.foreheadCondition);
        updateConditionBlock(mTextViewNoseCondition, mSeekBarNoseCondition, values.noseCondition);
        updateConditionBlock(mTextViewCheeksCondition, mSeekBarCheeksCondition, values.cheeksCondition);
        updateConditionBlock(mTextViewLipsCondition, mSeekBarLipsCondition, values.lipsCondition);
        updateConditionBlock(mTextViewChinCondition, mSeekBarChinCondition, values.chinCondition);

        updateSliderLabel(mTextViewExercise, (int) values.exercise, mExerciseStrings);
        mSeekBarExercise.setProgressToStep((int) values.exercise);

        updateSliderLabel(mTextViewDiet, (int) values.diet, mDietStrings);
        mSeekBarDiet.setProgressToStep((int) values.diet);

        updateSliderLabel(mTextViewHygiene, (int) values.hygiene, mHygieneStrings);
        mSeekBarHygiene.setProgressToStep((int) values.hygiene);

        updateSliderLabel(mTextViewWaterIntake, (int) values.waterIntake, mWaterIntakeStrings);
        mSeekBarWaterIntake.setProgressToStep((int) values.waterIntake);

        mOnPeriodCheckBox.setChecked(values.onPeriod == 1); //If onPeriod == 1 then set checked
    }

    /**
     * Called when user taps the onPeriod layout or the onPeriod checkbox. Toggles
     * the current value of the on period field.
     */
    private void toggleOnPeriodCheckbox() {
        mCurrentFieldValues.onPeriod = mOnPeriodCheckBox.isChecked() ? 1 : 0;
    }

    /**
     * Updates the passed in textView with the appropriate label and background color.
     * @param view the view to update
     * @param step the step of the slider to set the text and color too
     */
    public void updateSliderLabel(TextView view, int step, String[] labelArray) {
        if (step < labelArray.length) {
            view.setText(labelArray[step]);
        }
    }

    /**
     * Sets a condition block to the provided step.
     * @param view    The textview to update
     * @param seekbar The seekbar to update
     * @param step    The step to set to.
     */
    private void updateConditionBlock(TextView view, DiaryEntrySeekBar seekbar, long step) {
        updateSliderLabel(view, (int) step, mConditionStrings);
        seekbar.setProgressToStep((int) step);
        view.setBackgroundColor(mConditionColorIds[(int) step]);
    }

    /**
     * Checks if the current values of any field is different from its value at creation.
     * @return true if this entry has changed
     */
    private boolean entryHasChanged() {
        if (mInitialFieldValues.overallCondition != mCurrentFieldValues.overallCondition
                || mInitialFieldValues.foreheadCondition != mCurrentFieldValues.foreheadCondition
                || mInitialFieldValues.noseCondition != mCurrentFieldValues.noseCondition
                || mInitialFieldValues.cheeksCondition != mCurrentFieldValues.cheeksCondition
                || mInitialFieldValues.chinCondition != mCurrentFieldValues.chinCondition
                || mInitialFieldValues.exercise != mCurrentFieldValues.exercise
                || mInitialFieldValues.diet != mCurrentFieldValues.diet
                || mInitialFieldValues.hygiene != mCurrentFieldValues.hygiene
                || mInitialFieldValues.waterIntake != mCurrentFieldValues.waterIntake
                || mInitialFieldValues.onPeriod != mCurrentFieldValues.onPeriod) {
            return true;
        }
        return false;
    }

    /**
     * Called when the user presses the back button or the navigate up button.
     * Called by parent activity.
     */
    public void onBackButtonPressed() {
        if (entryHasChanged() || mNewEntry)
            saveCurrentDiaryEntry();
        else
            getActivity().finish();
    }

    /**
     * Called by parent activity when the edit list dialog is closed.
     * @param listModified true if the list of routines was modified.
     */
    public void onEditDialogClosed(boolean listModified){
        if (listModified){
            refreshRoutinesList();
        }
    }

    /**
     * Refreshes the listview containing this diary entries routines.
     */
    public void refreshRoutinesList() {
        Long[] args = {mDiaryEntryID};
        new LoadDiaryEntryRoutinesTask().execute(args);
    }

    /**
     * Calls to the saveDiaryEntry async task to save this diary entry.
     */
    private void saveCurrentDiaryEntry() {
        Long[] args = {mEpochTime,
                mCurrentFieldValues.overallCondition,
                mCurrentFieldValues.foreheadCondition,
                mCurrentFieldValues.noseCondition,
                mCurrentFieldValues.cheeksCondition,
                mCurrentFieldValues.lipsCondition,
                mCurrentFieldValues.chinCondition,
                mCurrentFieldValues.exercise,
                mCurrentFieldValues.diet,
                mCurrentFieldValues.hygiene,
                mCurrentFieldValues.waterIntake,
                mCurrentFieldValues.onPeriod};
        new SaveDiaryEntryTask().execute(args);
    }

    /**
     * Calls to deleteDiaryEntry task to delete this entry.
     */
    private void deleteCurrentDiaryEntry(boolean askUser) {
        if (askUser) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.diary_entry_delete_alert_dialog_message)
                    .setTitle(R.string.diary_entry_delete_alert_dialog_title)
                    .setIcon(R.drawable.ic_warning_black_24dp)
                    .setPositiveButton(R.string.alert_delete_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Boolean[] args = {true};
                            new DeleteDiaryEntryTask().execute(args);
                        }
                    })
                    .setNegativeButton(R.string.cancel_string, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        } else {
            Boolean[] args = {false};
            new DeleteDiaryEntryTask().execute(args);
        }
    }

    /**
     * Background task used to fetch data from the database corresponding to this date.
     * If there is no existing entry for this date then one is created.
     */
    private class LoadDiaryEntryTask extends AsyncTask<Long, Void, Cursor> {

        /**
         * @param params params[0] contains the date to fetch from database
         * @return cursor holding database query results
         */
        @Override
        protected Cursor doInBackground(Long... params) {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            long epochTime = params[0];
            String[] columns = {DiaryContract.DiaryEntry._ID, //Columns to retrieve
                    DiaryContract.DiaryEntry.COLUMN_DATE,
                    DiaryContract.DiaryEntry.COLUMN_PHOTO,
                    DiaryContract.DiaryEntry.COLUMN_OVERALL_CONDITION,
                    DiaryContract.DiaryEntry.COLUMN_FOREHEAD_CONDITION,
                    DiaryContract.DiaryEntry.COLUMN_CHEEK_CONDITION,
                    DiaryContract.DiaryEntry.COLUMN_CHIN_CONDITION,
                    DiaryContract.DiaryEntry.COLUMN_NOSE_CONDITION,
                    DiaryContract.DiaryEntry.COLUMN_LIPS_CONDITION,
                    DiaryContract.DiaryEntry.COLUMN_DIET,
                    DiaryContract.DiaryEntry.COLUMN_EXERCISE,
                    DiaryContract.DiaryEntry.COLUMN_HYGIENE,
                    DiaryContract.DiaryEntry.COLUMN_WATER_INTAKE,
                    DiaryContract.DiaryEntry.COLUMN_ON_PERIOD};

            String selection = DiaryContract.DiaryEntry.COLUMN_DATE + " = " + epochTime;

            Cursor rows = db.query(DiaryContract.DiaryEntry.TABLE_NAME, columns, selection, null, null, null, null);

            if (rows != null && rows.getCount() == 0) { //No entry found for this date, create a new one and save default values for a placeholder.
                ContentValues values = new ContentValues();
                values.put(DiaryContract.DiaryEntry.COLUMN_DATE, epochTime);
                values.put(DiaryContract.DiaryEntry.COLUMN_OVERALL_CONDITION, 3);
                values.put(DiaryContract.DiaryEntry.COLUMN_FOREHEAD_CONDITION, 3);
                values.put(DiaryContract.DiaryEntry.COLUMN_NOSE_CONDITION, 3);
                values.put(DiaryContract.DiaryEntry.COLUMN_CHEEK_CONDITION, 3);
                values.put(DiaryContract.DiaryEntry.COLUMN_LIPS_CONDITION, 3);
                values.put(DiaryContract.DiaryEntry.COLUMN_CHIN_CONDITION, 3);
                values.put(DiaryContract.DiaryEntry.COLUMN_EXERCISE, 0);
                values.put(DiaryContract.DiaryEntry.COLUMN_DIET, 0);
                values.put(DiaryContract.DiaryEntry.COLUMN_HYGIENE, 0);
                values.put(DiaryContract.DiaryEntry.COLUMN_WATER_INTAKE, 0);
                values.put(DiaryContract.DiaryEntry.COLUMN_ON_PERIOD, 0);
                mDiaryEntryID = db.insert(DiaryContract.DiaryEntry.TABLE_NAME, null, values); //Set returned row ID to this diary entry's ID

                //Create new lines in the diary_entry_routines table
                //Get routines that have a frequency of daily or include today's day of the week in their frequency
                String[] routineColumns = {DiaryContract.Routine._ID};
                String routineWhere = DiaryContract.Routine.COLUMN_FREQUENCY + " = '" + RoutineActivityDetail.ROUTINE_DAILY + "'";
                String dayOfWeek = "'%"+getDayOfWeek()+"%'"; //Format regex for like clause
                routineWhere = routineWhere + " OR "+ DiaryContract.Routine.COLUMN_FREQUENCY + " LIKE " + dayOfWeek;
                Cursor routinesFromFrequency = db.query(DiaryContract.Routine.TABLE_NAME,routineColumns,routineWhere,null,null,null,null);
                if (routinesFromFrequency != null && routinesFromFrequency.moveToFirst()){
                    do {
                        long routineID = routinesFromFrequency.getLong(routinesFromFrequency.getColumnIndex(DiaryContract.Routine._ID));
                        ContentValues routineValues = new ContentValues();
                        routineValues.put(DiaryContract.DiaryEntryRoutine.COLUMN_ROUTINE_ID,routineID);
                        routineValues.put(DiaryContract.DiaryEntryRoutine.COLUMN_DIARY_ENTRY_ID,mDiaryEntryID);
                        db.insert(DiaryContract.DiaryEntryRoutine.TABLE_NAME,null,routineValues);
                    }while (routinesFromFrequency.moveToNext());
                }
            }

            return rows;
        }

        /**
         * Set values of views and fields based on result returned form query.
         * @param result cursor that contains this entry from the Diary Entry table
         */
        @Override
        protected void onPostExecute(Cursor result) {
            if (result != null && result.moveToFirst()) { //Row was returned from query, an entry for this date exists
                mDiaryEntryID = result.getLong(result.getColumnIndex(DiaryContract.DiaryEntry._ID));

                long generalStep = result.getLong(result.getColumnIndex(DiaryContract.DiaryEntry.COLUMN_OVERALL_CONDITION));
                mInitialFieldValues.overallCondition = generalStep;
                mCurrentFieldValues.overallCondition = generalStep;

                long foreheadStep = result.getLong(result.getColumnIndex(DiaryContract.DiaryEntry.COLUMN_FOREHEAD_CONDITION));
                mInitialFieldValues.foreheadCondition = foreheadStep;
                mCurrentFieldValues.foreheadCondition = foreheadStep;

                long noseStep = result.getLong(result.getColumnIndex(DiaryContract.DiaryEntry.COLUMN_NOSE_CONDITION));
                mInitialFieldValues.noseCondition = noseStep;
                mCurrentFieldValues.noseCondition = noseStep;

                long cheeksStep = result.getLong(result.getColumnIndex(DiaryContract.DiaryEntry.COLUMN_CHEEK_CONDITION));
                mInitialFieldValues.cheeksCondition = cheeksStep;
                mCurrentFieldValues.cheeksCondition = cheeksStep;

                long lipsStep = result.getLong(result.getColumnIndex(DiaryContract.DiaryEntry.COLUMN_LIPS_CONDITION));
                mInitialFieldValues.lipsCondition = lipsStep;
                mCurrentFieldValues.lipsCondition = lipsStep;

                long chinStep = result.getLong(result.getColumnIndex(DiaryContract.DiaryEntry.COLUMN_CHIN_CONDITION));
                mInitialFieldValues.chinCondition = chinStep;
                mCurrentFieldValues.chinCondition = chinStep;

                long exercise = result.getLong(result.getColumnIndex(DiaryContract.DiaryEntry.COLUMN_EXERCISE));
                mInitialFieldValues.exercise = exercise;
                mCurrentFieldValues.exercise = exercise;

                long diet = result.getLong(result.getColumnIndex(DiaryContract.DiaryEntry.COLUMN_DIET));
                mInitialFieldValues.diet = diet;
                mCurrentFieldValues.diet = diet;

                long hygiene = result.getLong(result.getColumnIndex(DiaryContract.DiaryEntry.COLUMN_HYGIENE));
                mInitialFieldValues.hygiene = hygiene;
                mCurrentFieldValues.hygiene = hygiene;

                long waterIntake = result.getLong(result.getColumnIndex(DiaryContract.DiaryEntry.COLUMN_WATER_INTAKE));
                mInitialFieldValues.waterIntake = waterIntake;
                mCurrentFieldValues.waterIntake = waterIntake;

                long onPeriod = result.getLong(result.getColumnIndex(DiaryContract.DiaryEntry.COLUMN_ON_PERIOD));
                mInitialFieldValues.onPeriod = onPeriod;
                mCurrentFieldValues.onPeriod = onPeriod;

                updateFieldViews(mCurrentFieldValues);
            } else { //A new entry was created, default values have already been set
                mNewEntry = true;
            }
            refreshRoutinesList();
            mInitialLoadFinished = true;
        }
    }

    /**
     * AsyncTask to populate the routines listview for this diary entry.
     */
    private class LoadDiaryEntryRoutinesTask extends AsyncTask<Long, Void, Cursor> {

        /**
         * Grabs this diary entry's routines from the DiaryEntryRoutines table and
         * returns them via a cursor.
         * @param params params[0] - this diary entry's _ID
         * @return cursor object containing routines
         */
        @Override
        protected Cursor doInBackground(Long... params) {
            SQLiteDatabase db = mDbHelper.getReadableDatabase();

            long diaryEntryID = params[0];
            Cursor routineCursor = null;

            String[] columns = {DiaryContract.DiaryEntryRoutine.COLUMN_ROUTINE_ID};
            String where = DiaryContract.DiaryEntryRoutine.COLUMN_DIARY_ENTRY_ID + " = " + Long.toString(diaryEntryID);
            Cursor routineIDsCursor = db.query(DiaryContract.DiaryEntryRoutine.TABLE_NAME, columns, where, null, null, null, null);

            //Search routineIDsCursor and find every routine ID to fetch from Routines
            String[] routineColumns = {DiaryContract.Routine._ID, DiaryContract.Routine.COLUMN_NAME, DiaryContract.Routine.COLUMN_TIME};
            String routineWhere = "";
            if (routineIDsCursor.moveToFirst()) {
                do {
                    long routineID = routineIDsCursor.getLong(routineIDsCursor.getColumnIndex(DiaryContract.DiaryEntryRoutine.COLUMN_ROUTINE_ID));
                    if (routineWhere.length() > 0) { //Append OR to every ID past the first.
                        routineWhere = routineWhere + " OR ";
                    }
                    routineWhere = routineWhere + DiaryContract.Routine._ID + " = " + routineID;
                } while (routineIDsCursor.moveToNext());
                String routineOrderBy = DiaryContract.Routine.COLUMN_NAME + " ASC";
                routineCursor = db.query(DiaryContract.Routine.TABLE_NAME, routineColumns, routineWhere, null, null, null, routineOrderBy);
            }

            return routineCursor;
        }

        /**
         * Populates the listview with the results from the table query
         * @param cursor contains rows to insert into listview
         */
        @Override
        protected void onPostExecute(Cursor cursor) {
            mRoutinesListView.setAdapter(null);//Clear current adapter
            if (cursor != null) {
                String[] fromColumns = {DiaryContract.Routine.COLUMN_NAME, DiaryContract.Routine.COLUMN_TIME};
                int[] toViews = {R.id.routine_listview_name, R.id.routine_listView_time};
                SimpleCursorAdapter adapter = new SimpleCursorAdapter(getContext(), R.layout.listview_item_routine_main, cursor, fromColumns, toViews, 0);
                adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                    @Override
                    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                        if (columnIndex == cursor.getColumnIndex(DiaryContract.Routine.COLUMN_NAME)) {
                            TextView nameView = (TextView) view;
                            nameView.setText(cursor.getString(cursor.getColumnIndex(DiaryContract.Routine.COLUMN_NAME)));
                            return true;
                        } else if (columnIndex == cursor.getColumnIndex(DiaryContract.Routine.COLUMN_TIME)) {
                            TextView timeView = (TextView) view;
                            timeView.setText(cursor.getString(cursor.getColumnIndex(DiaryContract.Routine.COLUMN_TIME)));
                            return true;
                        }
                        return false;
                    }
                });
                mRoutinesListView.setAdapter(adapter);
            }
            if (mRoutinesListView.getAdapter() != null && mRoutinesListView.getAdapter().getCount() > 0){
                showLayout(mRoutinesListView);
            }else {
                showLayout(mNoRoutinesTextView);
            }
            hideLoadingScreen();
        }
    }


    /**
     * AsyncTask for saving this diary entry to the database. Called when the user explicitly presses the save button or when the
     * user chooses to save changes when leaving the fragment.
     */
    private class SaveDiaryEntryTask extends AsyncTask<Long, Void, Long> {

        /**
         * @param params params[0] - todays date as milliseconds from epoch
         *               params[1] - overall condition
         *               params[2] - forehead condition
         *               params[3] - nose condition
         *               params[4] - cheeks condition
         *               params[5] - lips condition
         *               params[6] - chin condition
         *               params[7] - exercise
         *               params[8] - diet
         *               params[9] - hygiene
         *               params[10] - water intake
         *               params[11] - on period
         */
        @Override
        protected Long doInBackground(Long... params) {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(DiaryContract.DiaryEntry.COLUMN_DATE, params[0]);
            values.put(DiaryContract.DiaryEntry.COLUMN_OVERALL_CONDITION, params[1]);
            values.put(DiaryContract.DiaryEntry.COLUMN_FOREHEAD_CONDITION, params[2]);
            values.put(DiaryContract.DiaryEntry.COLUMN_NOSE_CONDITION, params[3]);
            values.put(DiaryContract.DiaryEntry.COLUMN_CHEEK_CONDITION, params[4]);
            values.put(DiaryContract.DiaryEntry.COLUMN_LIPS_CONDITION, params[5]);
            values.put(DiaryContract.DiaryEntry.COLUMN_CHIN_CONDITION, params[6]);
            values.put(DiaryContract.DiaryEntry.COLUMN_EXERCISE, params[7]);
            values.put(DiaryContract.DiaryEntry.COLUMN_DIET, params[8]);
            values.put(DiaryContract.DiaryEntry.COLUMN_HYGIENE, params[9]);
            values.put(DiaryContract.DiaryEntry.COLUMN_WATER_INTAKE, params[10]);
            values.put(DiaryContract.DiaryEntry.COLUMN_ON_PERIOD, params[11]);

            String selection = DiaryContract.DiaryEntry.COLUMN_DATE + " = " + mEpochTime;
            return (long) db.update(DiaryContract.DiaryEntry.TABLE_NAME, values, selection, null);
        }

        @Override
        protected void onPostExecute(Long result) {
            if (result == -1) {
                Toast.makeText(getContext(), R.string.toast_save_failed, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), R.string.toast_save_success, Toast.LENGTH_SHORT).show();
            }
            Intent returnIntent = new Intent();
            returnIntent.putExtra(CalendarActivityMain.INTENT_DATE, mEpochTime);
            getActivity().setResult(Activity.RESULT_OK, returnIntent);
            getActivity().finish();
        }
    }

    /**
     * AsyncTask for deleting a diary entry.
     */
    private class DeleteDiaryEntryTask extends AsyncTask<Boolean, Void, Integer> {

        private boolean showToast;

        /**
         * @param params params[0] contains a boolean value denoting whether a toast should be displayed on delete.
         *               (no toast is shown when automatically deleting a new entry on back press).
         */
        @Override
        protected Integer doInBackground(Boolean... params) {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            showToast = params[0];

            String where = DiaryContract.DiaryEntry.COLUMN_DATE + " = ?";
            String[] whereArgs = {Long.toString(mEpochTime)};
            return db.delete(DiaryContract.DiaryEntry.TABLE_NAME, where, whereArgs);
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result == 0) {
                if (showToast)
                    Toast.makeText(getContext(), R.string.toast_delete_failed, Toast.LENGTH_SHORT).show();
            } else {
                if (showToast)
                    Toast.makeText(getContext(), R.string.toast_delete_successful, Toast.LENGTH_SHORT).show();
            }

            //Return to calendar activity with delete flag set
            Intent returnIntent = new Intent();
            returnIntent.putExtra(CalendarActivityMain.INTENT_DATE_DELETED, mEpochTime);
            getActivity().setResult(Activity.RESULT_OK, returnIntent);
            getActivity().finish();
        }
    }

    /**
     * Holds static strings used as keys when saving and restoring state.
     */
    protected static class DiaryEntryState {
        protected static final String DIARY_ENTRY_ID = "DIARY_ENTRY_ID";
        protected static final String DIARY_ENTRY_TIME = "DIARY_ENTRY_TIME";
        protected static final String NEW_DIARY_ENTRY = "NEW_DIARY_ENTRY";
        protected static final String ADDITIONAL_SHOWN = "ADDITIONAL_SHOWN";
        protected static final String INITIAL_FIELD_VALUES = "INITIAL_FIELD_VALUES";
        protected static final String CURRENT_FIELD_VALUES = "CURRENT_FIELD_VALUES";
    }
}
