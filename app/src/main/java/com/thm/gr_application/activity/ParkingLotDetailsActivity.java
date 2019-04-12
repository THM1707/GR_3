package com.thm.gr_application.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.bumptech.glide.Glide;
import com.thm.gr_application.R;
import com.thm.gr_application.data.CarDatabase;
import com.thm.gr_application.model.Car;
import com.thm.gr_application.model.ParkingLot;
import com.thm.gr_application.payload.InvoiceResponse;
import com.thm.gr_application.payload.ParkingLotResponse;
import com.thm.gr_application.retrofit.AppServiceClient;
import com.thm.gr_application.utils.Constants;
import com.wang.avi.AVLoadingIndicatorView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.json.JSONObject;
import retrofit2.HttpException;

public class ParkingLotDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean isFavorite;
    private ImageButton mImageButton;
    private String mToken;
    private ParkingLot mParkingLot;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private List<Car> mCarList = new ArrayList<>();
    private float mDistance;
    private AVLoadingIndicatorView mProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        setupVariables();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initCarList();
    }

    private void initCarList() {
        CarDatabase carDatabase = CarDatabase.getDatabase(this);
        Disposable disposable = carDatabase.getCarDao()
                .getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<List<Car>>() {
                    @Override
                    public void onSuccess(List<Car> cars) {
                        mCarList = cars;
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(ParkingLotDetailsActivity.this, e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
        mCompositeDisposable.add(disposable);
    }

    private void setupVariables() {
        Intent intent = getIntent();
        Long parkingLotId = intent.getLongExtra(Constants.EXTRA_PARKING_LOT, -1);
        mDistance = intent.getFloatExtra(Constants.EXTRA_DISTANCE, 0f);
        SharedPreferences sharedPreferences =
                getSharedPreferences(Constants.SHARED_PREF_USER, MODE_PRIVATE);
        mToken = sharedPreferences.getString(Constants.SHARED_TOKEN, null);
        Disposable disposable = AppServiceClient.getMyApiInstance(this)
                .getParkingLotById(mToken, parkingLotId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ParkingLotResponse>() {
                    @Override
                    public void onSuccess(ParkingLotResponse parkingLotResponse) {
                        if (parkingLotResponse.getMessage().equals("Is Favorite")) {
                            isFavorite = true;
                        } else if (parkingLotResponse.getMessage().equals("Not Favorite")) {
                            isFavorite = false;
                        }
                        mParkingLot = parkingLotResponse.getData();
                        initViews();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(ParkingLotDetailsActivity.this, R.string.error_server,
                                Toast.LENGTH_SHORT).show();
                    }
                });
        mCompositeDisposable.add(disposable);
    }

    private void initViews() {
        mProgressView = findViewById(R.id.progress_details);
        TextView textCapacity = findViewById(R.id.tv_capacity);
        TextView textAddress = findViewById(R.id.tv_address);
        TextView textActiveTime = findViewById(R.id.tv_active_time);
        TextView textDistance = findViewById(R.id.tv_distance);
        TextView textAvailable = findViewById(R.id.tv_available);
        ImageView imageView = findViewById(R.id.iv_detail);
        Toolbar toolbar = findViewById(R.id.toolbar_details);
        ConstraintLayout layout = findViewById(R.id.cl_details);
        TextView textStar = findViewById(R.id.tv_star);
        layout.bringChildToFront(toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mParkingLot.getName());
        }
        findViewById(R.id.bt_navigate).setOnClickListener(this);
        findViewById(R.id.bt_booking).setOnClickListener(this);
        if (mParkingLot.getStar() != 0f) {
            textStar.setText(String.format(Locale.getDefault(), "%.1f", mParkingLot.getStar()));
        }
        textStar.setOnClickListener(this);
        textAvailable.setText(mParkingLot.getCapacity() - mParkingLot.getCurrent() + " left");
        textCapacity.setText(String.valueOf(mParkingLot.getCapacity()));
        textAddress.setText(mParkingLot.getAddress());
        textActiveTime.setText(getActiveTimeText());
        textDistance.setText(getDistanceText());
        if (mParkingLot.getImage() != null) {
            Glide.with(this)
                    .load(Constants.END_POINT_URL + "/api/image/" + mParkingLot.getImage().getId())
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.parking_lot);
        }
        mImageButton = findViewById(R.id.ib_favorite);
        mImageButton.setImageResource(
                isFavorite ? R.drawable.ic_favorite_on : R.drawable.ic_favorite_off);
        mImageButton.setOnClickListener(v -> {
            if (isFavorite) {
                Disposable disposable = AppServiceClient.getMyApiInstance(this)
                        .removeFavorite(mToken, mParkingLot.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {
                                mImageButton.setImageResource(R.drawable.ic_favorite_off);
                            }

                            @Override
                            public void onError(Throwable e) {
                                Toast.makeText(ParkingLotDetailsActivity.this,
                                        R.string.error_server, Toast.LENGTH_SHORT).show();
                            }
                        });
                mCompositeDisposable.add(disposable);
            } else {
                Disposable disposable = AppServiceClient.getMyApiInstance(this)
                        .addFavorite(mToken, mParkingLot.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {
                                mImageButton.setImageResource(R.drawable.ic_favorite_on);
                            }

                            @Override
                            public void onError(Throwable e) {
                                Toast.makeText(ParkingLotDetailsActivity.this,
                                        R.string.error_server, Toast.LENGTH_SHORT).show();
                            }
                        });
                mCompositeDisposable.add(disposable);
            }
        });
    }

    private String getDistanceText() {
        if (mDistance < 1000) {
            return String.format(Locale.getDefault(), "%.0f m", mDistance);
        }
        return String.format(Locale.getDefault(), "%.1f km", mDistance / 1000);
    }

    private String getActiveTimeText() {
        return mParkingLot.getOpenTime() + " ~ " + mParkingLot.getCloseTime();
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

    @Override
    protected void onStop() {
        mCompositeDisposable.clear();
        super.onStop();
    }

    public void navigate() {
        String uriString = String.format(Locale.getDefault(), "google.navigation:q=%f,%f&mode=d",
                mParkingLot.getLatitude(), mParkingLot.getLongitude());
        Uri uri = Uri.parse(uriString);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(this, "Lame", Toast.LENGTH_SHORT).show();
        }
    }

    public void book() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (mCarList.isEmpty()) {
            // Show dialog to go to Car Activity to add car
            builder.setMessage(R.string.message_no_car)
                    .setPositiveButton(R.string.action_ok,
                            (dialog, which) -> ParkingLotDetailsActivity.this.startActivity(
                                    new Intent(ParkingLotDetailsActivity.this, CarActivity.class)))
                    .create()
                    .show();
        } else {
            // Make a new request
            List<String> plateList = new ArrayList<>();
            for (Car c : mCarList) {
                plateList.add(c.getLicensePlate());
            }
            String[] plateArray = plateList.toArray(new String[0]);
            View layout = getLayoutInflater().inflate(R.layout.dialog_booking, null);
            Spinner carSpinner = layout.findViewById(R.id.spinner_plate);
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                            plateArray);
            carSpinner.setAdapter(adapter);
            builder.setView(layout);
            AlertDialog dialog = builder.create();
            layout.findViewById(R.id.bt_booking).setOnClickListener(v -> {
                mProgressView.show();
                Disposable disposable = AppServiceClient.getMyApiInstance(this)
                        .requestBooking(mToken, mParkingLot.getId(),
                                carSpinner.getSelectedItem().toString())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<InvoiceResponse>() {
                            @Override
                            public void onSuccess(InvoiceResponse invoiceResponse) {
                                mProgressView.hide();
                                Toast.makeText(ParkingLotDetailsActivity.this,
                                        R.string.message_booking_success, Toast.LENGTH_SHORT)
                                        .show();
                            }

                            @Override
                            public void onError(Throwable e) {
                                mProgressView.hide();
                                if (e instanceof HttpException) {
                                    try {
                                        JSONObject jObjError = new JSONObject(
                                                ((HttpException) e).response()
                                                        .errorBody()
                                                        .string());
                                        switch (jObjError.getString("message")) {
                                            case Constants.BOOKING_RESULT_PENDING:
                                                Toast.makeText(ParkingLotDetailsActivity.this,
                                                        R.string.message_already_booking,
                                                        Toast.LENGTH_SHORT).show();
                                                break;
                                            case Constants.BOOKING_RESULT_EXIST:
                                                Toast.makeText(ParkingLotDetailsActivity.this,
                                                        R.string.message_plate_already_exist,
                                                        Toast.LENGTH_SHORT).show();
                                                break;
                                            case Constants.BOOKING_RESULT_FULL:
                                                Toast.makeText(ParkingLotDetailsActivity.this,
                                                        R.string.message_full_slot,
                                                        Toast.LENGTH_SHORT).show();
                                                break;
                                        }
                                    } catch (Exception ex) {
                                        Toast.makeText(ParkingLotDetailsActivity.this,
                                                ex.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(ParkingLotDetailsActivity.this,
                                            R.string.error_server, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                mCompositeDisposable.add(disposable);
                dialog.dismiss();
            });
            dialog.show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_booking:
                book();
                break;
            case R.id.bt_navigate:
                navigate();
                break;
            case R.id.tv_star:
                startReviewActivity();
                break;
            default:
                break;
        }
    }

    private void startReviewActivity() {
        Intent intent = new Intent(this, ReviewActivity.class);
        intent.putExtra(Constants.EXTRA_PARKING_LOT, mParkingLot.getId());
        intent.putExtra(Constants.EXTRA_PARKING_LOT_NAME, mParkingLot.getName());
        startActivity(intent);
    }
}
