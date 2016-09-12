package com.smithkeegan.mydailyskincare.customClasses;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;

/**
 * Custom seekBar class that increments the seekbar based on steps.
 * @author Keegan Smith
 * @since 9/12/2016
 */
public class DiaryEntrySeekBar extends SeekBar {

    private int[] mSteps;
    private int mNumSteps;

    public DiaryEntrySeekBar(Context context) {
        super(context);
        initializeView();
    }

    public DiaryEntrySeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView();
    }

    public DiaryEntrySeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView();
    }

    @TargetApi(21)
    public DiaryEntrySeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initializeView();
    }


    /*
    Initializes fields of this seekbar.
     */
    private void initializeView(){
        setMax(100);
        setNumSteps(7);
    }


    /*
    Sets this seekBar to the default step of centered.
     */
    public void setDefaultStep(){
        int mid = mNumSteps / 2;
        setProgressToStep(mid);
    }

    /*
    Sets the number of steps for this seekbar and also sets the values of those steps in
    the steps array.
     */
    public void setNumSteps(int steps){
        mNumSteps = steps;
        mSteps = new int[steps];
        int max = getMax();
        double stepSize = (double) max / (steps-1);
        for(int i = 0; i < steps-1; i++){
            mSteps[i] = (int) (stepSize * i);
        }
        mSteps[steps-1] = max;
    }

    /*
    Sets the progress of this seekbar to the specified step.
     */
    public void setProgressToStep(int step){
        if(step < mSteps.length)
            setProgress(mSteps[step]);
    }

    /**
     * Given a progress, this method returns the index of the nearest step in this
     * seekbar.
     */
    public int getNearestStep(int progress){
        int before = -1, after = -1, i = 0;
        int step = 0;
        boolean done = false;
        while(i < mSteps.length && !done){
            if( progress <= mSteps[i]){ //Get the step before and after the given progress
                before = i-1;
                after = i;
                done = true;
            }
            i++;
        }

        if(before > -1 && after > -1) { //Set the nearest step if before and after are valid
            step = (progress - mSteps[before] > mSteps[after] - progress) ? after : before;
        }

        return step;
    }

}
