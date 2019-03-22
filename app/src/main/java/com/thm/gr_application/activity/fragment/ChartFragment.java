package com.thm.gr_application.activity.fragment;

import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;

import com.db.chart.animation.Animation;
import com.db.chart.model.LineSet;
import com.db.chart.tooltip.Tooltip;
import com.db.chart.util.Tools;
import com.db.chart.view.LineChartView;
import com.thm.gr_application.R;
import com.thm.gr_application.model.Invoice;
import com.thm.gr_application.utils.Constants;
import com.thm.gr_application.utils.DateUtils;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;

@SuppressLint("UseSparseArrays")
public class ChartFragment extends Fragment {
    private LineChartView mChart;
    private List<Invoice> mEndedList = new ArrayList<>();
    private Tooltip mTip;
    private List<String> mLabelList = new ArrayList<>();
    private float[] mDoneValues = {0, 0, 0, 0, 0, 0};
    private float[] mCanceledValues = {0, 0, 0, 0, 0, 0};

    public ChartFragment() {
        // Required empty public constructor
    }

    public static ChartFragment newInstance(List<Invoice> endedList) {
        ChartFragment fragment = new ChartFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.BUNDLE_ENDED_LIST, (Serializable) endedList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mEndedList = (List<Invoice>) getArguments().getSerializable(Constants.BUNDLE_ENDED_LIST);
        }
        initTip();
        initLabelsAndValues();
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

        for (Invoice invoice : mEndedList) {
            DateTime createDate = new DateTime(invoice.getCreatedDate());
            int invoiceMonth = createDate.getMonthOfYear();
            int invoiceYear = createDate.getYear();
            for (i = 0; i <= 5; i++) {
                if ((invoiceYear == curYear && invoiceMonth == curMonth - 5 + i)
                        || (invoiceYear == curYear - 1 && invoiceMonth == curMonth + 7 + i)) {
                    if (invoice.getStatus().equals(Constants.STATUS_DONE)) {
                        mDoneValues[i]++;
                    } else {
                        mCanceledValues[i]++;
                    }
                }
            }
        }
    }

    private void initTip() {
        mTip = new Tooltip(getActivity(), R.layout.tooltips, R.id.value);

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        mChart = view.findViewById(R.id.chart);
        LineSet doneSet = new LineSet(mLabelList.toArray(new String[0]), mDoneValues);
        doneSet.setColor(Color.parseColor("#b3b5bb"))
                .setDotsColor(Color.parseColor("#ffc755"))
                .setThickness(4);
        mChart.addData(doneSet);

        LineSet cancelSet = new LineSet(mLabelList.toArray(new String[0]), mCanceledValues);
        cancelSet.setColor(Color.parseColor("#758cbb"))
                .setDotsColor(Color.parseColor("#758cbb"))
                .setThickness(4)
                .setDashed(new float[]{10f, 10f});
        mChart.addData(cancelSet);

        mChart.setXAxis(false)
                .setYAxis(false)
                .setStep(mDoneValues.length > 20 ? 10: 5)
                .setTooltips(mTip)
                .show(new Animation().setInterpolator(new BounceInterpolator())
                        .fromAlpha(0));
        return view;
    }
}
