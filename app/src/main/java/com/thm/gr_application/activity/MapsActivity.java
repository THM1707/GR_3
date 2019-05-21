package com.thm.gr_application.activity;

import android.annotation.SuppressLint;
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
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
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
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
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
import com.thm.gr_application.model.ParkingData;
import com.thm.gr_application.retrofit.AppServiceClient;
import com.thm.gr_application.utils.Constants;
import com.thm.gr_application.utils.ImageUtils;
import com.thm.gr_application.utils.NumberUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.joda.time.DateTime;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, View.OnClickListener,
        Drawer.OnDrawerItemClickListener, GoogleMap.OnInfoWindowClickListener {

    private static final String TAG = "MapsActivity";
    private static final float DEFAULT_ZOOM = 15.0f;
    private static final LatLng mDefaultLatLng = new LatLng(21.0307162, 105.7756564);
    private static final int REQUEST_GPS_CODE = 101;
    private static final int SEARCH_ACTIVITY_CODE = 102;
    private boolean isLocationPermissionGranted;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Map<String, ParkingData> mIdParkingMap = new HashMap<>();
    private BiMap<Marker, Long> mMarkerIdMap = HashBiMap.create();
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private View mLocationButton;
    private Drawer mDrawer;
    private SupportMapFragment mMapFragment;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private Location mCurrentLocation;
    private Map<Integer, Long> mSearchResultMap = new HashMap<>();
    AutocompleteSupportFragment mAutocompleteFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        sendNotificationToken();
        setupDefaultLocation();
        initViews();
        initGoogleServices();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void sendNotificationToken() {
        String authToken = getSharedPreferences(Constants.SHARED_PREF_USER, MODE_PRIVATE).getString(
                Constants.SHARED_TOKEN, null);
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.d(TAG, "onComplete: ", task.getException());
            }
            if (task.getResult() != null) {
                String token = task.getResult().getToken();
                Log.d(TAG, token);
                Disposable disposable = AppServiceClient.getMyApiInstance(this)
                        .notificationRegistration(authToken, token)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {
                                Log.d(TAG, "onComplete: Completed");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Toast.makeText(MapsActivity.this, R.string.error_server,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                mCompositeDisposable.add(disposable);
            }
        });
    }

    private void setupFirebaseDatabase() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("p/parking");
        database.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ParkingData parkingData = dataSnapshot.getValue(ParkingData.class);
                String id = dataSnapshot.getKey();
                if (id != null && parkingData != null) {
                    mIdParkingMap.put(id, parkingData);
                    createMarker(parkingData);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String id = dataSnapshot.getKey();
                ParkingData parkingData = dataSnapshot.getValue(ParkingData.class);
                if (id != null && parkingData != null) {
                    mIdParkingMap.put(id, parkingData);
                    Marker marker = mMarkerIdMap.inverse().get(parkingData.getId());
                    if (marker != null) {
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(
                                ImageUtils.getParkingBitmapFromVectorDrawable(MapsActivity.this,
                                        parkingData.getAvailable() == 0 ? R.drawable.ic_marker
                                                : R.drawable.ic_marker_2)));
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String id = dataSnapshot.getKey();
                ParkingData parkingData = dataSnapshot.getValue(ParkingData.class);
                if (id != null && parkingData != null) {
                    mIdParkingMap.remove(id);
                    Marker marker = mMarkerIdMap.inverse().get(parkingData.getId());
                    if (marker != null) {
                        marker.remove();
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MapsActivity.this, R.string.error_unknow, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createMarker(ParkingData parkingData) {
        Marker m = mMap.addMarker(new MarkerOptions().position(
                new LatLng(parkingData.getLatitude(), parkingData.getLongitude()))
                .icon(BitmapDescriptorFactory.fromBitmap(
                        ImageUtils.getParkingBitmapFromVectorDrawable(this,
                                parkingData.getAvailable() == 0 ? R.drawable.ic_marker
                                        : R.drawable.ic_marker_2))));
        if (parkingData.getType() == 0) {
            m.setIcon(BitmapDescriptorFactory.fromBitmap(
                    ImageUtils.getParkingBitmapFromVectorDrawable(this, R.drawable.ic_marker_3)));
        }
        mMarkerIdMap.put(m, parkingData.getId());
    }

    private void setupDefaultLocation() {
        mCurrentLocation = new Location("");
        mCurrentLocation.setLatitude(mDefaultLatLng.latitude);
        mCurrentLocation.setLongitude(mDefaultLatLng.longitude);
    }

    private void initViews() {
        mAutocompleteFragment =
                (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(
                        R.id.fragment_autocomplete);
        List<Place.Field> placeField =
                Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
        mAutocompleteFragment.setPlaceFields(placeField);
        mAutocompleteFragment.setCountry("VN");
        mAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(MapsActivity.this, status.getStatusMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        });

        FloatingActionButton searchFab = findViewById(R.id.fab_search);
        FloatingActionButton locationFab = findViewById(R.id.fab_my_location);
        locationFab.setOnClickListener(this);
        locationFab.setColorFilter(Color.WHITE);
        searchFab.setOnClickListener(this);
        searchFab.setColorFilter(Color.WHITE);
        findViewById(R.id.bt_nearest).setOnClickListener(this);
        findViewById(R.id.bt_navigation_drawer).setOnClickListener(this);
        setupNavigationDrawer();
    }

    private void setupNavigationDrawer() {
        SharedPreferences sharedPreferences =
                getSharedPreferences(Constants.SHARED_PREF_USER, MODE_PRIVATE);
        String role = sharedPreferences.getString(Constants.SHARED_ROLE, null);
        String name = sharedPreferences.getString(Constants.SHARED_NAME, null);
        String email = sharedPreferences.getString(Constants.SHARED_EMAIL, null);
        int gender = sharedPreferences.getInt(Constants.SHARED_GENDER, 0);
        AccountHeader headerResult = new AccountHeaderBuilder().withActivity(this)
                .withHeaderBackground(R.drawable.background)
                .addProfiles(new ProfileDrawerItem().withName(name)
                        .withEmail(email)
                        .withIcon(getResources().getDrawable(
                                gender == 0 ? R.drawable.ic_male : R.drawable.ic_female)))
                .withOnAccountHeaderProfileImageListener(
                        new AccountHeader.OnAccountHeaderProfileImageListener() {
                            @Override
                            public boolean onProfileImageClick(View view, IProfile profile,
                                    boolean current) {
                                Intent intent =
                                        new Intent(MapsActivity.this, ProfileActivity.class);
                                startActivity(intent);
                                return false;
                            }

                            @Override
                            public boolean onProfileImageLongClick(View view, IProfile profile,
                                    boolean current) {
                                return false;
                            }
                        })
                .withSelectionListEnabledForSingleProfile(false)
                .build();
        PrimaryDrawerItem bookmarkItem =
                new PrimaryDrawerItem().withIdentifier(Constants.MAP_ITEM_BOOKMARK)
                        .withName("Bookmark")
                        .withIcon(R.drawable.ic_favorite_on);
        PrimaryDrawerItem carItem = new PrimaryDrawerItem().withIdentifier(Constants.MAP_ITEM_CAR)
                .withName("Car")
                .withIcon(R.drawable.ic_car);
        PrimaryDrawerItem pendingItem =
                new PrimaryDrawerItem().withIdentifier(Constants.MAP_ITEM_PENDING)
                        .withName("Pending Request")
                        .withIcon(R.drawable.ic_pending);
        PrimaryDrawerItem rechargeItem =
                new PrimaryDrawerItem().withIdentifier(Constants.MAP_ITEM_RECHARGE)
                        .withName("Recharge")
                        .withIcon(R.drawable.ic_wallet);
        PrimaryDrawerItem historyItem =
                new PrimaryDrawerItem().withIdentifier(Constants.MAP_ITEM_HISTORY)
                        .withName("History")
                        .withIcon(R.drawable.ic_history);
        SecondaryDrawerItem helpItem =
                new SecondaryDrawerItem().withIdentifier(Constants.MAP_ITEM_HELP).withName("Help");
        SecondaryDrawerItem settingItem =
                new SecondaryDrawerItem().withIdentifier(Constants.MAP_ITEM_SETTING)
                        .withName("Setting");
        SecondaryDrawerItem managerItem =
                new SecondaryDrawerItem().withIdentifier(Constants.MAP_ITEM_MANAGER)
                        .withName("Manager");
        mDrawer = new DrawerBuilder().withAccountHeader(headerResult)
                .withActivity(this)
                .withSelectedItem(-1)
                .addDrawerItems(bookmarkItem, carItem, pendingItem, rechargeItem, historyItem,
                        new DividerDrawerItem(), helpItem, settingItem)
                .withOnDrawerItemClickListener(this)
                .build();
        findViewById(R.id.bt_navigation_drawer).setOnClickListener(this);
        if (role != null && role.equals(getString(R.string.role_manager))) {
            mDrawer.addItem(managerItem);
        }
    }

    private void initGoogleServices() {
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mMapFragment != null) {
            mMapFragment.getMapAsync(this);
        }

        if (!Places.isInitialized()) {
            Places.initialize(this, getString(R.string.google_maps_key));
        }
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
                    LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(),
                            mCurrentLocation.getLongitude());
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentLatLng, 15.0f);
                    mMap.animateCamera(update);
                }
            }
        };
        settingMap();
        checkLocationPermission();
        setupFirebaseDatabase();
    }

    private void initLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(300000);
        mLocationRequest.setFastestInterval(300000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    private void settingMap() {
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.setInfoWindowAdapter(new DetailsInfoAdapter());
        mMap.setOnInfoWindowClickListener(this);
        // Set map's day and night style
        try {
            DateTime current = new DateTime();
            int hour = current.getHourOfDay();
            boolean success;
            if ((hour >= 18 && hour < 24) || (hour >= 0 && hour < 6)) {
                success = mMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(this, R.raw.dark_style));
            } else {
                success = mMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(this, R.raw.normal_style));
            }

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
        mMap.setOnMarkerClickListener(this);
    }

    private void checkGps() {
        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
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
                            resolvable.startResolutionForResult(MapsActivity.this,
                                    REQUEST_GPS_CODE);
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
                break;
            case R.id.fab_search:
                if (!mSearchResultMap.isEmpty()) {
                    int size = mSearchResultMap.size();
                    for (int i = 1; i <= size; i++) {
                        Marker marker = mMarkerIdMap.inverse().get(mSearchResultMap.get(i));
                        ParkingData parkingData =
                                mIdParkingMap.get(String.valueOf(mSearchResultMap.get(i)));
                        if (marker != null && parkingData != null) {
                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(
                                    ImageUtils.getParkingBitmapFromVectorDrawable(MapsActivity.this,
                                            parkingData.getType() == 0 ? R.drawable.ic_marker_3
                                                    : R.drawable.ic_marker_2)));
                        }
                    }
                    mSearchResultMap.clear();
                }
                startActivityForResult(new Intent(MapsActivity.this, SearchActivity.class),
                        SEARCH_ACTIVITY_CODE);
                break;
            case R.id.bt_nearest:
                if (mIdParkingMap.isEmpty()) {
                    Toast.makeText(this, R.string.message_no_parking_lot, Toast.LENGTH_SHORT)
                            .show();
                } else {
                    String nearestId = "";
                    float nearestDistance = Float.MAX_VALUE;
                    Location nearestLocation = new Location("");
                    for (Map.Entry<String, ParkingData> data : mIdParkingMap.entrySet()) {
                        Location markerLocation = new Location("");
                        markerLocation.setLatitude(data.getValue().getLatitude());
                        markerLocation.setLongitude(data.getValue().getLongitude());
                        float distance = mCurrentLocation.distanceTo(markerLocation);
                        if (distance < nearestDistance) {
                            nearestId = data.getKey();
                            nearestDistance = distance;
                            nearestLocation = markerLocation;
                        }
                    }
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(
                            new LatLng(nearestLocation.getLatitude(),
                                    nearestLocation.getLongitude())));
                    Marker marker = mMarkerIdMap.inverse().get(Long.valueOf(nearestId));
                    if (marker != null) {
                        marker.showInfoWindow();
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GPS_CODE:
                switch (resultCode) {
                    case Activity.RESULT_CANCELED:
                        showDefaultLocation();
                        break;
                    case Activity.RESULT_OK:
                        requestMyLocation();
                        break;
                    default:
                        break;
                }
                break;
            case SEARCH_ACTIVITY_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    mSearchResultMap = (Map<Integer, Long>) data.getSerializableExtra(
                            Constants.EXTRA_SEARCH_RESULT);
                    double lat = data.getDoubleExtra(Constants.EXTRA_LATITUDE, 0);
                    double lng = data.getDoubleExtra(Constants.EXTRA_LONGITUDE, 0);
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
                    int size = mSearchResultMap.size();
                    for (int i = 1; i <= size; i++) {
                        Marker marker = mMarkerIdMap.inverse().get(mSearchResultMap.get(i));
                        if (marker != null) {
                            int imageSource = -1;
                            switch (i) {
                                case 1:
                                    imageSource = R.drawable.ic_number_1;
                                    break;
                                case 2:
                                    imageSource = R.drawable.ic_number_2;
                                    break;
                                case 3:
                                    imageSource = R.drawable.ic_number_3;
                                    break;
                            }
                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(
                                    ImageUtils.getParkingBitmapFromVectorDrawable(MapsActivity.this,
                                            imageSource)));
                        }
                    }
                }
                break;
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        marker.hideInfoWindow();
        Location markerLocation = new Location("");
        markerLocation.setLatitude(marker.getPosition().latitude);
        markerLocation.setLongitude(marker.getPosition().longitude);
        float distance = mCurrentLocation.distanceTo(markerLocation);
        Long id = mMarkerIdMap.get(marker);
        Intent intent = new Intent(this, ParkingInfoActivity.class);
        intent.putExtra(Constants.EXTRA_PARKING_LOT, id);
        intent.putExtra(Constants.EXTRA_DISTANCE, distance);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            isLocationPermissionGranted =
                    grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (isLocationPermissionGranted) {
                checkGps();
            } else {
                showDefaultLocation();
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
            finish();
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        mCompositeDisposable.clear();
        super.onStop();
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
                new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION },
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
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLatLng, DEFAULT_ZOOM));
    }

    private void requestMyLocation() {
        try {
            if (isLocationPermissionGranted) {
                mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest,
                        mLocationCallback, Looper.myLooper());
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        long id = drawerItem.getIdentifier();
        if (id == Constants.MAP_ITEM_BOOKMARK) {
            Intent intent = new Intent(MapsActivity.this, BookmarkActivity.class);
            Log.d(TAG, "onItemClick: "
                    + mCurrentLocation.getLatitude()
                    + ", "
                    + mCurrentLocation.getLongitude());
            intent.putExtra(Constants.EXTRA_LATITUDE, mCurrentLocation.getLatitude());
            intent.putExtra(Constants.EXTRA_LONGITUDE, mCurrentLocation.getLongitude());
            startActivity(intent);
        } else if (id == Constants.MAP_ITEM_CAR) {
            Intent intent = new Intent(MapsActivity.this, CarActivity.class);
            startActivity(intent);
        } else if (id == Constants.MAP_ITEM_HELP) {
            // TODO: 07/04/2019
            showHelp();
        } else if (id == Constants.MAP_ITEM_MANAGER) {
            finish();
        } else if (id == Constants.MAP_ITEM_PENDING) {
            startActivity(new Intent(MapsActivity.this, PendingActivity.class));
        } else if (id == Constants.MAP_ITEM_RECHARGE) {
            startActivity(new Intent(MapsActivity.this, RechargeActivity.class));
        } else if (id == Constants.MAP_ITEM_HISTORY) {
            startActivity(new Intent(MapsActivity.this, HistoryActivity.class));
        } else if (id == Constants.MAP_ITEM_SETTING) {
            // TODO: 14/05/2019  
        }

        mDrawer.closeDrawer();
        return false;
    }

    private void showHelp() {
    }

    class DetailsInfoAdapter implements GoogleMap.InfoWindowAdapter {

        private View mWindow;

        @SuppressLint("InflateParams")
        DetailsInfoAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.info_window, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            TextView addressText = mWindow.findViewById(R.id.tv_address);
            TextView availableText = mWindow.findViewById(R.id.tv_available);
            TextView reviewText = mWindow.findViewById(R.id.tv_review);
            TextView priceText = mWindow.findViewById(R.id.tv_price);
            Long id = mMarkerIdMap.get(marker);
            ParkingData parkingData = mIdParkingMap.get(String.valueOf(id));
            if (parkingData != null) {
                addressText.setText(parkingData.getName());
                if (parkingData.getType() == 0) {
                    availableText.setText(String.format(Locale.getDefault(), "Capacity: %d slots",
                            parkingData.getAvailable()));
                } else {
                    availableText.setText(String.format(Locale.getDefault(), "Available: %d slots",
                            parkingData.getAvailable()));
                }
                reviewText.setText(parkingData.getStar() == 0f ? "NA"
                        : String.format(Locale.getDefault(), "Review: %.1f",
                                parkingData.getStar()));
                String priceString = "Price: " + NumberUtils.getPriceNumber(parkingData.getPrice());
                priceText.setText(priceString);
            }
            return mWindow;
        }
    }
}
