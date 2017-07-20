package com.smithkeegan.mydailyskincare.analytics;

/**
 * Created by keegansmith on 7/20/17.
 */

public class MDSAnalytics {

    //Diary Entry
    public static final String EVENT_DIARY_ENTRY_OPENED = "diary_entry_opened";
    public static final String EVENT_DIARY_ENTRY_SHOW_MORE = "diary_entry_show_more_used";
    public static final String EVENT_DIARY_ENTRY_SAVE_EXIT = "diary_entry_saved";
    public static final String VALUE_DIARY_ENTRY_ORIGIN_CALENDAR = "calendar_cell";
    public static final String VALUE_DIARY_ENTRY_ORIGIN_DRAWER = "nav_drawer";

    //Routine
    public static final String EVENT_ROUTINE_OPENED = "routine_opened";
    public static final String EVENT_ROUTINE_SAVE_EXIT = "routine_saved";
    public static final String VALUE_ROUTINE_ORIGIN_DIARY_ENTRY = "parent_diary_entry";
    public static final String VALUE_ROUTINE_ORIGIN_ROUTINE_LIST = "routine_list";

    //Product
    public static final String EVENT_PRODUCT_OPENED = "product_opened";
    public static final String EVENT_PRODUCT_SAVE_EXIT = "product_saved";
    public static final String VALUE_PRODUCT_ORIGIN_ROUTINE = "parent_routine";
    public static final String VALUE_PRODUCT_ORIGIN_PRODUCT_LIST = "product_list";

    //Ingredient
    public static final String EVENT_INGREDIENT_OPENED = "ingredient_opened";
    public static final String EVENT_INGREDIENT_SAVE_EXIT = "ingredient_saved";
    public static final String VALUE_INGREDIENT_ORIGIN_PRODUCT = "parent_product";
    public static final String VALUE_INGREDIENT_ORIGIN_INGREDIENT_LIST = "ingredient_list";

    //MDS Analytics
    public static final String EVENT_MDS_ANALYTICS_OPENED = "mds_analytics_opened";
    public static final String EVENT_MDS_ANALYTICS_USED = "mds_analytics_used";
    public static final String PARAM_MDS_ANALYTICS_QUERY = "query";

    //Calendar
    public static final String EVENT_SCROLL_TO_DATE_OPENED = "calendar_scroll_to_date_opened";
    public static final String EVENT_SCROLL_TO_DATE_USED = "calendar_scroll_to_date_used";

    public static final String PARAM_REQUEST_ORIGIN = "request_origin"; //Used in DIARY_ENTRY_OPENED, ROUTINE_OPENED, PRODUCT_OPENED, INGREDIENT_OPENED
    public static final String PARAM_DATE = "date"; //Used in SCROLL_TO_DATE_USED and DIARY_ENTRY_OPENED

    public static final String PARAM_EXIT_METHOD = "exit_method";
    public static final String VALUE_EXIT_BACK_PRESSED = "back_button";
    public static final String VALUE_EXIT_SAVE_BUTTON_PRESSED = "save_button";
}
