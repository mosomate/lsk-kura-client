package me.ddns.mosinet.kuraclient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import me.ddns.mosinet.kuraclient.fragment.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    /*----- Activity lifecycle callbacks -----*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(R.id.main_content, new SettingsFragment())
                .commit();

    }
}
