package com.smithkeegan.mydailyskincare.ui.ingredient;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import com.smithkeegan.mydailyskincare.R;
import com.smithkeegan.mydailyskincare.core.DetailView;
import com.smithkeegan.mydailyskincare.core.IngredientDetailViewModel;
import com.smithkeegan.mydailyskincare.core.model.Ingredient;
import com.smithkeegan.mydailyskincare.data.DiaryDbHelper;

/**
 * Fragment class of the detail screen of ingredients. Handles the various actions taken
 * to manipulate an ingredient's details.
 *
 * @author Keegan Smith
 * @since 5/10/2016
 */
public class IngredientFragmentDetail extends Fragment implements DetailView {

    private DiaryDbHelper mDbHelper;
    private Boolean mNewEntry;
    private EditText mNameEditText;
    private EditText mCommentEditText;
    private CheckBox mIrritantCheckbox;

    IngredientDetailViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        View rootView = inflater.inflate(R.layout.fragment_ingredient_detail, container, false);
        mDbHelper = DiaryDbHelper.getInstance(getContext());

        mNameEditText = (EditText) rootView.findViewById(R.id.ingredient_name_edit);
        mCommentEditText = (EditText) rootView.findViewById(R.id.ingredient_comment_edit);
        mIrritantCheckbox = (CheckBox) rootView.findViewById(R.id.ingredient_irritant_checkbox);

        //Grab the ingredient that was pass from the list activity.
        Ingredient initialIngredient = getArguments().getParcelable(IngredientActivityDetail.INGREDIENT);
        getActivity().setTitle(R.string.ingredient_activity_title);
        if (initialIngredient == null) {
            initialIngredient = new Ingredient();
            initialIngredient.setId(-1); //Indicates this is a new ingredient
            getActivity().setTitle(R.string.ingredient_activity_title_new);
        }

        viewModel = new IngredientDetailViewModel(this);
        viewModel.setInitialIngredient(initialIngredient);
        return rootView;
    }

    /**
     * Method called by parent activity when the user presses the home button or the physical back button.
     * Asks the user if they want to save changes if there are changes to save.
     */
    public void onBackButtonPressed() {
        viewModel.onBackPressed(ingredientFromFields());
    }

    public void displaySaveAlert() {
        //Display an alert if changes should be saved.
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.ingredient_back_alert_dialog_message)
                .setTitle(R.string.ingredient_back_alert_dialog_title)
                .setPositiveButton(R.string.save_button_string, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //saveCurrentIngredient();
                        viewModel.saveIngredient(ingredientFromFields());
                    }
                })
                .setNegativeButton(R.string.no_string, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getActivity().finish();
                    }
                }).setNeutralButton(R.string.cancel_string, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    public void finish(){
        getActivity().finish();
    }

    private Ingredient ingredientFromFields() {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(mNameEditText.getText().toString());
        ingredient.setComment(mCommentEditText.getText().toString());
        ingredient.setIrritant(mIrritantCheckbox.isChecked());
        return ingredient;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_item_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_save:
                //saveCurrentIngredient();
                return true;
            case R.id.menu_action_delete:
                deleteCurrentIngredient();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


//    /**
//     * Saves the current fields when the activity is destroyed by a system process to be restored
//     * later.
//     * @param outState bundle that will contain this fragments current fields.
//     */
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
////        outState.putLong(IngredientState.INGREDIENT_ID, mExistingId);
////        outState.putBoolean(IngredientState.NEW_ENTRY, mNewEntry);
////        outState.putStringArray(IngredientState.INGREDIENT_NAME, new String[]{mInitialName, mNameEditText.getText().toString().trim()});
////        outState.putBooleanArray(IngredientState.INGREDIENT_IRRITANT, new boolean[]{mInitialCheck, mIrritantCheckbox.isChecked()});
////        outState.putStringArray(IngredientState.INGREDIENT_COMMENT, new String[]{mInitialComment, mCommentEditText.getText().toString().trim()});
//
//        super.onSaveInstanceState(outState);
//    }

//    /**
//     * Restores fields of this fragment from the restored instance state.
//     * @param savedInstance the restored state
//     */
//    public void restoreSavedState(Bundle savedInstance) {
//        mExistingId = savedInstance.getLong(IngredientState.INGREDIENT_ID);
//        mNewEntry = savedInstance.getBoolean(IngredientState.NEW_ENTRY);
//
//        String[] names = savedInstance.getStringArray(IngredientState.INGREDIENT_NAME);
//        if (names != null) {
//            mInitialName = names[0];
//            mNameEditText.setText(names[1]);
//        }
//
//        boolean[] irritants = savedInstance.getBooleanArray(IngredientState.INGREDIENT_IRRITANT);
//        if (irritants != null) {
//            mInitialCheck = irritants[0];
//            mIrritantCheckbox.setChecked(irritants[1]);
//        }
//
//        String[] comments = savedInstance.getStringArray(IngredientState.INGREDIENT_COMMENT);
//        if (comments != null) {
//            mInitialComment = comments[0];
//            mCommentEditText.setText(comments[1]);
//        }
//    }

    @Override
    public void updateView(Ingredient ingredient) {
        mNameEditText.setText(ingredient.getName());
        mCommentEditText.setText(ingredient.getComment());
        mIrritantCheckbox.setChecked(ingredient.isIrritant());
    }

//    /**
//     * Returns whether this entry has changed values.
//     * @return true if this entry has changed
//     */
//    private boolean entryHasChanged() {
//        String name = mNameEditText.getText().toString().trim();
//        boolean isIrritant = mIrritantCheckbox.isChecked();
//        String comment = mCommentEditText.getText().toString().trim();
//        return (!name.equals(mInitialName)) || (!isIrritant == mInitialCheck) || (!comment.equals(mInitialComment));
//    }

//    /**
//     * Based on conditions of the fragment, will begin the save ingredient task.
//     */
//    public void saveCurrentIngredient() {
//        if (mNameEditText.getText().toString().trim().length() == 0)
//            Toast.makeText(getContext(), R.string.toast_enter_valid_name, Toast.LENGTH_SHORT).show();
//        else {
//            if (entryHasChanged()) {
//                String name = mNameEditText.getText().toString().trim();
//                boolean isIrritant = mIrritantCheckbox.isChecked();
//                String irritant = isIrritant ? "1" : "0";
//                String comment = mCommentEditText.getText().toString().trim();
//                String[] params = {name, irritant, comment};
//                new SaveIngredientTask().execute(params);
//            } else { //Provide feedback if everything is ok
//                Toast.makeText(getContext(), R.string.toast_save_success, Toast.LENGTH_SHORT).show();
//                getActivity().finish();
//            }
//        }
//    }

    /**
     * Deletes this ingredient from the ingredient database.
     */
    public void deleteCurrentIngredient() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.ingredient_delete_alert_dialog_message)
                .setTitle(R.string.ingredient_delete_alert_dialog_title)
                .setIcon(R.drawable.ic_warning_black_24dp)
                .setPositiveButton(R.string.alert_delete_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //new DeleteIngredientTask().execute();
                    }
                })
                .setNegativeButton(R.string.cancel_string, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

//    /**
//     * Task to save a new ingredient or save changes to an existing ingredient.
//     * Performed as a background task.
//     */
//    private class SaveIngredientTask extends AsyncTask<String, Void, Long> {
//
//        @Override
//        protected Long doInBackground(String... params) {
//            SQLiteDatabase db = mDbHelper.getWritableDatabase();
//            ContentValues values = new ContentValues();
//
//            values.put(DiaryContract.Ingredient.COLUMN_NAME, params[0]);
//            values.put(DiaryContract.Ingredient.COLUMN_IRRITANT, Integer.parseInt(params[1]));
//            values.put(DiaryContract.Ingredient.COLUMN_COMMENT, params[2]);
//            if (mNewEntry)
//                return db.insert(DiaryContract.Ingredient.TABLE_NAME, null, values);
//            else {
//                String where = DiaryContract.Ingredient._ID + " = ?";
//                String[] whereArg = {mExistingId.toString()};
//                int rows = db.update(DiaryContract.Ingredient.TABLE_NAME, values, where, whereArg);
//                return Long.valueOf(rows == 0 ? -1 : 0); //return -1 if no rows affected, error storing
//            }
//        }
//
//        @Override
//        protected void onPostExecute(final Long param) {
//            if (param == -1) {
//                Toast.makeText(getContext(), R.string.toast_save_failed, Toast.LENGTH_SHORT).show();
//            } else {
//                Intent intent = new Intent();
//                intent.putExtra(IngredientActivityMain.INGREDIENT_FINISHED_ID, param);
//                getActivity().setResult(AppCompatActivity.RESULT_OK, intent);
//                Toast.makeText(getContext(), R.string.toast_save_success, Toast.LENGTH_SHORT).show();
//            }
//            getActivity().finish();
//        }
//    }

//    /**
//     * Task to delete the currently open ingredient. Performed in background.
//     */
//    private class DeleteIngredientTask extends AsyncTask<Void, Void, Integer> {
//
//        @Override
//        protected Integer doInBackground(Void... params) {
//            SQLiteDatabase db = mDbHelper.getWritableDatabase();
//            String where = DiaryContract.Ingredient._ID + " = ?";
//            String[] whereArgs = {mExistingId.toString()};
//            return db.delete(DiaryContract.Ingredient.TABLE_NAME, where, whereArgs);
//        }
//
//        @Override
//        protected void onPostExecute(final Integer param) {
//            if (param == 1) {
//                Toast.makeText(getContext(), R.string.toast_delete_successful, Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(getContext(), R.string.toast_delete_failed, Toast.LENGTH_SHORT).show();
//            }
//            getActivity().finish();
//        }
//    }


//    /**
//     * Task to load ingredient data from database on fragment load. Performed in background.
//     */
//    private class LoadIngredientTask extends AsyncTask<Long, Void, Cursor> {
//
//        @Override
//        protected Cursor doInBackground(Long... params) {
//            SQLiteDatabase db = mDbHelper.getReadableDatabase();
//            String[] columns = {DiaryContract.Ingredient.COLUMN_NAME, DiaryContract.Ingredient.COLUMN_IRRITANT, DiaryContract.Ingredient.COLUMN_COMMENT};
//            String where = DiaryContract.Ingredient._ID + " = " + params[0];
//            return db.query(DiaryContract.Ingredient.TABLE_NAME, columns, where, null, null, null, null);
//        }
//
//        @Override
//        protected void onPostExecute(final Cursor result) {
//            if (result.moveToFirst()) {
//                String name = result.getString(result.getColumnIndex(DiaryContract.Ingredient.COLUMN_NAME));
//                int irritantInt = result.getInt(result.getColumnIndex(DiaryContract.Ingredient.COLUMN_IRRITANT));
//                boolean isIrritant = irritantInt == 1; //boolean stored as int (1 == true, 0 == false
//                String comment = result.getString(result.getColumnIndex(DiaryContract.Ingredient.COLUMN_COMMENT));
//
//                mNameEditText.setText(name);
//                mInitialName = name;
//
//                mIrritantCheckbox.setChecked(isIrritant);
//                mInitialCheck = isIrritant;
//
//                mCommentEditText.setText(comment);
//                mInitialComment = comment;
//            }
//
//        }
//    }
}
