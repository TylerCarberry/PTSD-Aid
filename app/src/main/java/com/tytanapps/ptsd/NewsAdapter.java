package com.tytanapps.ptsd;

import android.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.MyViewHolder> {

    private static final String LOG_TAG = NewsAdapter.class.getSimpleName();

    private Fragment fragment;
    private List<News> newsList;
    private List<News> newsListAll;

    private int newsToDisplay;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public CardView rootCardView;
        public TextView titleTextView, messageTextView;

        //public TextView nameTextView, phoneTextView, addressTextView, detailsTextView;
        //public ImageView facilityImageView, callIcon, addressIcon;
        //public Button moreInfoButton;

        public MyViewHolder(View view) {
            super(view);

            rootCardView = (CardView) view.findViewById(R.id.news_cardview);
            titleTextView = (TextView) view.findViewById(R.id.news_title_textview);
            messageTextView = (TextView) view.findViewById(R.id.news_message_textview);
        }

    }

    public NewsAdapter(List<News> newsList, Fragment fragment) {
        // Display 10 news by default
        this(newsList, fragment, 10);
    }

    public NewsAdapter(List<News> newsList, Fragment fragment, int newsToDisplay) {
        this.fragment = fragment;
        this.newsToDisplay = newsToDisplay;

        this.newsList = new ArrayList<>();
        this.newsListAll = newsList;

        for(int i = 0; i < newsToDisplay; i++) {
            News news = newsList.get(i);
            this.newsList.add(news);
        }

        //loadFacilityImages();
    }

    @Override
    public MyViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_layout, parent, false);

        /*
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alternateCardExpansion(parent);
            }
        });
        */

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final News news = newsList.get(position);

        // If the facility does not have all of its information, do not show it
        if(news.getTitle() != null && news.getMessage() != null) {
            TextView titleTextView = holder.titleTextView;
            titleTextView.setText(news.getTitle());

            TextView messageTextView = holder.messageTextView;
            messageTextView.setText(news.getMessage());

        }
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    /*
    public void filter(String text) {

        if(text.isEmpty()){
            newsList.clear();

            for(int i = 0; i < newsToDisplay && i < newsListAll.size(); i++) {
                newsList.add(newsListAll.get(i));
            }
        } else {
            ArrayList<Facility> result = new ArrayList<>();
            text = text.toLowerCase().trim();
            for(Facility item: newsListAll) {

                if(item.getName().toLowerCase().contains(text) ||
                        item.getPhoneNumber().toLowerCase().contains(text) ||
                        item.getFullAddress().toLowerCase().contains(text)) {
                    result.add(item);
                } else {
                    for (String program : item.getPrograms()) {
                        if(program.toLowerCase().contains(text)) {
                            result.add(item);
                            break;
                        }
                    }
                }
            }
            newsList.clear();

            for(int i = 0; i < newsToDisplay && i < result.size(); i++) {
                newsList.add(result.get(i));
            }
        }

        loadFacilityImages();
        notifyDataSetChanged();
    }

    private void loadFacilityImages() {
        FacilityLoader facilityLoader = new FacilityLoader(fragment) {
            @Override
            public void errorLoadingResults(String errorMessage) {}
            @Override
            public void onSuccess(List<Facility> loadedFacilities) {}
        };

        Runnable callback = new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        };

        for(int i = 0; i < newsToDisplay && i < newsList.size(); i++) {
            Facility facility = newsList.get(i);
            if(facility.getFacilityImage() == null)
                facilityLoader.loadFacilityImage(newsList.get(i), callback);
        }
    }
    */


}