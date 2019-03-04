package com.thm.gr_application.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.thm.gr_application.R;
import com.thm.gr_application.data.CarDatabase;
import com.thm.gr_application.model.Car;

import java.util.List;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {
    private Context mContext;
    private List<Car> mCarList;

    public CarAdapter(Context context, List<Car> carList) {
        mContext = context;
        mCarList = carList;
    }

    public void setCarList(List<Car> carList) {
        mCarList = carList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_car, viewGroup, false);
        return new CarViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder carViewHolder, int i) {
        Car car = mCarList.get(i);
        carViewHolder.mPlateText.setText(car.getLicensePlate());
        carViewHolder.setListener((v, position) -> {
            mCarList.remove(position);
            notifyDataSetChanged();
            new Thread(() -> {
                CarDatabase database = CarDatabase.getDatabase(mContext);
                database.getCarDao().delete(car);
            }).start();
        });
    }

    @Override
    public int getItemCount() {
        return mCarList == null ? 0 : mCarList.size();
    }

    public class CarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mPlateText;
        private ImageButton mCloseButton;
        private CarClickListener mCarClickListener;

        CarViewHolder(@NonNull View itemView) {
            super(itemView);
            mCloseButton = itemView.findViewById(R.id.ib_delete_car);
            mCloseButton.setOnClickListener(this);
            mPlateText = itemView.findViewById(R.id.tv_plate);
        }

        void setListener(CarClickListener listener) {
            mCarClickListener = listener;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ib_delete_car:
                    mCarClickListener.onDelete(v, getAdapterPosition());
                    break;
            }
        }
    }
}
