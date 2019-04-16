package com.thm.gr_application.utils;

public class Constants {
    public static final String END_POINT_URL = "http://192.168.1.141:8080";

    /*
    Permission
     */
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    /*
    Extra code
     */
    public static final String EXTRA_PARKING_LOT = "parking_lot";
    public static final String EXTRA_PROPERTY = "extra_property";
    public static final String EXTRA_DISTANCE = "extra_distance";
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";
    public static final String EXTRA_PARKING_LOT_NAME = "extra_parking_lot_name";

    /*
    Shared preferences related
     */
    public static final String SHARED_PREF_USER = "shared_pref_user";
    public static final String SHARED_NAME = "shared_name";
    public static final String SHARED_PASSWORD = "shared_password";
    public static final String SHARED_TOKEN = "shared_token";
    public static final String SHARED_ROLE = "shared_role";
    public static final String SHARED_EMAIL = "shared_email";
    public static final String SHARED_GENDER = "shared_gender";

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
    public static final long MANAGE_ITEM_HISTORY = 5;
    public static final long MANAGE_ITEM_STATISTIC = 6;
    public static final long MANAGE_ITEM_DETAILS = 7;


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
    public static final String BOOKING_RESULT_PENDING = "PENDING";
    public static final String BOOKING_RESULT_EXIST = "EXIST";
    public static final String BOOKING_RESULT_FULL = "FULL";
}
