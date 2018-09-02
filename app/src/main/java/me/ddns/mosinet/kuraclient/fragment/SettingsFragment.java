package me.ddns.mosinet.kuraclient.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;

import me.ddns.mosinet.kuraclient.R;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    /* Preferences */


    private SharedPreferences sharedPrefs;

    private EditTextPreference brokerAddress;
    private EditTextPreference brokerUsername;
    private EditTextPreference brokerPassword;


    /*----- Fragment lifecycle callbacks -----*/


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        // Init shared preferences
        sharedPrefs = getPreferenceScreen().getSharedPreferences();

        // Get references for the preferences
        brokerAddress = (EditTextPreference)findPreference(getString(R.string.preferences_broker_address_key));
        brokerUsername = (EditTextPreference)findPreference(getString(R.string.preferences_broker_username_key));
        brokerPassword = (EditTextPreference)findPreference(getString(R.string.preferences_broker_password_key));
    }

    @Override
    public void onStart() {
        super.onStart();

        /* Set default state for preferences */

        // MQTT broker host/ip
        brokerAddress.setSummary(sharedPrefs.getString(getString(R.string.preferences_broker_address_key), ""));

        // MQTT broker username
        brokerUsername.setSummary(sharedPrefs.getString(getString(R.string.preferences_broker_username_key), ""));

        // MQTT broker username
        brokerPassword.setSummary(hideText(sharedPrefs.getString(getString(R.string.preferences_broker_password_key), "")));
    }

    @Override
    public void onResume() {
        super.onResume();

        // Listening for preference changes
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Unlisten for preference changes
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

        /* Update preferences on changes */

        // If the the broker address changes
        if (s.equals(getString(R.string.preferences_broker_address_key))) {
            brokerAddress.setSummary(sharedPrefs.getString(getString(R.string.preferences_broker_address_key), ""));
        }
        // If the custom displayname changes
        else if (s.equals(getString(R.string.preferences_broker_username_key))) {
            brokerUsername.setSummary(sharedPrefs.getString(getString(R.string.preferences_broker_username_key), ""));
        }
        // If the custom displayname changes
        else if (s.equals(getString(R.string.preferences_broker_password_key))) {
            brokerPassword.setSummary(hideText(sharedPrefs.getString(getString(R.string.preferences_broker_password_key), "")));
        }
    }


    /*----- Functions -----*/


    private String hideText(String text) {
        StringBuilder hiddenText = new StringBuilder();
        for (int i=0; i<text.length(); ++i) {
            hiddenText.append("\u2022");
        }
        return hiddenText.toString();
    }
}
