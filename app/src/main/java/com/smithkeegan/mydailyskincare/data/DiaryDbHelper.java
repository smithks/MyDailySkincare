
package com.smithkeegan.mydailyskincare.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This class is used to create and preform various manipulations of the my daily skincare database.
 * @author Keegan Smith
 * @since 5/4/2016
 */
public class DiaryDbHelper extends SQLiteOpenHelper {

    private static DiaryDbHelper instance;

    public static final int DATABASE_VERSION = 3; //Updated to version 3 on 11/15/2016
    public static final String DATABASE_NAME = "Diary.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String NOT_NULL = " NOT NULL";
    private static final String COMMA_SEP = ",";

    //Return a single instance of the db for all needs
    public static synchronized DiaryDbHelper getInstance(Context context){
        if (instance==null){
            instance = new DiaryDbHelper(context);
        }
        return instance;
    }

    private static final String SQL_DIARY_ENTRY_TABLE =
            "CREATE TABLE " + DiaryContract.DiaryEntry.TABLE_NAME +
                    " (" +
                    DiaryContract.DiaryEntry._ID + INTEGER_TYPE + " PRIMARY KEY" + COMMA_SEP +
                    DiaryContract.DiaryEntry.COLUMN_DATE + INTEGER_TYPE + NOT_NULL + " UNIQUE" + COMMA_SEP +
                    DiaryContract.DiaryEntry.COLUMN_PHOTO + INTEGER_TYPE + COMMA_SEP +
                    DiaryContract.DiaryEntry.COLUMN_OVERALL_CONDITION + INTEGER_TYPE + COMMA_SEP +
                    DiaryContract.DiaryEntry.COLUMN_FOREHEAD_CONDITION + INTEGER_TYPE + COMMA_SEP +
                    DiaryContract.DiaryEntry.COLUMN_NOSE_CONDITION + INTEGER_TYPE + COMMA_SEP +
                    DiaryContract.DiaryEntry.COLUMN_CHEEK_CONDITION + INTEGER_TYPE + COMMA_SEP +
                    DiaryContract.DiaryEntry.COLUMN_LIPS_CONDITION + INTEGER_TYPE + COMMA_SEP +
                    DiaryContract.DiaryEntry.COLUMN_CHIN_CONDITION + INTEGER_TYPE + COMMA_SEP +
                    DiaryContract.DiaryEntry.COLUMN_COMMENT + TEXT_TYPE + COMMA_SEP +
                    DiaryContract.DiaryEntry.COLUMN_EXERCISE + INTEGER_TYPE + COMMA_SEP +
                    DiaryContract.DiaryEntry.COLUMN_DIET + INTEGER_TYPE + COMMA_SEP +
                    DiaryContract.DiaryEntry.COLUMN_HYGIENE + INTEGER_TYPE + COMMA_SEP +
                    DiaryContract.DiaryEntry.COLUMN_WATER_INTAKE + INTEGER_TYPE + COMMA_SEP +
                    DiaryContract.DiaryEntry.COLUMN_ON_PERIOD + INTEGER_TYPE +
                    " );";

    private static final String SQL_MODIFIER_TABLE =
            "CREATE TABLE " + DiaryContract.Modifier.TABLE_NAME +
                    "( " +
                    DiaryContract.Modifier._ID + INTEGER_TYPE + " PRIMARY KEY" + COMMA_SEP +
                    DiaryContract.Modifier.COLUMN_NAME + TEXT_TYPE + NOT_NULL +
                    " );";

    private static final String SQL_CONDITION_MODIFIER_TABLE =
            "CREATE TABLE " + DiaryContract.ConditionModifier.TABLE_NAME +
                    " (" +
                    DiaryContract.ConditionModifier.COLUMN_DATE + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    DiaryContract.ConditionModifier.COLUMN_MODIFIER_ID + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    DiaryContract.ConditionModifier.COLUMN_LOCATION + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                    "CONSTRAINT "+ DiaryContract.ConditionModifier.CONSTRAINT_FK_DATE + " FOREIGN KEY (" + DiaryContract.ConditionModifier.COLUMN_DATE + ") REFERENCES " + DiaryContract.DiaryEntry.TABLE_NAME + "(" + DiaryContract.DiaryEntry.COLUMN_DATE + ") ON DELETE CASCADE" + COMMA_SEP +
                    "CONSTRAINT "+ DiaryContract.ConditionModifier.CONSTRAINT_FK_MODIFIER_ID +" FOREIGN KEY (" + DiaryContract.ConditionModifier.COLUMN_MODIFIER_ID + ") REFERENCES " + DiaryContract.Modifier.TABLE_NAME + "("+ DiaryContract.Modifier._ID + ") ON DELETE CASCADE" + COMMA_SEP +
                    "PRIMARY KEY (" + DiaryContract.ConditionModifier.COLUMN_DATE + COMMA_SEP + DiaryContract.ConditionModifier.COLUMN_MODIFIER_ID + COMMA_SEP + DiaryContract.ConditionModifier.COLUMN_LOCATION + ")" +
                    " );";

    private static final String SQL_ROUTINE_TABLE =
            "CREATE TABLE " + DiaryContract.Routine.TABLE_NAME +
                    " (" +
                    DiaryContract.Routine._ID + INTEGER_TYPE + " PRIMARY KEY" + COMMA_SEP +
                    DiaryContract.Routine.COLUMN_NAME + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                    DiaryContract.Routine.COLUMN_TIME + TEXT_TYPE + COMMA_SEP +
                    DiaryContract.Routine.COLUMN_FREQUENCY + TEXT_TYPE + COMMA_SEP +
                    DiaryContract.Routine.COLUMN_COMMENT + TEXT_TYPE +
                    " );";

    private static final String SQL_DIARY_ENTRY_ROUTINE_TABLE =
            "CREATE TABLE " + DiaryContract.DiaryEntryRoutine.TABLE_NAME +
                    " (" +
                    DiaryContract.DiaryEntryRoutine.COLUMN_DIARY_ENTRY_ID + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    DiaryContract.DiaryEntryRoutine.COLUMN_ROUTINE_ID + INTEGER_TYPE +NOT_NULL + COMMA_SEP +
                    "CONSTRAINT "+ DiaryContract.DiaryEntryRoutine.CONSTRAINT_FK_DIARY_ENTRY_ID + " FOREIGN KEY (" + DiaryContract.DiaryEntryRoutine.COLUMN_DIARY_ENTRY_ID + ") REFERENCES " + DiaryContract.DiaryEntry.TABLE_NAME + "(" + DiaryContract.DiaryEntry._ID + ") ON DELETE CASCADE" + COMMA_SEP +
                    "CONSTRAINT "+ DiaryContract.DiaryEntryRoutine.CONSTRAINT_FK_ROUTINE_ID + " FOREIGN KEY (" + DiaryContract.DiaryEntryRoutine.COLUMN_ROUTINE_ID + ") REFERENCES " + DiaryContract.Routine.TABLE_NAME + "(" + DiaryContract.Routine._ID + ") ON DELETE CASCADE" + COMMA_SEP +
                    "PRIMARY KEY (" + DiaryContract.DiaryEntryRoutine.COLUMN_DIARY_ENTRY_ID + COMMA_SEP + DiaryContract.DiaryEntryRoutine.COLUMN_ROUTINE_ID + ")" +
                    " );";

    private static final String SQL_PRODUCT_TABLE =
            "CREATE TABLE " + DiaryContract.Product.TABLE_NAME +
                    " (" +
                    DiaryContract.Product._ID + INTEGER_TYPE + " PRIMARY KEY" + COMMA_SEP +
                    DiaryContract.Product.COLUMN_BRAND + TEXT_TYPE + COMMA_SEP +
                    DiaryContract.Product.COLUMN_NAME + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                    DiaryContract.Product.COLUMN_TYPE + TEXT_TYPE + COMMA_SEP +
                    DiaryContract.Product.COLUMN_COMMENT + TEXT_TYPE +
                    " );";

    private static final String SQL_ROUTINE_PRODUCT_TABLE =
            "CREATE TABLE " + DiaryContract.RoutineProduct.TABLE_NAME +
                    " (" +
                    DiaryContract.RoutineProduct.COLUMN_ROUTINE_ID + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    DiaryContract.RoutineProduct.COLUMN_PRODUCT_ID + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    "CONSTRAINT "+ DiaryContract.RoutineProduct.CONSTRAINT_FK_ROUTINE_ID + " FOREIGN KEY (" + DiaryContract.RoutineProduct.COLUMN_ROUTINE_ID + ") REFERENCES " + DiaryContract.Routine.TABLE_NAME + "(" + DiaryContract.Routine._ID + ") ON DELETE CASCADE" + COMMA_SEP +
                    "CONSTRAINT "+ DiaryContract.RoutineProduct.CONSTRAINT_FK_PRODUCT_ID + " FOREIGN KEY (" + DiaryContract.RoutineProduct.COLUMN_PRODUCT_ID + ") REFERENCES " + DiaryContract.Product.TABLE_NAME + "(" + DiaryContract.Product._ID + ") ON DELETE CASCADE" + COMMA_SEP +
                    "PRIMARY KEY (" + DiaryContract.RoutineProduct.COLUMN_ROUTINE_ID + COMMA_SEP + DiaryContract.RoutineProduct.COLUMN_PRODUCT_ID + ")" +
                    " );";

    private static final String SQL_INGREDIENT_TABLE =
            "CREATE TABLE " + DiaryContract.Ingredient.TABLE_NAME +
                    " (" +
                    DiaryContract.Ingredient._ID + INTEGER_TYPE + " PRIMARY KEY " + COMMA_SEP +
                    DiaryContract.Ingredient.COLUMN_NAME + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                    DiaryContract.Ingredient.COLUMN_IRRITANT + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    DiaryContract.Ingredient.COLUMN_COMMENT + TEXT_TYPE +
                    " );";

    private static final String SQL_PRODUCT_INGREDIENT_TABLE =
            "CREATE TABLE " + DiaryContract.ProductIngredient.TABLE_NAME +
                    " (" +
                    DiaryContract.ProductIngredient.COLUMN_PRODUCT_ID + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    DiaryContract.ProductIngredient.COLUMN_INGREDIENT_ID + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    "CONSTRAINT "+ DiaryContract.ProductIngredient.CONSTRAINT_FK_PRODUCT_ID + " FOREIGN KEY (" + DiaryContract.ProductIngredient.COLUMN_PRODUCT_ID + ") REFERENCES " + DiaryContract.Product.TABLE_NAME + "(" + DiaryContract.Product._ID + ") ON DELETE CASCADE" + COMMA_SEP +
                    "CONSTRAINT " + DiaryContract.ProductIngredient.CONSTRAINT_FK_INGREDIENT_ID + " FOREIGN KEY (" + DiaryContract.ProductIngredient.COLUMN_INGREDIENT_ID + ") REFERENCES " + DiaryContract.Ingredient.TABLE_NAME + "(" + DiaryContract.Ingredient._ID + ") ON DELETE CASCADE" + COMMA_SEP +
                    "PRIMARY KEY (" + DiaryContract.ProductIngredient.COLUMN_PRODUCT_ID + COMMA_SEP + DiaryContract.ProductIngredient.COLUMN_INGREDIENT_ID + ")" +
                    " );";

    //Added 11/10/2016
    private static final String SQL_PRODUCT_ADD_COMMENT_COLUMN =
            "ALTER TABLE " + DiaryContract.Product.TABLE_NAME + " ADD COLUMN "+ DiaryContract.Product.COLUMN_COMMENT + TEXT_TYPE;

    //Added 11/15/2016
    private static final String SQL_DIARY_ENTRY_ADD_COMMENT_COLUMN =
            "ALTER TABLE " + DiaryContract.DiaryEntry.TABLE_NAME + " ADD COLUMN "+ DiaryContract.DiaryEntry.COLUMN_COMMENT + TEXT_TYPE;


    public DiaryDbHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION );
    }

    @Override
    public void onOpen(SQLiteDatabase db){
        super.onOpen(db);
        if(!db.isReadOnly())
            db.execSQL("PRAGMA foreign_keys=ON;"); //enable foreign keys
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_DIARY_ENTRY_TABLE);
        db.execSQL(SQL_MODIFIER_TABLE);
        db.execSQL(SQL_CONDITION_MODIFIER_TABLE);
        db.execSQL(SQL_ROUTINE_TABLE);
        db.execSQL(SQL_DIARY_ENTRY_ROUTINE_TABLE);
        db.execSQL(SQL_PRODUCT_TABLE);
        db.execSQL(SQL_ROUTINE_PRODUCT_TABLE);
        db.execSQL(SQL_INGREDIENT_TABLE);
        db.execSQL(SQL_PRODUCT_INGREDIENT_TABLE);

    }

    /**
     * Called when database needs to be updated
     * @param db the database to update
     * @param oldVersion the old version of the database
     * @param newVersion the new version
     * 11/10/2016 - Added Comment column to Product table.
     * 11/15/2016 - Added Comment column to Diary Entry table.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion){
            case 1: //Added 11/10/2016
                db.execSQL(SQL_PRODUCT_ADD_COMMENT_COLUMN);
            case 2: //Added 11/15/2016
                db.execSQL(SQL_DIARY_ENTRY_ADD_COMMENT_COLUMN);
        }
    }

    public void dropTables(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS "+ DiaryContract.RoutineProduct.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ DiaryContract.ProductIngredient.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ DiaryContract.ConditionModifier.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ DiaryContract.DiaryEntryRoutine.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ DiaryContract.DiaryEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ DiaryContract.Modifier.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ DiaryContract.Routine.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ DiaryContract.Product.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ DiaryContract.Ingredient.TABLE_NAME);
    }
}
