package com.codepath.apps.restclienttemplate.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
public class Tweet {
    public static final String TAG = "Tweet";

    public String body;
    public String createdAt;
    public String mediaUrl;
    public User user;
    public boolean liked;
    public boolean reTweeted;
    public long id;

    // Empty constructor for Required by parcel library
    public Tweet(){}

    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.body = jsonObject.getString("text");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.id = jsonObject.getLong("id");
        tweet.liked = jsonObject.getBoolean("favorited");
        tweet.reTweeted = jsonObject.getBoolean("retweeted");


        // Pulling media URL if it exists
        JSONObject entities = jsonObject.getJSONObject("entities");
        boolean hasMedia = entities.has("media");
        if (hasMedia){
            String mediaURL = entities.getJSONArray("media").getJSONObject(0).getString("media_url_https");
            tweet.mediaUrl = mediaURL;
        }
        else {
            tweet.mediaUrl = null;
        }

        return tweet;
    }

    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Tweet> tweets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            tweets.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return tweets;
    }
}
