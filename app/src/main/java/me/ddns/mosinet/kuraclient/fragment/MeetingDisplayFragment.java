package me.ddns.mosinet.kuraclient.fragment;

import android.graphics.PorterDuff;
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

    private Button pcSourceButton;
    private Button androidSourceButton;
    private Button hdmiSourceButton;

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
                        // Create map for metrics
                        Map<String, Object> metrics = new HashMap<>();

                        // Add power on command
                        metrics.put("command", "volume");
                        metrics.put("param", i);

                        ((MainActivity) getActivity()).getMqttClient().sendMessageToDevice("meeting_display", metrics);
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
                // Create map for metrics
                Map<String, Object> metrics = new HashMap<>();

                // Add power on command
                metrics.put("command", "volume");
                metrics.put("param", volumeSeekBar.getProgress());

                ((MainActivity) getActivity()).getMqttClient().sendMessageToDevice("meeting_display", metrics);
                volumeSettingInProgress = false;
            }
        });
        volumeSeekBar.setEnabled(false);

        // Create callback for power on
        powerOnListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create map for metrics
                Map<String, Object> metrics = new HashMap<>();

                // Add power on command
                metrics.put("command", "power");
                metrics.put("param", "on");

                ((MainActivity) getActivity()).getMqttClient().sendMessageToDevice("meeting_display", metrics);
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
                metrics.put("param", "off");

                ((MainActivity) getActivity()).getMqttClient().sendMessageToDevice("meeting_display", metrics);
            }
        };

        powerButton = view.findViewById(R.id.powerButton);
        powerButton.setOnClickListener(powerOnListener);

        pcSourceButton = view.findViewById(R.id.pcSourceButton);
        pcSourceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create map for metrics
                Map<String, Object> metrics = new HashMap<>();

                // Add power on command
                metrics.put("command", "source");
                metrics.put("param", "pc");

                ((MainActivity) getActivity()).getMqttClient().sendMessageToDevice("meeting_display", metrics);
            }
        });
        pcSourceButton.setEnabled(false);

        androidSourceButton = view.findViewById(R.id.androidSourceButton);
        androidSourceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create map for metrics
                Map<String, Object> metrics = new HashMap<>();

                // Add power on command
                metrics.put("command", "source");
                metrics.put("param", "android");

                ((MainActivity) getActivity()).getMqttClient().sendMessageToDevice("meeting_display", metrics);
            }
        });
        androidSourceButton.setEnabled(false);

        hdmiSourceButton = view.findViewById(R.id.hdmiSourceButton);
        hdmiSourceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create map for metrics
                Map<String, Object> metrics = new HashMap<>();

                // Add power on command
                metrics.put("command", "source");
                metrics.put("param", "hdmi");

                ((MainActivity) getActivity()).getMqttClient().sendMessageToDevice("meeting_display", metrics);
            }
        });
        hdmiSourceButton.setEnabled(false);

        // Update UI
        updateUI();
    }

    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        Log.w("messageArrived",mqttMessage.toString());

        long currentSystemTime = System.currentTimeMillis();

        JSONObject message = new JSONObject(mqttMessage.toString());
        Log.i("json", topic);

        if (topic.equals(getContext().getString(R.string.topic_prefix, "meeting_display/status/power"))) {
            String powerStatus = message.getJSONObject("metrics").getString("value");
            //Log.i("json", powerStatus);
            if (powerStatus.equals("on")) {
                powerButton.setText(R.string.meeting_display_power_off_title);
                powerButton.setOnClickListener(powerOffListener);
                volumeSeekBar.setEnabled(true);
                pcSourceButton.setEnabled(true);
                androidSourceButton.setEnabled(true);
                hdmiSourceButton.setEnabled(true);
            }
            else {
                powerButton.setText(R.string.meeting_display_power_on_title);
                powerButton.setOnClickListener(powerOnListener);
                volumeSeekBar.setEnabled(false);
                pcSourceButton.setEnabled(false);
                androidSourceButton.setEnabled(false);
                hdmiSourceButton.setEnabled(false);
            }
        }
        else if (topic.equals(getContext().getString(R.string.topic_prefix, "meeting_display/status/volume"))) {
            if (!volumeSettingInProgress && (currentSystemTime - lastUpdated) > 1000) {
                int volume = message.getJSONObject("metrics").getInt("value");
                volumeSeekBar.setProgress(volume);
            }
        }
        else if (topic.equals(getContext().getString(R.string.topic_prefix, "meeting_display/status/source"))) {
            String sourceStatus = message.getJSONObject("metrics").getString("value");

            // Remove all color filters from button background
            pcSourceButton.getBackground().clearColorFilter();
            androidSourceButton.getBackground().clearColorFilter();
            hdmiSourceButton.getBackground().clearColorFilter();

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

    public void setUiEnabled(boolean uiEnabled) {
        this.uiEnabled = uiEnabled;
        updateUI();
    }

    private void updateUI() {
        if (powerButton != null) {
            powerButton.setEnabled(uiEnabled);
        }
    }
}
