package com.codepath.apps.restclienttemplate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.Toast;

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
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import okhttp3.Headers;

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
        if (tweet.mediaUrl != null){
            holder.binding.ivMedia.setVisibility(ImageView.VISIBLE);
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
        else{
            holder.binding.ivMedia.setVisibility(ImageView.GONE);
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

        if (tweet.reTweeted){
            holder.binding.iconRetweet.setColorFilter(Color.rgb(0,0,255));
        }
        holder.binding.iconRetweet.setOnClickListener(new View.OnClickListener() {
            TwitterClient client = TwitterApp.getRestClient(context);

            @Override
            public void onClick(View view) {
                final int white = Color.rgb(255,255,255);
                final int blue = Color.rgb(0,0,255);
                if (!tweet.reTweeted){ // retweet
                    tweet.reTweeted = true;
                    holder.binding.iconRetweet.setColorFilter(blue);
                    client.reTweet(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "Successful retweet");
                            Toast.makeText(context, "Successful Re-tweet!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "UNSuccessful retweet");
                            Toast.makeText(context, "Failure to re-tweet", Toast.LENGTH_SHORT).show();
                            holder.binding.iconRetweet.setColorFilter(white);
                            tweet.reTweeted = false;
                        }
                    });
                }
                else{ // unretweet
                    tweet.reTweeted = false;
                    holder.binding.iconRetweet.setColorFilter(white);
                    client.unReTweet(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "Successful unretweet");
                            Toast.makeText(context, "Successful unretweet!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "UNSuccessful unretweet");
                            Toast.makeText(context, "failure to unretweet!", Toast.LENGTH_SHORT).show();
                            holder.binding.iconRetweet.setColorFilter(blue);
                            tweet.reTweeted = true;
                        }
                    });
                }
            }
        });

        if (tweet.liked){
            holder.binding.iconHeart.setColorFilter(Color.rgb(255,0,0));
        }
        holder.binding.iconHeart.setOnClickListener(new View.OnClickListener() {
            TwitterClient client = TwitterApp.getRestClient(context);

            @Override
            public void onClick(View view) {
                final int white = Color.rgb(255,255,255);
                final int red = Color.rgb(255,0,0);
                if (!tweet.liked){ // like it
                    tweet.liked = true;
                    holder.binding.iconHeart.setColorFilter(red);
                    client.like(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "Successful liking");
                            Toast.makeText(context, "Successful like!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "UNSuccessful like");
                            Toast.makeText(context, "Failure to like", Toast.LENGTH_SHORT).show();
                            holder.binding.iconHeart.setColorFilter(white);
                            tweet.liked = false;
                        }
                    });
                }
                else{ // un like
                    tweet.liked = false;
                    holder.binding.iconHeart.setColorFilter(white);
                    client.unLike(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "Successful un-like");
                            Toast.makeText(context, "Successful unlike!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "UNSuccessful unlike");
                            Toast.makeText(context, "failure to unlike!", Toast.LENGTH_SHORT).show();
                            holder.binding.iconHeart.setColorFilter(red);
                            tweet.liked = true;
                        }
                    });
                }
            }
        });


        holder.binding.tvBody.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = ((TimelineActivity) context).getSupportFragmentManager();
                DetailFragment detailFragment = DetailFragment.newInstance(tweet);
                detailFragment.show(fm, "detail_fragment");
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
