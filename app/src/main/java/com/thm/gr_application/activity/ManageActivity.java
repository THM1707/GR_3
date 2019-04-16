package com.thm.gr_application.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.thm.gr_application.R;
import com.thm.gr_application.adapter.InvoiceAdapter;
import com.thm.gr_application.adapter.listener.InvoiceAcceptListener;
import com.thm.gr_application.model.Invoice;
import com.thm.gr_application.model.ParkingLot;
import com.thm.gr_application.payload.InvoiceResponse;
import com.thm.gr_application.payload.InvoicesResponse;
import com.thm.gr_application.retrofit.AppServiceClient;
import com.thm.gr_application.utils.Constants;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.json.JSONObject;
import retrofit2.HttpException;

public class ManageActivity extends AppCompatActivity
        implements Drawer.OnDrawerItemClickListener, View.OnClickListener, InvoiceAcceptListener {
    private TextView mPromptText;
    private Drawer mDrawer;
    private List<Invoice> mInvoiceList = new ArrayList<>();
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private InvoiceAdapter mAdapter;
    private ParkingLot mParkingLot;
    private int mPendingNumber;
    private int mAvailable;
    private MenuItem mRefreshMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);
        mParkingLot = (ParkingLot) getIntent().getSerializableExtra(Constants.EXTRA_PROPERTY);
        mPendingNumber = mParkingLot.getPending();
        DatabaseReference pendingRef =
                FirebaseDatabase.getInstance().getReference("pending/" + mParkingLot.getId());
        pendingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int pending = dataSnapshot.getValue(Integer.class);
                if (pending > mPendingNumber) {
                    mRefreshMenu.setIcon(R.drawable.ic_refresh_pending_24dp);
                }
                mPendingNumber = pending;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        initViews();
        getData();

        DatabaseReference availableRef = FirebaseDatabase.getInstance()
                .getReference("parking/" + mParkingLot.getId() + "/available");
        availableRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int available = dataSnapshot.getValue(Integer.class);
                mAvailable = available;
                if (mPromptText != null) {
                    mPromptText.setText(getPromptString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getData() {
        String token = getSharedPreferences(Constants.SHARED_PREF_USER, MODE_PRIVATE).getString(
                Constants.SHARED_TOKEN, null);
        Disposable disposable = AppServiceClient.getMyApiInstance(this)
                .getActiveInvoices(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<InvoicesResponse>() {
                    @Override
                    public void onSuccess(InvoicesResponse invoicesResponse) {
                        if (mRefreshMenu != null) {
                            mRefreshMenu.setIcon(R.drawable.ic_refresh_24dp);
                        }
                        mInvoiceList = invoicesResponse.getData();
                        mAdapter.setInvoiceList(mInvoiceList);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(ManageActivity.this, R.string.error_server,
                                Toast.LENGTH_SHORT).show();
                    }
                });

        mCompositeDisposable.add(disposable);
    }

    private void initViews() {
        setupDrawer();
        Toolbar toolbar = findViewById(R.id.toolbar_invoice_history);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Parking lot manage");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        }
        mPromptText = findViewById(R.id.tv_car_prompt);
        RecyclerView recyclerView = findViewById(R.id.rv_invoice);
        mAdapter = new InvoiceAdapter(this, mInvoiceList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);
        FloatingActionButton addButton = findViewById(R.id.fab_add);
        addButton.setColorFilter(Color.WHITE);
        addButton.setOnClickListener(this);
    }

    private void setupDrawer() {
        SharedPreferences sharedPreferences =
                getSharedPreferences(Constants.SHARED_PREF_USER, MODE_PRIVATE);
        String name = sharedPreferences.getString(Constants.SHARED_NAME, null);
        String email = sharedPreferences.getString(Constants.SHARED_EMAIL, null);
        int gender = sharedPreferences.getInt(Constants.SHARED_GENDER, 0);
        AccountHeader headerResult = new AccountHeaderBuilder().withActivity(this)
                .withHeaderBackground(R.drawable.background)
                .addProfiles(new ProfileDrawerItem().withName(name)
                        .withEmail(email)
                        .withIcon(getResources().getDrawable(
                                gender == 0 ? R.drawable.ic_male : R.drawable.ic_female)))
                .withOnAccountHeaderProfileImageListener(
                        new AccountHeader.OnAccountHeaderProfileImageListener() {
                            @Override
                            public boolean onProfileImageClick(View view, IProfile profile,
                                    boolean current) {
                                Intent intent =
                                        new Intent(ManageActivity.this, ProfileActivity.class);
                                startActivity(intent);
                                return false;
                            }

                            @Override
                            public boolean onProfileImageLongClick(View view, IProfile profile,
                                    boolean current) {
                                return false;
                            }
                        })
                .withSelectionListEnabledForSingleProfile(false)
                .build();
        PrimaryDrawerItem historyItem =
                new PrimaryDrawerItem().withIdentifier(Constants.MANAGE_ITEM_HISTORY)
                        .withName("History")
                        .withIcon(R.drawable.ic_history_white_24dp)
                        .withIconTintingEnabled(true);
        PrimaryDrawerItem statisticItem =
                new PrimaryDrawerItem().withIdentifier(Constants.MANAGE_ITEM_STATISTIC)
                        .withName("Statistic")
                        .withIcon(R.drawable.ic_chart)
                        .withIconTintingEnabled(true);

        SecondaryDrawerItem detailsItem =
                new SecondaryDrawerItem().withIdentifier(Constants.MANAGE_ITEM_DETAILS)
                        .withName("Details")
                        .withIcon(R.drawable.ic_dashboard_black_24dp)
                        .withIconTintingEnabled(true);
        mDrawer = new DrawerBuilder().withAccountHeader(headerResult)
                .withActivity(this)
                .withSelectedItem(-1)
                .addDrawerItems(historyItem, statisticItem, new DividerDrawerItem(), detailsItem)
                .withOnDrawerItemClickListener(this)
                .build();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                if (!mDrawer.isDrawerOpen()) {
                    mDrawer.openDrawer();
                }
                return true;
            case R.id.menu_refresh:
                refresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refresh() {
        getData();
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        long id = drawerItem.getIdentifier();
        if (id == Constants.MANAGE_ITEM_HISTORY) {
            startActivity(new Intent(ManageActivity.this, InvoiceHistoryActivity.class));
        } else if (id == Constants.MANAGE_ITEM_STATISTIC) {
            startActivity(new Intent(ManageActivity.this, StatisticActivity.class));
        } else if (id == Constants.MANAGE_ITEM_DETAILS) {
            Intent intent = new Intent(ManageActivity.this, PropertyInfoActivity.class);
            intent.putExtra(Constants.EXTRA_PARKING_LOT, mParkingLot.getId());
            startActivity(intent);
        }
        mDrawer.closeDrawer();
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.manage_menu, menu);
        mRefreshMenu = menu.findItem(R.id.menu_refresh);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                mAdapter.getFilter().filter(text);
                return true;
            }
        });
        return true;
    }

    @Override
    protected void onStop() {
        mCompositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_add:
                checkInCar();
                break;
            default:
                break;
        }
    }

    private void checkInCar() {
        String token =
                getSharedPreferences(Constants.SHARED_PREF_USER, Context.MODE_PRIVATE).getString(
                        Constants.SHARED_TOKEN, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_invoice, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        view.findViewById(R.id.bt_add).setOnClickListener(v -> {
            EditText editText = view.findViewById(R.id.et_plate);
            String plate = editText.getText().toString();
            Disposable disposable = AppServiceClient.getMyApiInstance(this)
                    .createBooking(token, plate)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableSingleObserver<InvoiceResponse>() {
                        @Override
                        public void onSuccess(InvoiceResponse invoiceResponse) {
                            mAdapter.addInvoice(invoiceResponse.getInvoice());
                            mParkingLot = invoiceResponse.getParkingLot();
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (e instanceof HttpException) {
                                try {
                                    JSONObject jObjError = new JSONObject(
                                            ((HttpException) e).response().errorBody().string());
                                    Toast.makeText(ManageActivity.this,
                                            jObjError.getString("message"), Toast.LENGTH_SHORT)
                                            .show();
                                } catch (Exception ex) {
                                    Toast.makeText(ManageActivity.this, ex.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(ManageActivity.this, R.string.error_server,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            mCompositeDisposable.add(disposable);
            dialog.dismiss();
        });
        dialog.show();
    }

    public String getPromptString() {
        return mAvailable == 0 ? getString(R.string.prompt_no_car)
                : String.format(Locale.getDefault(), "Current: %d / %d",
                        mParkingLot.getCapacity() - mAvailable, mParkingLot.getCapacity());
    }

    @Override
    public void onInvoiceAccepted() {
        mPendingNumber--;
    }
}
