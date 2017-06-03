package com.tytanapps.ptsd.phone;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import com.tytanapps.ptsd.R;

import java.util.ArrayList;
import java.util.List;


public class PhoneDatabase {

    public static List<Phone> getPhones(Context context, boolean isVeteran) {
        List<Phone> phoneList = new ArrayList<>();

        Phone veteransCrisisLine = createPhone(context, R.string.veterans_crisis_line_title, R.string.veterans_crisis_line_details, R.drawable.veterans_crisis_line, R.string.phone_veterans_crisis_line, 10, true);
        Phone lifelineForVets = createPhone(context, R.string.lifeline_for_vets_title, R.string.lifeline_for_vets_details, R.drawable.nvf, R.string.phone_suicide_lifeline, 20, true);
        phoneList.add(veteransCrisisLine);
        phoneList.add(lifelineForVets);

        if (isVeteran) {
            Phone suicideLifeline = createPhone(context, R.string.suicide_lifeline_title, R.string.suicide_lifeline_phone_details, R.drawable.nspl, R.string.phone_suicide_lifeline, 30, false);
            Phone ncaad = createPhone(context, R.string.ncaad_title, R.string.alcohol_phone_details, R.drawable.ncadd, R.string.phone_alcoholism, 40, false);

            phoneList.add(suicideLifeline);
            phoneList.add(ncaad);
        }

        return phoneList;
    }

    private static Phone createPhone(Context context, @StringRes int title, @StringRes int description, @DrawableRes int iconRes, @StringRes int number, int order, boolean veteranOnly) {
        return new Phone(context.getString(title), context.getString(description), null, iconRes, context.getString(number), order, veteranOnly);
    }
}
