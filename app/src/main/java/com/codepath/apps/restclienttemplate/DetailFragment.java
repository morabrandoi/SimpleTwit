package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Parcelable;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.codepath.apps.restclienttemplate.databinding.FragmentComposeDialogBinding;
import com.codepath.apps.restclienttemplate.databinding.FragmentDetailBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import okhttp3.Headers;

public class DetailFragment extends DialogFragment {
    public static final String TAG = "DetailFragment";
    Tweet tweet;
    Context context;

    public DetailFragment() { }

    public static DetailFragment newInstance(Tweet tweet) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        Parcelable tweetParcel = Parcels.wrap(tweet);
        args.putParcelable("tweet", tweetParcel);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        Bundle args = getArguments();
        if (args != null) {
            tweet = (Tweet) Parcels.unwrap(args.getParcelable("tweet"));
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final FragmentDetailBinding binding = FragmentDetailBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();

        String fScreenName = "@" + tweet.user.screenName;

        binding.tvScreenName.setText(fScreenName);
        binding.tvBody.setText(tweet.body);
        binding.tvName.setText(tweet.user.name);

        // Setting recency text
        String rawJsonDate = tweet.createdAt;
        String formattedRecent = getRelativeTimeAgo(rawJsonDate);
        binding.tvRecency.setText(formattedRecent);
        // Setting profile image
        Glide.with(context).load(tweet.user.profileImageUrl).transform(new RoundedCorners( 80)).into(binding.ivProfileImage);

        // Setting media image if it exists.
        if (tweet.mediaUrl != null){
            binding.ivMedia.setVisibility(ImageView.VISIBLE);
            binding.pgBar.setVisibility(ProgressBar.VISIBLE);
            Glide.with(context)
                    .load(tweet.mediaUrl)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            binding.pgBar.setVisibility(ProgressBar.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            binding.pgBar.setVisibility(ProgressBar.GONE);
                            return false;
                        }
                    })
                    .into(binding.ivMedia);
            binding.ivMedia.setVisibility(ImageView.VISIBLE);
        }
        else{
            binding.ivMedia.setVisibility(ImageView.GONE);
        }

        binding.iconReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = ((TimelineActivity) context).getSupportFragmentManager();
                ComposeDialogFragment composeDialogFragment = ComposeDialogFragment.newInstance("Some Title", tweet);
                // Edit instance to take in twitter handle as argument
                composeDialogFragment.show(fm, "fragment_compose");
            }
        });

        if (tweet.reTweeted){
            binding.iconRetweet.setColorFilter(Color.rgb(0,0,255));
        }
        binding.iconRetweet.setOnClickListener(new View.OnClickListener() {
            TwitterClient client = TwitterApp.getRestClient(context);

            @Override
            public void onClick(View view) {
                final int white = Color.rgb(255,255,255);
                final int blue = Color.rgb(0,0,255);
                if (!tweet.reTweeted){ // retweet
                    tweet.reTweeted = true;
                    binding.iconRetweet.setColorFilter(blue);
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
                            binding.iconRetweet.setColorFilter(white);
                            tweet.reTweeted = false;
                        }
                    });
                }
                else{ // unretweet
                    tweet.reTweeted = false;
                    binding.iconRetweet.setColorFilter(white);
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
                            binding.iconRetweet.setColorFilter(blue);
                            tweet.reTweeted = true;
                        }
                    });
                }
            }
        });

        if (tweet.liked){
            binding.iconHeart.setColorFilter(Color.rgb(255,0,0));
        }
        binding.iconHeart.setOnClickListener(new View.OnClickListener() {
            TwitterClient client = TwitterApp.getRestClient(context);

            @Override
            public void onClick(View view) {
                final int white = Color.rgb(255,255,255);
                final int red = Color.rgb(255,0,0);
                if (!tweet.liked){ // like it
                    tweet.liked = true;
                    binding.iconHeart.setColorFilter(red);
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
                            binding.iconHeart.setColorFilter(white);
                            tweet.liked = false;
                        }
                    });
                }
                else{ // un like
                    tweet.liked = false;
                    binding.iconHeart.setColorFilter(white);
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
                            binding.iconHeart.setColorFilter(red);
                            tweet.liked = true;
                        }
                    });
                }
            }
        });

        return view;
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