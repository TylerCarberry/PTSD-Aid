package com.tytanapps.ptsd.fragments;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tytanapps.ptsd.News;
import com.tytanapps.ptsd.NewsAdapter;
import com.tytanapps.ptsd.NewsLoader;
import com.tytanapps.ptsd.R;
import com.tytanapps.ptsd.Utilities;

import java.util.ArrayList;
import java.util.List;


public class NewsFragment extends AnalyticsFragment {

    private static final String LOG_TAG = NewsFragment.class.getSimpleName();

    private NewsLoader newsLoader;
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


        //if(mParam1 != null) {
        //    TextView messageTextView = (TextView) rootView.findViewById(R.id.news_textview);
        //    messageTextView.setText(mParam1);
        //}
        //

        // Required empty public constructor
        newsLoader = setupNewsLoader(rootView);
        setupRefreshLayout(rootView);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        newsLoader.loadNews();
    }

    private NewsLoader setupNewsLoader(final View roorView) {
        return new NewsLoader(this) {
            @Override
            public void errorLoadingResults(String errorMessage) {
                Log.d(LOG_TAG, "errorLoadingResults() called with: " + "errorMessage = [" + errorMessage + "]");
                Toast.makeText(getActivity(), "ERROR", Toast.LENGTH_SHORT).show();

                NewsFragment.this.errorLoadingResults(errorMessage);
            }

            @Override
            public void onSuccess(List<News> loadedFacilities) {
                Log.d(LOG_TAG, "onSuccess() called with: " + "loadedFacilities = [" + loadedFacilities + "]");
                newsList.clear();
                for(News news : loadedFacilities) {
                    newsList.add(news);
                }
                setupRecyclerView(getView());

                SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) roorView.findViewById(R.id.swipeRefreshLayout);
                swipeRefreshLayout.setRefreshing(false);
                swipeRefreshLayout.setEnabled(true);

                roorView.findViewById(R.id.news_progressbar).setVisibility(View.GONE);

                if(mAdapter != null)
                    mAdapter.notifyDataSetChanged();
            }
        };
    }

    /**
     * Setup the RecyclerView and link it to the FacilityAdapter
     */
    private void setupRecyclerView(View rootView) {
        if(rootView != null) {
            RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

            mAdapter = new NewsAdapter(newsList, this, Math.min(newsList.size(), Utilities.getRemoteConfigInt(this, R.string.rc_news_to_display)));
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(mAdapter);
        }
    }

    /**
     * Setup the refresh layout to refresh on swipe down past the first item
     * @param rootView The root view of the fragment containing the refresh layout
     */
    private void setupRefreshLayout(View rootView) {
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                newsList.clear();
                mAdapter.notifyDataSetChanged();

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        newsLoader.refresh();
                    }
                });
                t.run();
            }
        });
        swipeRefreshLayout.setEnabled(false);
    }

    /**
     * There was an error loading the VA facilities. Display a message and a retry button.
     * @param errorMessage The message to show to the user
     */
    private void errorLoadingResults(String errorMessage) {
        View rootView = getView();
        if(rootView != null) {
            final TextView loadingTextview = (TextView) rootView.findViewById(R.id.news_loading_textview);
            if(loadingTextview != null) {
                loadingTextview.setVisibility(View.VISIBLE);
                loadingTextview.setText(errorMessage);
            }

            final ProgressBar loadingProgressbar = (ProgressBar) rootView.findViewById(R.id.news_progressbar);
            if(loadingProgressbar != null) {
                loadingProgressbar.setVisibility(View.INVISIBLE);
            }

            Button retryButton = (Button) rootView.findViewById(R.id.retry_load_button);
            if(retryButton != null) {
                retryButton.setVisibility(View.VISIBLE);

                retryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View retryButton) {
                        retryButton.setVisibility(View.INVISIBLE);

                        if(loadingTextview != null) {
                            loadingTextview.setText("");
                            loadingTextview.setVisibility(View.INVISIBLE);
                        }
                        if(loadingProgressbar != null)
                            loadingProgressbar.setVisibility(View.VISIBLE);

                        newsLoader.loadNews();
                    }
                });

            }
        }
    }
}
