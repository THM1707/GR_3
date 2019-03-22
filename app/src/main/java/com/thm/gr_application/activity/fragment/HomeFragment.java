package com.thm.gr_application.activity.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.thm.gr_application.R;
import com.thm.gr_application.adapter.BookingItemAdapter;
import com.thm.gr_application.model.Invoice;
import com.thm.gr_application.model.ParkingLot;
import com.thm.gr_application.payload.InvoiceResponse;
import com.thm.gr_application.retrofit.AppServiceClient;
import com.thm.gr_application.utils.Constants;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link HomeFragmentListeners}
 * interface.
 */
public class HomeFragment extends Fragment implements View.OnClickListener{

    private HomeFragmentListeners mListener;
    private List<Invoice> mAllList;
    private List<Invoice> mActiveList;
    private AVLoadingIndicatorView mProgressView;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private BookingItemAdapter mAdapter;
    private CheckBox mHistoryCheckBox;
    private TextView mCurrentText;
    private int mCapacity;
    private int mCurrent;

    public HomeFragment() {
    }

    public static HomeFragment newInstance(int capacity, int current, List<Invoice> allList, List<Invoice> activeList) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.BUNDLE_ALL_LIST, (Serializable) allList);
        args.putSerializable(Constants.BUNDLE_ACTIVE_LIST, (Serializable) activeList);
        args.putInt(Constants.BUNDLE_CAPACITY, capacity);
        args.putInt(Constants.BUNDLE_CURRENT, current);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAllList = (List<Invoice>) getArguments().getSerializable(Constants.BUNDLE_ALL_LIST);
            mActiveList = (List<Invoice>) getArguments().getSerializable(Constants.BUNDLE_ACTIVE_LIST);
            mCapacity = getArguments().getInt(Constants.BUNDLE_CAPACITY);
            mCurrent = getArguments().getInt(Constants.BUNDLE_CURRENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mHistoryCheckBox = view.findViewById(R.id.cb_history);
        mHistoryCheckBox.setOnClickListener(this);
        mCurrentText = view.findViewById(R.id.tv_current);
        setCurrentText();
        view.findViewById(R.id.iv_refresh).setOnClickListener(this);
        FloatingActionButton addButton = view.findViewById(R.id.fab_add);
        addButton.setColorFilter(Color.WHITE);
        mProgressView = view.findViewById(R.id.progress_home);
        RecyclerView recyclerView = view.findViewById(R.id.rv_invoice);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        EditText searchView = view.findViewById(R.id.et_search);
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mAdapter = new BookingItemAdapter(getActivity(), mActiveList, mAllList, parkingLot -> {
            Log.d("HOME FRAGMENT", "onParkingLotChange: " + parkingLot.getCurrent());
            mListener.onPropertyChanged(parkingLot);
            mCurrent = parkingLot.getCurrent();
            setCurrentText();
        });
        recyclerView.setAdapter(mAdapter);
        addButton.setOnClickListener(this);
        return view;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof HomeFragmentListeners) {
            mListener = (HomeFragmentListeners) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement HomeFragmentListeners");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mCompositeDisposable.clear();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_add:
                showAddDialog();
                break;
            case R.id.cb_history:
                if (mHistoryCheckBox.isChecked()) {
                    mAdapter.setData(true);
                } else {
                    mAdapter.setData(false);
                }
                break;
            case R.id.iv_refresh:
                mListener.onRefreshRequested(mAllList, mActiveList, mAdapter, mCurrentText);
                break;
            default:
                break;
        }
    }

    private void showAddDialog() {
        String token = getActivity().getSharedPreferences(Constants.SHARED_PREF_USER, Context.MODE_PRIVATE).getString(Constants.KEY_TOKEN, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getLayoutInflater().inflate(R.layout.dialog_invoice, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        view.findViewById(R.id.bt_add).setOnClickListener(v -> {
            mProgressView.show();
            EditText editText = view.findViewById(R.id.et_plate);
            String plate = editText.getText().toString();
            Disposable disposable = AppServiceClient.getMyApiInstance(getActivity()).createBooking(token, plate)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableSingleObserver<InvoiceResponse>() {
                        @Override
                        public void onSuccess(InvoiceResponse invoiceResponse) {
                            mProgressView.hide();
                            updateAdapter(invoiceResponse);
                            mListener.onPropertyChanged(invoiceResponse.getParkingLot());
                            mCurrent = invoiceResponse.getParkingLot().getCurrent();
                            setCurrentText();
                        }

                        @Override
                        public void onError(Throwable e) {
                            mProgressView.hide();
                            if (e instanceof HttpException) {
                                try {
                                    JSONObject jObjError = new JSONObject(((HttpException) e).response().errorBody().string());
                                    Toast.makeText(getActivity(), jObjError.getString("message"), Toast.LENGTH_SHORT).show();
                                } catch (Exception ex) {
                                    Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getActivity(), R.string.error_server, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            mCompositeDisposable.add(disposable);
            dialog.dismiss();
        });
        dialog.show();
    }

    private void updateAdapter(InvoiceResponse invoiceResponse) {
        mAdapter.addInvoice(invoiceResponse.getInvoice());
    }

    public interface HomeFragmentListeners {
        void onRefreshRequested(List<Invoice> allList, List<Invoice> activeList, BookingItemAdapter adapter, TextView currentText);
        void onPropertyChanged(ParkingLot parkingLot);
    }

    public void setCurrentText() {
        String text = String.format(Locale.getDefault(), "Now: %d/%d", mCurrent, mCapacity);
        mCurrentText.setText(text);
    }
}
