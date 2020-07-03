package com.codepath.apps.restclienttemplate;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.apps.restclienttemplate.databinding.FragmentComposeDialogBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;

import org.parceler.Parcels;


public class DetailFragment extends DialogFragment {


    Tweet tweet;
    public DetailFragment() {
        // Required empty public constructor
    }

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
        Bundle args = getArguments();
        if (args != null) {
            tweet = (Tweet) Parcels.unwrap(args.getParcelable("tweet"));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final FragmentComposeDialogBinding binding = FragmentComposeDialogBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();

        return view;
    }
}