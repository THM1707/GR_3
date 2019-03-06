package com.thm.gr_application.activity;

import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.thm.gr_application.R;
import com.thm.gr_application.adapter.CarAdapter;
import com.thm.gr_application.data.CarDatabase;
import com.thm.gr_application.model.Car;
import com.thm.gr_application.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class CarActivity extends AppCompatActivity {
    private List<Car> mCarList = new ArrayList<>();
    private CarDatabase mCarDatabase;
    private EditText mLicenseText;
    private CarAdapter mCarAdapter;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car);
        mCarDatabase = CarDatabase.getDatabase(this);
        Disposable disposable = mCarDatabase.getCarDao().getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<List<Car>>() {
                    @Override
                    public void onSuccess(List<Car> cars) {
                        mCarList = cars;
                        if (mCarAdapter != null) {
                            mCarAdapter.setCarList(mCarList);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(CarActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        mCompositeDisposable.add(disposable);
        initViews();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar_car);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        RecyclerView carRecycle = findViewById(R.id.rv_car);
        carRecycle.setLayoutManager(new LinearLayoutManager(this));
        mCarAdapter = new CarAdapter(this, mCarList);
        carRecycle.setAdapter(mCarAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.car_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_insert_car:
                showInsertDialog();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onStop() {
        mCompositeDisposable.clear();
        super.onStop();
    }

    private void showInsertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_car, null);
        mLicenseText = view.findViewById(R.id.et_plate);
        RadioGroup radioGroup = view.findViewById(R.id.rg_car_type);

        builder.setView(view);
        AlertDialog dialog = builder.create();
        view.findViewById(R.id.bt_add_car).setOnClickListener(v -> {
            int checked = radioGroup.getCheckedRadioButtonId();
            int type = Constants.CAR_TYPE_CAR;
            switch (checked) {
                case R.id.rb_car:
                    break;
                case R.id.rb_bus:
                    type = Constants.CAR_TYPE_BUS;
                    break;
                case R.id.rb_van:
                    type = Constants.CAR_TYPE_TRUCK;
                    break;
            }
            Car car = new Car(type, mLicenseText.getText().toString());
            Disposable disposable = Completable.fromAction(() -> mCarDatabase.getCarDao().insert(car))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableCompletableObserver() {
                        @Override
                        public void onComplete() {
                            mCarAdapter.addCar(car);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(CarActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            mCompositeDisposable.add(disposable);
            dialog.dismiss();
        });
        dialog.show();
    }
}
