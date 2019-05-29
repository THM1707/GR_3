package com.thm.gr_application.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.Group;
import com.thm.gr_application.R;
import com.thm.gr_application.payload.MessageResponse;
import com.thm.gr_application.payload.ProfileRequest;
import com.thm.gr_application.payload.ProfileResponse;
import com.thm.gr_application.retrofit.AppServiceClient;
import com.thm.gr_application.utils.Constants;
import com.wang.avi.AVLoadingIndicatorView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText mOldPasswordText;
    private EditText mNewPasswordText;
    private TextView mUsernameText;
    private TextView mEmailText;
    private EditText mNameText;
    private EditText mPhoneText;
    private Group mGroup;
    private ImageView mGenderImage;
    private RadioButton mMaleRadio;
    private RadioButton mFemaleRadio;
    private ImageButton mEditButton;
    private AVLoadingIndicatorView mProfileProgress;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private String mName;
    private String mPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initViews();
        showData();
    }

    private void showData() {
        String token = getSharedPreferences(Constants.SHARED_PREF_USER, MODE_PRIVATE).getString(
                Constants.SHARED_TOKEN, null);
        Disposable disposable = AppServiceClient.getMyApiInstance(this)
                .getProfile(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ProfileResponse>() {
                    @Override
                    public void onSuccess(ProfileResponse profileResponse) {
                        mProfileProgress.smoothToHide();
                        mUsernameText.setText(profileResponse.getUsername());
                        mEmailText.setText(profileResponse.getEmail());
                        mNameText.setText(profileResponse.getName());
                        mPhoneText.setText(profileResponse.getPhone());
                        mName = profileResponse.getName();
                        mPhone = profileResponse.getPhone();
                        if (profileResponse.getGender() == 0) {
                            mGenderImage.setImageResource(R.drawable.ic_male);
                            mMaleRadio.setChecked(true);
                        } else {
                            mGenderImage.setImageResource(R.drawable.ic_female);
                            mFemaleRadio.setChecked(true);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mProfileProgress.smoothToHide();
                        Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
        mCompositeDisposable.add(disposable);
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar_account);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Profile");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mProfileProgress = findViewById(R.id.progress_profile);
        mGenderImage = findViewById(R.id.iv_gender);
        mGroup = findViewById(R.id.group);
        mOldPasswordText = findViewById(R.id.et_old_password);
        mNewPasswordText = findViewById(R.id.et_new_password);
        mEmailText = findViewById(R.id.tv_email);
        mUsernameText = findViewById(R.id.tv_username);
        mNameText = findViewById(R.id.et_name);
        mNameText.setFocusable(false);
        mNameText.setClickable(false);
        mPhoneText = findViewById(R.id.et_phone);
        mPhoneText.setFocusable(false);
        mPhoneText.setClickable(false);
        mMaleRadio = findViewById(R.id.rb_male);
        mFemaleRadio = findViewById(R.id.rb_female);
        findViewById(R.id.ib_cancel).setOnClickListener(this);
        findViewById(R.id.ib_done).setOnClickListener(this);
        mEditButton = findViewById(R.id.ib_edit);
        mEditButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_edit:
                mGroup.setVisibility(View.VISIBLE);
                mEditButton.setVisibility(View.GONE);
                mNameText.setFocusableInTouchMode(true);
                mPhoneText.setFocusableInTouchMode(true);
                break;
            case R.id.ib_cancel:
                mGroup.setVisibility(View.GONE);
                mEditButton.setVisibility(View.VISIBLE);
                mNameText.setText(mName);
                mPhoneText.setText(mPhone);
                mNameText.setFocusable(false);
                mNameText.setClickable(false);
                mPhoneText.setFocusable(false);
                mPhoneText.setClickable(false);
                break;
            case R.id.ib_done:
                String name = mNameText.getText().toString();
                String phone = mPhoneText.getText().toString();
                int gender = mFemaleRadio.isChecked() ? 1 : 0;
                String oldPassword = mOldPasswordText.getText().toString();
                String newPassword = mNewPasswordText.getText().toString().isEmpty() ? null
                        : mNewPasswordText.getText().toString();
                ProfileRequest request =
                        new ProfileRequest(name, newPassword, oldPassword, phone, gender);
                changeProfile(request);
                break;
            default:
                break;
        }
    }

    private void changeProfile(ProfileRequest request) {
        String token = getSharedPreferences(Constants.SHARED_PREF_USER, MODE_PRIVATE).getString(
                Constants.SHARED_TOKEN, null);
        mProfileProgress.smoothToShow();
        Disposable disposable = AppServiceClient.getMyApiInstance(this)
                .changeProfile(token, request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<MessageResponse>() {
                    @Override
                    public void onSuccess(MessageResponse messageResponse) {
                        mProfileProgress.smoothToHide();
                        mGenderImage.setImageResource(request.getGender() == 0 ? R.drawable.ic_male
                                : R.drawable.ic_female);
                        mName = request.getName();
                        mPhone = request.getPhone();
                        SharedPreferences.Editor editor =
                                getSharedPreferences(Constants.SHARED_PREF_USER,
                                        MODE_PRIVATE).edit();
                        editor.putInt(Constants.SHARED_GENDER, request.getGender());
                        editor.putString(Constants.SHARED_NAME, mName);
                        editor.apply();
                        mNameText.setText(mName);
                        mPhoneText.setText(mPhone);
                        Toast.makeText(ProfileActivity.this, R.string.message_edit_profile_success,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mProfileProgress.smoothToHide();
                        if (e instanceof HttpException) {
                            if (((HttpException) e).code() == 404) {
                                Toast.makeText(ProfileActivity.this, R.string.error_wrong_password,
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ProfileActivity.this, R.string.error_server,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        mCompositeDisposable.add(disposable);
    }

    private void logout() {
        SharedPreferences sharedPreferences =
                getSharedPreferences(Constants.SHARED_PREF_USER, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    protected void onStop() {
        mCompositeDisposable.clear();
        super.onStop();
    }
}
