package com.thm.gr_application.activity;

import android.animation.PropertyValuesHolder;
import android.graphics.Color;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import com.db.chart.animation.Animation;
import com.db.chart.model.LineSet;
import com.db.chart.tooltip.Tooltip;
import com.db.chart.util.Tools;
import com.db.chart.view.LineChartView;
import com.thm.gr_application.R;
import com.thm.gr_application.model.Invoice;
import com.thm.gr_application.payload.InvoicesResponse;
import com.thm.gr_application.retrofit.AppServiceClient;
import com.thm.gr_application.utils.Constants;
import com.thm.gr_application.utils.DateUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;

public class StatisticActivity extends AppCompatActivity {

    private List<Invoice> mInvoiceList;
    private LineChartView mChart;
    private Tooltip mTip;
    private List<String> mLabelList = new ArrayList<>();
    private float[] mDoneValues = { 0, 0, 0, 0, 0, 0 };
    private float[] mCanceledValues = { 0, 0, 0, 0, 0, 0 };
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);
        initViews();
        getData();
        initTip();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar_statistic);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Statistic");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mChart = findViewById(R.id.chart);
    }

    private void getData() {
        String token = getSharedPreferences(Constants.SHARED_PREF_USER, MODE_PRIVATE).getString(
                Constants.SHARED_TOKEN, null);
        Disposable disposable = AppServiceClient.getMyApiInstance(this)
                .getDoneInvoices(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<InvoicesResponse>() {
                    @Override
                    public void onSuccess(InvoicesResponse invoicesResponse) {
                        mInvoiceList = invoicesResponse.getData();
                        initLabelsAndValues();
                        populateData();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(StatisticActivity.this, R.string.error_server,
                                Toast.LENGTH_SHORT).show();
                    }
                });

        mCompositeDisposable.add(disposable);
    }

    private void populateData() {
        LineSet doneSet = new LineSet(mLabelList.toArray(new String[0]), mDoneValues);
        doneSet.setColor(Color.parseColor("#b3b5bb"))
                .setDotsColor(Color.parseColor("#ffc755"))
                .setThickness(4);
        mChart.addData(doneSet);

        LineSet cancelSet = new LineSet(mLabelList.toArray(new String[0]), mCanceledValues);
        cancelSet.setColor(Color.parseColor("#758cbb"))
                .setDotsColor(Color.parseColor("#758cbb"))
                .setThickness(4)
                .setDashed(new float[] { 10f, 10f });
        mChart.addData(cancelSet);

        mChart.setXAxis(false)
                .setYAxis(false)
                .setStep(mDoneValues.length > 20 ? 10 : 5)
                .setTooltips(mTip)
                .show(new Animation().setInterpolator(new BounceInterpolator()).fromAlpha(0));
    }

    private void initTip() {
        mTip = new Tooltip(this, R.layout.tooltips, R.id.value);

        mTip.setVerticalAlignment(Tooltip.Alignment.BOTTOM_TOP);
        mTip.setDimensions((int) Tools.fromDpToPx(58), (int) Tools.fromDpToPx(25));

        mTip.setEnterAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 1),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f)).setDuration(200);

        mTip.setExitAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 0),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0f)).setDuration(200);

        mTip.setPivotX(Tools.fromDpToPx(65) / 2);
        mTip.setPivotY(Tools.fromDpToPx(25));
    }

    private void initLabelsAndValues() {
        DateTime now = DateTime.now();
        int curMonth = now.getMonthOfYear();
        int curYear = now.getYear();
        int i;
        for (i = curMonth - 5; i <= curMonth; i++) {
            if (i > 0) {
                mLabelList.add(DateUtils.getMonthShortNameFromInteger(i));
            } else {
                mLabelList.add(DateUtils.getMonthShortNameFromInteger(i + 12));
            }
        }

        for (Invoice invoice : mInvoiceList) {
            DateTime createDate = new DateTime(invoice.getCreatedDate());
            int invoiceMonth = createDate.getMonthOfYear();
            int invoiceYear = createDate.getYear();
            for (i = 0; i <= 5; i++) {
                if ((invoiceYear == curYear && invoiceMonth == curMonth - 5 + i) || (invoiceYear
                        == curYear - 1 && invoiceMonth == curMonth + 7 + i)) {
                    if (invoice.getStatus().equals(Constants.STATUS_DONE)) {
                        mDoneValues[i]++;
                    } else {
                        mCanceledValues[i]++;
                    }
                }
            }
        }
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
