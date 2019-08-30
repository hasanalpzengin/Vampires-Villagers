package com.hasanalpzengin.vampiresvillagers.broker;

import android.util.Log;

import java.util.Properties;
import java.util.concurrent.Callable;

import io.moquette.BrokerConstants;
import io.moquette.broker.Server;

public class MQTTBroker implements Callable<Boolean> {

    private static final String TAG = "MQTTBrokerThread";

    private Server server;
    private Properties config;

    public MQTTBroker(Properties config) {
        this.config = config;
    }

    public Server getServer() {
        return server;
    }

    public void stopServer() {
        server.stopServer();
    }

    @Override
    public Boolean call() throws Exception {
        try {
            // use ServerInstance singleton to get the same instance of server
            server = ServerInstance.getServerInstance();
            server.startServer(config);
            Log.d(TAG, "MQTT Broker Started on "+server.getPort());
            return true;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            throw new Exception(e.getLocalizedMessage());
        }
    }
}
