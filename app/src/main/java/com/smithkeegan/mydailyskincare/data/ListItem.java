package com.smithkeegan.mydailyskincare.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an item in a list.
 * Created by keegansmith on 7/12/17.
 */

public class ListItem {

    String name;
    ArrayList<String> extras;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addExtra(String extra){
        extras.add(extra);
    }

    public ArrayList<String> getExtras() {
        return extras;
    }

    public void setExtras(ArrayList<String> extras) {
        this.extras = extras;
    }
}
