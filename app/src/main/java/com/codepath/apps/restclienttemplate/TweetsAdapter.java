package com.codepath.apps.restclienttemplate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.ImageViewTargetFactory;
import com.bumptech.glide.request.target.Target;
import com.codepath.apps.restclienttemplate.databinding.ActivityTimelineBinding;
import com.codepath.apps.restclienttemplate.databinding.ItemTweetBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder>{
    public static final String TAG = "TweetsAdapter";

    Context context;
    List<Tweet> tweets;

    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
    }

    // For each row, inflate a layout
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTweetBinding binding = ItemTweetBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    // Bind values based on position of the element
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        // Get data at position
        final Tweet tweet = tweets.get(position);
        String fScreenName = "@" + tweet.user.screenName;

        holder.binding.tvScreenName.setText(fScreenName);
        holder.binding.tvBody.setText(tweet.body);
        holder.binding.tvName.setText(tweet.user.name);

        // Setting recency text
        String rawJsonDate = tweet.createdAt;
        String formattedRecent = getRelativeTimeAgo(rawJsonDate);
        holder.binding.tvRecency.setText(formattedRecent);
        // Setting profile image
        Glide.with(context).load(tweet.user.profileImageUrl).transform(new RoundedCorners( 80)).into(holder.binding.ivProfileImage);

        // Setting media image if it exists.
        Log.i(TAG, "My media URL: " + tweet.mediaUrl);
        if (tweet.mediaUrl != null){
            holder.binding.pgBar.setVisibility(ProgressBar.VISIBLE);
            Glide.with(context)
                    .load(tweet.mediaUrl)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            holder.binding.pgBar.setVisibility(ProgressBar.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.binding.pgBar.setVisibility(ProgressBar.GONE);
                            return false;
                        }
                    })
                    .into(holder.binding.ivMedia);
            holder.binding.ivMedia.setVisibility(ImageView.VISIBLE);
        }

        holder.binding.iconReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = ((TimelineActivity) context).getSupportFragmentManager();
                ComposeDialogFragment composeDialogFragment = ComposeDialogFragment.newInstance("Some Title", tweet);
                // Edit instance to take in twitter handle as argument
                composeDialogFragment.show(fm, "fragment_compose");
            }
        });
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    // Clean all elements of the recycler
    public void clear(){
        tweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<Tweet> tweetList){
        tweets.addAll(tweetList);
        notifyDataSetChanged();
    }

    // Define a View Holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemTweetBinding binding;

        public ViewHolder(ItemTweetBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(
                    dateMillis,
                    System.currentTimeMillis(),
                    DateUtils.SECOND_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return relativeDate;
    }
}
