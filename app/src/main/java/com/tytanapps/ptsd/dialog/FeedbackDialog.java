package com.tytanapps.ptsd.dialog;

import android.app.Activity;
import android.view.View;

import com.tytanapps.ptsd.R;
import com.tytanapps.ptsd.utils.PtsdUtil;

import io.doorbell.android.Doorbell;


public class FeedbackDialog extends Doorbell {

    public FeedbackDialog(Activity activity) {
        super(activity, 3961, activity.getString(R.string.api_key_doorbell));

        addProperty("device", PtsdUtil.getDeviceInformation(activity));
        setMessageHint(activity.getString(R.string.feedback_message_hint));
        setPoweredByVisibility(View.GONE); // Hide the "Powered by Doorbell.io" text
    }

}
