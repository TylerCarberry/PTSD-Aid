package com.tytanapps.ptsd.website;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import com.tytanapps.ptsd.R;

import java.util.ArrayList;
import java.util.List;


public class WebsiteDatabase {

    public static List<Website> getWebsites(Context context, boolean isVeteran) {
        List<Website> websiteList = new ArrayList<>();

        if (isVeteran) {
            Website veteransChat = createWebsite(context, R.string.veterans_chat_title, R.string.veterans_chat_details, R.drawable.veterans_crisis_line, R.string.website_chat, 10, false);
            Website veteranAffairs = createWebsite(context, R.string.veteran_affairs_title, R.string.veteran_affairs_details, R.drawable.va, R.string.website_va, 20, false);
            Website selfQuiz = createWebsite(context, R.string.self_quiz_title, R.string.self_quiz_details, R.drawable.veterans_quiz, R.string.website_self_check, 30, false);

            websiteList.add(veteransChat);
            websiteList.add(veteranAffairs);
            websiteList.add(selfQuiz);
        }

        Website nimh = createWebsite(context, R.string.nimh_title, R.string.nimh_details, R.drawable.nimh, R.string.website_nimh, 40, true);
        Website ptsdCoach = createWebsite(context, R.string.ptsd_coach_title, R.string.ptsd_coach_details, R.drawable.ptsd_coach, R.string.website_coach, 50, true);

        websiteList.add(nimh);
        websiteList.add(ptsdCoach);

        return websiteList;
    }

    private static Website createWebsite(Context context, @StringRes int title, @StringRes int description, @DrawableRes int iconRes, @StringRes int url, int order, boolean veteransOnly) {
        return new Website(context.getString(title), context.getString(description), null, iconRes, context.getString(url), order, veteransOnly);
    }
}
