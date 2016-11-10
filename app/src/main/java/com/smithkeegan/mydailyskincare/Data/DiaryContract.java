package com.smithkeegan.mydailyskincare.data;

import android.provider.BaseColumns;

/**
 * Contract used to maintain database constants.
 * @author Keegan Smith
 * @since 5/3/2016
 */
public final class DiaryContract {

    //Empty constructor
    public DiaryContract(){}

    public static abstract class DiaryEntry implements BaseColumns{
        public static final String TABLE_NAME = "DiaryEntry";
        public static final String COLUMN_DATE = "Date";
        public static final String COLUMN_PHOTO = "Photo";
        public static final String COLUMN_OVERALL_CONDITION = "OverallCondition";
        public static final String COLUMN_FOREHEAD_CONDITION = "ForeheadCondition";
        public static final String COLUMN_NOSE_CONDITION = "NoseCondition";
        public static final String COLUMN_CHEEK_CONDITION = "CheekCondition";
        public static final String COLUMN_LIPS_CONDITION = "LipsCondition";
        public static final String COLUMN_CHIN_CONDITION = "ChinCondition";
        public static final String COLUMN_EXERCISE = "Exercise";
        public static final String COLUMN_DIET = "Diet";
        public static final String COLUMN_HYGIENE = "Hygiene";
        public static final String COLUMN_WATER_INTAKE = "WaterIntake";
        public static final String COLUMN_ON_PERIOD = "OnPeriod";
    }

    public static abstract class ConditionModifier implements BaseColumns{
        public static final String TABLE_NAME = "ConditionModifier";
        public static final String CONSTRAINT_FK_DATE = "fk_date";
        public static final String CONSTRAINT_FK_MODIFIER_ID = "fk_modifier_id";
        public static final String COLUMN_DATE = "Date";
        public static final String COLUMN_MODIFIER_ID = "ModifierID";
        public static final String COLUMN_LOCATION = "Location";
    }

    public static abstract class Modifier implements BaseColumns{
        public static final String TABLE_NAME = "Modifier";
        public static final String COLUMN_NAME = "Name";
    }

    public static abstract class DiaryEntryRoutine implements BaseColumns{
        public static final String TABLE_NAME = "DiaryEntryRoutine";
        public static final String CONSTRAINT_FK_DIARY_ENTRY_ID = "fk_diary_entry_id";
        public static final String CONSTRAINT_FK_ROUTINE_ID = "fk_routine_id";
        public static final String COLUMN_DIARY_ENTRY_ID = "DiaryEntryID";
        public static final String COLUMN_ROUTINE_ID = "RoutineID";
    }

    public static abstract class Routine implements BaseColumns{
        public static final String TABLE_NAME = "Routine";
        public static final String COLUMN_NAME = "Name";
        public static final String COLUMN_TIME = "Time";
        public static final String COLUMN_COMMENT = "Comment";
        public static final String COLUMN_FREQUENCY = "Frequency";
    }

    public static abstract class RoutineProduct implements BaseColumns{
        public static final String TABLE_NAME = "RoutineProduct";
        public static final String CONSTRAINT_FK_ROUTINE_ID = "fk_routine_id";
        public static final String CONSTRAINT_FK_PRODUCT_ID = "fk_product_id";
        public static final String COLUMN_ROUTINE_ID = "RoutineID";
        public static final String COLUMN_PRODUCT_ID = "ProductID";
    }

    public static abstract class Product implements BaseColumns{
        public static final String TABLE_NAME = "Product";
        public static final String COLUMN_BRAND = "Brand";
        public static final String COLUMN_NAME = "Name";
        public static final String COLUMN_TYPE = "Type";
        public static final String COLUMN_COMMENT = "Comment";
    }

    public static abstract class ProductIngredient implements BaseColumns{
        public static final String TABLE_NAME = "ProductIngredient";
        public static final String CONSTRAINT_FK_INGREDIENT_ID = "fk_ingredient_id";
        public static final String CONSTRAINT_FK_PRODUCT_ID = "fk_product_id";
        public static final String COLUMN_PRODUCT_ID = "ProductID";
        public static final String COLUMN_INGREDIENT_ID = "IngredientID";
    }

    public static abstract class Ingredient implements BaseColumns{
        public static final String TABLE_NAME = "Ingredient";
        public static final String COLUMN_NAME = "Name";
        public static final String COLUMN_IRRITANT = "Irritant";
        public static final String COLUMN_COMMENT = "Comment";
    }
}
