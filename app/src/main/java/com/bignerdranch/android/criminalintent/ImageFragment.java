package com.bignerdranch.android.criminalintent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;


import java.io.File;

/**
 * Created by Fulgen on 12/05/2016.
 */
public class ImageFragment extends DialogFragment{

    private static final String ARG_IMAGE_URI = "image_uri";

    //private String mUri;
    //private ImageView mImageView;


    public static ImageFragment newInstance(Uri uri) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_IMAGE_URI,uri.toString());

        ImageFragment fragment = new ImageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String mUri = (String) getArguments().getSerializable(ARG_IMAGE_URI);
        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.image_view,null);

        ImageView mImageView = (ImageView) v.findViewById(R.id.crime_photo_viewer);
        mImageView.setImageURI(Uri.parse(new File(mUri).toString()));

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .create();

    }


}
