package com.smithkeegan.mydailyskincare.core.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.smithkeegan.mydailyskincare.data.DiaryContract;

/**
 * Created by keegansmith on 7/19/17.
 */

public class Ingredient extends MDSItem implements Parcelable {

    String name;
    boolean isIrritant;
    String comment;

    public Ingredient(){
        name = "";
        isIrritant = false;
        comment = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isIrritant() {
        return isIrritant;
    }

    public void setIrritant(boolean irritant) {
        isIrritant = irritant;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    protected Ingredient(Parcel in) {
        name = in.readString();
        isIrritant = in.readByte() != 0;
        comment = in.readString();
    }

    public static final Creator<Ingredient> CREATOR = new Creator<Ingredient>() {
        @Override
        public Ingredient createFromParcel(Parcel in) {
            return new Ingredient(in);
        }

        @Override
        public Ingredient[] newArray(int size) {
            return new Ingredient[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeByte((byte) (isIrritant ? 1 : 0));
        parcel.writeString(comment);
    }
}
