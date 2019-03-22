package com.thm.gr_application.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.thm.gr_application.R;
import com.thm.gr_application.model.ParkingLot;
import com.thm.gr_application.payload.ParkingLotsResponse;
import com.thm.gr_application.retrofit.AppServiceClient;
import com.thm.gr_application.utils.Constants;
import com.thm.gr_application.utils.ImageUtils;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, View.OnClickListener, Drawer.OnDrawerItemClickListener {

    private static final String TAG = "MapsActivity";
    private static final float DEFAULT_ZOOM = 15.0f;
    private static final LatLng mDefaultLocation = new LatLng(21.0307162, 105.7756564);
    private static final int AUTOCOMPLETE_REQUEST_CODE = 102;
    private boolean isLocationPermissionGranted;
    private boolean isMarkersReady = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private List<ParkingLot> mParkingLotList = new ArrayList<>();
    private Map<Marker, ParkingLot> mMarkerParkingLotMap = new HashMap<>();
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private View mLocationButton;
    private Drawer mDrawer;
    private SupportMapFragment mMapFragment;
    private List<Long> mFavorites;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private Location mCurrentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setupVariables();
        initViews();
        mapAndPlaceFragmentInit();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getExtrasFromIntent();
    }

    private void setupVariables() {
        mFavorites = getFavorites();
    }

    private void initViews() {
        findViewById(R.id.bt_navigation_drawer).setOnClickListener(this);
        findViewById(R.id.tv_place_search).setOnClickListener(this);
        setupNavigationDrawer();
    }

    private void setupNavigationDrawer() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_USER, MODE_PRIVATE);
        String role = sharedPreferences.getString(Constants.KEY_ROLE, null);
        String username = sharedPreferences.getString(Constants.KEY_USERNAME, null);
        String email = sharedPreferences.getString(Constants.KEY_EMAIL, null);
        PrimaryDrawerItem bookmarkItem =
                new PrimaryDrawerItem()
                        .withIdentifier(Constants.MAP_ITEM_BOOKMARK)
                        .withName("Bookmark")
                        .withIcon(R.drawable.ic_favorite_on);
        PrimaryDrawerItem carItem =
                new PrimaryDrawerItem()
                        .withIdentifier(Constants.MAP_ITEM_CAR)
                        .withName("Car")
                        .withIcon(R.drawable.ic_car);
        PrimaryDrawerItem pendingItem =
                new PrimaryDrawerItem()
                        .withIdentifier(Constants.MAP_ITEM_PENDING)
                        .withName("Pending Request")
                        .withIcon(R.drawable.ic_pending);
        SecondaryDrawerItem helpItem =
                new SecondaryDrawerItem()
                        .withIdentifier(Constants.MAP_ITEM_HELP)
                        .withName("Help");
        SecondaryDrawerItem managerItem =
                new SecondaryDrawerItem()
                        .withIdentifier(Constants.MAP_ITEM_MANAGER)
                        .withName("Manager");

        AccountHeader headerResult =
                new AccountHeaderBuilder()
                        .withActivity(this)
                        .withHeaderBackground(R.drawable.login_background)
                        .addProfiles(
                                new ProfileDrawerItem().withName(username).withEmail(email).withIcon(getResources().getDrawable(R.drawable.default_user))
                        )
                        .withOnAccountHeaderProfileImageListener(new AccountHeader.OnAccountHeaderProfileImageListener() {
                            @Override
                            public boolean onProfileImageClick(View view, IProfile profile, boolean current) {
                                Intent intent = new Intent(MapsActivity.this, AccountManagementActivity.class);
                                startActivity(intent);
                                return false;
                            }

                            @Override
                            public boolean onProfileImageLongClick(View view, IProfile profile, boolean current) {
                                return false;
                            }
                        })
                        .withSelectionListEnabledForSingleProfile(false)
                        .build();

        mDrawer = new DrawerBuilder()
                .withAccountHeader(headerResult)
                .withActivity(this)
                .withSelectedItem(-1)
                .addDrawerItems(
                        bookmarkItem,
                        carItem,
                        pendingItem,
                        new DividerDrawerItem(),
                        helpItem
                )
                .withOnDrawerItemClickListener(this)
                .build();
        findViewById(R.id.bt_navigation_drawer).setOnClickListener(this);
        if (role != null && role.equals(getString(R.string.role_manager))) {
            mDrawer.addItem(managerItem);
        }
    }

    private void mapAndPlaceFragmentInit() {
        mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mMapFragment != null) {
            mMapFragment.getMapAsync(this);
        }
        FloatingActionButton floatingActionButton = findViewById(R.id.fab_my_location);
        floatingActionButton.setOnClickListener(this);
        floatingActionButton.setColorFilter(Color.WHITE);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }
    }

    private void getExtrasFromIntent() {
        String token = getIntent().getStringExtra(Constants.EXTRA_TOKEN);
        getParkingLotList(token);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        initLocationRequest();
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                List<Location> locationList = locationResult.getLocations();
                if (locationList.size() > 0) {
                    mCurrentLocation = locationList.get(locationList.size() - 1);
                    LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentLatLng, 15.0f);
                    mMap.animateCamera(update);
                }
            }
        };
        settingMap();
        checkLocationPermission();
    }

    private void initLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(300000);
        mLocationRequest.setFastestInterval(300000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    private void checkGps() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnCompleteListener(task1 -> {
            try {
                task1.getResult(ApiException.class);
                // All location settings are satisfied. The client can initialize location
                // requests here.
                updateMyLocationUI();

            } catch (ApiException exception) {
                switch (exception.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the
                        // user a dialog.
                        try {
                            // Cast to a resolvable exception.
                            ResolvableApiException resolvable = (ResolvableApiException) exception;
                            // Show the dialog by calling startResolution   ForResult(),
                            // and check the result in onActivityResult().
                            resolvable.startResolutionForResult(MapsActivity.this, 101);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        } catch (ClassCastException e) {
                            // Ignore, should be an impossible error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }

        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_place_search:
                // Set the fields to specify which types of place data to return.
                List<Place.Field> fields = Collections.singletonList(Place.Field.LAT_LNG);
                // Start the autocomplete intent.
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN, fields)
                        .setCountry("VN")
                        .build(this);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
                break;
            case R.id.bt_navigation_drawer:
                if (!mDrawer.isDrawerOpen()) {
                    mDrawer.openDrawer();
                }
                break;
            case R.id.fab_my_location:
                if (mMap != null) {
                    if (isLocationPermissionGranted) {
                        mLocationButton.callOnClick();
                    } else {
                        requestLocationPermission();
                    }
                }
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 101:
                switch (resultCode) {
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        showDefaultLocation();
                        break;
                    case Activity.RESULT_OK:
                        requestMyLocation();
                        break;
                    default:
                        break;
                }
                break;
            case AUTOCOMPLETE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Place place = Autocomplete.getPlaceFromIntent(data);
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                    Toast.makeText(this, R.string.error_place_api, Toast.LENGTH_SHORT).show();
                } else if (resultCode == AutocompleteActivity.RESULT_CANCELED) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(20.9984926, 105.7943954)));
                }
                break;
        }
    }

    private void createMarkers() {
        for (ParkingLot p : mParkingLotList) {
            Marker m = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(p.getLatitude(), p.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromBitmap(ImageUtils.getParkingBitmapFromVectorDrawable(this)))
                    .title(p.getName()));
            mMarkerParkingLotMap.put(m, p);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Location markerLocation = new Location("");
        markerLocation.setLatitude(marker.getPosition().latitude);
        markerLocation.setLongitude(marker.getPosition().longitude);
        float distance = mCurrentLocation.distanceTo(markerLocation);
        ParkingLot p = mMarkerParkingLotMap.get(marker);
        Intent intent = new Intent(this, ParkingLotDetailsActivity.class);
        intent.putExtra(Constants.EXTRA_PARKING_LOT, p);
        intent.putExtra(Constants.EXTRA_DISTANCE, distance);
        startActivity(intent);
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                isLocationPermissionGranted = grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (isLocationPermissionGranted) {
                    checkGps();
                } else {
                    showDefaultLocation();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mFusedLocationProviderClient != null && mLocationCallback != null) {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        }
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen()) {
            mDrawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    private void getParkingLotList(String token) {
        Disposable disposable = AppServiceClient.getMyApiInstance(this).getParkingLots(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ParkingLotsResponse>() {
                    @Override
                    public void onSuccess(ParkingLotsResponse parkingLotsResponse) {
                        mParkingLotList = parkingLotsResponse.getData();
                        if (mMap != null) {
                            createMarkers();
                            isMarkersReady = true;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof HttpException) {
                            if (((HttpException) e).code() == 401) {
                                Toast.makeText(MapsActivity.this, R.string.error_session, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MapsActivity.this, R.string.error_server, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MapsActivity.this, R.string.error_server, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        mCompositeDisposable.add(disposable);
    }

    @Override
    protected void onStop() {
        mCompositeDisposable.clear();
        super.onStop();
    }

    private void settingMap() {
        mMap.getUiSettings().setMapToolbarEnabled(false);
        try {
            DateTime current = new DateTime();
            int hour = current.getHourOfDay();
            boolean success;
            if ((hour >= 18 && hour < 24) || (hour >= 0 && hour < 6)) {
                success = mMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(this, R.raw.dark_style));
            } else {
                success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.normal_style));
            }

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
        mMap.setOnMarkerClickListener(this);
        if (!isMarkersReady) {
            createMarkers();
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            isLocationPermissionGranted = true;
            mMap.setMyLocationEnabled(true);
            mLocationButton = mMapFragment.getView().findViewById(Integer.parseInt("2"));
            if (mLocationButton != null) {
                mLocationButton.setVisibility(View.GONE);
            }
            checkGps();
        } else {
            requestLocationPermission();

        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
    }

    private void updateMyLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            requestMyLocation();
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void showDefaultLocation() {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
    }

    private void requestMyLocation() {
        try {
            if (isLocationPermissionGranted) {
                mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private List<Long> getFavorites() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_USER, MODE_PRIVATE);
        String json = sharedPreferences.getString(Constants.KEY_FAVORITE, null);
        return new Gson().fromJson(json, new TypeToken<List<Long>>() {
        }.getType());
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        long id = drawerItem.getIdentifier();
        if (id == Constants.MAP_ITEM_BOOKMARK) {
            Intent intent = new
                    Intent(MapsActivity.this, BookmarkActivity.class);
            mFavorites = getFavorites();
            List<ParkingLot> favoriteList = new ArrayList<>();
            for (ParkingLot p : mParkingLotList) {
                if (mFavorites.contains(p.getId())) {
                    favoriteList.add(p);
                }
            }
            for (ParkingLot p : favoriteList) {
                Log.d(TAG, "onNavigationItemSelected: " + p.getId());
            }
            intent.putExtra(Constants.EXTRA_FAVORITE, (Serializable) favoriteList);
            startActivity(intent);
        } else if (id == Constants.MAP_ITEM_CAR) {
            Intent intent = new Intent(MapsActivity.this, CarActivity.class);
            startActivity(intent);
        } else if (id == Constants.MAP_ITEM_HELP) {

        } else if (id == Constants.MAP_ITEM_MANAGER) {
            finish();
        } else if (id == Constants.MAP_ITEM_PENDING) {
            startActivity(new Intent(MapsActivity.this, PendingActivity.class));
        }

        mDrawer.closeDrawer();
        return false;
    }
}
