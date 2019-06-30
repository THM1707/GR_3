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
import com.db.chart.model.Bar;
import com.db.chart.model.BarSet;
import com.db.chart.model.LineSet;
import com.db.chart.tooltip.Tooltip;
import com.db.chart.util.Tools;
import com.db.chart.view.BarChartView;
import com.db.chart.view.LineChartView;
import com.thm.gr_application.R;
import com.thm.gr_application.model.Invoice;
import com.thm.gr_application.payload.InvoicesResponse;
import com.thm.gr_application.retrofit.AppServiceClient;
import com.thm.gr_application.utils.Constants;
import com.thm.gr_application.utils.DateUtils;
import com.wang.avi.AVLoadingIndicatorView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;

public class StatisticActivity extends AppCompatActivity {
    private AVLoadingIndicatorView mStatsProgress;
    private List<Invoice> mInvoiceList;
    private LineChartView mBillChart;
    private BarChartView mIncomeChart;
    private Tooltip mBillTip;
    private Tooltip mIncomeTip;
    private List<String> mLabelList = new ArrayList<>();
    private float[] mDoneValues = { 0, 0, 0, 0, 0, 0 };
    private float[] mCanceledValues = { 0, 0, 0, 0, 0, 0 };
    private int[] mIncomeValues = { 0, 0, 0, 0, 0, 0 };
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
        mBillChart = findViewById(R.id.bill_chart);
        mIncomeChart = findViewById(R.id.income_chart);
        mStatsProgress = findViewById(R.id.progress_stats);
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
                        mStatsProgress.smoothToHide();
                        mInvoiceList = invoicesResponse.getData();
                        initLabelsAndValues();
                        populateData();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mStatsProgress.smoothToHide();
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
        mBillChart.addData(doneSet);

        LineSet cancelSet = new LineSet(mLabelList.toArray(new String[0]), mCanceledValues);
        cancelSet.setColor(Color.parseColor("#758cbb"))
                .setDotsColor(Color.parseColor("#758cbb"))
                .setThickness(4)
                .setDashed(new float[] { 10f, 10f });
        mBillChart.addData(cancelSet);

        mBillChart.setXAxis(false)
                .setYAxis(false)
                .setStep(mDoneValues.length > 20 ? 10 : 5)
                .setTooltips(mBillTip)
                .show(new Animation().setInterpolator(new BounceInterpolator()).fromAlpha(0));

        BarSet barSet = new BarSet();
        Bar bar;
        for (int i = 0; i < mLabelList.size(); i++) {
            bar = new Bar(mLabelList.get(i), mIncomeValues[i]);
            switch (i) {
                case 0:
                    bar.setColor(Color.parseColor("#90A4AE"));
                    break;
                case 1:
                    bar.setColor(Color.parseColor("#BA68C8"));
                    break;
                case 2:
                    bar.setColor(Color.parseColor("#EF9A9A"));
                    break;
                case 3:
                    bar.setColor(Color.parseColor("#B388FF"));
                    break;
                case 4:
                    bar.setColor(Color.parseColor("#00E5FF"));
                    break;
                case 5:
                    bar.setColor(Color.parseColor("#FFCA28"));
                    break;
                default:
                    break;
            }
            barSet.addBar(bar);
        }
        mIncomeChart.addData(barSet);
        int[] order = { 5, 4, 3, 2, 1, 0 };
        mIncomeChart.setXAxis(false)
                .setYAxis(false)
                .setStep(100000)
                .setTooltips(mIncomeTip)
                .show(new Animation().inSequence(.5f, order));
    }

    private void initTip() {
        mBillTip = new Tooltip(this, R.layout.tooltips, R.id.value);
        mIncomeTip = new Tooltip(this, R.layout.tooltips_2, R.id.value_2);

        mBillTip.setVerticalAlignment(Tooltip.Alignment.BOTTOM_TOP);
        mBillTip.setDimensions((int) Tools.fromDpToPx(58), (int) Tools.fromDpToPx(25));

        mBillTip.setEnterAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 1),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f)).setDuration(200);

        mBillTip.setExitAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 0),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0f)).setDuration(200);

        mBillTip.setPivotX(Tools.fromDpToPx(65) / 2);
        mBillTip.setPivotY(Tools.fromDpToPx(25));

        mIncomeTip.setVerticalAlignment(Tooltip.Alignment.BOTTOM_TOP);
        mIncomeTip.setDimensions((int) Tools.fromDpToPx(106), (int) Tools.fromDpToPx(25));

        mIncomeTip.setEnterAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 1),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f)).setDuration(200);

        mIncomeTip.setExitAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 0),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0f)).setDuration(200);

        mIncomeTip.setPivotX(Tools.fromDpToPx(65) / 2);
        mIncomeTip.setPivotY(Tools.fromDpToPx(25));
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
                        mIncomeValues[i] += invoice.getIncome();
                    } else {
                        mCanceledValues[i]++;
                    }
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
