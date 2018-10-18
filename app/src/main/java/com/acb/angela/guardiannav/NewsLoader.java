package com.acb.angela.guardiannav;


import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;

import com.acb.angela.guardiannav.utils.QueryUtils;

import java.util.List;

/**
 * AsyncTaskLoader implementation that opens a network connection and
 * query The Guardian API.
 */
public class NewsLoader extends AsyncTaskLoader<List<NewsArticle>> {

    // Data from the API
    private List<NewsArticle> mData;
    private Bundle mBundle;

    /**
     * Create a loader object
     *
     * @param context the {@link Context} of the application
     */
    public NewsLoader(Context context, Bundle bundle) {
        super(context);
        mBundle = bundle;
    }


    @Override
    public void onStartLoading() {
        if (mData != null) {
            // Use cached data
            deliverResult(mData);
        } else {
            forceLoad();
        }
    }


    @Override
    public List<NewsArticle> loadInBackground() {
        // Perform the network request, parse the response, and extract a list of news articles.
        List<NewsArticle> articleList = null;
        if (mBundle != null) {
            articleList = QueryUtils.fetchNewsData(mBundle.getString("uri"));
        }
        return articleList;
    }

    @Override
    public void deliverResult(List<NewsArticle> data) {
        // Cache data
        mData = data;
        super.deliverResult(data);
    }
}

