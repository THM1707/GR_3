package com.thm.gr_application.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.thm.gr_application.R;
import com.thm.gr_application.payload.SignUpRequest;
import com.thm.gr_application.payload.SignUpResponse;
import com.thm.gr_application.retrofit.AppServiceClient;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setupViews();
    }

    private void setupViews() {
        findViewById(R.id.bt_sign_up).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_sign_up:
                EditText textName = findViewById(R.id.tv_name);
                EditText textUsername = findViewById(R.id.tv_username);
                EditText textEmail = findViewById(R.id.tv_email);
                EditText textPassword = findViewById(R.id.tv_password);
                String name = textName.getText().toString();
                String username = textUsername.getText().toString();
                String email = textEmail.getText().toString();
                String password = textPassword.getText().toString();

                final SignUpRequest request = new SignUpRequest(name, username, email, password);
                System.out.println(request);
                AppServiceClient.getMyApiInstance(SignUpActivity.this).signUp(request).enqueue(new Callback<SignUpResponse>() {
                    @Override
                    public void onResponse(Call<SignUpResponse> call, Response<SignUpResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                JSONObject jObjError = new JSONObject(response.errorBody().string());
                                Toast.makeText(SignUpActivity.this, jObjError.getString("message"), Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<SignUpResponse> call, Throwable t) {
                        Toast.makeText(SignUpActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
    }
}
