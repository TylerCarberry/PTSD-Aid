package com.tytanapps.ptsd;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class FacilityAdapter extends RecyclerView.Adapter<FacilityAdapter.MyViewHolder> {

    private static final String LOG_TAG = FacilityAdapter.class.getSimpleName();

    private Context context;
    private List<Facility> facilityList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView, phoneTextView, addressTextView, detailsTextView;
        public ImageView facilityImageView, callIcon, addressIcon;
        public Button moreInfoButton;

        public MyViewHolder(View view) {
            super(view);

            facilityImageView = (ImageView) view.findViewById(R.id.facility_imageview);
            nameTextView = (TextView) view.findViewById(R.id.facility_name_textview);
            phoneTextView = (TextView) view.findViewById(R.id.facility_phone_textview);
            addressTextView = (TextView) view.findViewById(R.id.facility_address_textview);
            detailsTextView = (TextView) view.findViewById(R.id.facility_details);
            moreInfoButton = (Button) view.findViewById(R.id.more_info_button);
            callIcon = (ImageView) view.findViewById(R.id.facility_phone_icon);
            addressIcon = (ImageView) view.findViewById(R.id.facility_address_icon);
        }

    }


    public FacilityAdapter(List<Facility> facilityList, Context context) {
        this.facilityList = facilityList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.facility_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final Facility facility = facilityList.get(position);


        // If the facility does not have all of its information, do not show it
        if(facility.getName() != null && facility.getDescription() != null && facility.getPhoneNumber() != null) {
            TextView nameTextView = holder.nameTextView;
            nameTextView.setText(facility.getName());

            TextView descriptionTextView = holder.detailsTextView;
            descriptionTextView.setText(facility.getDescription());

            View.OnClickListener callOnClick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDialer(Utilities.getFirstPhoneNumber(facility.getPhoneNumber()));
                }
            };

            TextView phoneTextView = holder.phoneTextView;
            phoneTextView.setText(facility.getPhoneNumber());
            phoneTextView.setOnClickListener(callOnClick);

            ImageView phoneIcon = holder.callIcon;
            phoneIcon.setOnClickListener(callOnClick);

            View.OnClickListener mapOnClick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        openMapIntent(getMapUri(facility.getName(), facility.getCity(), facility.getState()));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            };

            TextView addressTextView =  holder.addressTextView;
            addressTextView.setText(facility.getFullAddress());
            addressTextView.setOnClickListener(mapOnClick);

            ImageView addressIcon =  holder.addressIcon;
            addressIcon.setOnClickListener(mapOnClick);

            ImageView facilityImageView = holder.facilityImageView;
            Bitmap facilityImage = facility.getFacilityImage();
            if(facilityImage != null)
                facilityImageView.setImageBitmap(facility.getFacilityImage());
            else
                facilityImageView.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.default_facility_image));

            facilityImageView.setOnClickListener(mapOnClick);

            // Tapping the more info button opens the website
            Button moreInfoButton = holder.moreInfoButton;
            moreInfoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = Utilities.getFirstPhoneNumber(facility.getUrl());
                    openUrl(url);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return facilityList.size();
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
            context.startActivity(intent);
        } catch (ActivityNotFoundException activityNotFoundException) {
            Toast.makeText(context, R.string.error_open_dialer, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Open the maps app to a specified location
     * @param geoLocation The uri of the location to open
     */
    private void openMapIntent(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    /**
     * Get the url for the Google Maps Api
     * @param name The street address
     * @param town The town
     * @param state The state. Can be initials or full name
     * @return The url for the street view api
     * @throws UnsupportedEncodingException If the address cannot be encoded into a url
     */
    private Uri getMapUri(String name, String town, String state) throws UnsupportedEncodingException {
        // Encode the address
        String location = name + ", " + town + ", " + state;
        location = URLEncoder.encode(location, "UTF-8");

        return Uri.parse("geo:0,0?q=" + location);
    }

    /**
     * Opens the browser to the specified url
     * Precondition: url is a valid url
     * @param url The url to open
     */
    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        context.startActivity(intent);
    }
}