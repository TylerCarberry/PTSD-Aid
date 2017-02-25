package com.tytanapps.ptsd;

import android.app.Fragment;
import android.util.Log;

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
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static android.content.ContentValues.TAG;

/**
 * Load the list of recent PTSD News Articles
 * @author Tyler Carberry
 */
public abstract class NewsLoader {
    private static final String LOG_TAG = NewsLoader.class.getSimpleName();

    private Fragment fragment;

    // Stores the news that have already loaded
    // Key: Press id
    // Value: The News with the given id
    private HashMap<Integer, News> knownNews = new HashMap<>();

    // The number of news to load from the api
    private static final int NEWS_TO_LOAD = 50;

    public NewsLoader(Fragment fragment) {
        this.fragment = fragment;
    }

    public abstract void errorLoadingResults(String errorMessage);
    public abstract void onSuccess(List<News> loadedNews);


    public void loadNews() {
        // Start with the base api url
        Observable<News> fetchNews = Observable.just(calculateNewsUrl()).map(new Func1<String, JSONObject>() {
            @Override
            public JSONObject call(String url) {
                try {
                    String response = Utilities.readFromUrl(url).substring(2);
                    Log.d(TAG, "call() called with: url = [" + url + "]");
                    Log.d(TAG, "call: " + response);
                    return new JSONObject(response);
                } catch (IOException e) {
                    e.printStackTrace();
                    errorLoadingResults("Unable to load the news. Check you internet connection.");
                    return null;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        /*}).filter(new Func1<JSONObject, Boolean>() {
            @Override
            public Boolean call(JSONObject j) {
                return j != null;
            }*/
        // Given the JSONObject response, return a list of urls that contain more info about each individual news article
        }).map(new Func1<JSONObject, List<Integer>>() {
            @Override
            public List<Integer> call(JSONObject jsonObject) {
                try {
                    List<Integer> pressids = new LinkedList<>();
                    int numberOfResults = jsonObject.getInt("MATCHES");
                    for (int i = 1; i <= numberOfResults; i++) {
                        JSONObject ptsdProgramJson = jsonObject.getJSONObject("RESULTS").getJSONObject("" + i);
                        pressids.add(ptsdProgramJson.getInt("PRESS_ID"));
                    }
                    return pressids;
                }catch (JSONException e) {
                    Log.e(LOG_TAG, "call: ", e);
                    return null;
                }
            }
            // Flatten the list into just it's individual elements
        }).flatMap(new Func1<List<Integer>, Observable<Integer>>() {
            @Override
            public Observable<Integer> call(List<Integer> strings) {
                return Observable.from(strings);
            }
            // Get the api request for each individual news story
        }).flatMap(new Func1<Integer, Observable<? extends News>>() {
            @Override
            public Observable<? extends News> call(Integer integer) {
                return fetchArticle(integer);
            }
        });

        Observer<News> newsObserver = new Observer<News>() {
            /**
             * Gets called after all News items have loaded
             */
            @Override
            public void onCompleted() {
                Log.d(LOG_TAG, "onCompleted() called");
                allNewsHaveLoaded();
            }

            /**
             * If an error occurred
             * @param e the error that occurred
             */
            @Override
            public void onError(Throwable e) {
                Log.e(LOG_TAG, e.toString());
                errorLoadingResults("Unable to load the news. Check you internet connection.");
            }

            /**
             * Get called for every News object
             * @param news The news object that loaded
             */
            @Override
            public void onNext(News news) {
                knownNews.put(news.getPressId(), news);
                // Save the news to a file so it doesn't need to be loaded next time
                cacheNews(news);
            }
        };

        fetchNews
                .subscribeOn(Schedulers.newThread()) // Load the News on a new thread
                .observeOn(AndroidSchedulers.mainThread()) // Use the UI thread to display the news
                .subscribe(newsObserver);
    }

    private Observable<News> fetchArticle(final int pressid) {
        return Observable.concat(
                fetchArticleFromCache(pressid),
                fetchArticleFromNetwork(pressid)
        ).filter(new Func1<News, Boolean>() {
            @Override
            public Boolean call(News news) {
                return news != null;
            }
        }).first();
    }


    private Observable<News> fetchArticleFromCache(final int pressid) {
        return Observable.just(pressid).map(new Func1<Integer, News>() {
            @Override
            public News call(Integer pressid) {
                return readCachedNews(pressid);
            }
        });
    }


    private Observable<News> fetchArticleFromNetwork(final int pressId) {
        return Observable.just(pressId).map(new Func1<Integer, News>() {
            @Override
            public News call(Integer integer) {
                try {
                    String response = Utilities.readFromUrl(calculateArticleURL(pressId));
                    response = response.substring(2);
                    JSONObject rootJson = new JSONObject(response).getJSONObject("RESULTS").getJSONObject("1");
                    return parseJSONNews(rootJson);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        });
    }

    /**
     * Given a json response from the api, return a News object
     * @param rootJson The json response from the api
     * @return The article converted into a News
     * @throws JSONException If the json is malformed
     */
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

}
