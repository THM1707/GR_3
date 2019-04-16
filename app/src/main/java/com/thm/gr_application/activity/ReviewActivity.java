package com.thm.gr_application.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;
import com.thm.gr_application.R;
import com.thm.gr_application.adapter.ReviewAdapter;
import com.thm.gr_application.model.Review;
import com.thm.gr_application.payload.ReviewRequest;
import com.thm.gr_application.payload.ReviewResposne;
import com.thm.gr_application.payload.ReviewsResponse;
import com.thm.gr_application.retrofit.AppServiceClient;
import com.thm.gr_application.utils.Constants;
import com.thm.gr_application.utils.DateUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;
import retrofit2.HttpException;

public class ReviewActivity extends AppCompatActivity implements View.OnClickListener {

    private List<Review> mReviewList = new ArrayList<>();
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private TextView mDateText;
    private TextView mCommentText;
    private Group mCommentGroup;
    private ReviewAdapter mReviewAdapter;
    private SharedPreferences mSharedPreferences;
    private Review mOwnReview;
    private SimpleRatingBar mOwnRatingBar;
    private TextView mPromptText;
    private Long mParkingLotId;
    private boolean isManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        mSharedPreferences = getSharedPreferences(Constants.SHARED_PREF_USER, MODE_PRIVATE);
        mParkingLotId = getIntent().getLongExtra(Constants.EXTRA_PARKING_LOT, -1);
        String role = mSharedPreferences.getString(Constants.SHARED_ROLE, null);
        if (role != null) {
            isManager = role.equals(getResources().getString(R.string.role_manager));
        }
        initViews();
        getData();
    }

    @Override
    protected void onStop() {
        mCompositeDisposable.clear();
        super.onStop();
    }

    private void getData() {
        String token = mSharedPreferences.getString(Constants.SHARED_TOKEN, null);
        String email = mSharedPreferences.getString(Constants.SHARED_EMAIL, null);
        Disposable disposable = AppServiceClient.getMyApiInstance(this)
                .getReviews(token, mParkingLotId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ReviewsResponse>() {
                    @Override
                    public void onSuccess(ReviewsResponse reviewsResponse) {
                        mReviewList = reviewsResponse.getData();
                        if (mReviewList.isEmpty()) {
                            mPromptText.setText(R.string.prompt_no_review);
                        }
                        mReviewAdapter.setReviewList(mReviewList);
                        if (!isManager) {
                            for (Review r : mReviewList) {
                                if (r.getOwnerEmail().equals(email)) {
                                    mOwnReview = r;
                                    mCommentText.setText(r.getComment());
                                    mOwnRatingBar.setRating(r.getStar());
                                    mCommentGroup.setVisibility(View.VISIBLE);
                                    mDateText.setText(DateUtils.getFormattedDateStringFromISOString(
                                            r.getUpdatedAt()));
                                } else {
                                    mCommentText.setText(R.string.message_no_comment);
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof HttpException) {
                            Toast.makeText(ReviewActivity.this, ((HttpException) e).message(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ReviewActivity.this, R.string.error_server,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        mCompositeDisposable.add(disposable);
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar_review);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            String title = getIntent().getStringExtra(Constants.EXTRA_PARKING_LOT_NAME);
            actionBar.setTitle(title);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Group groupOwnReview = findViewById(R.id.group_own_review);
        if (isManager) {
            groupOwnReview.setVisibility(View.GONE);
        }
        mPromptText = findViewById(R.id.tv_review_prompt);
        mCommentText = findViewById(R.id.tv_comment);
        mCommentText.setOnClickListener(this);
        mDateText = findViewById(R.id.tv_date);
        ImageButton deleteButton = findViewById(R.id.iv_delete);
        ImageButton editButton = findViewById(R.id.iv_edit);
        deleteButton.setOnClickListener(this);
        editButton.setOnClickListener(this);
        mCommentGroup = findViewById(R.id.group_review);
        mOwnRatingBar = findViewById(R.id.rating);
        RecyclerView reviewRecycler = findViewById(R.id.rv_reviews);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        reviewRecycler.setLayoutManager(layoutManager);
        mReviewAdapter = new ReviewAdapter(mReviewList);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(this, layoutManager.getOrientation());
        reviewRecycler.setAdapter(mReviewAdapter);
        reviewRecycler.addItemDecoration(dividerItemDecoration);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_edit:
                showEditReviewDialog();
                break;
            case R.id.iv_delete:
                showConfirmDialog();
                break;
            case R.id.tv_comment:
                showSubmitReviewDialog();
                break;
        }
    }

    private void showConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_delete_car)
                .setPositiveButton(R.string.action_ok, (dialog, which) -> deleteReview())
                .create()
                .show();
    }

    private void deleteReview() {
        String token = mSharedPreferences.getString(Constants.SHARED_TOKEN, null);
        Disposable disposable = AppServiceClient.getMyApiInstance(this)
                .deleteReview(token, mOwnReview.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        mReviewList.remove(mOwnReview);
                        mReviewAdapter.setReviewList(mReviewList);
                        mCommentText.setText(R.string.message_no_comment);
                        mCommentGroup.setVisibility(View.GONE);
                        mOwnRatingBar.setRating(0);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(ReviewActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
        mCompositeDisposable.add(disposable);
    }

    // TODO: 11/04/2019 handle error properly
    private void showSubmitReviewDialog() {
        String token = mSharedPreferences.getString(Constants.SHARED_TOKEN, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_review, null);
        RatingBar ratingBar = view.findViewById(R.id.own_rating);
        EditText commentText = view.findViewById(R.id.et_comment);
        Button button = view.findViewById(R.id.bt_submit);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        button.setOnClickListener(v -> {
            if (ratingBar.getRating() == 0) {
                Toast.makeText(this, "Stars must greater than 0", Toast.LENGTH_SHORT).show();
            } else {
                String comment = commentText.getText().toString();
                if (comment.isEmpty()) {
                    comment = "No comment";
                }
                ReviewRequest request =
                        new ReviewRequest(mParkingLotId, comment, (int) ratingBar.getRating());
                Disposable disposable = AppServiceClient.getMyApiInstance(ReviewActivity.this)
                        .submitReview(token, request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<ReviewResposne>() {
                            @Override
                            public void onSuccess(ReviewResposne response) {
                                mCommentGroup.setVisibility(View.VISIBLE);
                                mOwnReview = response.getData();
                                mDateText.setText(DateUtils.getFormattedDateStringFromISOString(
                                        mOwnReview.getUpdatedAt()));
                                mReviewList.add(mOwnReview);
                                mReviewAdapter.setReviewList(mReviewList);
                                mCommentText.setText(mOwnReview.getComment());
                                mOwnRatingBar.setRating(mOwnReview.getStar());
                                if (mReviewList.size() == 1) {
                                    mPromptText.setText(R.string.prompt_all_review);
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                Toast.makeText(ReviewActivity.this, e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                mCompositeDisposable.add(disposable);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    // TODO: 11/04/2019 handle error
    private void showEditReviewDialog() {
        String token = mSharedPreferences.getString(Constants.SHARED_TOKEN, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_review, null);
        RatingBar ratingBar = view.findViewById(R.id.own_rating);
        ratingBar.setRating(mOwnReview.getStar());
        EditText commentText = view.findViewById(R.id.et_comment);
        commentText.setText(mOwnReview.getComment());
        Button button = view.findViewById(R.id.bt_submit);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        button.setOnClickListener(v -> {
            String comment = commentText.getText().toString();
            if (comment.isEmpty()) {
                comment = "No comment";
            }
            Disposable disposable = AppServiceClient.getMyApiInstance(ReviewActivity.this)
                    .editReview(token, mOwnReview.getId(), (int) ratingBar.getRating(), comment)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableSingleObserver<ReviewResposne>() {
                        @Override
                        public void onSuccess(ReviewResposne response) {
                            int index = mReviewList.indexOf(mOwnReview);
                            mOwnReview = response.getData();
                            mDateText.setText(DateUtils.getFormattedDateStringFromISOString(
                                    mOwnReview.getUpdatedAt()));
                            mCommentText.setText(mOwnReview.getComment());
                            mOwnRatingBar.setRating(mOwnReview.getStar());
                            mReviewList.set(index, mOwnReview);
                            mReviewAdapter.setReviewList(mReviewList);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(ReviewActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
            mCompositeDisposable.add(disposable);
            dialog.dismiss();
        });
        dialog.show();
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
}
