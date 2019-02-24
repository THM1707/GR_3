package com.thm.gr_application.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.thm.gr_application.R;
import com.thm.gr_application.adapter.BookmarkAdapter;
import com.thm.gr_application.model.ParkingLot;
import com.thm.gr_application.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class BookmarkActivity extends AppCompatActivity {

    private static final String TAG = "BookmarkActivity";
    private List<ParkingLot> mBookmarkedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);
        setupVariables();
        initViews();

    }

    private void setupVariables() {
        mBookmarkedList = (List<ParkingLot>) getIntent().getSerializableExtra(Constants.EXTRA_FAVORITE);
        for (ParkingLot p: mBookmarkedList) {
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
        BookmarkAdapter adapter = new BookmarkAdapter(this, mBookmarkedList);
        recyclerView.setAdapter(adapter);
    }
}
