package com.codepath.apps.restclienttemplate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view);
    }

    // Bind values based on position of the element
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get data at position
        Tweet tweet = tweets.get(position);
        // Bind tweet to view holder
        holder.bind(tweet);
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
        ImageView ivProfileImage;
        ImageView ivMedia;
        ImageView iconReply;
        TextView tvBody;
        TextView tvName;
        TextView tvScreenName;
        TextView tvRecency;

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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            ivMedia = itemView.findViewById(R.id.ivMedia);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvName = itemView.findViewById(R.id.tvName);
            tvRecency = itemView.findViewById(R.id.tvRecency);

            iconReply = itemView.findViewById(R.id.iconReply);

        }

        public void bind(final Tweet tweet) {
            // Setting tweet body and screen name and name
            String fScreenName = "@" + tweet.user.screenName;
            tvScreenName.setText(fScreenName);
            tvBody.setText(tweet.body);
            tvName.setText(tweet.user.name);

            // Setting recency text
            String rawJsonDate = tweet.createdAt;
            String formattedRecent = getRelativeTimeAgo(rawJsonDate);
            tvRecency.setText(formattedRecent);
            // Setting profile image
            Glide.with(context).load(tweet.user.profileImageUrl).into(ivProfileImage);

            // Setting media image if it exists.
            Log.i(TAG, "My media URL: " + tweet.mediaUrl);
            if (tweet.mediaUrl != null){
                Glide.with(context).load(tweet.mediaUrl).into(ivMedia);
                ivMedia.setVisibility(ImageView.VISIBLE);
            }

            iconReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentManager fm = ((TimelineActivity) context).getSupportFragmentManager();
                    ComposeDialogFragment composeDialogFragment = ComposeDialogFragment.newInstance("Some Title", tweet);
                    // Edit instance to take in twitter handle as argument
                    composeDialogFragment.show(fm, "fragment_compose");
                }
            });




        }
    }
}
