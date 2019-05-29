package com.thm.gr_application.activity;

import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.thm.gr_application.R;
import com.thm.gr_application.adapter.InvoiceAdapter;
import com.thm.gr_application.adapter.listener.InvoiceAcceptListener;
import com.thm.gr_application.model.Invoice;
import com.thm.gr_application.payload.InvoicesResponse;
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

public class HistoryActivity extends AppCompatActivity implements InvoiceAcceptListener {

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private TextView mTotalText;
    private TextView mCanceledText;
    private List<Invoice> mInvoiceList = new ArrayList<>();
    private InvoiceAdapter mAdapter;
    private AVLoadingIndicatorView mHistoryProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        initViews();
        getData();
    }

    private void getData() {
        String token = getSharedPreferences(Constants.SHARED_PREF_USER, MODE_PRIVATE).getString(
                Constants.SHARED_TOKEN, null);
        Disposable disposable = AppServiceClient.getMyApiInstance(this)
                .getHistory(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<InvoicesResponse>() {
                    @Override
                    public void onSuccess(InvoicesResponse invoicesResponse) {
                        mHistoryProgress.smoothToHide();
                        mInvoiceList = invoicesResponse.getData();
                        int canceledCount = 0;
                        for (Invoice invoice : mInvoiceList) {
                            if (invoice.getStatus().equals(Constants.STATUS_CANCEL)) {
                                canceledCount++;
                            }
                        }
                        mTotalText.setText(String.valueOf(mInvoiceList.size()));
                        mCanceledText.setText(String.valueOf(canceledCount));
                        mAdapter.setInvoiceList(mInvoiceList);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mHistoryProgress.smoothToHide();
                        Toast.makeText(HistoryActivity.this, R.string.error_server,
                                Toast.LENGTH_SHORT).show();
                    }
                });
        mCompositeDisposable.add(disposable);
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar_history);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("History");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mHistoryProgress = findViewById(R.id.progress_pending);
        mTotalText = findViewById(R.id.tv_total);
        mCanceledText = findViewById(R.id.tv_canceled);
        RecyclerView historyRecycler = findViewById(R.id.rv_history);
        mAdapter = new InvoiceAdapter(this, mInvoiceList);
        historyRecycler.setLayoutManager(new LinearLayoutManager(this));
        historyRecycler.setAdapter(mAdapter);
    }

    @Override
    protected void onStop() {
        mCompositeDisposable.clear();
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onInvoiceAccepted() {

    }
}
