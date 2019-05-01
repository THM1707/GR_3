package com.thm.gr_application.adapter;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.thm.gr_application.R;
import com.thm.gr_application.activity.ParkingInfoActivity;
import com.thm.gr_application.adapter.listener.BookmarkClickListener;
import com.thm.gr_application.model.ParkingLot;
import com.thm.gr_application.utils.Constants;

import java.util.List;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder> {

    private List<ParkingLot> mBookmarkList;
    private Context mContext;
    private Location mCurrentLocation;

    public BookmarkAdapter(Context context, List<ParkingLot> bookmarkList, double latitude,
            double longitude) {
        mContext = context;
        mBookmarkList = bookmarkList;
        mCurrentLocation = new Location("");
        mCurrentLocation.setLatitude(latitude);
        mCurrentLocation.setLongitude(longitude);
    }

    public void setBookmarkList(List<ParkingLot> bookmarkList) {
        this.mBookmarkList = bookmarkList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookmarkViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_favorite, viewGroup, false);
        return new BookmarkViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkViewHolder bookmarkViewHolder, int i) {
        ParkingLot p = mBookmarkList.get(i);
        Log.d("BookmarkAdapter", "onBindViewHolder: " + p.getLatitude() + ", " + p.getLongitude());
        Log.d("BookmarkAdapter", "onBindViewHolder: " + mCurrentLocation.getLatitude() + ", " + mCurrentLocation.getLongitude());
        Location destination = new Location("");
        destination.setLongitude(p.getLongitude());
        destination.setLatitude(p.getLatitude());
        float distance = mCurrentLocation.distanceTo(destination);
        Log.d("BookmarkAdapter", "onBindViewHolder: " + distance);
        bookmarkViewHolder.setListener((v, position) -> {
            Intent intent = new Intent(mContext, ParkingInfoActivity.class);
            intent.putExtra(Constants.EXTRA_PARKING_LOT, p.getId());
            intent.putExtra(Constants.EXTRA_DISTANCE, distance);
            mContext.startActivity(intent);
        });
        Glide.with(mContext)
                .load(Constants.END_POINT_URL + "/api/image/" + mBookmarkList.get(i)
                        .getImage()
                        .getId())
                .into(bookmarkViewHolder.mImage);
        bookmarkViewHolder.textCapacity.setText(String.valueOf(p.getCapacity()));
        bookmarkViewHolder.textAddress.setText(p.getAddress());
    }

    @Override
    public int getItemCount() {
        return mBookmarkList == null ? 0 : mBookmarkList.size();
    }

    public static class BookmarkViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private ImageView mImage;
        private TextView textAddress;
        private TextView textCapacity;
        private BookmarkClickListener mListener;

        BookmarkViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            textAddress = itemView.findViewById(R.id.tv_bookmarked_address);
            textCapacity = itemView.findViewById(R.id.tv_capacity);
            mImage = itemView.findViewById(R.id.iv_favorite);
        }

        void setListener(BookmarkClickListener listener) {
            mListener = listener;
        }

        @Override
        public void onClick(View v) {
            mListener.onClick(v, getAdapterPosition());
        }
    }
}
