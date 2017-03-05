package com.tytanapps.ptsd.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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
import android.widget.TextView;

import com.bhargavms.dotloader.DotLoader;
import com.tytanapps.ptsd.News;
import com.tytanapps.ptsd.NewsAdapter;
import com.tytanapps.ptsd.NewsLoader;
import com.tytanapps.ptsd.R;
import com.tytanapps.ptsd.Utilities;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class NewsFragment extends AnalyticsFragment {

    private static final String LOG_TAG = NewsFragment.class.getSimpleName();

    private NewsLoader newsLoader;
    private List<News> newsList = new ArrayList<>();
    private NewsAdapter mAdapter;

    private Unbinder unbinder;
    @BindView(R.id.swipeRefreshLayout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.news_progressbar) DotLoader loadingProgressBar;
    @BindView(R.id.news_loading_textview) TextView loadingTextView;
    @BindView(R.id.retry_load_button) Button retryLoadButton;

    public NewsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_news, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        newsLoader = setupNewsLoader();
        setupRefreshLayout();
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
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
        if(getActivity().getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    private NewsLoader setupNewsLoader() {
        return new NewsLoader(this) {
            @Override
            public void errorLoadingResults(String errorMessage) {
                NewsFragment.this.errorLoadingResults(errorMessage);
            }

            @Override
            public void onSuccess(List<News> loadedNews) {
                NewsFragment.this.onSuccess(loadedNews);
            }
        };
    }

    /**
     * When all of the news have loaded, add them to the recycler view and display them
     * @param loadedNews The loaded news
     */
    private void onSuccess(List<News> loadedNews) {
        newsList.clear();
        for(News news : loadedNews) {
            newsList.add(news);
        }

        setupRecyclerView();
        enableRefreshLayout();

        // Hide the progress bar
        if(loadingProgressBar != null)
            loadingProgressBar.setVisibility(View.GONE);

        if(mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    private void enableRefreshLayout() {
        if(swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setEnabled(true);
        }
    }

    /**
     * Setup the RecyclerView and link it to the NewsAdapter
     */
    private void setupRecyclerView() {
        if(recyclerView != null) {
            mAdapter = new NewsAdapter(newsList, Math.min(newsList.size(), Utilities.getRemoteConfigInt(this, R.string.rc_news_to_display)));
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(mAdapter);
        }
    }

    /**
     * Setup the refresh layout to refresh on swipe down past the first item
     */
    private void setupRefreshLayout() {
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
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
    }

    /**
     * Scroll the facility recycler view to the top
     */
    private void scrollFacilityListToTop() {
        recyclerView.scrollToPosition(0);
    }

    /**
     * There was an error loading the news. Display a message and a retry button.
     * @param errorMessage The message to show to the user
     */
    private void errorLoadingResults(String errorMessage) {
        if(getView() != null) {
            swipeRefreshLayout.setRefreshing(false);
            if (newsList.size() > 0) {
                Snackbar.make(getView(), "Unable to refresh news articles", Snackbar.LENGTH_SHORT).show();
            } else {
                loadingTextView.setVisibility(View.VISIBLE);
                loadingTextView.setText(errorMessage);
                loadingProgressBar.setVisibility(View.INVISIBLE);
                retryLoadButton.setVisibility(View.VISIBLE);
            }
        }
    }

    @OnClick(R.id.retry_load_button)
    public void retryLoadNews() {
        retryLoadButton.setVisibility(View.INVISIBLE);
        loadingTextView.setText("");
        loadingTextView.setVisibility(View.INVISIBLE);
        loadingProgressBar.setVisibility(View.VISIBLE);

        newsLoader.loadNews();
    }
}
