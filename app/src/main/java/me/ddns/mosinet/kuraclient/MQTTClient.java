package me.ddns.mosinet.kuraclient;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class MQTTClient {

    /*----- Fields -----*/


    // Context
    private Context applicationContext;

    // Paho MQTT Client
    private MqttAndroidClient mqttAndroidClient;

    // Broker credentials
    private String brokerUsername;
    private String brokerPassword;


    /*----- Constructors -----*/


    MQTTClient(Context context, String address, String username, String password){
        // Set context
        applicationContext = context;

        // Set connection credentials
        brokerUsername = username;
        brokerPassword = password;

        // Init client
        mqttAndroidClient = new MqttAndroidClient(context, context.getString(R.string.broker_uri, address), context.getString(R.string.client_id));
    }

    public void connect() throws MqttException {
        // Set connect options
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setUserName(brokerUsername);
        mqttConnectOptions.setPassword(brokerPassword.toCharArray());

        // Try to connect
        mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {

                DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                disconnectedBufferOptions.setBufferEnabled(true);
                disconnectedBufferOptions.setBufferSize(100);
                disconnectedBufferOptions.setPersistBuffer(false);
                disconnectedBufferOptions.setDeleteOldestMessages(false);
                mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.w("mqtt", "Failed to connect to: " + mqttAndroidClient.getServerURI() + exception.toString());
            }
        });
    }

    public void disconnect() throws MqttException {
        mqttAndroidClient.disconnect();
    }

    public void setCallback(MqttCallbackExtended callback) {
        mqttAndroidClient.setCallback(callback);
    }

    public void sendMessageToDevice(String device, Map<String, Object> metrics) {
        try {
            // Message
            JSONObject messageBody = new JSONObject();

            // Timestamp
            messageBody.put("sentOn", System.currentTimeMillis());

            // Init metrics
            JSONObject messageMetrics = new JSONObject();

            // Add metrics
            for (Map.Entry<String, Object> metric : metrics.entrySet()) {
                messageMetrics.put(metric.getKey(), metric.getValue());
            }

            // Add metrics to the message
            messageBody.put("metrics", messageMetrics);

            // Create new MQTT message
            MqttMessage message = new MqttMessage();
            message.setPayload(messageBody.toString().getBytes());
            message.setQos(2);
            message.setRetained(false);

            // Publish
            try {
                mqttAndroidClient.publish(applicationContext.getString(R.string.control_topic, device), message);
            }
            catch (MqttException e) {
                Log.i("MqttException", "Exception during sending 'power off' command: " + e.getMessage());
            }
        } catch (JSONException e) {
            Log.i(this.getClass().getName(), "Exception during json serialization: " + e.getMessage());
        }
    }

    public void subscribeToDeviceStatuses(IMqttActionListener listener) throws MqttException {
        mqttAndroidClient.subscribe(applicationContext.getString(R.string.status_topic), 1, null, listener);
    }
}
