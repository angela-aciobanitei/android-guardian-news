package com.acb.angela.guardiannav;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.acb.angela.guardiannav.utils.QueryUtils;

import java.util.ArrayList;
import java.util.List;

public class NewsActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<List<NewsArticle>>,
        SharedPreferences.OnSharedPreferenceChangeListener{

    // Constant value for the news articles loader ID
    private static final int NEWS_LOADER_ID = 1;

    // Adapter for the list of news articles
    private NewsArticleAdapter mAdapter;

    // The container for a list of NewsArticle objects.
    private ListView mNewsList;

    // This view is only visible when the list has no items.
    private TextView mEmptyView;

    // The loader indicator.
    private ProgressBar mProgressBar;

    // DrawerLayout acts as a top-level container for window content that allows for
    // interactive "drawer" views to be pulled out from one or both vertical edges of the window.
    private DrawerLayout mDrawerLayout = null;
    private ListView mDrawerList;

    // ActionBarDrawerToggle provides a handy way to tie together the functionality of
    // DrawerLayout and the framework ActionBar to implement the design for navigation drawers.
    private ActionBarDrawerToggle mDrawerToggle;

    private String mSearchString = "";
    private String mSearchSection = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        // Get search section and search string from bundle.
        if (savedInstanceState != null) {
            mSearchSection = savedInstanceState.getString(getString(R.string.key_search_section));
            mSearchString = savedInstanceState.getString(getString(R.string.key_search_string));
        }

        // Set title for Action Bar.
        setActionBarTitle(mSearchString, mSearchSection);

        // Set the view that is displayed when the list is empty.
        mNewsList = findViewById(R.id.article_list_view);
        mEmptyView = findViewById(R.id.empty_view);
        mEmptyView.setText(R.string.no_news_message);
        mNewsList.setEmptyView(mEmptyView);

        // Initialize and set the adapter on the {@link ListView}
        // so the list can be populated in the user interface.
        mAdapter = new NewsArticleAdapter(this, new ArrayList<NewsArticle>());
        mNewsList.setAdapter(mAdapter);

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected news article.
        mNewsList.setOnItemClickListener(new NewsItemClickListener());

        // Find a reference to the {@link ProgressBar} in the layout.
        mProgressBar = findViewById(R.id.loading_indicator);

        if(QueryUtils.isNetworkAvailable(this)) {
            initSearch(mSearchString, mSearchSection);
            getSupportLoaderManager()
                    .initLoader(NEWS_LOADER_ID, null, NewsActivity.this);
        }
        else {
            // Display error message to the user, but first hide the progress bar.
            mProgressBar.setVisibility(View.GONE);
            mEmptyView.setText(R.string.no_internet_connection);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.drawer_list);

        // Add navigation drawer header.
        View header = getLayoutInflater().inflate(R.layout.drawer_header, null);
        mDrawerList.addHeaderView(header);

        // Set up the adapter for the drawer's list view.
        mDrawerList.setAdapter(new ArrayAdapter<>(
                this,
                R.layout.drawer_list_item,
                getResources().getStringArray(R.array.navigation_drawer_list)));

        // Set up click listener for the navigation drawer list items.
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // Enable ActionBar app icon to behave as action to toggle nav drawer.
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        // Set up drawer using ActionBarDrawerToggle.
        setupDrawerToggle();

        // Select(highlight) first item from nav drawer list on startup.
        if (savedInstanceState == null) {
            mDrawerList.setItemChecked(1, true);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        executeSearch();
    }

    //Callback method when OnSharedPreferenceChangeListener is triggered.
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.contains(getString(R.string.settings_order_by_key))
            || key.contains(getString(R.string.settings_order_date_key))){
            // The onCreateLoader method will read the preferences
            getSupportLoaderManager().restartLoader(NEWS_LOADER_ID, null, this);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }


    private void setActionBarTitle(String searchString, String searchSection) {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            if (searchSection == null) {
                actionBar.setTitle("Headlines " + searchString);
            }
            else {
                if (TextUtils.equals(searchString, "")) {
                    actionBar.setTitle("Headlines "+ searchSection);
                }
                else {
                    actionBar.setTitle("Headlines "+ searchSection + "/" + searchString);
                }
            }
        }
    }

    private void initSearch(String searchString, String searchSection) {

        // Get a SharedPreferences instance that points to the default file
        // that is used by the preference framework in the given context.
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        // Retrieve a String value from the preferences.
        String edition = sharedPrefs.getString(
                getString(R.string.settings_edition_key),
                getString(R.string.settings_edition_default)
        );

        String orderBy  = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );

        String orderDate  = sharedPrefs.getString(
                getString(R.string.settings_order_date_key),
                getString(R.string.settings_order_date_default)
        );

        String url;
        if (searchSection == null) {
            // Build up the query URI for a specific search string
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .encodedAuthority("content.guardianapis.com")
                    .appendPath("search")
                    .appendQueryParameter("editions", edition)
                    .appendQueryParameter("q", searchString)
                    .appendQueryParameter("tags", "news")
                    .appendQueryParameter("order-date", orderDate)
                    .appendQueryParameter("order-by", orderBy)
                    .appendQueryParameter("show-references", "author")
                    .appendQueryParameter("show-tags", "contributor")
                    .appendQueryParameter("show-fields", "thumbnail")
                    .appendQueryParameter("api-key", "test");
            url = builder.build().toString();

        } else {
            // Build up the query URI for a specific search section
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .encodedAuthority("content.guardianapis.com")
                    .appendPath("search")
                    .appendQueryParameter("editions", edition)
                    .appendQueryParameter("q", searchString)
                    .appendQueryParameter("section", searchSection)
                    .appendQueryParameter("order-date", orderDate)
                    .appendQueryParameter("order-by", orderBy)
                    .appendQueryParameter("show-references", "author")
                    .appendQueryParameter("show-tags", "contributor")
                    .appendQueryParameter("show-fields", "thumbnail")
                    .appendQueryParameter("api-key", "test");
            url = builder.build().toString();
        }
        Bundle args = new Bundle();
        args.putString("uri", url);
        getSupportLoaderManager().restartLoader(NEWS_LOADER_ID, args, this);
        mEmptyView.setVisibility(View.GONE);
    }

    /* The click listener for the ListView in the main activity layout. */
    private class NewsItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Find the current news article that was clicked on
            NewsArticle currentArticle = mAdapter.getItem(position);

            // Convert the String URL into a URI object (to pass into the Intent constructor)
            Uri uri = null;
            if (currentArticle != null)
                uri = Uri.parse(currentArticle.getUrl());

            // Create intent and launch a new activity
            Intent websiteIntent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(websiteIntent);
        }
    }

    /* The click listener for the ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
                case 1:
                    onSectionClick(null, mDrawerLayout, mDrawerList);
                    break;
                case 2:
                    onSectionClick(getString(R.string.section_world), mDrawerLayout, mDrawerList);
                    break;
                case 3:
                    onSectionClick(getString(R.string.section_business), mDrawerLayout, mDrawerList);
                    break;
                case 4:
                    onSectionClick(getString(R.string.section_sport), mDrawerLayout, mDrawerList);
                    break;
                case 5:
                    onSectionClick(getString(R.string.section_football), mDrawerLayout, mDrawerList);
                    break;
                case 6:
                    onSectionClick(getString(R.string.section_politics), mDrawerLayout, mDrawerList);
                    break;
                case 7:
                    onSectionClick(getString(R.string.section_environment), mDrawerLayout, mDrawerList);
                    break;
                case 8:
                    onSectionClick(getString(R.string.section_education), mDrawerLayout, mDrawerList);
                    break;
                case 9:
                    onSectionClick(getString(R.string.section_science), mDrawerLayout, mDrawerList);
                    break;
                case 10:
                    onSectionClick(getString(R.string.section_tech), mDrawerLayout, mDrawerList);
                    break;
                case 11:
                    onSectionClick(getString(R.string.section_culture), mDrawerLayout, mDrawerList);
                    break;
                case 12:
                    onSectionClick(getString(R.string.section_film), mDrawerLayout, mDrawerList);
                    break;
                case 13:
                    onSectionClick(getString(R.string.section_music), mDrawerLayout, mDrawerList);
                    break;
                case 14:
                    onSectionClick(getString(R.string.section_books), mDrawerLayout, mDrawerList);
                    break;
                case 15:
                    onSectionClick(getString(R.string.section_travel), mDrawerLayout, mDrawerList);
                    break;
            }
        }
    }

   protected void onSectionClick(String searchSection, DrawerLayout drawerLayout, ListView drawerList) {
        mSearchSection = searchSection;

        drawerLayout.closeDrawer(drawerList);

        mProgressBar.setVisibility(View.VISIBLE);
        mNewsList.setVisibility(View.GONE);

        setActionBarTitle(mSearchString, mSearchSection);

       executeSearch();
   }

    private void executeSearch() {
        if (QueryUtils.isNetworkAvailable(this)) {
            initSearch(mSearchString, mSearchSection);
        } else {
            // Display error message to the user, but first hide the progress bar.
            mProgressBar.setVisibility(View.GONE);
            mNewsList.setEmptyView(mEmptyView);
            mEmptyView.setText(getString(R.string.no_internet_connection));
            if (mAdapter != null) {
                mAdapter.clear();
            }
        }
    }

    /**
     * Set up drawer using ActionBarDrawerToggle.
     * ActionBarDrawerToggle ties together the the proper interactions
     * between the sliding drawer and the action bar app icon.
     */
    private void setupDrawerToggle() {
        // Override DrawerLayout.DrawerListener callback methods
        // with an instance of the ActionBarDrawerToggle class.
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.drawer_open,
                R.string.drawer_close) {

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // Set title for Action Bar.
                mSearchString = "";
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("News Navigation ");
                }
                // Creates call to onPrepareOptionsMenu()
                invalidateOptionsMenu();
            }

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                // Set title for Action Bar.
                setActionBarTitle(mSearchString, mSearchSection);
                // Creates call to onPrepareOptionsMenu()
                invalidateOptionsMenu();
            }
        };
        // Enable or disable the drawer indicator. The indicator defaults to enabled.
        // When the indicator is disabled, the ActionBar will revert to displaying the home-as-up
        // indicator provided by the Activity's theme in the android.R.attr.homeAsUpIndicator
        // attribute instead of the animated drawer glyph.
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        // Add listener to listen for open and close events.
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggle
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Called to retrieve per-instance state from an activity before being killed
     * so that the state can be restored in onCreate(Bundle) or onRestoreInstanceState(Bundle)
     * (the Bundle populated by this method will be passed to both).
     *
     * @param outState Bundle in which to place your saved state.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(getString(R.string.key_search_string), mSearchString);
        outState.putString(getString(R.string.key_search_section), mSearchSection);
    }

    /**
     * Initialize the contents of the Activity's standard options menu.
     * This is only called once, the first time the options menu is displayed.
     * To update the menu every time it is displayed, see onPrepareOptionsMenu(Menu).
     * The default implementation populates the menu with standard system menu items.
     * These are placed in the CATEGORY_SYSTEM group so that they will be correctly
     * ordered with application-defined menu items.
     * Deriving classes should always call through to the base implementation.
     * You can safely hold on to menu (and any items created from it), making modifications
     * to it as desired, until the next time onCreateOptionsMenu() is called.
     * When you add items to the menu, you can implement the Activity's
     * onOptionsItemSelected(MenuItem) method to handle them there.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed; if you return false it will not be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) item.getActionView();
        // Set listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            /**
             * Called when the user submits the query. This could be due to a key press
             * on the keyboard or due to pressing a submit button. The listener can override
             * the standard behavior by returning true to indicate that it has handled the
             * submit request. Otherwise return false to let the SearchView handle the
             * submission by launching any associated intent.
             *
             * @param query String: the query text that is to be submitted
             * @return true if the query has been handled by the listener,
             *         false to let the SearchView perform the default action.
             */
            @Override
            public boolean onQueryTextSubmit(String query) {
                mProgressBar.setVisibility(View.VISIBLE);
                mNewsList.setVisibility(View.GONE);

                searchView.onActionViewCollapsed();
                mSearchString = query;

                setActionBarTitle(mSearchString, mSearchSection);

                executeSearch();
                return false;
            }

            /**
             * Called when the query text is changed by the user.
             *
             * @param newText	String: the new content of the query text field.
             * @return  false if the SearchView should perform the default action of showing any
             *          suggestions if available, true if the action was handled by the listener.
             */
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    /*Called whenever we call invalidateOptionsMenu()*/
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_search).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal processing
     * happen (calling the item's Runnable or sending a message to its Handler as appropriate).
     * You can use this method for any items for which you would like to do processing without
     * those other facilities.  Derived classes should call through to the base class for it to
     * perform the default menu handling.
     *
     * @param item The menu item that was selected.
     * @return Return false to allow normal menu processing to proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);

    }


    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return  A new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<List<NewsArticle>> onCreateLoader(int id, Bundle args) {
        return new NewsLoader(this, args);
    }


    /**
     * Called when the data has been loaded. Gets the desired information from
     * the JSON and updates the Views.
     *
     * @param loader The loader that has finished.
     * @param data The JSON response from The Guardian API.
     */
    @Override
    public void onLoadFinished(Loader<List<NewsArticle>> loader, List<NewsArticle> data) {

        // Hide loader indicator.
        mProgressBar.setVisibility(View.GONE);

        // Clear the adapter of previous data.
        if (mAdapter != null)
            mAdapter.clear();

        // If there is a valid list of {@link NewsArticle}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (data != null && !data.isEmpty()) {
            mAdapter.addAll(data);
            mNewsList.setVisibility(View.VISIBLE);
        } else {
            mNewsList.setVisibility(View.GONE);
            mNewsList.setEmptyView(mEmptyView);
            mEmptyView.setText(R.string.no_news_message);
        }
    }


    /**
     * Called when a previously created loader is being reset, thus making its data unavailable.
     *
     * @param loader The loader that was reset.
     */
    @Override
    public void onLoaderReset(Loader<List<NewsArticle>> loader) {
        // Clear the adapter of previous data
        if (mAdapter != null)
            mAdapter.clear();
    }


}

