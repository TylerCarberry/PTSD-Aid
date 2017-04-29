package com.tytanapps.ptsd;

import android.content.Context;

import com.tytanapps.ptsd.firebase.RemoteConfig;

import angtrim.com.fivestarslibrary.FiveStarsDialog;


public class RatingDialog extends FiveStarsDialog {

    public RatingDialog(Context context, String supportEmail) {
        super(context, supportEmail);

        int ratingUpperBound = (int) RemoteConfig.getFirebaseRemoteConfig().getDouble("rating_upper_bound");

        setRateText(context.getString(R.string.rating_prompt_message));
        setTitle(context.getString(R.string.rating_prompt_title));
        setForceMode(false);
        setUpperBound(ratingUpperBound);
    }
}
