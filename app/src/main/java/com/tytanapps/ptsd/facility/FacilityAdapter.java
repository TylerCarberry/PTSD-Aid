package com.tytanapps.ptsd.facility;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.tytanapps.ptsd.R;
import com.tytanapps.ptsd.SearchableAdapter;
import com.tytanapps.ptsd.utils.ExternalAppUtil;
import com.tytanapps.ptsd.utils.PtsdUtil;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static butterknife.ButterKnife.findById;

class FacilityAdapter extends SearchableAdapter<FacilityAdapter.FacilityViewHolder, Facility> {

    private Fragment fragment;

    class FacilityViewHolder extends RecyclerView.ViewHolder {
        CardView rootCardView;
        TextView nameTextView, phoneTextView, addressTextView, detailsTextView;
        ImageView facilityImageView, callIcon, addressIcon;
        Button moreInfoButton;

        FacilityViewHolder(View view) {
            super(view);

            rootCardView      = findById(view, R.id.facility_cardview);
            facilityImageView = findById(view, R.id.facility_imageview);
            nameTextView      = findById(view, R.id.facility_name_textview);
            phoneTextView     = findById(view, R.id.facility_phone_textview);
            addressTextView   = findById(view, R.id.facility_address_textview);
            detailsTextView   = findById(view, R.id.facility_details);
            moreInfoButton    = findById(view, R.id.more_info_button);
            callIcon          = findById(view, R.id.facility_phone_icon);
            addressIcon       = findById(view, R.id.facility_address_icon);
        }

    }

    public FacilityAdapter(List<Facility> facilityList, Fragment fragment) {
        super(facilityList);
        this.fragment = fragment;
    }

    FacilityAdapter(List<Facility> facilityList, Fragment fragment, int facilitiesToDisplay) {
        super(facilityList, facilitiesToDisplay);
        this.fragment = fragment;
        loadFacilityImages();
    }

    @Override
    public FacilityViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.facility_layout, parent, false);

        return new FacilityViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final FacilityViewHolder holder, int position) {
        final Facility facility = list.get(position);

        // If the facility does not have all of its information, do not show it
        if (facility.getName() != null && facility.getDescription() != null && facility.getPhoneNumber() != null) {
            TextView nameTextView = holder.nameTextView;
            nameTextView.setText(facility.getName());

            TextView descriptionTextView = holder.detailsTextView;
            descriptionTextView.setText(facility.getDescription());

            View.OnClickListener callOnClick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ExternalAppUtil.openDialer(fragment.getActivity(), PtsdUtil.getFirstPhoneNumber(facility.getPhoneNumber()));
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
                        ExternalAppUtil.openMapIntent(fragment, ExternalAppUtil.getMapUri(facility.getName(), facility.getCity(), facility.getState()));
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
            if (facilityImage != null) {
                facilityImageView.setImageBitmap(facility.getFacilityImage());
            }
            facilityImageView.setOnClickListener(mapOnClick);

            // Tapping the more info button opens the website
            Button moreInfoButton = holder.moreInfoButton;
            moreInfoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = PtsdUtil.getFirstPhoneNumber(facility.getUrl());
                    ExternalAppUtil.openBrowserIntent(fragment, url);
                }
            });

        }
    }

    @Override
    public void filter(String search) {
        super.filter(search);
        loadFacilityImages();
    }

    private void loadFacilityImages() {
        FacilityLoader facilityLoader = new FacilityLoader(fragment) {
            @Override
            public void errorLoadingResults(Throwable throwable) {}
            @Override
            public void onSuccess(List<Facility> loadedFacilities) {}

            @Override
            public void onLoadedImage(int facilityId) {
                for (int i = 0; i < list.size(); i++) {
                    Facility facility = list.get(i);
                    if (facility.getFacilityId() == facilityId) {
                        notifyItemChanged(i);
                    }
                }
            }
        };

        for (int i = 0; i < numToDisplay && i < list.size(); i++) {
            Facility facility = list.get(i);
            if (facility.getFacilityImage() == null) {
                facilityLoader.loadFacilityImage(list.get(i));
            }
        }
    }


}