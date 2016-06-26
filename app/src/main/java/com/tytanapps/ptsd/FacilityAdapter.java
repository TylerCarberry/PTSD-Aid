package com.tytanapps.ptsd;

import android.content.Context;
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
                    Utilities.openDialer(context, Utilities.getFirstPhoneNumber(facility.getPhoneNumber()));
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
                        Utilities.openMapIntent(context, getMapUri(facility.getName(), facility.getCity(), facility.getState()));
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
                    Utilities.openBrowserIntent(context, url);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return facilityList.size();
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

}