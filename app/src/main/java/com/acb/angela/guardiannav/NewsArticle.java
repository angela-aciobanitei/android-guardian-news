package com.acb.angela.guardiannav;

import android.graphics.Bitmap;

/**
 * The Data  Model object is an NewsArticle objects with the following properties:
 *
 *      title - the title of the article,
 *      section - the section the article belongs to,
 *      date - the date when the article was published,
 *      author - the author of the article,
 *      thumbnail - the image for the article,
 *      URL - the web URL for the article.
 *
 */

public class NewsArticle {

    private String mTitle;
    private String mSection;
    private String mDate;
    private String mAuthor;
    private String mThumbnail;
    private String mUrl;

    public NewsArticle(String title, String section, String date, String author,
                       String thumbnail, String url) {
        mTitle = title;
        mSection = section;
        mDate = date;
        mAuthor = author;
        mThumbnail = thumbnail;
        mUrl = url;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSection() {
        return mSection;
    }

    public String getDate() {
        return mDate;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getThumbnail() {
        return mThumbnail;
    }

    public String getUrl() {
        return mUrl;
    }
}

