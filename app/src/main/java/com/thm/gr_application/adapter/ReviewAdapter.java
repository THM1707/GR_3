package com.thm.gr_application.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;
import com.thm.gr_application.R;
import com.thm.gr_application.model.Review;
import com.thm.gr_application.utils.DateUtils;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
    private List<Review> mReviewList;

    public ReviewAdapter(List<Review> reviewList) {
        mReviewList = reviewList;
    }

    public void setReviewList(List<Review> reviewList) {
        mReviewList.clear();
        mReviewList.addAll(reviewList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = mReviewList.get(position);
        holder.ratingBar.setRating(review.getStar());
        holder.commentText.setText(review.getComment());
        holder.nameText.setText(review.getOwnerName());
        holder.dateText.setText(
                DateUtils.getFormattedDateStringFromISOString(review.getUpdatedAt()));
    }

    @Override
    public int getItemCount() {
        return mReviewList == null ? 0 : mReviewList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView dateText;
        private TextView nameText;
        private TextView commentText;
        private SimpleRatingBar ratingBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.tv_date);
            nameText = itemView.findViewById(R.id.tv_user_name);
            commentText = itemView.findViewById(R.id.tv_comment);
            ratingBar = itemView.findViewById(R.id.rating);
        }
    }
}
