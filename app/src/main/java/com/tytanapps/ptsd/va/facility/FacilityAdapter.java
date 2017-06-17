package com.tytanapps.ptsd.va.facility;

import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;
import com.squareup.picasso.Picasso;
import com.tytanapps.ptsd.PTSDApplication;
import com.tytanapps.ptsd.R;
import com.tytanapps.ptsd.utils.ExternalAppUtil;
import com.tytanapps.ptsd.utils.PtsdUtil;
import com.tytanapps.ptsd.utils.StringUtil;
import com.tytanapps.ptsd.va.SearchableAdapter;
import com.tytanapps.ptsd.va.facility.maps.MapsClient;
import com.tytanapps.ptsd.va.facility.maps.MapsResult;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class FacilityAdapter extends SearchableAdapter<FacilityAdapter.FacilityViewHolder, Facility> {

    @Inject MapsClient mapsClient;

    private Fragment fragment;

    public class FacilityViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.facility_cardview) CardView rootCardView;
        @BindView(R.id.facility_imageview) ImageView facilityImageView;
        @BindView(R.id.facility_name_textview) TextView nameTextView;
        @BindView(R.id.facility_phone_textview) TextView phoneTextView;
        @BindView(R.id.facility_address_textview) TextView addressTextView;
        @BindView(R.id.facility_details) TextView detailsTextView;
        @BindView(R.id.facility_phone_icon) ImageView callIcon;
        @BindView(R.id.facility_address_icon) ImageView addressIcon;
        @BindView(R.id.more_info_button) Button moreInfoButton;

        FacilityViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

    FacilityAdapter(List<Facility> facilityList, Fragment fragment, int numFacilitiesToDisplay) {
        super(facilityList, numFacilitiesToDisplay);
        this.fragment = fragment;

        ((PTSDApplication) fragment.getActivity().getApplication()).getPtsdComponent().inject(this);
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
                        FirebaseCrash.report(e);
                        e.printStackTrace();
                    }
                }
            };

            TextView addressTextView = holder.addressTextView;
            addressTextView.setText(facility.getFullAddress());
            addressTextView.setOnClickListener(mapOnClick);

            holder.addressIcon.setOnClickListener(mapOnClick);

            final ImageView facilityImageView = holder.facilityImageView;
            facilityImageView.setOnClickListener(mapOnClick);

            final Picasso picasso = Picasso.with(facilityImageView.getContext());
            if (!StringUtil.isNullOrEmpty(facility.getImageUrl())) {
                picasso.load(facility.getImageUrl()).into(facilityImageView);
            } else {
                Call<MapsResult> imageResult = mapsClient.getFacilityImage("" + facility.getFacilityId());
                imageResult.enqueue(new Callback<MapsResult>() {
                    @Override
                    public void onResponse(@NonNull Call<MapsResult> call, @NonNull Response<MapsResult> response) {
                        facility.setImageUrl(response.body().getUrl());
                        picasso.load(response.body().getUrl()).into(facilityImageView);
                    }

                    @Override
                    public void onFailure(@NonNull Call<MapsResult> call, @NonNull Throwable t) {
                        Timber.e(call.toString(), t);
                    }
                });
            }

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

}