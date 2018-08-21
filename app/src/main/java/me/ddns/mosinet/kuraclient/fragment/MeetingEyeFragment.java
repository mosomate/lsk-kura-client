package me.ddns.mosinet.kuraclient.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import me.ddns.mosinet.kuraclient.R;

public class MeetingEyeFragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meeting_eye, container, false);
    }

    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
    }
}
