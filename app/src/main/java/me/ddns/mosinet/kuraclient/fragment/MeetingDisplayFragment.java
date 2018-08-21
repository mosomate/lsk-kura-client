package me.ddns.mosinet.kuraclient.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import me.ddns.mosinet.kuraclient.MainActivity;
import me.ddns.mosinet.kuraclient.R;

public class MeetingDisplayFragment extends Fragment {

    /*----- Fields -----*/

    /* Views */
    private Button powerButton;
    private View.OnClickListener powerOnListener;
    private View.OnClickListener powerOffListener;

    private TextView volumeLabel;
    private SeekBar volumeSeekBar;

    // Fields for the volume seekbar
    private long lastUpdated;
    private boolean volumeSettingInProgress = false;

    // bool for UI enable/disable
    private boolean uiEnabled = false;


    /*----- Fragment lifecycle callbacks -----*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meeting_display, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /* Init views */
        volumeLabel = view.findViewById(R.id.volumeLabel);
        lastUpdated = System.currentTimeMillis();

        volumeSeekBar = view.findViewById(R.id.volumeSeekBar);
        volumeSeekBar.setMax(100);
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                long currentSystemTime = System.currentTimeMillis();

                volumeLabel.setText(getString(R.string.meeting_display_volume_title, i));

                if (b) {
                    if ((currentSystemTime - lastUpdated) > 200) {
                        lastUpdated = currentSystemTime;
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                volumeSettingInProgress = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                volumeSettingInProgress = false;
            }
        });

        // Create callback for power on
        powerOnListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create map for metrics
                Map<String, Object> metrics = new HashMap<>();

                // Add power on command
                metrics.put("command", "power");
                metrics.put("value", 1);

                try {
                    ((MainActivity) getActivity()).getMqttClient().sendMessageToDevice("meeting_display", metrics);
                }
                catch (MqttException e) {
                    Log.i("MqttException", "Exception during sending 'power on' command: " + e.getMessage());
                }
            }
        };

        // Create callback for power off
        powerOffListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create map for metrics
                Map<String, Object> metrics = new HashMap<>();

                // Add power on command
                metrics.put("command", "power");
                metrics.put("value", 0);

                try {
                    ((MainActivity) getActivity()).getMqttClient().sendMessageToDevice("meeting_display", metrics);
                }
                catch (MqttException e) {
                    Log.i("MqttException", "Exception during sending 'power off' command: " + e.getMessage());
                }
            }
        };

        powerButton = view.findViewById(R.id.powerButton);

        // Update UI
        updateUI();
    }

    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        Log.w("messageArrived",mqttMessage.toString());

        long currentSystemTime = System.currentTimeMillis();

        JSONObject message = new JSONObject(mqttMessage.toString());

        if (topic.equals(getContext().getString(R.string.topic_prefix, "meeting_display/status/power"))) {
            String powerStatus = message.getJSONObject("metrics").getString("power");
        }
        else if (topic.equals(getContext().getString(R.string.topic_prefix, "meeting_display/status/volume"))) {
            if (!volumeSettingInProgress && (currentSystemTime - lastUpdated) > 1000) {
                int volume = message.getJSONObject("metrics").getInt("volume");
                volumeSeekBar.setProgress(volume);
            }
        }
    }

    public void setUiEnabled(boolean uiEnabled) {
        this.uiEnabled = uiEnabled;
        updateUI();
    }

    private void updateUI() {
        if (powerButton != null) {
            powerButton.setEnabled(uiEnabled);
        }
        if (volumeSeekBar != null) {
            volumeSeekBar.setEnabled(uiEnabled);
        }
    }
}
