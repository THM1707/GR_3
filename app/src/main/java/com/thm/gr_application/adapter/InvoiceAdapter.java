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
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.RecyclerView;
import com.thm.gr_application.R;
import com.thm.gr_application.adapter.listener.InvoiceAcceptListener;
import com.thm.gr_application.adapter.listener.InvoiceClickListener;
import com.thm.gr_application.model.Invoice;
import com.thm.gr_application.payload.InvoiceResponse;
import com.thm.gr_application.retrofit.AppServiceClient;
import com.thm.gr_application.utils.Constants;
import com.thm.gr_application.utils.DateUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.ViewHolder>
        implements Filterable {
    private List<Invoice> mInvoiceListFiltered;
    private Context mContext;
    private List<Invoice> mInvoiceList;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private InvoiceAcceptListener mListener;

    public InvoiceAdapter(Context context, List<Invoice> invoiceList) {
        mContext = context;
        mInvoiceList = invoiceList;
        mInvoiceListFiltered = invoiceList;
        mListener = (InvoiceAcceptListener) context;
    }

    public void setInvoiceList(List<Invoice> invoiceList) {
        mInvoiceList = invoiceList;
        mInvoiceListFiltered = invoiceList;
        notifyDataSetChanged();
    }

    private void removeInvoice(int position) {
        mInvoiceList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mInvoiceList.size());
    }

    public void addInvoice(Invoice invoice) {
        mInvoiceList.add(invoice);
        notifyItemInserted(mInvoiceList.size() - 1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new InvoiceAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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
                holder.mEndGroup.setVisibility(View.GONE);
                holder.mStatusText.setText("A");
                holder.mStatusText.setBackgroundResource(R.drawable.rounded_active);
                holder.mActionButton.setVisibility(View.VISIBLE);
                holder.mActionButton.setImageResource(R.drawable.ic_error_24dp);
                holder.mCreateTimeText.setText(createDateString);
                holder.setListener((v, pos) -> showAlert(
                        mContext.getString(R.string.alert_withdraw, invoice.getPlate()),
                        (dialog, which) -> checkout(invoice, pos)));
                break;
            case Constants.STATUS_PENDING:
                holder.mEndGroup.setVisibility(View.GONE);
                holder.mActionButton.setImageResource(R.drawable.ic_checked_24dp);
                holder.mActionButton.setVisibility(View.VISIBLE);
                holder.mStatusText.setText("P");
                holder.mStatusText.setBackgroundResource(R.drawable.rounded_pending);
                holder.mCreateTimeText.setText(createDateString);
                holder.setListener((v, position1) -> showAlert(
                        mContext.getString(R.string.alert_accept, invoice.getPlate()),
                        (dialog, which) -> accept(invoice)));
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
            default:
                break;
        }
    }

    private void checkout(Invoice invoice, int pos) {
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
                        removeInvoice(pos);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(mContext, R.string.error_server, Toast.LENGTH_SHORT).show();
                    }
                });
        mCompositeDisposable.add(disposable);
    }

    private void accept(Invoice invoice) {
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
                        mListener.onInvoiceAccepted();
                        mInvoiceList.set(mInvoiceList.indexOf(invoice),
                                invoiceResponse.getInvoice());
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(mContext, R.string.error_server, Toast.LENGTH_SHORT).show();
                    }
                });
        mCompositeDisposable.add(disposable);
    }

    private void showAlert(String message, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(message).setPositiveButton(R.string.action_ok, listener).create().show();
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
                    mInvoiceListFiltered = mInvoiceList;
                } else {
                    List<Invoice> filteredList = new ArrayList<>();
                    for (Invoice i : mInvoiceList) {
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
        Group mEndGroup;
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
            mEndGroup = view.findViewById(R.id.group_end);
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

    @Override
    public void onViewRecycled(@NonNull InvoiceAdapter.ViewHolder holder) {
        super.onViewRecycled(holder);
        mCompositeDisposable.clear();
    }
}
