package com.tytanapps.ptsd.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.facilities_menu, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(mAdapter != null)
                    mAdapter.filter(query);
                scrollFacilityListToTop();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(mAdapter != null)
                    mAdapter.filter(newText);
                scrollFacilityListToTop();
                return true;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        newsLoader.loadNews();

        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.nav_news).setChecked(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        dismissKeyboard();
    }

    /**
     * Close the on screen keyboard
     */
    private void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
    }

    private NewsLoader setupNewsLoader(final View rootView) {
        return new NewsLoader(this) {
            @Override
            public void errorLoadingResults(String errorMessage) {
                NewsFragment.this.errorLoadingResults(errorMessage);
            }

            @Override
            public void onSuccess(List<News> loadedNews) {
                NewsFragment.this.onSuccess(loadedNews, rootView);
            }
        };
    }

    /**
     * When all of the news have loaded, add them to the recycler view and display them
     * @param loadedNews The loaded news
     * @param rootView The root view of the fragment
     */
    private void onSuccess(List<News> loadedNews, View rootView) {
        newsList.clear();
        for(News news : loadedNews) {
            newsList.add(news);
        }
        setupRecyclerView(getView());

        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setEnabled(true);

        // Hide the progress bar
        rootView.findViewById(R.id.news_progressbar).setVisibility(View.GONE);

        if(mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    /**
     * Setup the RecyclerView and link it to the NewsAdapter
     */
    private void setupRecyclerView(View rootView) {
        if(rootView != null) {
            RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

            mAdapter = new NewsAdapter(newsList, Math.min(newsList.size(), Utilities.getRemoteConfigInt(this, R.string.rc_news_to_display)));
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
     * Scroll the facility recycler view to the top
     */
    private void scrollFacilityListToTop() {
        View rootView = getView();
        if(rootView != null) {
            RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
            recyclerView.scrollToPosition(0);
        }
    }

    /**
     * There was an error loading the news. Display a message and a retry button.
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
