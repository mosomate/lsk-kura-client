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

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import me.ddns.mosinet.kuraclient.MainActivity;
import me.ddns.mosinet.kuraclient.R;

public class MeetingEyeFragment extends Fragment{

    /*----- Fields -----*/

    /* Views */
    private Button powerButton;
    private View.OnClickListener powerOnListener;
    private View.OnClickListener powerOffListener;

    // Tilting
    private Button tiltLeftButton;
    private Button tiltUpButton;
    private Button tiltRightButton;
    private Button tiltDownButton;

    // Zoom
    private Button zoomInButton;
    private Button zoomOutButton;

    // Presets
    private Button preset1Button;
    private Button preset2Button;
    private Button preset3Button;

    // bool for UI enable/disable
    private boolean uiEnabled = false;


    /*----- Fragment lifecycle callbacks -----*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meeting_eye, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /* Init views */

        // Tilting
        tiltLeftButton = view.findViewById(R.id.tiltLeftButton);
        tiltLeftButton.setEnabled(false);
        tiltUpButton = view.findViewById(R.id.tiltUpButton);
        tiltUpButton.setEnabled(false);
        tiltRightButton = view.findViewById(R.id.tiltRightButton);
        tiltRightButton.setEnabled(false);
        tiltDownButton = view.findViewById(R.id.tiltDownButton);
        tiltDownButton.setEnabled(false);

        // Zoom
        zoomInButton = view.findViewById(R.id.zoomInButton);
        zoomInButton.setEnabled(false);
        zoomOutButton = view.findViewById(R.id.zoomOutButton);
        zoomOutButton.setEnabled(false);

        // Presets
        preset1Button = view.findViewById(R.id.preset1Button);
        preset1Button.setEnabled(false);
        preset2Button = view.findViewById(R.id.preset2Button);
        preset2Button.setEnabled(false);
        preset3Button = view.findViewById(R.id.preset3Button);
        preset3Button.setEnabled(false);

        // Create callback for power on
        powerOnListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create map for metrics
                Map<String, Object> metrics = new HashMap<>();

                // Add power on command
                metrics.put("command", "power");
                metrics.put("param", "on");

                ((MainActivity) getActivity()).getMqttClient().sendMessageToDevice("meeting_eye", metrics);
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

                ((MainActivity) getActivity()).getMqttClient().sendMessageToDevice("meeting_eye", metrics);
            }
        };

        powerButton = view.findViewById(R.id.powerButton);
        powerButton.setOnClickListener(powerOnListener);

        // Update UI
        updateUI();
    }

    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        Log.w("messageArrived",mqttMessage.toString());

        long currentSystemTime = System.currentTimeMillis();

        JSONObject message = new JSONObject(mqttMessage.toString());
        Log.i("json", topic);

        if (topic.equals(getContext().getString(R.string.topic_prefix, "meeting_eye/status/power"))) {
            String powerStatus = message.getJSONObject("metrics").getString("value");
            //Log.i("json", powerStatus);
            if (powerStatus.equals("on")) {
                powerButton.setText(R.string.meeting_eye_power_off_title);
                powerButton.setOnClickListener(powerOffListener);

                tiltLeftButton.setEnabled(true);
                tiltUpButton.setEnabled(true);
                tiltRightButton.setEnabled(true);
                tiltDownButton.setEnabled(true);

                zoomInButton.setEnabled(true);
                zoomOutButton.setEnabled(true);

                preset1Button.setEnabled(true);
                preset2Button.setEnabled(true);
                preset3Button.setEnabled(true);
            }
            else {
                powerButton.setText(R.string.meeting_eye_power_on_title);
                powerButton.setOnClickListener(powerOnListener);

                tiltLeftButton.setEnabled(false);
                tiltUpButton.setEnabled(false);
                tiltRightButton.setEnabled(false);
                tiltDownButton.setEnabled(false);

                zoomInButton.setEnabled(false);
                zoomOutButton.setEnabled(false);

                preset1Button.setEnabled(false);
                preset2Button.setEnabled(false);
                preset3Button.setEnabled(false);
            }
        }
        else if (topic.equals(getContext().getString(R.string.topic_prefix, "meeting_eye/status/preset"))) {
            int preset = message.getJSONObject("metrics").getInt("value");

            // Clear all preset background color filter
            preset1Button.getBackground().clearColorFilter();
            preset2Button.getBackground().clearColorFilter();
            preset3Button.getBackground().clearColorFilter();

            switch (preset) {
                case 1:
                    preset1Button.getBackground().setColorFilter(getContext().getColor(R.color.buttonHighLightColor), PorterDuff.Mode.MULTIPLY);
                    break;
                case 2:
                    preset2Button.getBackground().setColorFilter(getContext().getColor(R.color.buttonHighLightColor), PorterDuff.Mode.MULTIPLY);
                    break;
                case 3:
                    preset3Button.getBackground().setColorFilter(getContext().getColor(R.color.buttonHighLightColor), PorterDuff.Mode.MULTIPLY);
                    break;
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
