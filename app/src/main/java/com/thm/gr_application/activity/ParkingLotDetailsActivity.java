package com.thm.gr_application.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thm.gr_application.R;
import com.thm.gr_application.model.ParkingLot;
import com.thm.gr_application.retrofit.AppServiceClient;
import com.thm.gr_application.utils.Constants;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParkingLotDetailsActivity extends AppCompatActivity {

    private boolean isFavorite;
    private ImageButton mImageButton;
    private String mToken;
    private ParkingLot mParkingLot;
    private List<Long> mFavorites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_lot_details);
        setupVariables();
        initViews(mParkingLot);

    }

    private void setupVariables() {
        Intent intent = getIntent();
        mParkingLot = (ParkingLot) intent.getSerializableExtra(Constants.EXTRA_PARKING_LOT);
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_USER, MODE_PRIVATE);
        mToken = sharedPreferences.getString(Constants.KEY_TOKEN, null);
        String json = sharedPreferences.getString(Constants.KEY_FAVORITE, null);
        mFavorites = new Gson().fromJson(json, new TypeToken<List<Long>>() {
        }.getType());
        isFavorite = mFavorites.contains(mParkingLot.getId());
    }

    private void initViews(ParkingLot parkingLot) {
        TextView textCapacity = findViewById(R.id.tv_capacity);
        TextView textAddress = findViewById(R.id.tv_address);
        Toolbar toolbar = findViewById(R.id.toolbar_details);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }
        mImageButton = findViewById(R.id.ib_favorite);
        mImageButton.setImageResource(isFavorite ? R.drawable.ic_favorite_on : R.drawable.ic_favorite_off);
        mImageButton.setOnClickListener(v -> {
            if (isFavorite) {
                AppServiceClient.getMyApiInstance(this).removeFavorite(mToken, parkingLot.getId()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {

                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {

                    }
                });
                mImageButton.setImageResource(R.drawable.ic_favorite_off);
            } else {
                AppServiceClient.getMyApiInstance(this).addFavorite(mToken, parkingLot.getId()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {

                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {

                    }
                });
                mImageButton.setImageResource(R.drawable.ic_favorite_on);
            }
            changeFavorite(mParkingLot, isFavorite);
            isFavorite = !isFavorite;
        });
        textCapacity.setText(String.valueOf(parkingLot.getCapacity()));
        textAddress.setText(parkingLot.getAddress());

    }

    private void changeFavorite(ParkingLot parkingLot, boolean b) {
        if (b) {
            mFavorites.remove(parkingLot.getId());
        } else {
            mFavorites.add(parkingLot.getId());
        }
        String json = new Gson().toJson(mFavorites);
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_USER, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.KEY_FAVORITE, json);
        editor.apply();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void nav(View view) {
        String uriString = String.format(Locale.getDefault(), "google.navigation:q=%f,%f", mParkingLot.getLatitude(), mParkingLot.getLongitude());
        Uri uri = Uri.parse(uriString);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }
}
