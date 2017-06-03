package com.tytanapps.ptsd;

import android.content.Context;

import com.tytanapps.ptsd.firebase.RemoteConfig;

import javax.inject.Inject;

import angtrim.com.fivestarslibrary.FiveStarsDialog;


public class RatingDialog extends FiveStarsDialog {

    @Inject RemoteConfig remoteConfig;

    public RatingDialog(Context context, String supportEmail) {
        super(context, supportEmail);
        ((PTSDApplication)context.getApplicationContext()).getPtsdComponent().inject(this);

        int ratingUpperBound = remoteConfig.getInt(R.string.rc_rating_upper_bound);

        setRateText(context.getString(R.string.rating_prompt_message));
        setTitle(context.getString(R.string.rating_prompt_title));
        setForceMode(false);
        setUpperBound(ratingUpperBound);
    }
}
