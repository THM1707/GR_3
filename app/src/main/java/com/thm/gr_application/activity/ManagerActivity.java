package com.thm.gr_application.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.thm.gr_application.R;
import com.thm.gr_application.model.ParkingLot;
import com.thm.gr_application.utils.Constants;

public class ManagerActivity extends AppCompatActivity implements View.OnClickListener {

    private ParkingLot mProperty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);
        Intent intent = getIntent();
        mProperty = (ParkingLot) intent.getSerializableExtra(Constants.EXTRA_PROPERTY);
        initViews();
    }

    private void initViews() {
        TextView textAddress = findViewById(R.id.tv_address);
        TextView textPosition = findViewById(R.id.tv_position);
        TextView textCapacity = findViewById(R.id.tv_capacity);
        TextView textSpare = findViewById(R.id.tv_spare);
        TextView textName = findViewById(R.id.tv_name);
        textAddress.setText(mProperty.getAddress());
        textCapacity.setText(String.valueOf(mProperty.getCapacity()));
        textSpare.setText(String.valueOf(mProperty.getCapacity()));
        String pos = mProperty.getLatitude() + ", " + mProperty.getLongitude();
        textPosition.setText(pos);
        textName.setText(mProperty.getName());
        findViewById(R.id.fab_user).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_user:
                SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_USER, MODE_PRIVATE);
                String token = sharedPreferences.getString(Constants.KEY_TOKEN, null);
                Intent intent = new Intent(this, MapsActivity.class);
                intent.putExtra(Constants.EXTRA_TOKEN, token);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}