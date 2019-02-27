package com.thm.gr_application.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thm.gr_application.R;
import com.thm.gr_application.adapter.BookmarkAdapter;
import com.thm.gr_application.model.ParkingLot;
import com.thm.gr_application.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class BookmarkActivity extends AppCompatActivity {

    private static final String TAG = "BookmarkActivity";
    private List<ParkingLot> mBookmarkedList;
    private BookmarkAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);
        setupVariables();
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForRemoval();
    }

    private void checkForRemoval() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_USER, MODE_PRIVATE);
        String json = sharedPreferences.getString(Constants.KEY_FAVORITE, null);
        List<Long> favorite = new Gson().fromJson(json, new TypeToken<List<Long>>() {
        }.getType());
        if (favorite.size() != mBookmarkedList.size()) {
            List<ParkingLot> changedList = new ArrayList<>();
            for (ParkingLot p : mBookmarkedList) {
                if (favorite.contains(p.getId())) {
                    changedList.add(p);
                }
            }
            mBookmarkedList = changedList;
            mAdapter.setBookmarkList(mBookmarkedList);
        }
    }

    private void setupVariables() {
        mBookmarkedList = (List<ParkingLot>) getIntent().getSerializableExtra(Constants.EXTRA_FAVORITE);
        for (ParkingLot p : mBookmarkedList) {
            Log.d(TAG, "setupVariables: " + p.getId());
        }
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar_bookmark);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Bookmark");
        }
        RecyclerView recyclerView = findViewById(R.id.rv_bookmark);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new BookmarkAdapter(this, mBookmarkedList);
        recyclerView.setAdapter(mAdapter);
    }
}
