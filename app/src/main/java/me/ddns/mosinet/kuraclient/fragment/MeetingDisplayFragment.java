package me.ddns.mosinet.kuraclient.fragment;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import me.ddns.mosinet.kuraclient.R;

public class MeetingDisplayFragment extends DeviceFragment {

    /*----- Fields -----*/


    /* Views */

    // Power
    private Button powerButton;
    private View.OnClickListener powerOnListener;
    private View.OnClickListener powerOffListener;

    // Source
    private Button pcSourceButton;
    private Button androidSourceButton;
    private Button hdmiSourceButton;

    // Volume
    private TextView volumeLabel;
    private SeekBar volumeSeekBar;


    /* Other */

    // Timestamp for the last time the seekbar was updated
    private long lastUpdated;
    private boolean volumeSettingInProgress = false;


    /*----- Fragment lifecycle callbacks -----*/


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meeting_display, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        /* Init volume views */

        // Volume label
        volumeLabel = view.findViewById(R.id.volumeLabel);

        // Volume seekbar
        volumeSeekBar = view.findViewById(R.id.volumeSeekBar);
        // The max volume is 100
        volumeSeekBar.setMax(100);
        // Set change listener
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // If it's user interaction
                if (b) {
                    // Get current time
                    long currentSystemTime = System.currentTimeMillis();

                    // Send volume setting command if at least 200 ms
                    // has been elapsed from the previous one
                    if ((currentSystemTime - lastUpdated) > 200) {
                        // Send command
                        sendSingleParamCommand("volume", i);

                        // Update timestamp
                        lastUpdated = currentSystemTime;
                    }
                }

                // Update volume indicator label to the new volume
                volumeLabel.setText(getString(R.string.meeting_display_volume_title, i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Set to true when the user starts to drag the seekbar
                volumeSettingInProgress = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Send volume settings command when the user
                // stops to drag the seekbar to send the last volume
                sendSingleParamCommand("volume", volumeSeekBar.getProgress());

                // Set to false, the user is done with the seekbar
                volumeSettingInProgress = false;

                // Update timestamp
                lastUpdated = System.currentTimeMillis();
            }
        });
        // Disable the seekbar by default
        volumeSeekBar.setEnabled(false);
        // Set last updated to current time
        lastUpdated = System.currentTimeMillis();


        /* Init power views */

        // Create callback for power on action
        powerOnListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSingleParamCommand("power", "on");
            }
        };

        // Create callback for power off action
        powerOffListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSingleParamCommand("power", "off");
            }
        };

        powerButton = view.findViewById(R.id.powerButton);
        // The default listener is the power on
        powerButton.setOnClickListener(powerOnListener);


        /* Init source selection buttons */

        // PC
        pcSourceButton = view.findViewById(R.id.pcSourceButton);
        pcSourceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSingleParamCommand("source", "pc");
            }
        });
        pcSourceButton.setEnabled(false);

        // Android
        androidSourceButton = view.findViewById(R.id.androidSourceButton);
        androidSourceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSingleParamCommand("source", "android");
            }
        });
        androidSourceButton.setEnabled(false);

        // HDMI
        hdmiSourceButton = view.findViewById(R.id.hdmiSourceButton);
        hdmiSourceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSingleParamCommand("source", "android");
            }
        });
        hdmiSourceButton.setEnabled(false);


        // Update UI
        updateUI();
    }


    /*----- Functions -----*/


    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        Log.w("mqtt", "Message arrived: " + mqttMessage.toString());

        // Transform message to JSON
        JSONObject message = new JSONObject(mqttMessage.toString());

        // If the message is related to power status changes
        if (topic.equals(getContext().getString(R.string.topic_prefix, "meeting_display/status/power"))) {
            // Get power status
            boolean isTurnedOn = message.getJSONObject("metrics").getString("value").equals("on");

            // Set label for power on/off button
            powerButton.setText( (isTurnedOn)?(R.string.meeting_display_power_off_title):(R.string.meeting_display_power_on_title) );

            // Set click listener
            powerButton.setOnClickListener( (isTurnedOn)?(powerOffListener):(powerOnListener) );

            // Enable / disable views
            volumeSeekBar.setEnabled(isTurnedOn);
            pcSourceButton.setEnabled(isTurnedOn);
            androidSourceButton.setEnabled(isTurnedOn);
            hdmiSourceButton.setEnabled(isTurnedOn);
        }
        // If the message is related to volume status changes
        else if (topic.equals(getContext().getString(R.string.topic_prefix, "meeting_display/status/volume"))) {
            // Update the seekbar if at least 1 second has elapsed from the
            // last user initiated volume setting and setting is not in progress
            if (!volumeSettingInProgress && (System.currentTimeMillis() - lastUpdated) > 1000) {
                // Set seekbar
                volumeSeekBar.setProgress( message.getJSONObject("metrics").getInt("value") );
            }
        }
        // If the message is related to source status changes
        else if (topic.equals(getContext().getString(R.string.topic_prefix, "meeting_display/status/source"))) {
            // Get source status
            String sourceStatus = message.getJSONObject("metrics").getString("value");

            // Remove all color filters from button background
            pcSourceButton.getBackground().clearColorFilter();
            androidSourceButton.getBackground().clearColorFilter();
            hdmiSourceButton.getBackground().clearColorFilter();

            // Set background color filter for the right source button
            if (sourceStatus.equals("pc")) {
                pcSourceButton.getBackground().setColorFilter(getContext().getColor(R.color.buttonHighLightColor), PorterDuff.Mode.MULTIPLY);
            }
            else if (sourceStatus.equals("android")) {
                androidSourceButton.getBackground().setColorFilter(getContext().getColor(R.color.buttonHighLightColor), PorterDuff.Mode.MULTIPLY);
            }
            else if (sourceStatus.equals("hdmi")) {
                hdmiSourceButton.getBackground().setColorFilter(getContext().getColor(R.color.buttonHighLightColor), PorterDuff.Mode.MULTIPLY);
            }
        }
    }

    @Override
    protected void updateUI() {
        if (powerButton != null) {
            powerButton.setEnabled(isUiEnabled());
        }
    }

    @Override
    protected String getMqttDeviceId() {
        return "meeting_display";
    }
}
