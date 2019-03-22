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
    public static final String EXTRA_DISTANCE = "extra_distance";

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
    public static final long MAP_ITEM_PENDING = 4;

    /*
    Invoice Status
     */
    public static final String STATUS_PENDING = "STATUS_PENDING";
    public static final String STATUS_ACTIVE = "STATUS_ACTIVE";
    public static final String STATUS_DONE = "STATUS_DONE";
    public static final String STATUS_CANCEL = "STATUS_CANCELED";

    /*
    Serializable for fragments arguments
     */
    public static final String BUNDLE_ALL_LIST = "ALL_LIST";
    public static final String BUNDLE_ACTIVE_LIST = "ACTIVE_LIST";
    public static final String BUNDLE_ENDED_LIST = "ENDED_LIST";
    public static final String BUNDLE_CAPACITY = "BUNDLE_CAPACITY";
    public static final String BUNDLE_CURRENT = "BUNDLE_CURRENT";

    /*
    Booking status
     */
    public static final String BOOKING_RESULT_OK = "OK";
    public static final String BOOKING_RESULT_CHANGED = "CHANGED";
    public static final String BOOKING_RESULT_PENDING = "RESULT_PENDING";
    public static final String BOOKING_RESULT_EXIST = "RESULT_EXIST";
    public static final String BOOKING_RESULT_COLLAPSE = "RESULT_COLLAPSE";
    public static final String BOOKING_RESULT_FULL = "RESULT_FULL";


}
