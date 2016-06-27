package com.tytanapps.ptsd.fragments;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tytanapps.ptsd.News;
import com.tytanapps.ptsd.NewsAdapter;
import com.tytanapps.ptsd.R;
import com.tytanapps.ptsd.Utilities;

import java.util.ArrayList;
import java.util.List;


public class NewsFragment extends AnalyticsFragment {

    private List<News> newsList = new ArrayList<>();
    private NewsAdapter mAdapter;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public NewsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_news, container, false);


        if(mParam1 != null) {
            TextView messageTextView = (TextView) rootView.findViewById(R.id.news_textview);
            messageTextView.setText(mParam1);
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        News news1 = new News("Title 1", "Message 1");
        News news2 = new News("Title 2", "Message 2");
        News news3 = new News("Title 3", "Message 3");

        newsList.add(news1);
        newsList.add(news2);
        newsList.add(news3);

        setupRecyclerView(getView());

        mAdapter.notifyDataSetChanged();
    }

    /**
     * Setup the RecyclerView and link it to the FacilityAdapter
     */
    private void setupRecyclerView(View rootView) {
        if(rootView != null) {
            RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

            mAdapter = new NewsAdapter(newsList, this, Math.min(newsList.size(), Utilities.getRemoteConfigInt(this, R.string.rc_facilities_to_display)));
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(mAdapter);
        }
    }
}
