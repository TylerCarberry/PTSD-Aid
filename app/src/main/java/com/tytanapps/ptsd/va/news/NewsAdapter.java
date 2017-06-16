package com.tytanapps.ptsd.va.news;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.tytanapps.ptsd.R;
import com.tytanapps.ptsd.va.SearchableAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

class NewsAdapter extends SearchableAdapter<NewsAdapter.NewsViewHolder, News> {

    class NewsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.news_cardview) CardView rootCardView;
        @BindView(R.id.news_title_textview) TextView titleTextView;
        @BindView(R.id.news_date_textview) TextView dateTextView;
        @BindView(R.id.news_message_textview) ExpandableTextView messageTextView;

        NewsViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    /**
     * Create a news adapter
     * @param newsList List of news to display
     * @param numNewsToDisplay Number of news to display on screen
     */
    NewsAdapter(List<News> newsList, int numNewsToDisplay) {
        super(newsList, numNewsToDisplay);
    }

    @Override
    public NewsViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_layout, parent, false);

        return new NewsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final NewsViewHolder holder, int position) {
        final News news = list.get(position);

        // If the news does not have all of its information, do not show it
        if (news.getTitle() != null && news.getMessage() != null) {
            holder.titleTextView.setText(news.getTitle());
            holder.messageTextView.setText(news.getMessage());
            holder.dateTextView.setText(news.getPressDate());
        }
    }

}