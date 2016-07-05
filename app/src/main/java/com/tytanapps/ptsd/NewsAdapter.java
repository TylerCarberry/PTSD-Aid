package com.tytanapps.ptsd;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private static final String LOG_TAG = NewsAdapter.class.getSimpleName();

    private List<News> newsList;
    private List<News> newsListAll;

    private int newsToDisplay;

    public class NewsViewHolder extends RecyclerView.ViewHolder {
        public CardView rootCardView;
        public TextView titleTextView, dateTextView;
        public ExpandableTextView messageTextView;

        public NewsViewHolder(View view) {
            super(view);

            rootCardView = (CardView) view.findViewById(R.id.news_cardview);
            titleTextView = (TextView) view.findViewById(R.id.news_title_textview);
            messageTextView = (ExpandableTextView) view.findViewById(R.id.news_message_textview);
            dateTextView = (TextView) view.findViewById(R.id.news_date_textview);

        }

    }

    public NewsAdapter(List<News> newsList) {
        // Display 10 news by default
        this(newsList, 10);
    }

    /**
     * Create a news adapter
     * @param newsList List of news to display
     * @param newsToDisplay Number of news to display on screen
     */
    public NewsAdapter(List<News> newsList, int newsToDisplay) {
        this.newsToDisplay = newsToDisplay;

        this.newsList = new ArrayList<>();
        this.newsListAll = newsList;

        for(int i = 0; i < newsToDisplay && i < newsList.size(); i++) {
            News news = newsList.get(i);
            this.newsList.add(news);
        }
    }

    @Override
    public NewsViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_layout, parent, false);

        return new NewsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final NewsViewHolder holder, int position) {
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


    /**
     * Only include a subset of News in the adapter
     * @param text The text to search by
     */
    public void filter(String text) {
        // If the search message is empty, include everything
        if(text == null || text.isEmpty()){
            newsList.clear();

            for(int i = 0; i < newsToDisplay && i < newsListAll.size(); i++) {
                newsList.add(newsListAll.get(i));
            }
        } else {
            ArrayList<News> result = new ArrayList<>();
            text = text.toLowerCase().trim();
            for(News item: newsListAll) {
                // Search by the title, body, and press date
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