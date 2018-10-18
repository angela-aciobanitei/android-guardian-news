package com.acb.angela.guardiannav;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public static class GuardianPreferenceFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener{

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Inflate the given XML resource and add the preference hierarchy
            // to the current preference hierarchy.
            addPreferencesFromResource(R.xml.settings_main);

            // Find a Preference based on its key.
            Preference edition = findPreference(getString(R.string.settings_edition_key));
            bindPreferenceSummaryToValue(edition);

            Preference orderBy = findPreference(getString(R.string.settings_order_by_key));
            bindPreferenceSummaryToValue(orderBy);

            Preference orderDate = findPreference(getString(R.string.settings_order_date_key));
            bindPreferenceSummaryToValue(orderDate);
        }

        /**
         * Called when a Preference has been changed by the user. This is called
         * before the state of the Preference is about to be updated and
         * before the state is persisted.
         *
         * @param preference The changed Preference.
         * @param newValue The new value of the Preference.
         * @return True to update the state of the Preference with the new value.
         */
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            String stringValue = newValue.toString();

            if (preference instanceof ListPreference) {
                // Cast from Preference to ListPreference.
                ListPreference listPreference = (ListPreference) preference;

                // Find the index of the given value (in the entry values array).
                int prefIndex = listPreference.findIndexOfValue(stringValue);
                if (prefIndex >= 0) {
                    // The list of entries to be shown in the list in subsequent dialogs.
                    CharSequence[] labels = listPreference.getEntries();
                    // Set the summary for this Preference.
                    preference.setSummary(labels[prefIndex]);
                }
            }
            else {
                preference.setSummary(stringValue);
            }

            return true;
        }


        private void bindPreferenceSummaryToValue(Preference preference) {
            // Sets the callback to be invoked when this Preference is changed
            // by the user (but before the internal state has been updated).
            preference.setOnPreferenceChangeListener(this);

            // Get a SharedPreferences instance that points to the default file
            // that is used by the preference framework in the given context.
            SharedPreferences preferences = PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext());
            // Retrieve a String value from the preferences.
            String preferenceString = preferences.getString(preference.getKey(), "");
            onPreferenceChange(preference, preferenceString);
        }
    }


}
