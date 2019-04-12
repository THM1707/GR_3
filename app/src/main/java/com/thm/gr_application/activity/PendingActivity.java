package com.thm.gr_application.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.Group;
import com.thm.gr_application.R;
import com.thm.gr_application.data.CarDatabase;
import com.thm.gr_application.model.Car;
import com.thm.gr_application.model.Invoice;
import com.thm.gr_application.model.ParkingLot;
import com.thm.gr_application.payload.InvoiceResponse;
import com.thm.gr_application.payload.MessageResponse;
import com.thm.gr_application.retrofit.AppServiceClient;
import com.thm.gr_application.utils.Constants;
import com.wang.avi.AVLoadingIndicatorView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.joda.time.DateTime;
import org.json.JSONObject;
import retrofit2.HttpException;

public class PendingActivity extends AppCompatActivity implements View.OnClickListener {
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private AVLoadingIndicatorView mHistoryProgress;
    private TextView mAddressText;
    private TextView mDateText;
    private TextView mPlateText;
    private TextView mEmptyText;
    private LinearLayout mEmptyLayout;
    private Invoice mInvoice;
    private ImageView mEmptyImage;
    private Group mGroup;
    private List<Car> mCarList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending);
        initViews();
        initCarList();
        getData();
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
                        Toast.makeText(PendingActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
        mCompositeDisposable.add(disposable);
    }

    private void getData() {
        String token = getSharedPreferences(Constants.SHARED_PREF_USER, MODE_PRIVATE).getString(
                Constants.SHARED_TOKEN, null);
        mHistoryProgress.show();
        Disposable disposable = AppServiceClient.getMyApiInstance(this)
                .getUserPending(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<InvoiceResponse>() {
                    @Override
                    public void onSuccess(InvoiceResponse invoiceResponse) {
                        mHistoryProgress.hide();
                        if (invoiceResponse.getMessage().equals("OK")) {
                            mGroup.setVisibility(View.VISIBLE);
                            mEmptyLayout.setVisibility(View.GONE);
                            mInvoice = invoiceResponse.getInvoice();
                            ParkingLot p = invoiceResponse.getParkingLot();
                            mAddressText.setText(p.getAddress());
                            DateTime date = new DateTime(mInvoice.getCreatedDate());
                            DateTime.Property pDoW = date.dayOfWeek();
                            String strTF = pDoW.getAsText(Locale.getDefault());
                            String dateString = strTF
                                    + " "
                                    + date.getDayOfMonth()
                                    + "-"
                                    + date.getMonthOfYear()
                                    + "-"
                                    + date.getYear();
                            mDateText.setText(dateString);
                            mPlateText.setText(mInvoice.getPlate());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mHistoryProgress.hide();
                        mEmptyText.setText(R.string.error_server);
                        mEmptyImage.setImageResource(R.drawable.ic_disconnected);
                        Toast.makeText(PendingActivity.this,
                                "There are something wrong with server", Toast.LENGTH_SHORT).show();
                    }
                });
        mCompositeDisposable.add(disposable);
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar_pending);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Pending booking");
        }
        mGroup = findViewById(R.id.group_invoice);
        mEmptyText = findViewById(R.id.tv_empty);
        mHistoryProgress = findViewById(R.id.progress_history);
        mAddressText = findViewById(R.id.tv_address);
        mDateText = findViewById(R.id.tv_date);
        mPlateText = findViewById(R.id.tv_plate);
        mEmptyLayout = findViewById(R.id.ll_history);
        mEmptyImage = findViewById(R.id.iv_empty);
        findViewById(R.id.bt_cancel).setOnClickListener(this);
        findViewById(R.id.bt_change).setOnClickListener(this);
    }

    @Override
    protected void onStop() {
        mCompositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_cancel:
                showCancelDialog();
                break;
            case R.id.bt_change:
                changePlate();
        }
    }

    private void changePlate() {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);
        if (mCarList.isEmpty()) {
            // Show dialog to go to Car Activity to add car
            builder.setMessage(R.string.message_no_car)
                    .setPositiveButton(R.string.action_ok,
                            (dialog, which) -> PendingActivity.this.startActivity(
                                    new Intent(PendingActivity.this, CarActivity.class)))
                    .create()
                    .show();
        } else {
            // Make a new request
            String token = getSharedPreferences(Constants.SHARED_PREF_USER, MODE_PRIVATE).getString(
                    Constants.SHARED_TOKEN, null);
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
                String plate = carSpinner.getSelectedItem().toString();
                Disposable disposable = AppServiceClient.getMyApiInstance(this)
                        .changeReservePlate(token, mInvoice.getId(), plate)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<MessageResponse>() {
                            @Override
                            public void onSuccess(MessageResponse messageResponse) {
                                Toast.makeText(PendingActivity.this,
                                        R.string.message_reserve_changed, Toast.LENGTH_SHORT)
                                        .show();
                                mPlateText.setText(plate);
                            }

                            @Override
                            public void onError(Throwable e) {
                                if (e instanceof HttpException) {
                                    try {
                                        JSONObject jObjError = new JSONObject(
                                                ((HttpException) e).response()
                                                        .errorBody()
                                                        .string());
                                        switch (jObjError.getString("message")) {
                                            case Constants.BOOKING_RESULT_EXIST:
                                                Toast.makeText(PendingActivity.this,
                                                        R.string.message_plate_already_exist,
                                                        Toast.LENGTH_SHORT).show();
                                                break;
                                        }
                                    } catch (Exception ex) {
                                        Toast.makeText(PendingActivity.this, ex.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(PendingActivity.this, R.string.error_server,
                                            Toast.LENGTH_SHORT).show();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showCancelDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_cancel)
                .setPositiveButton(R.string.action_ok, (dialog, which) -> cancelBooking())
                .create()
                .show();
    }

    private void cancelBooking() {
        String token = getSharedPreferences(Constants.SHARED_PREF_USER, MODE_PRIVATE).getString(
                Constants.SHARED_TOKEN, null);
        Disposable disposable = AppServiceClient.getMyApiInstance(this)
                .cancelPending(token, mInvoice.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<MessageResponse>() {
                    @Override
                    public void onSuccess(MessageResponse messageResponse) {
                        mEmptyLayout.setVisibility(View.VISIBLE);
                        mGroup.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(PendingActivity.this, "Something wrong, try again later",
                                Toast.LENGTH_SHORT).show();
                    }
                });
        mCompositeDisposable.add(disposable);
    }
}
