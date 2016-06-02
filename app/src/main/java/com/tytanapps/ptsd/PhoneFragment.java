package com.tytanapps.ptsd;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
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
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;


/**
 * Displays a list of common veteran hotlines. Shows a brief description for each hotline and
 * the phone number to call. Tapping on the hotline calls them.
 */
public class PhoneFragment extends Fragment {

    private static final String LOG_TAG = PhoneFragment.class.getSimpleName();

    public PhoneFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_phone, container, false);



        // Write a message to the database
        //writeToDatabase(database);



        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                readPhoneNumbers(database);
            }
        });
        t.start();
    }

    private void writeToDatabase(FirebaseDatabase database) {


        DatabaseReference myRef = database.getReference("phone_support");
        HashMap<String, HashMap<String, Object>> phones = new HashMap<>();

        HashMap<String, Object> veteransCrisisLineHashmap = createPhoneHashMap("Veterans Crisis Line", getString(R.string.phone_veterans_crisis_line), getString(R.string.veterans_support_phone_details), R.drawable.veterans_crisis_line);
        phones.put((String) veteransCrisisLineHashmap.get("name"), veteransCrisisLineHashmap);

        HashMap<String, Object> suicideHashmap = createPhoneHashMap("Suicide Lifeline", getString(R.string.phone_suicide_lifeline), getString(R.string.suicide_lifeline_phone_details), R.drawable.nspl);
        phones.put((String) suicideHashmap.get("name"), suicideHashmap);

        HashMap<String, Object> ncaadHashmap = createPhoneHashMap("NCAAD", getString(R.string.phone_alcoholism), getString(R.string.alcohol_phone_details), R.drawable.ncadd);
        phones.put((String) ncaadHashmap.get("name"), ncaadHashmap);

        HashMap<String, Object> lifelineVetsHashmap = createPhoneHashMap("Lifeline for Vets", getString(R.string.phone_veterans_foundation_hotline), getString(R.string.veterans_foundation_phone_details), R.drawable.nvf);
        phones.put((String) lifelineVetsHashmap.get("name"), lifelineVetsHashmap);

        myRef.setValue(phones);
    }

    private HashMap<String, Object> createPhoneHashMap(String name, String phoneNumber, String description, int drawableId) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", name);
        hashMap.put("description", description);
        hashMap.put("phone_number", phoneNumber);

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

    private void readPhoneNumbers(final FirebaseDatabase database) {
        DatabaseReference myRef = database.getReference("phone_support");

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        View rootView = getView();
                        if(rootView != null) {

                            GenericTypeIndicator<HashMap<String, HashMap<String, String>>> t
                                    = new GenericTypeIndicator<HashMap<String, HashMap<String, String>>>() {
                            };
                            HashMap<String, HashMap<String, String>> hashMap = dataSnapshot.getValue(t);

                            if (hashMap != null) {

                                LinearLayout phoneNumbersLinearLayout = (LinearLayout) rootView.findViewById(R.id.phone_linear_layout);
                                LayoutInflater inflater = LayoutInflater.from(getActivity());

                                for (HashMap<String, String> phoneSupport : hashMap.values()) {

                                    insertPhoneCard(phoneSupport, phoneNumbersLinearLayout, inflater);


                                }
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

    private void insertPhoneCard(final HashMap<String, String> phoneSupport, final LinearLayout phoneNumbersLinearLayout, final LayoutInflater inflater) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                String name = phoneSupport.get("name");
                String desc = phoneSupport.get("description");
                String phone = phoneSupport.get("phone_number");
                String bitmap_base64 = phoneSupport.get("icon");

                CardView phoneCardView = getPhoneCardView(inflater, phoneNumbersLinearLayout, name, phone);

                TextView descTextView = (TextView) phoneCardView.findViewById(R.id.phone_details_textview);
                descTextView.setText(desc);

                TextView phoneTextView = (TextView) phoneCardView.findViewById(R.id.phone_number_textview);
                phoneTextView.setText(phone);


                if (bitmap_base64 != null) {
                    ImageView iconImageView = (ImageView) phoneCardView.findViewById(R.id.phone_icon_imageview);

                    byte[] imageAsBytes = Base64.decode(bitmap_base64, Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);

                    iconImageView.setImageBitmap(bmp);
                }

            }
        });

        t.run();


    }

    private CardView getPhoneCardView(LayoutInflater inflater, LinearLayout phoneNumbersLinearLayout, String name, final String phoneNumber) {
        CardView phoneCardView = null;

        for(int i = 0; i < phoneNumbersLinearLayout.getChildCount(); i++) {
            View child = phoneNumbersLinearLayout.getChildAt(i);
            if(child.getTag() != null && child.getTag().equals(name))
                phoneCardView = (CardView) child;
        }
        if(phoneCardView == null) {
            phoneCardView = (CardView) inflater.inflate(R.layout.phone_cardview, (ViewGroup) getView(), false);

            phoneCardView.setTag(name);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(0, 0, 0, (int) getResources().getDimension(R.dimen.activity_vertical_margin));
            phoneCardView.setLayoutParams(params);

            phoneCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDialer(phoneNumber);
                }
            });

            phoneNumbersLinearLayout.addView(phoneCardView);
            Animation bottomUp = AnimationUtils.loadAnimation(getActivity(),
                    R.anim.bottom_up);
            phoneCardView.startAnimation(bottomUp);
        }

        return phoneCardView;
    }

    /**
     * Open the dialer with a phone number entered
     * This does not call the number directly, the user needs to press the call button
     * @param phoneNumber The phone number to call
     */
    private void openDialer(String phoneNumber) {

        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        } catch (ActivityNotFoundException activityNotFoundException) {
            Toast.makeText(getActivity(), R.string.error_open_dialer, Toast.LENGTH_SHORT).show();
        }
    }
}
