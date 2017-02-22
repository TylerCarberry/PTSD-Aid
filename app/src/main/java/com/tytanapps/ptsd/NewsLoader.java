package com.tytanapps.ptsd;

import android.app.Activity;
import android.app.Fragment;
import android.util.Log;

import com.android.volley.RequestQueue;
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
import java.util.LinkedList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Load the list of recent PTSD News Articles
 * @author Tyler Carberry
 */
public abstract class NewsLoader {
    private static final String LOG_TAG = NewsLoader.class.getSimpleName();

    private Fragment fragment;

    // The number of news articles that have already loaded, either by API or from cache
    // This number is still incremented when the API load fails
    //private int numberOfLoadedArticles = 0;

    // Stores the news that have already loaded
    // Key: Press id
    // Value: The News with the given id
    private HashMap<Integer, News> knownNews = new HashMap<>();

    private static final int NEWS_TO_LOAD = 50;

    public NewsLoader(Fragment fragment) {
        this.fragment = fragment;
    }

    public abstract void errorLoadingResults(String errorMessage);
    public abstract void onSuccess(List<News> loadedNews);


    public void loadNews() {
        final String url = calculateNewsUrl();

        Observable<String> fetchNews = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    String data = Utilities.readFromUrl(url);
                    subscriber.onNext(data); // Emit the contents of the URL
                    subscriber.onCompleted(); // Nothing more to emit
                } catch(Exception e){
                    subscriber.onError(e); // In case there are network errors
                }
            }
        });

        Observer<String> newsObserver = new Observer<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.e(LOG_TAG, e.toString());
                errorLoadingResults("Unable to load the news. Check you internet connection.");
            }

            @Override
            public void onNext(String response) {
                // The JSON that the sever responds starts with //
                // Trim the first two characters to create valid JSON.
                response = response.substring(2);

                // Load the initial JSON request
                try {
                    JSONObject rootJson = new JSONObject(response).getJSONObject("RESULTS");
                    final int numberOfResults = new JSONObject(response).getInt("MATCHES");

                    if (numberOfResults == 0) {
                        errorLoadingResults(fragment.getString(R.string.error_load_news));
                        return;
                    }

                    List<Integer> pressIds = new LinkedList<>();
                    for (int i = 1; i <= numberOfResults; i++) {
                        JSONObject ptsdProgramJson = rootJson.getJSONObject(""+i);
                        pressIds.add(ptsdProgramJson.getInt("PRESS_ID"));
                    }

                    Observable<String> observable = Observable.from(pressIds).map(new Func1<Integer, String>() {
                        @Override
                        public String call(Integer news_id) {
                            try {
                                return Utilities.readFromUrl(calculateArticleURL(news_id));
                            } catch (IOException e) {
                                e.printStackTrace();
                                return "";
                            }
                        }
                    });

                    observable
                            .subscribeOn(Schedulers.newThread()) // Create a new Thread
                            .observeOn(AndroidSchedulers.mainThread()) // Use the UI thread
                            .subscribe(new Subscriber<String>() {
                                @Override
                                public void onCompleted() {
                                    Log.d(LOG_TAG, "onCompleted() called");
                                    allNewsHaveLoaded();
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.e(LOG_TAG, e.toString());
                                    //incrementLoadedArticleCount(numberOfNews);
                                }

                                @Override
                                public void onNext(String response) {
                                    Log.d(LOG_TAG, "onNext() called with: response = [" + response + "]");


                                    // The JSON that the sever responds starts with //
                                    // I am cropping the first two characters to create valid JSON.
                                    response = response.substring(2);

                                    try {
                                        JSONObject rootJson = new JSONObject(response).getJSONObject("RESULTS").getJSONObject("1");
                                        News news = parseJSONNews(rootJson);

                                        knownNews.put(news.getPressId(), news);

                                        // Save the news to a file so it doesn't need to be loaded next time
                                        cacheNews(news);

                                        //numberOfLoadedArticles++;

                                        //if(numberOfLoadedArticles == numberOfNews)
                                        //    allNewsHaveLoaded();

                                    } catch (JSONException e) {
                                        FirebaseCrash.report(e);
                                        e.printStackTrace();
                                        //incrementLoadedArticleCount(numberOfNews);
                                    }
                                }
                            });



                } catch (JSONException e) {
                    FirebaseCrash.report(e);
                    e.printStackTrace();
                }
            }
        };

        fetchNews
                .subscribeOn(Schedulers.newThread()) // Create a new Thread
                .observeOn(AndroidSchedulers.mainThread()) // Use the UI thread
                .subscribe(newsObserver);
    }

    private void loadArticle(final int news_id, final int numberOfNews) {
        /*
        News cachedNews = readCachedNews(news_id);
        if(cachedNews != null) {
            knownNews.put(news_id, cachedNews);
            incrementLoadedArticleCount(numberOfNews);
            return;
        }
        */

        final String url = calculateArticleURL(news_id);
        Observable<String> fetchNews = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    String data = Utilities.readFromUrl(url);
                    subscriber.onNext(data); // Emit the contents of the URL
                    subscriber.onCompleted(); // Nothing more to emit
                } catch(Exception e){
                    subscriber.onError(e); // In case there are network errors
                }
            }
        });

        fetchNews
                .subscribeOn(Schedulers.newThread()) // Create a new Thread
                .observeOn(AndroidSchedulers.mainThread()) // Use the UI thread
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        Log.e(LOG_TAG, e.toString());
                        incrementLoadedArticleCount(numberOfNews);
                    }

                    @Override
                    public void onNext(String response) {
                        // The JSON that the sever responds starts with //
                        // I am cropping the first two characters to create valid JSON.
                        response = response.substring(2);

                        try {
                            JSONObject rootJson = new JSONObject(response).getJSONObject("RESULTS").getJSONObject("1");
                            News news = parseJSONNews(rootJson);

                            knownNews.put(news_id, news);

                            // Save the news to a file so it doesn't need to be loaded next time
                            cacheNews(news);

                            //numberOfLoadedArticles++;

                            //if(numberOfLoadedArticles == numberOfNews)
                            //    allNewsHaveLoaded();

                        } catch (JSONException e) {
                            FirebaseCrash.report(e);
                            e.printStackTrace();
                            incrementLoadedArticleCount(numberOfNews);
                        }
                    }
                });
    }

    private void incrementLoadedArticleCount(int numberOfNews) {
        //numberOfLoadedArticles++;

        //if(numberOfLoadedArticles == numberOfNews)
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
        pressDate = pressDate.replace(",", "");

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
    }

    public void refresh() {
        //numberOfLoadedArticles = 0;
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
        return "https://www.va.gov/webservices/press/releases.cfc?method=getPress_array&StartDate=01/01/2015&EndDate=01/01/2025&MaxRecords=" + NEWS_TO_LOAD + "&license=" + fragment.getString(R.string.api_key_press_release) + "&returnFormat=json";
    }

    private String calculateArticleURL(int pressId) {
        return "https://www.va.gov/webservices/press/releases.cfc?method=getPressDetail_array&press_id=" + pressId + "&license=" + fragment.getString(R.string.api_key_press_release) + "&returnFormat=json";
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
