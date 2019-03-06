package com.thm.gr_application.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.Group;
import androidx.transition.TransitionManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.thm.gr_application.R;
import com.thm.gr_application.payload.MessageResponse;
import com.thm.gr_application.retrofit.AppServiceClient;
import com.thm.gr_application.utils.Constants;

public class AccountManagementActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText mOldPasswordText;
    private EditText mNewPasswordText;
    private Group mGroup;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private ConstraintLayout mConstraintLayout;
    private ConstraintSet mStartSet = new ConstraintSet();
    private ConstraintSet mEndSet = new ConstraintSet();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_management);
        initViews();

    }

    private void initViews() {
        mConstraintLayout = findViewById(R.id.cl_account);
        mStartSet.clone(this, R.layout.activity_account_management);
        mEndSet.clone(this, R.layout.activity_account_management_visible);
        Toolbar toolbar = findViewById(R.id.toolbar_account);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Account Management");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mOldPasswordText = findViewById(R.id.et_old_password);
        mNewPasswordText = findViewById(R.id.et_new_password);
        mGroup = findViewById(R.id.group);
        findViewById(R.id.ib_done).setOnClickListener(this);
        findViewById(R.id.ib_logout).setOnClickListener(this);
        findViewById(R.id.ib_change_password).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_done:
                changePassword();
                break;
            case R.id.ib_logout:
                logout();
                break;

            case R.id.ib_change_password:
                if (mGroup.getVisibility() == View.VISIBLE) {
                    TransitionManager.beginDelayedTransition(mConstraintLayout);
                    mStartSet.applyTo(mConstraintLayout);
                } else {
                    TransitionManager.beginDelayedTransition(mConstraintLayout);
                    mEndSet.applyTo(mConstraintLayout);
                }
                break;

            default:
                break;
        }
    }

    private void changePassword() {
        if (mOldPasswordText.getText() == null || mNewPasswordText.getText() == null || mNewPasswordText.getText().length() < 6) {
            Toast.makeText(this, "You need to fill valid password", Toast.LENGTH_SHORT).show();
        } else {
            String oldPassword = mOldPasswordText.getText().toString();
            String newPassword = mNewPasswordText.getText().toString();
            String token = getSharedPreferences(Constants.SHARED_PREF_USER, MODE_PRIVATE).getString(Constants.KEY_TOKEN, null);
            Disposable disposable = AppServiceClient.getMyApiInstance(this).changePassword(token, oldPassword, newPassword)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableSingleObserver<MessageResponse>() {

                        @Override
                        public void onSuccess(MessageResponse messageResponse) {
                            Toast.makeText(AccountManagementActivity.this, messageResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (e instanceof HttpException) {
                                if (((HttpException) e).code() == 400){
                                    Toast.makeText(AccountManagementActivity.this, "Password not correct", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(AccountManagementActivity.this, R.string.error_bad_credential, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(AccountManagementActivity.this, R.string.error_sign_in, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            mCompositeDisposable.add(disposable);
        }
    }

    private void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_USER, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
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

    @Override
    protected void onStop() {
        mCompositeDisposable.clear();
        super.onStop();
    }
}
