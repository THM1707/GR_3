package com.thm.gr_application.activity;

import android.os.Bundle;
import android.widget.RadioButton;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.thm.gr_application.R;
import com.thm.gr_application.payload.SignUpRequest;
import com.thm.gr_application.payload.SignUpResponse;
import com.thm.gr_application.retrofit.AppServiceClient;

import com.wang.avi.AVLoadingIndicatorView;
import org.json.JSONObject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private AVLoadingIndicatorView mProgressBar;

    private EditText mNameText;
    private EditText mUsernameText;
    private EditText mEmailText;
    private EditText mPasswordText;
    private EditText mPhoneText;
    private RadioButton mMaleRadio;
    private RadioButton mFemaleRadio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initViews();
    }

    private void initViews() {
        mNameText = findViewById(R.id.et_name);
        mUsernameText = findViewById(R.id.et_username);
        mEmailText = findViewById(R.id.et_email);
        mPasswordText = findViewById(R.id.et_password);
        mPhoneText = findViewById(R.id.et_phone);
        mMaleRadio = findViewById(R.id.rb_male);
        mFemaleRadio = findViewById(R.id.rb_female);
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

                String name = mNameText.getText().toString();
                String username = mUsernameText.getText().toString();
                String email = mEmailText.getText().toString();
                String password = mPasswordText.getText().toString();
                String phone = mPhoneText.getText().toString();
                int gender = mMaleRadio.isChecked() ? 0 : 1;

                final SignUpRequest request = new SignUpRequest(name, username, email, password, phone, gender);
                Disposable disposable = AppServiceClient.getMyApiInstance(SignUpActivity.this)
                        .signUp(request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(disposable1 -> mProgressBar.setVisibility(View.VISIBLE))
                        .subscribeWith(new DisposableSingleObserver<SignUpResponse>() {
                            @Override
                            public void onSuccess(SignUpResponse signUpResponse) {
                                mProgressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(SignUpActivity.this, signUpResponse.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                onBackPressed();
                            }

                            @Override
                            public void onError(Throwable e) {
                                mProgressBar.setVisibility(View.INVISIBLE);
                                if (e instanceof HttpException) {
                                    try {
                                        JSONObject jObjError = new JSONObject(
                                                ((HttpException) e).response()
                                                        .errorBody()
                                                        .string());
                                        Toast.makeText(SignUpActivity.this,
                                                jObjError.getString("message"), Toast.LENGTH_SHORT)
                                                .show();
                                    } catch (Exception ex) {
                                        Toast.makeText(SignUpActivity.this, ex.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(SignUpActivity.this, R.string.error_sign_in,
                                            Toast.LENGTH_SHORT).show();
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
