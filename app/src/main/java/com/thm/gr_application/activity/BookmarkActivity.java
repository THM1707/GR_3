package com.thm.gr_application.activity;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.thm.gr_application.R;
import com.thm.gr_application.adapter.BookmarkAdapter;
import com.thm.gr_application.model.ParkingLot;
import com.thm.gr_application.payload.ParkingLotsResponse;
import com.thm.gr_application.retrofit.AppServiceClient;
import com.thm.gr_application.utils.Constants;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.List;

public class BookmarkActivity extends AppCompatActivity {

    private List<ParkingLot> mBookmarkedList;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private BookmarkAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);
        initViews();
    }

    @Override
    protected void onResume() {
        setupVariables();
        super.onResume();
    }

    private void setupVariables() {
        String token = getSharedPreferences(Constants.SHARED_PREF_USER, MODE_PRIVATE).getString(
                Constants.SHARED_TOKEN, null);
        Disposable disposable = AppServiceClient.getMyApiInstance(this)
                .getFavorite(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ParkingLotsResponse>() {
                    @Override
                    public void onSuccess(ParkingLotsResponse parkingLotsResponse) {
                        mBookmarkedList = parkingLotsResponse.getData();
                        if (mAdapter != null) {
                            mAdapter.setBookmarkList(mBookmarkedList);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(BookmarkActivity.this, R.string.error_server,
                                Toast.LENGTH_SHORT).show();
                    }
                });
        mCompositeDisposable.add(disposable);
    }

    private void initViews() {
        double longitude = getIntent().getDoubleExtra(Constants.EXTRA_LONGITUDE, 0);
        double latitude = getIntent().getDoubleExtra(Constants.EXTRA_LATITUDE, 0);
        Toolbar toolbar = findViewById(R.id.toolbar_account);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Bookmark");
        }
        RecyclerView recyclerView = findViewById(R.id.rv_bookmark);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new BookmarkAdapter(this, mBookmarkedList, latitude, longitude);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onStop() {
        mCompositeDisposable.clear();
        super.onStop();
    }
}
