package com.thm.gr_application.activity;

import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.thm.gr_application.R;
import com.thm.gr_application.model.ParkingLot;
import com.thm.gr_application.payload.ParkingLotResponse;
import com.thm.gr_application.retrofit.AppServiceClient;
import com.thm.gr_application.utils.Constants;
import com.thm.gr_application.utils.NumberUtils;
import com.wang.avi.AVLoadingIndicatorView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.Locale;

public class PropertyInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private ParkingLot mProperty;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private AVLoadingIndicatorView mDetailsProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_details);
        mDetailsProgress = findViewById(R.id.progress_details);
        getData();
    }

    private void getData() {
        String token = getSharedPreferences(Constants.SHARED_PREF_USER, MODE_PRIVATE).getString(
                Constants.SHARED_TOKEN, null);
        Long id = getIntent().getLongExtra(Constants.EXTRA_PARKING_LOT, -1);
        Disposable disposable = AppServiceClient.getMyApiInstance(this)
                .getParkingLotById(token, id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ParkingLotResponse>() {
                    @Override
                    public void onSuccess(ParkingLotResponse parkingLotResponse) {
                        mDetailsProgress.smoothToHide();
                        mProperty = parkingLotResponse.getData();
                        initViews();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mDetailsProgress.smoothToHide();
                        Toast.makeText(PropertyInfoActivity.this, R.string.error_server,
                                Toast.LENGTH_SHORT).show();
                    }
                });
        mCompositeDisposable.add(disposable);
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar_details);
        toolbar.bringToFront();
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        TextView addressText = findViewById(R.id.tv_address);
        TextView positionText = findViewById(R.id.tv_coordinates);
        TextView capacityText = findViewById(R.id.tv_capacity);
        TextView nameText = findViewById(R.id.tv_name);
        TextView activeTimeText = findViewById(R.id.tv_active_time);
        TextView starText = findViewById(R.id.tv_star);
        TextView priceText = findViewById(R.id.tv_price);
        if (mProperty.getStar() != 0f) {
            starText.setText(String.format(Locale.getDefault(), "%.1f", mProperty.getStar()));
        }
        starText.setOnClickListener(this);
        ImageView imageView = findViewById(R.id.iv_detail);
        if (mProperty.getImage() != null) {
            Glide.with(this)
                    .load(Constants.END_POINT_URL + "/api/image/" + mProperty.getImage().getId())
                    .apply(new RequestOptions().placeholder(R.drawable.no_image).fitCenter())
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.parking_lot);
        }
        addressText.setText(mProperty.getAddress());
        capacityText.setText(String.valueOf(mProperty.getCapacity()));
        String activeTime = mProperty.getOpenTime() + " ~ " + mProperty.getCloseTime();
        activeTimeText.setText(activeTime);
        String pos = mProperty.getLatitude() + ", " + mProperty.getLongitude();
        positionText.setText(pos);
        nameText.setText(mProperty.getName());
        priceText.setText(NumberUtils.getPriceNumber(mProperty.getPrice()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_star:
                Intent intent = new Intent(PropertyInfoActivity.this, ReviewActivity.class);
                intent.putExtra(Constants.EXTRA_PARKING_LOT, mProperty.getId());
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onStop() {
        mCompositeDisposable.clear();
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
