package me.ddns.mosinet.kuraclient.fragment;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import me.ddns.mosinet.kuraclient.R;

public class MeetingEyeFragment extends DeviceFragment {

    /*----- Fields -----*/


    /* Views */

    // Power
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


    /*----- Fragment lifecycle callbacks -----*/


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meeting_eye, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /* Init power views */

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
        // The default listener is the power on
        powerButton.setOnClickListener(powerOnListener);


        /* Init tilt views */

        // Left
        tiltLeftButton = view.findViewById(R.id.tiltLeftButton);
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
        tiltLeftButton.setEnabled(false);

        // Up
        tiltUpButton = view.findViewById(R.id.tiltUpButton);
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
        tiltUpButton.setEnabled(false);

        // Right
        tiltRightButton = view.findViewById(R.id.tiltRightButton);
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
        tiltRightButton.setEnabled(false);

        // Down
        tiltDownButton = view.findViewById(R.id.tiltDownButton);
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
        tiltDownButton.setEnabled(false);


        /* Init zoom views */

        // In
        zoomInButton = view.findViewById(R.id.zoomInButton);
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
        zoomInButton.setEnabled(false);

        // Out
        zoomOutButton = view.findViewById(R.id.zoomOutButton);
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
        zoomOutButton.setEnabled(false);


        /* Init preset views */

        preset1Button = view.findViewById(R.id.preset1Button);
        preset1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSingleParamCommand("preset", 1);
            }
        });
        preset1Button.setEnabled(false);

        preset2Button = view.findViewById(R.id.preset2Button);
        preset2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSingleParamCommand("preset", 2);
            }
        });
        preset2Button.setEnabled(false);

        preset3Button = view.findViewById(R.id.preset3Button);
        preset3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSingleParamCommand("preset", 3);
            }
        });
        preset3Button.setEnabled(false);


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
        if (topic.equals(getContext().getString(R.string.topic_prefix, "meeting_eye/status/power"))) {
            // Get power status
            boolean isTurnedOn = message.getJSONObject("metrics").getString("value").equals("on");

            // Set label for power on/off button
            powerButton.setText( (isTurnedOn)?(R.string.meeting_eye_power_off_title):(R.string.meeting_eye_power_on_title) );

            // Set click listener
            powerButton.setOnClickListener( (isTurnedOn)?(powerOffListener):(powerOnListener) );

            // Enable / disable views
            tiltLeftButton.setEnabled(isTurnedOn);
            tiltUpButton.setEnabled(isTurnedOn);
            tiltRightButton.setEnabled(isTurnedOn);
            tiltDownButton.setEnabled(isTurnedOn);

            zoomInButton.setEnabled(isTurnedOn);
            zoomOutButton.setEnabled(isTurnedOn);

            preset1Button.setEnabled(isTurnedOn);
            preset2Button.setEnabled(isTurnedOn);
            preset3Button.setEnabled(isTurnedOn);
        }
        else if (topic.equals(getContext().getString(R.string.topic_prefix, "meeting_eye/status/preset"))) {
            int preset = message.getJSONObject("metrics").getInt("value");

            // Clear all preset background color filter
            preset1Button.getBackground().clearColorFilter();
            preset2Button.getBackground().clearColorFilter();
            preset3Button.getBackground().clearColorFilter();

            // Set color filter for the right preset button
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

    @Override
    protected void updateUI() {
        if (powerButton != null) {
            powerButton.setEnabled(isUiEnabled());
        }
    }

    @Override
    protected String getMqttDeviceId() {
        return "meeting_eye";
    }
}
