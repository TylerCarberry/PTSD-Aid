package com.tytanapps.ptsd.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tytanapps.ptsd.R;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;


/**
 * Displays a list of websites to find more information about PTSD. Shows a brief description for
 * each website. Tapping on the card opens the website.
 */
public class WebsiteFragment extends Fragment {

    private static final String LOG_TAG = WebsiteFragment.class.getSimpleName();

    public WebsiteFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_website, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                //writeToDatabase(database);
                readWebsites(database);
            }
        });
        t.start();
    }

    private void writeToDatabase(FirebaseDatabase database) {
        DatabaseReference myRef = database.getReference("web_support");
        HashMap<String, HashMap<String, Object>> phones = new HashMap<>();

        HashMap<String, Object> veteransChatHashmap = createWebsiteHashMap(getString(R.string.veterans_chat_title), getString(R.string.website_chat), getString(R.string.veterans_chat_details), R.drawable.veterans_crisis_line);
        phones.put((String) veteransChatHashmap.get("name"), veteransChatHashmap);

        HashMap<String, Object> nimhHashmap = createWebsiteHashMap(getString(R.string.nimh_title), getString(R.string.website_nimh), getString(R.string.nimh_details), R.drawable.nimh);
        phones.put((String) nimhHashmap.get("name"), nimhHashmap);

        HashMap<String, Object> vaHashmap = createWebsiteHashMap(getString(R.string.veterans_affairs), getString(R.string.website_va), getString(R.string.veteran_affairs_details), R.drawable.va);
        phones.put((String) vaHashmap.get("name"), vaHashmap);

        HashMap<String, Object> selfQuizHashmap = createWebsiteHashMap(getString(R.string.self_quiz), getString(R.string.website_self_check), getString(R.string.quiz_details), R.drawable.veterans_quiz);
        phones.put((String) selfQuizHashmap.get("name"), selfQuizHashmap);

        HashMap<String, Object> ptsdCoachHashmap = createWebsiteHashMap(getString(R.string.ptsd_coach), getString(R.string.website_coach), getString(R.string.ptsd_coach_details), R.drawable.ptsd_coach);
        phones.put((String) ptsdCoachHashmap.get("name"), ptsdCoachHashmap);

        myRef.setValue(phones);
    }

    private HashMap<String, Object> createWebsiteHashMap(String name, String url, String description, int drawableId) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", name);
        hashMap.put("description", description);
        hashMap.put("url", url);

        Bitmap bmp =  BitmapFactory.decodeResource(getResources(), drawableId);//your image
        ByteArrayOutputStream bYtE = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, bYtE);
        bmp.recycle();
        byte[] byteArray = bYtE.toByteArray();
        String imageFile = Base64.encodeToString(byteArray, Base64.DEFAULT);

        hashMap.put("icon", imageFile);

        return hashMap;
    }


    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static String drawableToBase64 (Drawable drawable) {
        Bitmap bitmap = drawableToBitmap(drawable);
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(mutableBitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);


        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

        return encoded;
    }

    public static Bitmap base64ToBitmap(String base64) {
        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    private void readWebsites(final FirebaseDatabase database) {
        DatabaseReference myRef = database.getReference("web_support");

        // Read from the database
        myRef.orderByChild("order").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        View rootView = getView();
                        if(rootView != null) {
                            LinearLayout websiteLinearLayout = (LinearLayout) rootView.findViewById(R.id.website_linear_layout);
                            LayoutInflater inflater = LayoutInflater.from(getActivity());

                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                insertWebCard(child, websiteLinearLayout, inflater);
                            }
                        }
                    }
                });

                t.run();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(LOG_TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void insertWebCard(final DataSnapshot snapshot, final LinearLayout websitesLinearLayout, final LayoutInflater inflater) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                String name = (String) snapshot.child("name").getValue();
                String desc = (String) snapshot.child("description").getValue();
                String url = (String) snapshot.child("url").getValue();
                String bitmap_base64 = (String) snapshot.child("icon").getValue();

                CardView webCardView = getWebCardView(inflater, websitesLinearLayout, name, url);

                TextView nameTextView = (TextView) webCardView.findViewById(R.id.website_name_textview);
                nameTextView.setText(name);

                TextView detailsTextView = (TextView) webCardView.findViewById(R.id.website_details_textview);
                detailsTextView.setText(desc);

                if (bitmap_base64 != null) {
                    ImageView iconImageView = (ImageView) webCardView.findViewById(R.id.website_icon_imageview);
                    Bitmap bmp = decodeBitmap(bitmap_base64);
                    iconImageView.setImageBitmap(bmp);
                }
            }
        });

        t.run();
    }

    /**
     * Decode a bitmap from a base64 string
     * @param bitmap_base64 The encoded bitmap
     * @return The decoded bitmap
     */
    private Bitmap decodeBitmap(String bitmap_base64) {
        byte[] imageAsBytes = Base64.decode(bitmap_base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
    }

    private CardView getWebCardView(LayoutInflater inflater, LinearLayout websitesLinearLayout, String name, final String url) {
        CardView websiteCardView = null;

        for(int i = 0; i < websitesLinearLayout.getChildCount(); i++) {
            View child = websitesLinearLayout.getChildAt(i);
            if(child.getTag() != null && child.getTag().equals(name))
                websiteCardView = (CardView) child;
        }
        if(websiteCardView == null) {
            websiteCardView = (CardView) inflater.inflate(R.layout.web_cardview, (ViewGroup) getView(), false);

            websiteCardView.setTag(name);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(0, 0, 0, (int) getResources().getDimension(R.dimen.activity_vertical_margin));
            websiteCardView.setLayoutParams(params);

            websiteCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openBrowser(url);
                }
            });

            websitesLinearLayout.addView(websiteCardView);
            Animation bottomUp = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_up);
            websiteCardView.startAnimation(bottomUp);
        }

        return websiteCardView;
    }

    /**
     * Open a website in the browser
     * Precondition: url is a valid url
     * @param url The url to open
     */
    private void openBrowser(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

}
