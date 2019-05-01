package com.thm.gr_application.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.thm.gr_application.R;
import com.thm.gr_application.payload.SearchRequest;
import com.thm.gr_application.payload.SearchResponse;
import com.thm.gr_application.retrofit.AppServiceClient;
import com.thm.gr_application.utils.Constants;
import com.xw.repo.BubbleSeekBar;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "AppCompatActivity";
    PlacesClient mPlacesClient;
    List<Place.Field> placeField =
            Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
    private BubbleSeekBar mPriceSeek;
    private BubbleSeekBar mDistanceSeek;
    private BubbleSeekBar mTimeSeek;
    private RadioButton mPriceButton;
    private double mLatitude = 0;
    private double mLongitude = 0;
    private Map<Integer, Long> mResultMap = new HashMap<>();
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initViews();
        initPlaces();
        setupAutocomplete();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar_search);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Smart search");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mPriceSeek = findViewById(R.id.sb_price);
        mDistanceSeek = findViewById(R.id.sb_distance);
        mTimeSeek = findViewById(R.id.sb_remain);
        mPriceButton = findViewById(R.id.rb_price);
        findViewById(R.id.bt_search).setOnClickListener(this);
    }

    private void setupAutocomplete() {
        AutocompleteSupportFragment fragment =
                (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(
                        R.id.fragment_autocomplete);
        if (fragment != null) {
            fragment.setPlaceFields(placeField);
            fragment.setCountry("VN");
            fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    if (place.getLatLng() != null) {
                        mLatitude = place.getLatLng().latitude;
                        mLongitude = place.getLatLng().longitude;
                    }
                }

                @Override
                public void onError(@NonNull Status status) {
                    Toast.makeText(SearchActivity.this, status.getStatusMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void initPlaces() {
        Places.initialize(this, getString(R.string.google_maps_key));
        mPlacesClient = Places.createClient(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_search) {
            if (mLatitude == 0 || mLongitude == 0) {
                Toast.makeText(this, R.string.message_select_destination, Toast.LENGTH_SHORT)
                        .show();
            } else {
                float distance = mDistanceSeek.getProgressFloat();
                int budget = mPriceSeek.getProgress();
                int remain = mTimeSeek.getProgress();
                int option = mPriceButton.isChecked() ? 1 : 0;
                SearchRequest request =
                        new SearchRequest(mLatitude, mLongitude, distance, remain, budget, option);
                String token =
                        getSharedPreferences(Constants.SHARED_PREF_USER, MODE_PRIVATE).getString(
                                Constants.SHARED_TOKEN, null);
                Disposable disposable = AppServiceClient.getMyApiInstance(SearchActivity.this)
                        .getSearchResult(token, request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<SearchResponse>() {
                            @Override
                            public void onSuccess(SearchResponse searchResponse) {
                                mResultMap = searchResponse.getData();
                                if (mResultMap.isEmpty()) {
                                    Toast.makeText(SearchActivity.this, R.string.message_no_fit,
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Intent returnIntent = new Intent();
                                    returnIntent.putExtra(Constants.EXTRA_SEARCH_RESULT,
                                            (Serializable) mResultMap);
                                    returnIntent.putExtra(Constants.EXTRA_LATITUDE, mLatitude);
                                    returnIntent.putExtra(Constants.EXTRA_LONGITUDE, mLongitude);
                                    setResult(Activity.RESULT_OK, returnIntent);
                                    finish();
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                Toast.makeText(SearchActivity.this, R.string.error_server,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                mCompositeDisposable.add(disposable);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCompositeDisposable.clear();
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }
}
