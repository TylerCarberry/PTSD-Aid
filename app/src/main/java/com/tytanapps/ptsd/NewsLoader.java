package com.tytanapps.ptsd;

import android.app.Activity;
import android.app.Fragment;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.crash.FirebaseCrash;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Tyler on 6/26/16.
 */
public abstract class NewsLoader {
    private static final String LOG_TAG = NewsLoader.class.getSimpleName();

    private Fragment fragment;

    // The number of news articles that have already loaded, either by API or from cache
    // This number is still incremented when the API load fails
    private int numberOfLoadedArticles = 0;

    // Stores the news that have already loaded
    // Key: Press id
    // Value: The News with the given id
    private HashMap<Integer, News> knownNews = new HashMap<>();


    public NewsLoader(Fragment fragment) {
        this.fragment = fragment;
    }

    public abstract void errorLoadingResults(String errorMessage);
    public abstract void onSuccess(List<News> loadedNews);


    public void loadNews() {
        String url = calculateNewsUrl();

        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // The JSON that the sever responds starts with //
                // Trim the first two characters to create valid JSON.
                response = response.substring(2);

                // Load the initial JSON request
                try {
                    JSONObject rootJson = new JSONObject(response).getJSONObject("RESULTS");
                    int numberOfResults = new JSONObject(response).getInt("MATCHES");

                    if (numberOfResults == 0) {
                        errorLoadingResults("Unable to load the news. Check you internet connection.");
                        return;
                    }

                    for (int i = 1; i <= numberOfResults; i++) {
                        JSONObject ptsdProgramJson = rootJson.getJSONObject(""+i);
                        int pressId = ptsdProgramJson.getInt("PRESS_ID");

                        loadArticle(pressId, numberOfResults);

                    }
                } catch (JSONException e) {
                    FirebaseCrash.report(e);
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, error.toString());
                errorLoadingResults("Unable to load the news. Check you internet connection.");
            }
        });

        // Set a longer Volley timeout policy
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, // Timeout in milliseconds
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Start loading the PTSD programs in the background
        RequestQueue requestQueue = getRequestQueue();
        if (requestQueue != null)
            requestQueue.add(stringRequest);
        else
            errorLoadingResults("Unable to load the news. Check you internet connection.");
    }

    private void loadArticle(final int news_id, final int numberOfNews) {
        News cachedNews = readCachedNews(news_id);
        if(cachedNews != null) {
            knownNews.put(news_id, cachedNews);
            incrementLoadedArticleCount(numberOfNews);
            return;
        }

        String url = calculateArticleURL(news_id);

        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // The JSON that the sever responds starts with //
                // I am cropping the first two characters to create valid JSON.
                response = response.substring(2);

                try {
                    JSONObject rootJson = new JSONObject(response).getJSONObject("RESULTS").getJSONObject("1");
                    News news = parseJSONNews(rootJson);

                    knownNews.put(news_id, news);

                    // Save the news to a file so it doesn't need to be loaded next time
                    cacheNews(news);

                    numberOfLoadedArticles++;

                    if(numberOfLoadedArticles == numberOfNews)
                        allNewsHaveLoaded();

                } catch (JSONException e) {
                    FirebaseCrash.report(e);
                    e.printStackTrace();
                    incrementLoadedArticleCount(numberOfNews);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, error.toString());
                incrementLoadedArticleCount(numberOfNews);
            }
        });

        // Set a retry policy in case of SocketTimeout & ConnectionTimeout Exceptions.
        // Volley does retry for you if you have specified the policy.
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Start loading the image in the background
        RequestQueue requestQueue = getRequestQueue();
        if(requestQueue != null)
            requestQueue.add(stringRequest);
        else
            numberOfLoadedArticles++;
    }

    private void incrementLoadedArticleCount(int numberOfNews) {
        numberOfLoadedArticles++;

        if(numberOfLoadedArticles == numberOfNews)
            allNewsHaveLoaded();
    }

    private News parseJSONNews(JSONObject rootJson) throws JSONException {
        String title = rootJson.getString("PRESS_TITLE");

        String article = rootJson.getString("PRESS_TEXT");
        article = Utilities.htmlToText(article);
        article = article.substring(article.indexOf("–") + 1).trim();

        // Remove the extra space at the end of the text
        while(article.charAt(article.length() - 1) == '#' ||
                article.charAt(article.length() - 1) == '\n' ||
                // The two spaces below are different symbols. Do not simplify these lines
                article.charAt(article.length() - 1) == ' ' ||
                article.charAt(article.length() - 1) == ' ') {
            article = article.substring(0, article.length() - 1);
        }

        String pressDate = rootJson.getString("PRESS_DATE");
        pressDate = pressDate.substring(0, pressDate.length() - 8);

        return new News(title, article, rootJson.getInt("PRESS_ID"), pressDate);
    }

    /**
     * Called when all news have loaded and knownValues is fully populated
     */
    public void allNewsHaveLoaded() {
        ArrayList<News> newsArrayList = new ArrayList<>();
        for(News news : knownNews.values()) {
            newsArrayList.add(news);
        }

        // Sort the news by date (newest first)
        Collections.sort(newsArrayList);

        onSuccess(newsArrayList);
    };

    public void refresh() {
        numberOfLoadedArticles = 0;
        knownNews.clear();
        loadNews();
    }

    private void cacheNews(News news) {
        File file = getNewsFile(news.getPressId());
        ObjectOutput out;

        try {
            out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(news);
            out.close();
        } catch (IOException e) {
            FirebaseCrash.report(e);
            e.printStackTrace();
        }
    }

    private News readCachedNews(int pressId) {
        ObjectInputStream input;
        File file = getNewsFile(pressId);

        News news = null;

        try {
            input = new ObjectInputStream(new FileInputStream(file));
            news = (News) input.readObject();

            input.close();
        } catch (FileNotFoundException e) {
            // If the file was not found, nothing is wrong.
            // It just means that the news has not yet been cached.
        } catch (IOException | ClassNotFoundException e) {
            FirebaseCrash.report(e);
            e.printStackTrace();
        }

        return news;
    }

    /**
     * Get the file path of the news
     * @param pressId The press id of the news
     * @return The file path of the news
     */
    private File getNewsFile(int pressId) {
        String fileName = "news" + pressId;
        return new File(fragment.getActivity().getFilesDir(), fileName);
    }



    /**
     * Get the url for the PTSD Programs API
     * @return The url for the PTSD Programs API
     */
    private String calculateNewsUrl() {
        return "http://www.va.gov/webservices/press/releases.cfc?method=getPress_array&StartDate=01/01/2016&EndDate=01/01/2020&MaxRecords=10&license=" + fragment.getString(R.string.api_key_press_release) + "&returnFormat=json";
    }

    private String calculateArticleURL(int pressId) {
        return "http://www.va.gov/webservices/press/releases.cfc?method=getPressDetail_array&press_id=" + pressId + "&license=" + fragment.getString(R.string.api_key_press_release) + "&returnFormat=json";
    }

    /**
     * Get the request queue and create it if necessary
     * Precondition: fragment is a member of MainActivity
     * @return The request queue
     */
    private RequestQueue getRequestQueue() {
        Activity parentActivity = fragment.getActivity();
        if(parentActivity != null && parentActivity instanceof MainActivity) {
            return ((MainActivity) fragment.getActivity()).getRequestQueue();
        }
        return null;
    }

}
