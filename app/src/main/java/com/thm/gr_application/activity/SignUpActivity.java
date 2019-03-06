package com.thm.gr_application.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.thm.gr_application.R;
import com.thm.gr_application.payload.SignUpRequest;
import com.thm.gr_application.payload.SignUpResponse;
import com.thm.gr_application.retrofit.AppServiceClient;

import org.json.JSONObject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initViews();
    }

    private void initViews() {
        findViewById(R.id.bt_sign_up).setOnClickListener(this);
        mProgressBar = findViewById(R.id.progress_sign_up);
    }

    @Override
    protected void onStop() {
        mCompositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_sign_up:
                EditText textName = findViewById(R.id.et_name);
                EditText textUsername = findViewById(R.id.et_username);
                EditText textEmail = findViewById(R.id.et_email);
                EditText textPassword = findViewById(R.id.et_password);
                String name = textName.getText().toString();
                String username = textUsername.getText().toString();
                String email = textEmail.getText().toString();
                String password = textPassword.getText().toString();

                final SignUpRequest request = new SignUpRequest(name, username, email, password);
                Disposable disposable = AppServiceClient.getMyApiInstance(SignUpActivity.this).signUp(request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(disposable1 -> mProgressBar.setVisibility(View.VISIBLE))
                        .subscribeWith(new DisposableSingleObserver<SignUpResponse>() {
                            @Override
                            public void onSuccess(SignUpResponse signUpResponse) {
                                mProgressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(SignUpActivity.this, signUpResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                onBackPressed();
                            }

                            @Override
                            public void onError(Throwable e) {
                                mProgressBar.setVisibility(View.INVISIBLE);
                                if (e instanceof HttpException) {
                                    try {
                                        JSONObject jObjError = new JSONObject(((HttpException) e).response().errorBody().string());
                                        Toast.makeText(SignUpActivity.this, jObjError.getString("message"), Toast.LENGTH_SHORT).show();
                                    } catch (Exception ex) {
                                        Toast.makeText(SignUpActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(SignUpActivity.this, R.string.error_sign_in, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                mCompositeDisposable.add(disposable);
                break;
            default:
                break;
        }
    }
}
