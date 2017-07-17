package com.smithkeegan.mydailyskincare.ui.ingredient;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.smithkeegan.mydailyskincare.R;
import com.smithkeegan.mydailyskincare.data.DiaryContract;
import com.smithkeegan.mydailyskincare.data.ListItem;
import com.smithkeegan.mydailyskincare.model.ItemListViewModel;

import java.util.List;
import java.util.zip.Inflater;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Fragment class of the ingredient main screen. Contains the users ingredients displayed in a list view.
 * @author Keegan Smith
 * @since 5/10/2016
 */
public class IngredientFragmentMain extends Fragment {

    ItemListViewModel viewModel;
    private RecyclerView mIngredientsList;
    private TextView mNoIngredientsTextView;
    private Button mNewIngredientButton;

    Disposable disposable;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ingredient_main,container,false);

        setMemberVariables(rootView);
        setButtonListener();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        populateIngredientList();

    }

    private void setMemberVariables(View rootView){
        mIngredientsList = (RecyclerView) rootView.findViewById(R.id.ingredient_main_recycler_view);
        mNoIngredientsTextView = (TextView) rootView.findViewById(R.id.ingredient_main_no_ingredients_text);
        mNewIngredientButton = (Button) rootView.findViewById(R.id.ingredient_main_new_button);
        viewModel = new ItemListViewModel(getActivity().getApplicationContext());

        mIngredientsList.setHasFixedSize(true);
        mIngredientsList.setLayoutManager(new LinearLayoutManager(getContext()));
        DividerItemDecoration itemDecoration = new DividerItemDecoration(mIngredientsList.getContext(), new LinearLayoutManager(getContext()).getOrientation());
        mIngredientsList.addItemDecoration(itemDecoration);
    }

    private void populateIngredientList(){
        disposable = viewModel.getItemObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<ListItem>>() {
            @Override
            public void accept(@NonNull List<ListItem> list) throws Exception {
                displayItems(list);
            }
        });
        viewModel.requestList(ItemListViewModel.RequestType.INGREDIENTS);
    }

    private void displayItems(List<ListItem> items){
        ItemListRecyclerAdapter adapter = new ItemListRecyclerAdapter(items);
        mIngredientsList.setAdapter(adapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!disposable.isDisposed()){
            disposable.dispose();
        }
    }

    public class ItemListRecyclerAdapter extends RecyclerView.Adapter<IngredientViewHolder> {
        List<ListItem> data;

        public ItemListRecyclerAdapter(List<ListItem> data){
            this.data = data;
        }

        @Override
        public IngredientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView view = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_item_ingredient_main,parent,false);
            return new IngredientViewHolder(view);
        }

        @Override
        public void onBindViewHolder(IngredientViewHolder holder, final int position) {
            holder.itemName.setText(data.get(position).getExtras().get(DiaryContract.Ingredient.COLUMN_NAME));
            holder.itemName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), IngredientActivityDetail.class);
                    intent.putExtra(IngredientActivityDetail.NEW_INGREDIENT,false);
                    intent.putExtra(IngredientActivityDetail.ENTRY_ID,(long)data.get(position).getId());
                    startActivity(intent);
                }
            });

        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    public class IngredientViewHolder extends RecyclerView.ViewHolder{

        TextView itemName;

        public IngredientViewHolder(View itemView) {
            super(itemView);
            itemName = (TextView) itemView;
        }
    }

    /**
     * Sets listener for the new ingredient button.
     */
    public void setButtonListener(){
        mNewIngredientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),IngredientActivityDetail.class);
                intent.putExtra(IngredientActivityDetail.NEW_INGREDIENT,true);
                startActivityForResult(intent,IngredientActivityMain.INGREDIENT_FINISHED);
            }
        });
    }
}
