package com.thm.gr_application.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.thm.gr_application.R;
import com.thm.gr_application.payload.MessageResponse;
import com.thm.gr_application.retrofit.AppServiceClient;
import com.thm.gr_application.utils.Constants;
import com.thm.gr_application.utils.NumberUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class RechargeActivity extends AppCompatActivity implements View.OnClickListener {

    private int mBudget;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private String mToken = "";
    private TextView mBudgetText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recharge);
        initViews();
        getData();
    }

    private void getData() {
        mToken = getSharedPreferences(Constants.SHARED_PREF_USER, MODE_PRIVATE).getString(
                Constants.SHARED_TOKEN, null);
        Disposable disposable = AppServiceClient.getMyApiInstance(this)
                .getBudget(mToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<MessageResponse>() {
                    @Override
                    public void onSuccess(MessageResponse messageResponse) {
                        mBudget = Integer.valueOf(messageResponse.getMessage());
                        mBudgetText.setText(NumberUtils.getAmountNumber(mBudget));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(RechargeActivity.this, R.string.error_server,
                                Toast.LENGTH_SHORT).show();
                    }
                });
        mCompositeDisposable.add(disposable);
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar_recharge);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Recharge");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mBudgetText = findViewById(R.id.tv_budget);
        findViewById(R.id.bt_recharge_5).setOnClickListener(this);
        findViewById(R.id.bt_recharge_10).setOnClickListener(this);
        findViewById(R.id.bt_recharge_20).setOnClickListener(this);
        findViewById(R.id.bt_recharge_50).setOnClickListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCompositeDisposable.clear();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_recharge_5:
                showAlert(0);
                break;
            case R.id.bt_recharge_10:
                showAlert(1);
                break;
            case R.id.bt_recharge_20:
                showAlert(2);
                break;
            case R.id.bt_recharge_50:
                showAlert(3);
                break;
            default:
                break;
        }
    }

    private void showAlert(int i) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.message_confirm_recharge)
                .setPositiveButton(R.string.action_ok, (dialog, which) -> recharge(i))
                .create()
                .show();
    }

    private void recharge(int i) {
        Disposable disposable = AppServiceClient.getMyApiInstance(this)
                .recharge(mToken, i)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<MessageResponse>() {
                    @Override
                    public void onSuccess(MessageResponse messageResponse) {
                        Toast.makeText(RechargeActivity.this, R.string.message_recharge_success,
                                Toast.LENGTH_SHORT).show();
                        switch (i) {
                            case 0:
                                mBudget += 50000;
                                break;
                            case 1:
                                mBudget += 100000;
                                break;
                            case 2:
                                mBudget += 200000;
                                break;
                            case 3:
                                mBudget += 500000;
                                break;
                        }
                        mBudgetText.setText(NumberUtils.getAmountNumber(mBudget));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(RechargeActivity.this, R.string.error_server,
                                Toast.LENGTH_SHORT).show();
                    }
                });
        mCompositeDisposable.add(disposable);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
