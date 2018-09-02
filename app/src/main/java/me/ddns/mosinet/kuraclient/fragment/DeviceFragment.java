package me.ddns.mosinet.kuraclient.fragment;

import android.support.v4.app.Fragment;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.HashMap;
import java.util.Map;

import me.ddns.mosinet.kuraclient.MainActivity;

public abstract class DeviceFragment extends Fragment {

    /* ----- Fields ----- */


    // bool for UI enable/disable
    private boolean uiEnabled = false;


    /* ----- Functions ----- */


    // Updates the UI views
    abstract protected void updateUI();

    // Enable/disable UI
    public void setUiEnabled(boolean uiEnabled) {
        this.uiEnabled = uiEnabled;
        updateUI();
    }

    // Must return the device ID, like "meeting_display"
    abstract protected String getMqttDeviceId();

    // Sends a command with single parameter
    protected void sendSingleParamCommand(String command, Object value) {
        // Create map for metrics
        Map<String, Object> metrics = new HashMap<>();

        // Add command
        metrics.put("command", command);
        metrics.put("param", value);

        ((MainActivity) getActivity()).getMqttClient().sendMessageToDevice(getMqttDeviceId(), metrics);
    }

    abstract public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception;


    /* ----- Getters / setters ----- */


    public boolean isUiEnabled() {
        return uiEnabled;
    }
}
