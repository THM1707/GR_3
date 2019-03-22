package com.thm.gr_application.activity;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.thm.gr_application.R;
import com.thm.gr_application.activity.fragment.ChartFragment;
import com.thm.gr_application.activity.fragment.HomeFragment;
import com.thm.gr_application.activity.fragment.PropertyDetailsFragment;
import com.thm.gr_application.adapter.BookingItemAdapter;
import com.thm.gr_application.model.Invoice;
import com.thm.gr_application.model.ParkingLot;
import com.thm.gr_application.payload.InvoiceIndexResponse;
import com.thm.gr_application.retrofit.AppServiceClient;
import com.thm.gr_application.utils.Constants;
import com.wang.avi.AVLoadingIndicatorView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ManagerActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, HomeFragment.HomeFragmentListeners {

    private ParkingLot mProperty;

    private List<Invoice> mAllList = new ArrayList<>();
    private List<Invoice> mEndedList = new ArrayList<>();
    private List<Invoice> mActiveList = new ArrayList<>();
    private BottomNavigationView mNavigation;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private AVLoadingIndicatorView mProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);
        mProperty = (ParkingLot) getIntent().getSerializableExtra(Constants.EXTRA_PROPERTY);
        initData();
        initViews();
    }

    private void initData() {
        String token = getSharedPreferences(Constants.SHARED_PREF_USER, Context.MODE_PRIVATE).getString(Constants.KEY_TOKEN, null);
        Disposable disposable = AppServiceClient.getMyApiInstance(this).invoiceIndex(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<InvoiceIndexResponse>() {
                    @Override
                    public void onSuccess(InvoiceIndexResponse invoiceIndexResponse) {
                        mProgressView.hide();
                        mAllList = invoiceIndexResponse.getAll();
                        mActiveList = invoiceIndexResponse.getActive();
                        mEndedList = invoiceIndexResponse.getEnded();
                        mNavigation.setSelectedItemId(R.id.navigation_home);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mProgressView.hide();
                        Toast.makeText(ManagerActivity.this, R.string.error_server, Toast.LENGTH_SHORT).show();
                    }
                });
        mCompositeDisposable.add(disposable);
    }

    private void initViews() {
        mProgressView = findViewById(R.id.progress_manager);
        mNavigation = findViewById(R.id.bnv_manager);
        mNavigation.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment;
        switch (item.getItemId()) {
            case R.id.navigation_home:
                fragment = HomeFragment.newInstance(mProperty.getCapacity(), mProperty.getCurrent(), mAllList, mActiveList);
                loadFragment(fragment);
                return true;
            case R.id.navigation_details:
                fragment = PropertyDetailsFragment.newInstance(mProperty);
                loadFragment(fragment);
                return true;
            case R.id.navigation_chart:
                fragment = ChartFragment.newInstance(mEndedList);
                loadFragment(fragment);
                return true;
        }
        return false;
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_content, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onRefreshRequested(List<Invoice> allList, List<Invoice> activeList, BookingItemAdapter adapter, TextView currentText) {
        mProgressView.bringToFront();
        mProgressView.show();
        String token = getSharedPreferences(Constants.SHARED_PREF_USER, Context.MODE_PRIVATE).getString(Constants.KEY_TOKEN, null);
        Disposable disposable = AppServiceClient.getMyApiInstance(this).invoiceIndex(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<InvoiceIndexResponse>() {
                    @Override
                    public void onSuccess(InvoiceIndexResponse invoiceIndexResponse) {
                        mProgressView.hide();
                        mAllList = invoiceIndexResponse.getAll();
                        mActiveList = invoiceIndexResponse.getActive();
                        mEndedList = invoiceIndexResponse.getEnded();
                        allList.clear();
                        allList.addAll(mAllList);
                        activeList.clear();
                        activeList.addAll(mActiveList);
                        adapter.notifyDataSetChanged();
                        mProperty.setCurrent(mActiveList.size());
                        String text = String.format(Locale.getDefault(), "Now: %d/%d", mActiveList.size(), mProperty.getCapacity());
                        currentText.setText(text);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mProgressView.hide();
                        Toast.makeText(ManagerActivity.this, R.string.error_server, Toast.LENGTH_SHORT).show();
                    }
                });
        mCompositeDisposable.add(disposable);
    }

    @Override
    public void onPropertyChanged(ParkingLot parkingLot) {
        mProperty = parkingLot;
    }
}
