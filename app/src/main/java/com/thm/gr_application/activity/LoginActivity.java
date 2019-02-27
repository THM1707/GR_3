package com.thm.gr_application.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.thm.gr_application.R;
import com.thm.gr_application.payload.CredentialResponse;
import com.thm.gr_application.payload.TestResponse;
import com.thm.gr_application.retrofit.AppServiceClient;
import com.thm.gr_application.utils.Constants;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;

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
                mProgressView.setVisibility(View.VISIBLE);
                String username = mUsernameView.getText().toString();
                String password = mPasswordView.getText().toString();
                AppServiceClient.getMyApiInstance(this).login(username, password).enqueue(new Callback<CredentialResponse>() {
                    @Override
                    public void onResponse(@NonNull  Call<CredentialResponse> call, @NonNull Response<CredentialResponse> response) {
                        mProgressView.setVisibility(View.INVISIBLE);
                        if (!response.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, R.string.error_bad_credential, Toast.LENGTH_SHORT).show();
                        } else {
                            if (response.body() != null) {
                                SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_USER, MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(Constants.KEY_PASSWORD, password);
                                editor.putString(Constants.KEY_USERNAME, username);
                                editor.putString(Constants.KEY_TOKEN, "Bearer " + response.body().getAccessToken());
                                List<Long> favorites = response.body().getFavorites();
                                Gson gson = new Gson();
                                String json = gson.toJson(favorites);
                                editor.putString(Constants.KEY_FAVORITE, json);
                                editor.apply();
                                startMapActivity(response.body().getAccessToken());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<CredentialResponse> call, Throwable t) {
                        mProgressView.setVisibility(View.INVISIBLE);
                        Toast.makeText(LoginActivity.this, R.string.error_sign_in, Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            default:
                break;
        }
    }

    private void startMapActivity(String token) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(Constants.EXTRA_TOKEN, "Bearer " + token);
        startActivity(intent);
    }
}

