package com.thm.gr_application.retrofit;

import android.content.Context;

import com.thm.gr_application.utils.Constants;

public class AppServiceClient extends ServiceClient {
    private static GRApi mGRApiInstance;

    public static GRApi getMyApiInstance(Context context) {
        if (mGRApiInstance == null) {
            mGRApiInstance = createService(context, Constants.END_POINT_URL, GRApi.class);
        }
        return mGRApiInstance;
    }
}
