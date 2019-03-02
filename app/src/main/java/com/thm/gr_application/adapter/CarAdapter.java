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

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {
    private static final String TAG = "CarAdapter";
    private Context mContext;
    private List<Car> mCarList;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    public CarAdapter(Context context, List<Car> carList) {
        mContext = context;
        mCarList = carList;
    }

    public void setCarList(List<Car> carList) {
        mCarList = carList;
        notifyDataSetChanged();
    }

    public void addCar(Car car) {
        mCarList.add(car);
        notifyItemInserted(mCarList.size() - 1);
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
            Disposable disposable = Completable.fromAction(() -> CarDatabase.getDatabase(mContext).getCarDao().delete(car.getLicensePlate()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableCompletableObserver() {
                        @Override
                        public void onComplete() {
                            mCarList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, mCarList.size());
                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });
            mCompositeDisposable.add(disposable);
        });
    }

    @Override
    public void onViewRecycled(@NonNull CarViewHolder holder) {
        mCompositeDisposable.clear();
        super.onViewRecycled(holder);
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
