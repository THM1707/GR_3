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
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
import com.thm.gr_application.utils.Constants;
import com.thm.gr_application.utils.ImageUtils;
import io.reactivex.disposables.CompositeDisposable;
import java.util.Collections;
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
    private static final int AUTOCOMPLETE_REQUEST_CODE = 102;
    private static final int REQUEST_GPS_CODE = 101;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setupDefaultLocation();
        setupFirebaseDatabase();
        initViews();
        initGoogleServices();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void setupFirebaseDatabase() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("parking");
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

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
        mMarkerIdMap.put(m, parkingData.getId());
    }

    private void setupDefaultLocation() {
        mCurrentLocation = new Location("");
        mCurrentLocation.setLatitude(mDefaultLatLng.latitude);
        mCurrentLocation.setLongitude(mDefaultLatLng.longitude);
    }

    private void initViews() {
        FloatingActionButton floatingActionButton = findViewById(R.id.fab_my_location);
        floatingActionButton.setOnClickListener(this);
        floatingActionButton.setColorFilter(Color.WHITE);
        findViewById(R.id.bt_navigation_drawer).setOnClickListener(this);
        findViewById(R.id.tv_place_search).setOnClickListener(this);
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
        SecondaryDrawerItem helpItem =
                new SecondaryDrawerItem().withIdentifier(Constants.MAP_ITEM_HELP).withName("Help");
        SecondaryDrawerItem managerItem =
                new SecondaryDrawerItem().withIdentifier(Constants.MAP_ITEM_MANAGER)
                        .withName("Manager");
        mDrawer = new DrawerBuilder().withAccountHeader(headerResult)
                .withActivity(this)
                .withSelectedItem(-1)
                .addDrawerItems(bookmarkItem, carItem, pendingItem, new DividerDrawerItem(),
                        helpItem)
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
            Places.initialize(getApplicationContext(),
                    getResources().getString(R.string.google_maps_key));
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
            case R.id.tv_place_search:
                // Set the fields to specify which types of place data to return.
                List<Place.Field> fields = Collections.singletonList(Place.Field.LAT_LNG);
                // Start the autocomplete intent.
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN,
                        fields).setCountry("VN").build(this);
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
            case AUTOCOMPLETE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Place place = Autocomplete.getPlaceFromIntent(data);
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                    Toast.makeText(this, R.string.error_place_api, Toast.LENGTH_SHORT).show();
                } else if (resultCode == AutocompleteActivity.RESULT_CANCELED) {
                    mMap.animateCamera(
                            CameraUpdateFactory.newLatLng(new LatLng(20.9984926, 105.7943954)));
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
        Intent intent = new Intent(this, ParkingLotDetailsActivity.class);
        intent.putExtra(Constants.EXTRA_PARKING_LOT, id);
        intent.putExtra(Constants.EXTRA_DISTANCE, distance);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
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
        }

        mDrawer.closeDrawer();
        return false;
    }

    private void showHelp() {
    }

    class DetailsInfoAdapter implements GoogleMap.InfoWindowAdapter {

        private View mWindow;

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
            Long id = mMarkerIdMap.get(marker);
            ParkingData parkingData = mIdParkingMap.get(String.valueOf(id));
            if (parkingData != null) {
                addressText.setText(parkingData.getName());
                availableText.setText(String.format(Locale.getDefault(), "Available: %d slots",
                        parkingData.getAvailable()));
                reviewText.setText(parkingData.getStar() == 0f ? "NA"
                        : String.format(Locale.getDefault(), "Review: %.1f",
                                parkingData.getStar()));
            }
            return mWindow;
        }
    }
}
