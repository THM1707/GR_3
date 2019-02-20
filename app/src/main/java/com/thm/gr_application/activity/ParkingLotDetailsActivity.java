package com.thm.gr_application.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.thm.gr_application.R;
import com.thm.gr_application.model.ParkingLot;

public class ParkingLotDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_lot_details);
        Intent intent = getIntent();
        ParkingLot parkingLot = (ParkingLot) intent.getSerializableExtra("parking_lot");
        updateViews(parkingLot);

    }

    private void updateViews(ParkingLot parkingLot) {
        TextView textCapacity = findViewById(R.id.tv_capacity);
        TextView textAddress = findViewById(R.id.tv_address);
        textCapacity.setText(String.valueOf(parkingLot.getCapacity()));
        textAddress.setText(parkingLot.getAddress());
    }
}
