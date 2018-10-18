package com.acb.angela.guardiannav.utils;


import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.acb.angela.guardiannav.NewsArticle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Utility class used to query the http://content.guardianapis.com api
 * to fetch news stories related to a certain topic.
 */

public final class QueryUtils {

    // Tag for the log messages
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    // Prevent accidental instantiation of this class
    private QueryUtils() {
    }

    /**
     * Query the search String and return a list of {@link NewsArticle} objects.
     */
    public static List<NewsArticle> fetchNewsData( final String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link NewsArticle}s
        return extractDataFromJson(jsonResponse);
    }



    /**
     * Returns new URL object from a given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            // The constructor URL(String spec)
            // Creates a URL object from the String representation.
            // Throws MalformedURLException - if no protocol is specified,
            // or an unknown protocol is found, or spec is null.
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        // Initialize json response
        String jsonResponse = "";

        // If URL is null, we shouldn't try to make an HTTP request, return early
        if (url == null)
            return jsonResponse;

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            // Call the URL object's openConnection method to get a URLConnection object
            // then cast it to a HttpURLConnection type
            urlConnection = (HttpURLConnection) url.openConnection();
            // Use this object to setup parameters and general request properties
            // that you may need before connecting
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            // Initiate the connection
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }


    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {

        // StringBuilder is a  mutable sequence of characters.
        // We'll use its append() method to build the output result.
        StringBuilder output = new StringBuilder();

        // InputStream represents an input stream of bytes (small chunks of data).
        if (inputStream != null) {
            // InputStreamReader is a bridge from byte streams to character streams.
            // The constructor InputStreamReader(InputStream in, Charset cs)
            // creates an InputStreamReader that uses the given charset.
            // The static method  Charset.forName(String charsetName)
            // returns a charset object for the named charset.
            InputStreamReader inputStreamReader = new
                    InputStreamReader(inputStream, Charset.forName("UTF-8"));

            // BufferedReader wrapper helps reading text for character-input stream
            // The constructor BufferedReader(Reader in) creates a
            // buffering character-input stream that uses a default-sized input buffer.
            BufferedReader reader = new BufferedReader(inputStreamReader);

            // Read one line of text at a time and append it to the output result
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }

        // Convert the mutable StringBuilder to an immutable String and return it.
        return output.toString();
    }


    /**
     * Return a list of {@link NewsArticle} objects that has been built up from
     * parsing the given JSON response.
     */
    private static List<NewsArticle> extractDataFromJson(String response) {
        // Return early if no data was returned from the HTTP request
        if (TextUtils.isEmpty(response)) {
            return null;
        }

        // Initialize list of strings to hold the extracted news articles
        List<NewsArticle> newsArticles = new ArrayList<>();

        // Traverse the raw JSON response parameter and extract relevant information
        try {
            // Create a JSONObject from the JSON response string
            JSONObject jsonResponse = new JSONObject(response);

            // Extract the JSONArray associated with the key called "response",
            // which represents a list of results (or articles).
            JSONObject jsonResults = jsonResponse.getJSONObject("response");
            JSONArray resultsArray = jsonResults.getJSONArray("results");

            if (resultsArray.length() > 0) {

                // For each article in the resultsArray, create an {@link NewsArticle} object
                for (int i = 0; i < resultsArray.length(); i++) {

                    JSONObject jsonArticle = resultsArray.getJSONObject(i);

                    String title = "" ;
                    if(jsonArticle.has("webTitle")) {
                        title = jsonArticle.getString("webTitle");
                    }

                    String section= "";
                    if(jsonArticle.has("sectionName")) {
                        section = jsonArticle.getString("sectionName");
                    }

                    String date = "";
                    if(jsonArticle.has("webPublicationDate")) {
                        date = jsonArticle.getString("webPublicationDate");
                    }
                    date = formatDate(date);

                    StringBuilder authors = new StringBuilder();
                    if (jsonArticle.has("tags")) {
                        JSONArray tagsArray = jsonArticle.getJSONArray("tags");
                        for (int j = 0; j < tagsArray.length(); j++) {
                            JSONObject tag = tagsArray.getJSONObject(j);
                            authors.append(tag.getString("webTitle")).append(" ");
                        }
                    }

                    String url = "";
                    if(jsonArticle.has("webUrl")) {
                        url = jsonArticle.getString("webUrl");
                    }

                    String thumbnail = "";
                    if(jsonArticle.has("fields")) {
                        JSONObject fields = jsonArticle.getJSONObject("fields");
                        thumbnail = fields.getString("thumbnail");
                    }

                    newsArticles.add(new NewsArticle(title, section, date, authors.toString(),
                            thumbnail, url));
                }
            }

        } catch (JSONException e) {
            Log.e("QueryUtils", "Error parsing JSON response", e);
        }

        // Return the successfully parsed news articles as a {@link List} object
        return newsArticles;
    }

    /**
     * Return the formatted date string (i.e. "Mar 3, 1984") from a Date object.
     */
    private static String formatDate(String rawDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        try {
            Date date = dateFormat.parse(rawDate);
            SimpleDateFormat finalDateFormat = new SimpleDateFormat(
                    "MMM d, yyy", Locale.US);
            return finalDateFormat.format(date);
        } catch (ParseException e) {
            Log.e("QueryUtils", "Error parsing JSON date: ", e);
            return "";
        }
    }



    public static boolean isNetworkAvailable(Activity activity) {
        // ConnectivityManager is a class that answers queries about the state of network
        // connectivity. It also notifies applications when network connectivity changes.
        ConnectivityManager manager = (ConnectivityManager)
                activity.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Instantiate a NetworkInfo object. It describes the status of a network interface.
        // Use getActiveNetworkInfo() to get an instance that represents the current network
        // connection. Requires the ACCESS_NETWORK_STATE permission.
        NetworkInfo networkInfo = null;
        if (manager != null) {
            networkInfo = manager.getActiveNetworkInfo();
        }

        boolean isAvailable = false;
        // If network is present and connected to the web
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }

        return isAvailable;
    }

}


