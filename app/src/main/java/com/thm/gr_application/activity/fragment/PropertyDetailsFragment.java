package com.thm.gr_application.activity.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.thm.gr_application.R;
import com.thm.gr_application.activity.ReviewActivity;
import com.thm.gr_application.model.ParkingLot;
import com.thm.gr_application.utils.Constants;
import java.util.Locale;

public class PropertyDetailsFragment extends Fragment implements View.OnClickListener {

    private ParkingLot mProperty;

    public PropertyDetailsFragment() {

    }

    public static PropertyDetailsFragment newInstance(ParkingLot property) {
        PropertyDetailsFragment fragment = new PropertyDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.EXTRA_PROPERTY, property);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initArgs();
    }

    private void initArgs() {
        if (getArguments() != null) {
            mProperty = (ParkingLot) getArguments().getSerializable(Constants.EXTRA_PROPERTY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_manager_details, container, false);
        TextView textAddress = view.findViewById(R.id.tv_address);
        TextView textPosition = view.findViewById(R.id.tv_coordinates);
        TextView textCapacity = view.findViewById(R.id.tv_capacity);
        TextView textName = view.findViewById(R.id.tv_name);
        TextView textActiveTime = view.findViewById(R.id.tv_active_time);
        TextView starText = view.findViewById(R.id.tv_star);
        starText.setText(String.format(Locale.getDefault(), "%.1f", mProperty.getStar()));
        starText.setOnClickListener(this);
        ImageView imageView = view.findViewById(R.id.iv_detail);
        if (mProperty.getImage() != null) {
            Glide.with(this)
                    .load(Constants.END_POINT_URL + "/api/image/" + mProperty.getImage().getId())
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.parking_lot);
        }
        textAddress.setText(mProperty.getAddress());
        textCapacity.setText(String.valueOf(mProperty.getCapacity()));
        String activeTime = mProperty.getOpenTime() + " ~ " + mProperty.getCloseTime();
        textActiveTime.setText(activeTime);
        String pos = mProperty.getLatitude() + ", " + mProperty.getLongitude();
        textPosition.setText(pos);
        textName.setText(mProperty.getName());
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_star:
                Intent intent = new Intent(getActivity(), ReviewActivity.class);
                intent.putExtra(Constants.EXTRA_PARKING_LOT, mProperty.getId());
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}