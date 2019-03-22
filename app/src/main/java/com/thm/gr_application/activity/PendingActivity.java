package com.thm.gr_application.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.thm.gr_application.R;
import com.thm.gr_application.model.Invoice;
import com.thm.gr_application.model.ParkingLot;
import com.thm.gr_application.payload.InvoiceResponse;
import com.thm.gr_application.payload.MessageResponse;
import com.thm.gr_application.retrofit.AppServiceClient;
import com.thm.gr_application.utils.Constants;
import com.wang.avi.AVLoadingIndicatorView;

import org.joda.time.DateTime;

import java.util.Locale;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.Group;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending);
        initViews();
        getData();

    }

    private void getData() {
        String token = getSharedPreferences(Constants.SHARED_PREF_USER, MODE_PRIVATE).getString(Constants.KEY_TOKEN, null);
        mHistoryProgress.show();
        Disposable disposable = AppServiceClient.getMyApiInstance(this).getUserPending(token)
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
                            String dateString = strTF + " " + date.getDayOfMonth() + "-" + date.getMonthOfYear() + "-" + date.getYear();
                            mDateText.setText(dateString);
                            mPlateText.setText(mInvoice.getPlate());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mHistoryProgress.hide();
                        mEmptyText.setText("Cannot connect to server, please try again later");
                        mEmptyImage.setImageResource(R.drawable.ic_disconnected);
                        Toast.makeText(PendingActivity.this, "There are something wrong with server", Toast.LENGTH_SHORT).show();
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
                .create().show();

    }

    private void cancelBooking() {
        String token = getSharedPreferences(Constants.SHARED_PREF_USER, MODE_PRIVATE).getString(Constants.KEY_TOKEN, null);
        Disposable disposable = AppServiceClient.getMyApiInstance(this).cancelPending(token, mInvoice.getId())
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
                        Toast.makeText(PendingActivity.this, "Something wrong, try again later", Toast.LENGTH_SHORT).show();
                    }
                });
        mCompositeDisposable.add(disposable);
    }
}
