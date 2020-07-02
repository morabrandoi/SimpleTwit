package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;
// ...

public class ComposeDialogFragment extends DialogFragment implements TextView.OnEditorActionListener {
// STATIC
    public interface ComposeDialogListener {
        void onFinishEditDialog(Tweet tweet);
    }

    public static final int MAX_TWEET_LENGTH = 280;
    public static final String TAG = "ComposeFragment";

    public static ComposeDialogFragment newInstance(String title, Tweet tweet) {
        ComposeDialogFragment frag = new ComposeDialogFragment();
        Bundle args = new Bundle();
        if (tweet != null){
            args.putString("screen_name", tweet.user.screenName);
        }
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }
// Non-Static
    TwitterClient client;
    EditText etCompose;
    Button btnTweet;
    TextView tvCount;
    Tweet finishedTweet;

    // Empty constructor required for dialogFragment
    public ComposeDialogFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compose_dialog, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        client = TwitterApp.getRestClient(getContext());

        etCompose = view.findViewById(R.id.etCompose);
        btnTweet = view.findViewById(R.id.btnTweet);
        tvCount = view.findViewById(R.id.tvCount);

        Bundle args = getArguments();
        if (args.containsKey("screen_name")) {
            String preFill = "@" + args.getString("screen_name") + " ";
            etCompose.setText(preFill);
        }

        etCompose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int tweetLength = etCompose.getText().toString().length();
                int charsLeft = MAX_TWEET_LENGTH - tweetLength;
                String charLeftMessage = charsLeft + "/" + MAX_TWEET_LENGTH;
                tvCount.setText(charLeftMessage);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Set click listener on the button.
        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tweetContent = etCompose.getText().toString();
                // If tweet is too long or too short, don't tweet, tell user.
                if (tweetContent.isEmpty()) {
                    Toast.makeText(getContext(), "Sorry your tweet cannot be empty!", Toast.LENGTH_LONG).show();
                    return;
                }
                if (tweetContent.length() > MAX_TWEET_LENGTH) {
                    Toast.makeText(getContext(), "Sorry your tweet is too long!", Toast.LENGTH_LONG).show();
                    return;
                }
                client.publishTweet(tweetContent, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "on success to publish Tweet");
                        try {
                            finishedTweet = Tweet.fromJson(json.jsonObject);
                            Log.i(TAG, "publishedTweet says: " + finishedTweet.body);
                            onEditorAction(etCompose, EditorInfo.IME_ACTION_DONE, null);
                            dismiss();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure to publish tweet, response: " + response, throwable);
                    }
                });
            }
        });

        // Show soft keyboard automatically and request focus to etCompose
        etCompose.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (EditorInfo.IME_ACTION_DONE == i) {
            // Return input text back to activity through the implemented listener
            ComposeDialogListener listener = (ComposeDialogListener) getActivity();
            listener.onFinishEditDialog(finishedTweet);
            // Close the dialog and return back to the parent activity
            dismiss();
            return true;
        }
        return false;
    }
}
