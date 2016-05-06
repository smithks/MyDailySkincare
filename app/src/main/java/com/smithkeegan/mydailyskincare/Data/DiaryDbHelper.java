
package com.smithkeegan.mydailyskincare.Data;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * @author Keegan Smith
 * @since 5/4/2016
 */
public class DiaryDbHelper extends SQLiteOpenHelper {

    private static DiaryDbHelper instance;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Diary.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String NOT_NULL = " NOT NULL";
    private static final String AUTOINCREMENT = " AUTOINCREMENT";
    private static final String COMMA_SEP = ",";

    //Return a single instance of the db for all needs
    public static synchronized DiaryDbHelper getInstance(Context context){
        if (instance==null){
            instance = new DiaryDbHelper(context.getApplicationContext());
        }
        return instance;
    }

    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "mesage" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);


        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);


            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {


                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){

            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }


    }

    private static final String SQL_DIARY_ENTRY_TABLE =
            "CREATE TABLE " + DiaryContract.DiaryEntry.TABLE_NAME +
                    " (" +
                    DiaryContract.DiaryEntry.COLUMN_DATE + INTEGER_TYPE + " PRIMARY KEY" + NOT_NULL + COMMA_SEP +
                    DiaryContract.DiaryEntry.COLUMN_PHOTO + INTEGER_TYPE + COMMA_SEP +
                    DiaryContract.DiaryEntry.COLUMN_GENERAL_CONDITION + INTEGER_TYPE + COMMA_SEP +
                    DiaryContract.DiaryEntry.COLUMN_FOREHEAD_CONDITION + INTEGER_TYPE + COMMA_SEP +
                    DiaryContract.DiaryEntry.COLUMN_NOSE_CONDITION + INTEGER_TYPE + COMMA_SEP +
                    DiaryContract.DiaryEntry.COLUMN_CHEEK_CONDITION + INTEGER_TYPE + COMMA_SEP +
                    DiaryContract.DiaryEntry.COLUMN_LIPS_CONDITION + INTEGER_TYPE + COMMA_SEP +
                    DiaryContract.DiaryEntry.COLUMN_CHIN_CONDITION + INTEGER_TYPE + COMMA_SEP +
                    DiaryContract.DiaryEntry.COLUMN_EXERCISE + INTEGER_TYPE + COMMA_SEP +
                    DiaryContract.DiaryEntry.COLUMN_DIET + INTEGER_TYPE + COMMA_SEP +
                    DiaryContract.DiaryEntry.COLUMN_HYGIENE + INTEGER_TYPE + COMMA_SEP +
                    DiaryContract.DiaryEntry.COLUMN_WATER_INTAKE + INTEGER_TYPE + COMMA_SEP +
                    DiaryContract.DiaryEntry.COLUMN_ON_PERIOD + INTEGER_TYPE +
                    " );";

    private static final String SQL_MODIFIER_TABLE =
            "CREATE TABLE" + DiaryContract.Modifier.TABLE_NAME +
                    "( " +
                    DiaryContract.Modifier._ID + INTEGER_TYPE + " PRIMARY KEY" + NOT_NULL + AUTOINCREMENT + COMMA_SEP +
                    DiaryContract.Modifier.COLUMN_NAME + TEXT_TYPE + NOT_NULL +
                    " );";

    private static final String SQL_CONDITION_MODIFIER_TABLE =
            "CREATE TABLE " + DiaryContract.ConditionModifier.TABLE_NAME +
                    " (" +
                    DiaryContract.ConditionModifier.COLUMN_DATE + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    DiaryContract.ConditionModifier.COLUMN_MODIFIER_ID + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    DiaryContract.ConditionModifier.COLUMN_LOCATION + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                    "FOREIGN KEY (" + DiaryContract.ConditionModifier.COLUMN_DATE + ") REFERENCES " + DiaryContract.DiaryEntry.TABLE_NAME + "(" + DiaryContract.DiaryEntry.COLUMN_DATE + ")" + COMMA_SEP +
                    "FOREIGN KEY (" + DiaryContract.ConditionModifier.COLUMN_MODIFIER_ID + ") REFERENCES " + DiaryContract.Modifier.TABLE_NAME + "("+ DiaryContract.Modifier._ID + ")" + COMMA_SEP +
                    "PRIMARY KEY (" + DiaryContract.ConditionModifier.COLUMN_DATE + COMMA_SEP + DiaryContract.ConditionModifier.COLUMN_MODIFIER_ID + COMMA_SEP + DiaryContract.ConditionModifier.COLUMN_LOCATION + ")" +
                    " );";

    private static final String SQL_ROUTINE_TABLE =
            "CREATE TABLE " + DiaryContract.Routine.TABLE_NAME +
                    " (" +
                    DiaryContract.Routine._ID + INTEGER_TYPE + " PRIMARY KEY" + AUTOINCREMENT + NOT_NULL + COMMA_SEP +
                    DiaryContract.Routine.COLUMN_NAME + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                    DiaryContract.Routine.COLUMN_TIME + INTEGER_TYPE +
                    " );";

    private static final String SQL_DIARY_ENTRY_ROUTINE_TABLE =
            "CREATE TABLE " + DiaryContract.DiaryEntryRoutine.TABLE_NAME +
                    " (" +
                    DiaryContract.DiaryEntryRoutine.COLUMN_DATE + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    DiaryContract.DiaryEntryRoutine.COLUMN_ROUTINE_ID + INTEGER_TYPE +NOT_NULL + COMMA_SEP +
                    "FOREIGN KEY (" + DiaryContract.DiaryEntryRoutine.COLUMN_DATE + ") REFERENCES " + DiaryContract.DiaryEntry.TABLE_NAME + "(" + DiaryContract.DiaryEntry.COLUMN_DATE + ")" + COMMA_SEP +
                    "FOREIGN KEY (" + DiaryContract.DiaryEntryRoutine.COLUMN_ROUTINE_ID + ") REFERENCES" + DiaryContract.Routine.TABLE_NAME + "(" + DiaryContract.Routine._ID + ")" + COMMA_SEP +
                    "PRIMARY KEY (" + DiaryContract.DiaryEntryRoutine.COLUMN_DATE + COMMA_SEP + DiaryContract.DiaryEntryRoutine.COLUMN_ROUTINE_ID + ")" +
                    " );";

    private static final String SQL_PRODUCT_TABLE =
            "CREATE TABLE " + DiaryContract.Product.TABLE_NAME +
                    " (" +
                    DiaryContract.Product._ID + INTEGER_TYPE + " PRIMARY KEY" + AUTOINCREMENT + NOT_NULL + COMMA_SEP +
                    DiaryContract.Product.COLUMN_BRAND + TEXT_TYPE + COMMA_SEP +
                    DiaryContract.Product.COLUMN_NAME + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                    DiaryContract.Product.COLUMN_TYPE + TEXT_TYPE + NOT_NULL +
                    " );";

    private static final String SQL_ROUTINE_PRODUCT_TABLE =
            "CREATE TABLE " + DiaryContract.RoutineProduct.TABLE_NAME +
                    " (" +
                    DiaryContract.RoutineProduct.COLUMN_ROUTINE_ID + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    DiaryContract.RoutineProduct.COLUMN_PRODUCT_ID + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    "FOREIGN KEY (" + DiaryContract.RoutineProduct.COLUMN_ROUTINE_ID + ") REFERENCES " + DiaryContract.Routine.TABLE_NAME + "(" + DiaryContract.Routine._ID + ")" + COMMA_SEP +
                    "FOREIGN KEY (" + DiaryContract.RoutineProduct.COLUMN_PRODUCT_ID + ") REFERENCES " + DiaryContract.Product.TABLE_NAME + "(" + DiaryContract.Product._ID + ")" + COMMA_SEP +
                    "PRIMARY KEY (" + DiaryContract.RoutineProduct.COLUMN_ROUTINE_ID + COMMA_SEP + DiaryContract.RoutineProduct.COLUMN_PRODUCT_ID + ")" +
                    " );";

    private static final String SQL_INGREDIENT_TABLE =
            "CREATE TABLE " + DiaryContract.Ingredient.TABLE_NAME +
                    " (" +
                    DiaryContract.Ingredient._ID + INTEGER_TYPE + " PRIMARY KEY " + AUTOINCREMENT + NOT_NULL + COMMA_SEP +
                    DiaryContract.Ingredient.COLUMN_NAME + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                    DiaryContract.Ingredient.COLUMN_IRRITANT + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    DiaryContract.Ingredient.COLUMN_COMMENT + TEXT_TYPE +
                    " );";

    private static final String SQL_PRODUCT_INGREDIENT_TABLE =
            "CREATE TABLE " + DiaryContract.ProductIngredient.TABLE_NAME +
                    " (" +
                    DiaryContract.ProductIngredient.COLUMN_PRODUCT_ID + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    DiaryContract.ProductIngredient.COLUMN_INGREDIENT_ID + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    "FOREIGN KEY (" + DiaryContract.ProductIngredient.COLUMN_PRODUCT_ID + ") REFERENCES " + DiaryContract.Product.TABLE_NAME + "(" + DiaryContract.Product._ID + ")" + COMMA_SEP +
                    "FOREIGN KEY (" + DiaryContract.ProductIngredient.COLUMN_INGREDIENT_ID + ") REFERENCES " + DiaryContract.Ingredient.TABLE_NAME + "(" + DiaryContract.Ingredient._ID + ")" + COMMA_SEP +
                    "PRIMARY KEY (" + DiaryContract.ProductIngredient.COLUMN_PRODUCT_ID + COMMA_SEP + DiaryContract.ProductIngredient.COLUMN_INGREDIENT_ID + ")" +
                    " );";


    private static final String SQL_TABLES =
            SQL_DIARY_ENTRY_TABLE +
                    SQL_MODIFIER_TABLE +
                    SQL_CONDITION_MODIFIER_TABLE +
                    SQL_ROUTINE_TABLE +
                    SQL_DIARY_ENTRY_ROUTINE_TABLE +
                    SQL_PRODUCT_TABLE +
                    SQL_ROUTINE_PRODUCT_TABLE +
                    SQL_INGREDIENT_TABLE +
                    SQL_PRODUCT_INGREDIENT_TABLE;


    public DiaryDbHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION );
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_TABLES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ DiaryContract.DiaryEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ DiaryContract.Modifier.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ DiaryContract.ConditionModifier.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ DiaryContract.Routine.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ DiaryContract.DiaryEntryRoutine.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ DiaryContract.Product.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ DiaryContract.RoutineProduct.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ DiaryContract.Ingredient.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ DiaryContract.ProductIngredient.TABLE_NAME);
        onCreate(db);

    }
}
