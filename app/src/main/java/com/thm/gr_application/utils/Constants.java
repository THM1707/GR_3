package com.thm.gr_application.utils;

public class Constants {
    public static final String END_POINT_URL = "http://192.168.1.228:8080";

    /*
    Permission
     */
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    /*
    Extra code
     */
    public static final String EXTRA_TOKEN = "extra_token";
    public static final String EXTRA_PARKING_LOT = "parking_lot";
    public static final String EXTRA_FAVORITE = "extra_favorite";
    public static final String EXTRA_PROPERTY = "extra_property";
    public static final String EXTRA_IS_FAVORITE = "extra_is_favorite";
    /*
    Shared preferences related
     */
    public static final String SHARED_PREF_USER = "shared_pref_user";
    public static final String KEY_USERNAME = "shared_username";
    public static final String KEY_PASSWORD = "shared_password";
    public static final String KEY_TOKEN = "shared_token";
    public static final String KEY_FAVORITE = "shared_pref_favorite";
    public static final String KEY_ROLE = "shared_pref_role";
    public static final String KEY_EMAIL = "shared_pref_email";

    /*
    Car type for picking
     */
    public static final int CAR_TYPE_CAR = 0;
    public static final int CAR_TYPE_TRUCK = 1;
    public static final int CAR_TYPE_BUS = 2;

    /*
    Navigation Drawer's item identifier
     */
    public static final long MAP_ITEM_BOOKMARK = 0;
    public static final long MAP_ITEM_CAR = 1;
    public static final long MAP_ITEM_HELP = 2;
    public static final long MAP_ITEM_MANAGER = 3;
}
