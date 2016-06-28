package com.tytanapps.ptsd;

import android.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ms.square.android.expandabletextview.ExpandableTextView;

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
        public TextView titleTextView, dateTextView;
        public ExpandableTextView messageTextView;

        public MyViewHolder(View view) {
            super(view);

            rootCardView = (CardView) view.findViewById(R.id.news_cardview);
            titleTextView = (TextView) view.findViewById(R.id.news_title_textview);
            messageTextView = (ExpandableTextView) view.findViewById(R.id.news_message_textview);
            dateTextView = (TextView) view.findViewById(R.id.news_date_textview);

        }

    }

    public NewsAdapter(List<News> newsList, Fragment fragment) {
        // Display 10 news by default
        this(newsList, fragment, 7);
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

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final News news = newsList.get(position);

        // If the news does not have all of its information, do not show it
        if(news.getTitle() != null && news.getMessage() != null) {
            TextView titleTextView = holder.titleTextView;
            titleTextView.setText(news.getTitle());

            ExpandableTextView messageTextView = holder.messageTextView;
            messageTextView.setText(news.getMessage());

            TextView dateTextViewTextView = holder.dateTextView;
            dateTextViewTextView.setText(news.getPressDate());
        }
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }


    public void filter(String text) {

        if(text.isEmpty()){
            newsList.clear();

            for(int i = 0; i < newsToDisplay && i < newsListAll.size(); i++) {
                newsList.add(newsListAll.get(i));
            }
        } else {
            ArrayList<News> result = new ArrayList<>();
            text = text.toLowerCase().trim();
            for(News item: newsListAll) {

                if(item.getTitle().toLowerCase().contains(text) ||
                        item.getMessage().toLowerCase().contains(text) ||
                        item.getPressDate().toLowerCase().contains(text)) {
                    result.add(item);
                }
            }
            newsList.clear();

            for(int i = 0; i < newsToDisplay && i < result.size(); i++) {
                newsList.add(result.get(i));
            }
        }
        notifyDataSetChanged();
    }


}