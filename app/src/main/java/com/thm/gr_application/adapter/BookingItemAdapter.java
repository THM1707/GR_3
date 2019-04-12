package com.thm.gr_application.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.thm.gr_application.R;
import com.thm.gr_application.adapter.listener.InvoiceClickListener;
import com.thm.gr_application.model.Invoice;
import com.thm.gr_application.model.ParkingLot;
import com.thm.gr_application.payload.InvoiceResponse;
import com.thm.gr_application.retrofit.AppServiceClient;
import com.thm.gr_application.utils.Constants;
import com.thm.gr_application.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class BookingItemAdapter extends RecyclerView.Adapter<BookingItemAdapter.ViewHolder>
        implements Filterable {

    private List<Invoice> mActiveList;
    private Context mContext;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private List<Invoice> mInvoiceListFiltered;
    private List<Invoice> mAllList;
    private boolean doShowHistory;
    private ParkingLotChangedListener mListenter;

    public BookingItemAdapter(Context context, List<Invoice> items, List<Invoice> allList,
            ParkingLotChangedListener listener) {
        mContext = context;
        mActiveList = items;
        mInvoiceListFiltered = items;
        doShowHistory = false;
        mAllList = allList;
        mListenter = listener;
    }

    public void setData(boolean bool) {
        doShowHistory = bool;
        if (bool) {
            mInvoiceListFiltered = mAllList;
            notifyDataSetChanged();
        } else {
            mInvoiceListFiltered = mActiveList;
            notifyDataSetChanged();
        }
    }

    public void addInvoice(Invoice invoice) {
        mActiveList.add(invoice);
        mAllList.add(invoice);
        notifyItemInserted(doShowHistory ? mAllList.size() - 1 : mActiveList.size() - 1);
    }

    public void removeInvoice(int position) {
        mActiveList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mActiveList.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Invoice invoice = mInvoiceListFiltered.get(position);
        holder.mPlateText.setText(invoice.getPlate());
        if (invoice.isBooked()) {
            holder.mBookImage.setVisibility(View.VISIBLE);
        } else {
            holder.mBookImage.setVisibility(View.INVISIBLE);
        }
        String createDateString =
                DateUtils.getFormattedDateTimeStringFromISOString(invoice.getCreatedDate());
        String endDateString = "";
        if (invoice.getEndDate() != null) {
            endDateString = DateUtils.getFormattedDateTimeStringFromISOString(invoice.getEndDate());
        }
        switch (invoice.getStatus()) {
            case Constants.STATUS_ACTIVE:
                holder.mStatusText.setText("A");
                holder.mStatusText.setBackgroundResource(R.drawable.rounded_active);
                holder.mActionButton.setVisibility(View.VISIBLE);
                holder.mActionButton.setImageResource(R.drawable.ic_error_24dp);
                holder.mCreateTimeText.setText(createDateString);
                holder.setListener((v, position1) -> showAlert(
                        mContext.getString(R.string.alert_withdraw, invoice.getPlate()),
                        (dialog, which) -> withdraw(holder, invoice, position1)));
                break;
            case Constants.STATUS_CANCEL:
                holder.mStatusText.setText("C");
                holder.mStatusText.setBackgroundResource(R.drawable.rounded_cancel);
                holder.mActionButton.setVisibility(View.INVISIBLE);
                holder.mCreateTimeText.setText(createDateString);
                holder.mEndTimeText.setText(endDateString);
                break;
            case Constants.STATUS_DONE:
                holder.mStatusText.setText("D");
                holder.mStatusText.setBackgroundResource(R.drawable.rounded_done);
                holder.mActionButton.setVisibility(View.INVISIBLE);
                holder.mCreateTimeText.setText(createDateString);
                holder.mEndTimeText.setText(endDateString);
                break;
            case Constants.STATUS_PENDING:
                holder.mActionButton.setImageResource(R.drawable.ic_checked_24dp);
                holder.mActionButton.setVisibility(View.VISIBLE);
                holder.mStatusText.setText("P");
                holder.mStatusText.setBackgroundResource(R.drawable.rounded_pending);
                holder.mCreateTimeText.setText(createDateString);
                holder.setListener((v, position1) -> showAlert(
                        mContext.getString(R.string.alert_accept, invoice.getPlate()),
                        (dialog, which) -> accept(holder, invoice)));
                break;
            default:
                holder.mEndTimeText.setText("DEAD");
                break;
        }
    }

    private void showAlert(String message, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(message).setPositiveButton(R.string.action_ok, listener).create().show();
    }

    private void accept(ViewHolder holder, Invoice invoice) {
        String token =
                mContext.getSharedPreferences(Constants.SHARED_PREF_USER, Context.MODE_PRIVATE)
                        .getString(Constants.SHARED_TOKEN, null);
        Disposable disposable = AppServiceClient.getMyApiInstance(mContext)
                .acceptBooking(token, invoice.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<InvoiceResponse>() {
                    @Override
                    public void onSuccess(InvoiceResponse invoiceResponse) {
                        holder.mStatusText.setText("A");
                        holder.mStatusText.setBackgroundResource(R.drawable.rounded_active);
                        holder.mActionButton.setImageResource(R.drawable.ic_error_24dp);
                        holder.setListener((v, position1) -> showAlert(
                                mContext.getString(R.string.alert_withdraw, invoice.getPlate()),
                                (dialog, which) -> withdraw(holder, invoice, position1)));
                        changeInvoice(mActiveList, invoice, invoiceResponse.getInvoice());
                        changeInvoice(mAllList, invoice, invoiceResponse.getInvoice());
                        mListenter.onParkingLotChange(invoiceResponse.getParkingLot());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(mContext, R.string.error_server, Toast.LENGTH_SHORT).show();
                    }
                });
        mCompositeDisposable.add(disposable);
    }

    private void changeInvoice(List<Invoice> list, Invoice from, Invoice to) {
        list.set(list.indexOf(from), to);
    }

    private void withdraw(ViewHolder holder, Invoice invoice, int position) {
        String token =
                mContext.getSharedPreferences(Constants.SHARED_PREF_USER, Context.MODE_PRIVATE)
                        .getString(Constants.SHARED_TOKEN, null);
        Disposable disposable = AppServiceClient.getMyApiInstance(mContext)
                .withdraw(token, invoice.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<InvoiceResponse>() {
                    @Override
                    public void onSuccess(InvoiceResponse invoiceResponse) {
                        if (doShowHistory) {
                            holder.mEndTimeText.setText(
                                    DateUtils.getFormattedDateTimeStringFromISOString(
                                            invoiceResponse.getInvoice().getEndDate()));
                            holder.mStatusText.setText("D");
                            holder.mStatusText.setBackgroundResource(R.drawable.rounded_done);
                            holder.mActionButton.setVisibility(View.INVISIBLE);
                            mActiveList.remove(invoice);
                        } else {
                            removeInvoice(position);
                        }
                        changeInvoice(mAllList, invoice, invoiceResponse.getInvoice());
                        mListenter.onParkingLotChange(invoiceResponse.getParkingLot());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(mContext, R.string.error_server, Toast.LENGTH_SHORT).show();
                    }
                });
        mCompositeDisposable.add(disposable);
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        mCompositeDisposable.clear();
    }

    @Override
    public int getItemCount() {
        return mInvoiceListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    mInvoiceListFiltered = doShowHistory ? mAllList : mActiveList;
                } else {
                    List<Invoice> filteredList = new ArrayList<>();
                    for (Invoice i : doShowHistory ? mAllList : mActiveList) {
                        if (i.getPlate().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(i);
                        }
                    }
                    mInvoiceListFiltered = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = mInvoiceListFiltered;
                return filterResults;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mInvoiceListFiltered = (List<Invoice>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View mView;
        TextView mStatusText;
        TextView mPlateText;
        TextView mCreateTimeText;
        TextView mEndTimeText;
        ImageView mBookImage;
        ImageButton mActionButton;
        InvoiceClickListener mClickListener;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mStatusText = view.findViewById(R.id.tv_status);
            mPlateText = view.findViewById(R.id.tv_plate);
            mCreateTimeText = view.findViewById(R.id.tv_create_time);
            mEndTimeText = view.findViewById(R.id.tv_end_time);
            mBookImage = view.findViewById(R.id.iv_booked);
            mActionButton = view.findViewById(R.id.iv_action);
            mActionButton.setOnClickListener(this);
        }

        void setListener(InvoiceClickListener listener) {
            mClickListener = listener;
        }

        @Override
        public void onClick(View v) {
            mClickListener.action(v, getAdapterPosition());
        }
    }

    public interface ParkingLotChangedListener {
        void onParkingLotChange(ParkingLot parkingLot);
    }
}
