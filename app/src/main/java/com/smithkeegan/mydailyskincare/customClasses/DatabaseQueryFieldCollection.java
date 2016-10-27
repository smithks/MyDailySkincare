package com.smithkeegan.mydailyskincare.customClasses;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Object to hold the fields that will be used in this database access
 */
public class DatabaseQueryFieldCollection implements Parcelable {
    public String TABLE;
    public String[] COLUMNS;
    public String ORDER_BY;
    public String WHERE;
    public String WHERE_ARG;

    public DatabaseQueryFieldCollection() {
        TABLE = "";
        COLUMNS = null;
        ORDER_BY = "";
        WHERE = "";
        WHERE_ARG = "";
    }

    protected DatabaseQueryFieldCollection(Parcel in) {
        TABLE = in.readString();
        COLUMNS = in.createStringArray();
        ORDER_BY = in.readString();
        WHERE = in.readString();
        WHERE_ARG = in.readString();
    }

    public static final Creator<DatabaseQueryFieldCollection> CREATOR = new Creator<DatabaseQueryFieldCollection>() {
        @Override
        public DatabaseQueryFieldCollection createFromParcel(Parcel in) {
            return new DatabaseQueryFieldCollection(in);
        }

        @Override
        public DatabaseQueryFieldCollection[] newArray(int size) {
            return new DatabaseQueryFieldCollection[size];
        }
    };

    public void setColumns(String[] columns) {
        COLUMNS = columns;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(TABLE);
        dest.writeStringArray(COLUMNS);
        dest.writeString(ORDER_BY);
        dest.writeString(WHERE);
        dest.writeString(WHERE_ARG);
    }
}
