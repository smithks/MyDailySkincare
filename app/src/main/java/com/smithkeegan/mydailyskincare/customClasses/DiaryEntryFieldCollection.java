package com.smithkeegan.mydailyskincare.customClasses;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Helper class used to hold values of fields in the diary entry fragment for comparison.
 * @author Keegan Smith
 * @since 10/6/2016
 */
public class DiaryEntryFieldCollection implements Parcelable {
    public long overallCondition;
    public long foreheadCondition;
    public long noseCondition;
    public long cheeksCondition;
    public long lipsCondition;
    public long chinCondition;
    public long exercise;
    public long diet;
    public long hygiene;
    public long waterIntake;
    public long onPeriod;

    public static final Creator<DiaryEntryFieldCollection> CREATOR = new Creator<DiaryEntryFieldCollection>() {
        @Override
        public DiaryEntryFieldCollection createFromParcel(Parcel in) {
            return new DiaryEntryFieldCollection(in);
        }

        @Override
        public DiaryEntryFieldCollection[] newArray(int size) {
            return new DiaryEntryFieldCollection[size];
        }
    };

    public DiaryEntryFieldCollection(){
        overallCondition = 3;
        foreheadCondition = 3;
        noseCondition = 3;
        cheeksCondition = 3;
        lipsCondition = 3;
        chinCondition = 3;
        exercise = 0;
        diet = 0;
        hygiene = 0;
        waterIntake = 0;
        onPeriod = 0;
    }

    public DiaryEntryFieldCollection(Parcel in) {
        overallCondition = in.readLong();
        foreheadCondition = in.readLong();
        noseCondition = in.readLong();
        cheeksCondition = in.readLong();
        lipsCondition = in.readLong();
        chinCondition = in.readLong();
        exercise = in.readLong();
        diet = in.readLong();
        hygiene = in.readLong();
        waterIntake = in.readLong();
        onPeriod = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(overallCondition);
        dest.writeLong(foreheadCondition);
        dest.writeLong(noseCondition);
        dest.writeLong(cheeksCondition);
        dest.writeLong(lipsCondition);
        dest.writeLong(chinCondition);
        dest.writeLong(exercise);
        dest.writeLong(diet);
        dest.writeLong(hygiene);
        dest.writeLong(waterIntake);
        dest.writeLong(onPeriod);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
