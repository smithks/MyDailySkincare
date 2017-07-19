package com.smithkeegan.mydailyskincare.core;

import com.smithkeegan.mydailyskincare.core.model.Ingredient;

/**
 * Created by keegansmith on 7/19/17.
 */

public class IngredientDetailViewModel {

    Ingredient initialIngredient;
    DetailView ingredientView;

    public IngredientDetailViewModel(DetailView view){
        ingredientView = view;
    }

    public void setInitialIngredient(Ingredient initialIngredient){
        this.initialIngredient = initialIngredient;
        ingredientView.updateView(initialIngredient);
    }

    public void onBackPressed(Ingredient currentIngredient){
        if (dataChanged(initialIngredient,currentIngredient)){
            if (initialIngredient.getId() == -1){ //New initialIngredient, ask if user wants to save
                ingredientView.displaySaveAlert();
            }
        }else{
            ingredientView.finish();
        }
    }

    public boolean dataChanged(Ingredient original, Ingredient current){
        return !original.getName().equals(current.getName())
                || !original.getComment().equals(current.getComment())
                || !original.isIrritant()==current.isIrritant();
    }

    public void saveIngredient(Ingredient ingredient){

    }

}
