package com.thm.gr_application.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.thm.gr_application.R;
import com.thm.gr_application.model.ParkingLot;
import com.thm.gr_application.payload.CredentialResponse;
import com.thm.gr_application.retrofit.AppServiceClient;
import com.thm.gr_application.utils.Constants;
import com.wang.avi.AVLoadingIndicatorView;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mUsernameView;
    private EditText mPasswordView;
    private AVLoadingIndicatorView mProgressView;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupViews();
    }

    private void setupViews() {
        mUsernameView = findViewById(R.id.et_username);
        findViewById(R.id.bt_login).setOnClickListener(this);
        findViewById(R.id.tv_sign_up).setOnClickListener(this);
        mProgressView = findViewById(R.id.progress_login);
        mPasswordView = findViewById(R.id.et_password);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_sign_up:
                Intent intent = new Intent(this, SignUpActivity.class);
                startActivity(intent);
                break;

            case R.id.bt_login:
                mProgressView.bringToFront();
                mProgressView.show();
                String username = mUsernameView.getText().toString();
                String password = mPasswordView.getText().toString();
                Disposable disposable = AppServiceClient.getMyApiInstance(this).login(username, password)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<CredentialResponse>() {
                            @Override
                            public void onSuccess(CredentialResponse credentialResponse) {
                                mProgressView.hide();
                                storeValues(password, credentialResponse);
                                if (credentialResponse.getRole().equals(getString(R.string.role_user))) {
                                    startMapActivity(credentialResponse.getAccessToken());
                                } else {
                                    startManagerActivity(credentialResponse.getProperty());
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                mProgressView.hide();
                                if (e instanceof HttpException) {
                                    Toast.makeText(LoginActivity.this, R.string.error_bad_credential, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(LoginActivity.this, R.string.error_sign_in, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                mCompositeDisposable.add(disposable);
                break;
            default:
                break;
        }
    }

    private void startManagerActivity(ParkingLot property) {
        Intent intent = new Intent(this, ManagerActivity.class);
        intent.putExtra(Constants.EXTRA_PROPERTY, property);
        startActivity(intent);
        finish();
    }

    private void storeValues(String password, CredentialResponse response) {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_USER, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String accessToken = response.getAccessToken();
        String json = new Gson().toJson(response.getFavorites());
        String role = response.getRole();
        String username = response.getUsername();
        String email = response.getEmail();
        editor.putString(Constants.KEY_PASSWORD, password);
        editor.putString(Constants.KEY_USERNAME, username);
        editor.putString(Constants.KEY_EMAIL, email);
        editor.putString(Constants.KEY_TOKEN, "Bearer " + accessToken);
        editor.putString(Constants.KEY_FAVORITE, json);
        editor.putString(Constants.KEY_ROLE, role);
        editor.apply();
    }

    @Override
    protected void onStop() {
        mCompositeDisposable.clear();
        super.onStop();
    }

    private void startMapActivity(String token) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(Constants.EXTRA_TOKEN, "Bearer " + token);
        startActivity(intent);
        finish();
    }
}
