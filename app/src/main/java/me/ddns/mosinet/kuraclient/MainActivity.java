package me.ddns.mosinet.kuraclient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import me.ddns.mosinet.kuraclient.fragment.DeviceFragment;
import me.ddns.mosinet.kuraclient.fragment.MeetingDisplayFragment;
import me.ddns.mosinet.kuraclient.fragment.MeetingEyeFragment;

public class MainActivity extends AppCompatActivity {

    /*----- Fields -----*/
    
    
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    // MQTT Client for communication with the devices
    private MQTTClient mqttClient;

    /* Views */

    // Label at the bottom of the screen
    private TextView mqttStateTextView;

    // Fragments for devices
    private DeviceFragment meetingDisplayFragment = new MeetingDisplayFragment();
    private DeviceFragment meetingEyeFragment = new MeetingEyeFragment();


    /*----- Activity lifecycle callbacks -----*/
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Keep the device awake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        /* Init views */

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // ViewPager
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Tab layout
        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        // MQTT status label
        mqttStateTextView = findViewById(R.id.mqttStateTextView);
    }

    @Override
    protected void onStart() {
        super.onStart();

        /* Set MQTT client from shared preferences */

        // Get shared preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        String brokerAddress = sharedPref.getString(getString(R.string.preferences_broker_address_key), "");
        String brokerUsername = sharedPref.getString(getString(R.string.preferences_broker_username_key), "");
        String brokerPassword = sharedPref.getString(getString(R.string.preferences_broker_password_key), "");

        if (brokerAddress.equals("") || brokerUsername.equals("") || brokerPassword.equals("")) {
            return;
        }

        // Init client
        mqttClient = new MQTTClient(getApplicationContext(), brokerAddress, brokerUsername, brokerPassword);
        mqttClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                // Set broker state label
                mqttStateTextView.setText(R.string.mqtt_state_connected);

                // Enable control
                meetingDisplayFragment.setUiEnabled(true);
                meetingEyeFragment.setUiEnabled(true);

                // Subscribe to device status topics
                try {
                    mqttClient.subscribeToDeviceStatuses(new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Log.i("mqtt","Subscribed to device status changes!");
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            Log.i("mqtt", "Failed to subscribe to device status changes!");
                        }
                    });
                }
                catch (MqttException e) {
                    Log.i("mqtt", "Failed to subscribe to device status changes!");
                }
            }

            @Override
            public void connectionLost(Throwable throwable) {
                // Set broker state label
                mqttStateTextView.setText(R.string.mqtt_state_not_connected);

                // Disable control
                meetingDisplayFragment.setUiEnabled(false);
                meetingEyeFragment.setUiEnabled(false);
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                // If the message is related to the Meeting Display
                if (topic.startsWith(getString(R.string.topic_prefix, "meeting_display"))) {
                    meetingDisplayFragment.messageArrived(topic, mqttMessage);
                }
                // If the message is related to the Meeting Eye
                else if (topic.startsWith(getString(R.string.topic_prefix, "meeting_eye"))) {
                    meetingEyeFragment.messageArrived(topic, mqttMessage);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });

        // Connect to MQTT broker
        try {
            mqttClient.connect();
        }
        catch (MqttException e) {
            Log.i("mqtt", "Connection attemp failed!");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Disconnect from MQTT broker
        if (mqttClient != null) {
            try {
                mqttClient.disconnect();
            }
            catch (MqttException e) {
                Log.i("mqtt", "Disconnection attemp failed!");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_kura_client, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // Open settings activity
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return meetingDisplayFragment;
                case 1:
                    return meetingEyeFragment;
                default:
                    return new Fragment();
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }
    }


    /* ----- Functions ----- */


    public MQTTClient getMqttClient() {
        return mqttClient;
    }
}
