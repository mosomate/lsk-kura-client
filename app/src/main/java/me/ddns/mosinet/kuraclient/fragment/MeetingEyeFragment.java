package me.ddns.mosinet.kuraclient.fragment;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
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
        tiltLeftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch(motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        sendSingleParamCommand("tilt", "left");
                        return true;
                    case MotionEvent.ACTION_UP:
                        sendSingleParamCommand("tilt", "stop");
                        return true;
                }
                return false;
            }
        });

        tiltUpButton = view.findViewById(R.id.tiltUpButton);
        tiltUpButton.setEnabled(false);
        tiltUpButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch(motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        sendSingleParamCommand("tilt", "up");
                        return true;
                    case MotionEvent.ACTION_UP:
                        sendSingleParamCommand("tilt", "stop");
                        return true;
                }
                return false;
            }
        });

        tiltRightButton = view.findViewById(R.id.tiltRightButton);
        tiltRightButton.setEnabled(false);
        tiltRightButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch(motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        sendSingleParamCommand("tilt", "right");
                        return true;
                    case MotionEvent.ACTION_UP:
                        sendSingleParamCommand("tilt", "stop");
                        return true;
                }
                return false;
            }
        });

        tiltDownButton = view.findViewById(R.id.tiltDownButton);
        tiltDownButton.setEnabled(false);
        tiltDownButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch(motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        sendSingleParamCommand("tilt", "down");
                        return true;
                    case MotionEvent.ACTION_UP:
                        sendSingleParamCommand("tilt", "stop");
                        return true;
                }
                return false;
            }
        });

        // Zoom
        zoomInButton = view.findViewById(R.id.zoomInButton);
        zoomInButton.setEnabled(false);
        zoomInButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch(motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        sendSingleParamCommand("zoom", "in");
                        return true;
                    case MotionEvent.ACTION_UP:
                        sendSingleParamCommand("zoom", "stop");
                        return true;
                }
                return false;
            }
        });

        zoomOutButton = view.findViewById(R.id.zoomOutButton);
        zoomOutButton.setEnabled(false);
        zoomOutButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch(motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        sendSingleParamCommand("zoom", "out");
                        return true;
                    case MotionEvent.ACTION_UP:
                        sendSingleParamCommand("zoom", "stop");
                        return true;
                }
                return false;
            }
        });

        // Presets
        preset1Button = view.findViewById(R.id.preset1Button);
        preset1Button.setEnabled(false);
        preset1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSingleParamCommand("preset", 1);
            }
        });

        preset2Button = view.findViewById(R.id.preset2Button);
        preset2Button.setEnabled(false);
        preset2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSingleParamCommand("preset", 2);
            }
        });

        preset3Button = view.findViewById(R.id.preset3Button);
        preset3Button.setEnabled(false);
        preset3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSingleParamCommand("preset", 3);
            }
        });

        // Create callback for power on
        powerOnListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSingleParamCommand("power", "on");
            }
        };

        // Create callback for power off
        powerOffListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSingleParamCommand("power", "off");
            }
        };

        powerButton = view.findViewById(R.id.powerButton);
        powerButton.setOnClickListener(powerOnListener);

        // Update UI
        updateUI();
    }

    public void messageArrived(String topic, MqttMessage mqttMessage) {
        Log.w("messageArrived",mqttMessage.toString());

        long currentSystemTime = System.currentTimeMillis();

        Log.i("json", topic);

        try {
            Map<String, Object> metrics = getMetricsFromMessage(mqttMessage);

            if (topic.equals(getContext().getString(R.string.topic_prefix, "meeting_eye/status/power"))) {
                //Log.i("json", powerStatus);
                String powerStatus = (String)metrics.get("value");
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
                } else {
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
            } else if (topic.equals(getContext().getString(R.string.topic_prefix, "meeting_eye/status/preset"))) {
                int preset = (int)metrics.get("value");

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
        catch (JSONException e) {
            Log.i("json", "Error occured while parsin received message: " + e.getMessage());
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

    private void sendSingleParamCommand(String command, Object value) {
        // Create map for metrics
        Map<String, Object> metrics = new HashMap<>();

        // Add power on command
        metrics.put("command", command);
        metrics.put("param", value);

        ((MainActivity) getActivity()).getMqttClient().sendMessageToDevice("meeting_eye", metrics);
    }

    private Map<String,Object> getMetricsFromMessage(MqttMessage mqttMessage) throws JSONException {
        JSONObject message = new JSONObject(mqttMessage.toString());

        JSONArray jsonMetrics = message.getJSONArray("metrics");

        HashMap<String, Object> metrics = new HashMap<String, Object>();
        for (int i = 0; i < jsonMetrics.length(); i++) {
            JSONObject j = jsonMetrics.getJSONObject(i);
            Iterator it = j.keys();
            while (it.hasNext()) {
                String n = (String)it.next();
                metrics.put(n, j.get(n));
            }
        }

        return metrics;
    }
}
