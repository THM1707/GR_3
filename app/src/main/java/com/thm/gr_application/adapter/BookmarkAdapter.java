package com.thm.gr_application.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thm.gr_application.R;
import com.thm.gr_application.activity.ParkingLotDetailsActivity;
import com.thm.gr_application.model.ParkingLot;
import com.thm.gr_application.utils.Constants;

import java.util.List;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder> {

    private List<ParkingLot> mBookmarkList;
    private Context mContext;

    public BookmarkAdapter(Context context, List<ParkingLot> bookmarkList) {
        mContext = context;
        mBookmarkList = bookmarkList;
    }

    public void setBookmarkList(List<ParkingLot> bookmarkList) {
        this.mBookmarkList = bookmarkList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookmarkViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.bookmarked_item, viewGroup, false);
        return new BookmarkViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkViewHolder bookmarkViewHolder, int i) {
        ParkingLot p = mBookmarkList.get(i);
        bookmarkViewHolder.setListener((v, position) -> {
            Intent intent = new Intent(mContext, ParkingLotDetailsActivity.class);
            intent.putExtra(Constants.EXTRA_PARKING_LOT, p);
            intent.putExtra(Constants.EXTRA_IS_FAVORITE, true);
            mContext.startActivity(intent);
        });
        bookmarkViewHolder.textCapacity.setText(String.valueOf(p.getCapacity()));
        bookmarkViewHolder.textAddress.setText(p.getAddress());

    }

    @Override
    public int getItemCount() {
        return mBookmarkList == null ? 0 : mBookmarkList.size();
    }

    public static class BookmarkViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView textAddress;
        private TextView textCapacity;
        private BookmarkClickListener mListener;

        public BookmarkViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            textAddress = itemView.findViewById(R.id.tv_bookmarked_address);
            textCapacity = itemView.findViewById(R.id.tv_capacity);
        }

        public void setListener(BookmarkClickListener listener) {
            mListener = listener;
        }

        @Override
        public void onClick(View v) {
            mListener.onClick(v, getAdapterPosition());
        }
    }
}
