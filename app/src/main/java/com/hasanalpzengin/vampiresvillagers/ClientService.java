package com.hasanalpzengin.vampiresvillagers;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.hivemq.client.internal.mqtt.MqttAsyncClient;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

public class ClientService extends Service{
    private Mqtt3AsyncClient mqttClient;
    private String serverUri;
    private Context context;
    private String nickname;
    public static final String VOTE_TOPIC = "/vv/vote";
    public static final String JOIN_TOPIC = "/vv/join";
    public static final String LEAVE_TOPIC = "/vv/leave";
    public static final String CONFIG_TOPIC= "/vv/config";
    public static final String KILL_TOPIC = "/vv/kill";
    public static final String PLAYER_LIST_TOPIC = "/vv/playerList";
    private boolean isHost;
    private final IBinder mBinder = new LocalBinder();

    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.serverUri = intent.getStringExtra("serverUri");
        this.nickname = intent.getStringExtra("nickname");
        this.isHost = intent.getBooleanExtra("isHost", false);
        doConnect();
        return START_STICKY;
    }

    private void doConnect() {
        //connect
        mqttClient = MqttClient.builder().useMqttVersion3().serverHost(serverUri).identifier(nickname).buildAsync();
        mqttClient.connect().whenComplete((connAck ,throwable)->{
            if (throwable == null){
                afterConnect();
            }
        });

    }

    private void afterConnect(){
        if (isHost) {
            mqttClient.subscribeWith().topicFilter(JOIN_TOPIC).qos(MqttQos.AT_MOST_ONCE).callback((message)->{
                GameController.addPlayer(new String(message.getPayloadAsBytes()));
                Log.d("Recieved Message: ",new String(message.getPayloadAsBytes()));
                String payload = "roundTime"+"@"+GameController.getRoundTime();
                //publish config
                mqttClient.publishWith().topic(CONFIG_TOPIC).payload(payload.getBytes()).qos(MqttQos.AT_MOST_ONCE).send();
                payload = "vampireCount"+"@"+GameController.getVampireCount();
                mqttClient.publishWith().topic(CONFIG_TOPIC).payload(payload.getBytes()).qos(MqttQos.AT_MOST_ONCE).send();
                mqttClient.publishWith().topic(PLAYER_LIST_TOPIC).payload(generatePlayerList().getBytes()).qos(MqttQos.AT_MOST_ONCE).send();
                GameActivity.updateLobby();
            }).send();
        }else{
            mqttClient.subscribeWith().topicFilter(PLAYER_LIST_TOPIC).qos(MqttQos.AT_MOST_ONCE).callback((message)->{
                String[] players = (new String(message.getPayloadAsBytes())).split("@");
                for (String player: players) {
                    GameController.addPlayer(player);
                }
                GameActivity.updateLobby();
            }).send();
        }
        GameController.addPlayer(nickname);
        //config message
        mqttClient.subscribeWith().topicFilter(CONFIG_TOPIC).qos(MqttQos.AT_MOST_ONCE).callback((message)->{
            String config = new String(message.getPayloadAsBytes());
            String[] parsedConfig = config.split("@");
            switch (parsedConfig[0]){
                case "roundTime": GameController.setRoundTime(Integer.parseInt(parsedConfig[1])); break;
                case "vampireCount": GameController.setVampireCount(Integer.parseInt(parsedConfig[1])); break;
                case "setVampire": {
                    if (parsedConfig[1].equals(GameController.getNickname())) {
                        GameController.setVampire(true);
                    }
                    break;
                }
                case "gameStatus":{
                    GameController.setGameStatus(new String(parsedConfig[1].getBytes()));
                    break;
                }
                default: Log.w("ClientService", "Topic not supported");
            }
        }).send();
        mqttClient.subscribeWith().topicFilter(VOTE_TOPIC).qos(MqttQos.AT_MOST_ONCE).callback((message)->{
            String voteMessage = new String(message.getPayloadAsBytes());
            String[] voteMessageParsed = voteMessage.split("@");
            GameController.vote(voteMessageParsed[0], voteMessageParsed[1]);
        }).send();
        mqttClient.subscribeWith().topicFilter(LEAVE_TOPIC).qos(MqttQos.AT_MOST_ONCE).callback((message)->{
            String leaveMessage = new String(message.getPayloadAsBytes());
            GameController.removePlayer(leaveMessage);
        }).send();
        mqttClient.subscribeWith().topicFilter(KILL_TOPIC).qos(MqttQos.AT_MOST_ONCE).callback((message)->{
            if ((new String(message.getPayloadAsBytes()).equals(nickname))){
                GameController.dead();
            }
        }).send();
        mqttClient.publishWith().topic(JOIN_TOPIC).payload(nickname.getBytes()).send();
    }

    public String generatePlayerList(){
        StringBuilder stringBuilder = new StringBuilder();
        for (String player : GameController.getPlayerList()){
            stringBuilder.append(player);
            stringBuilder.append("@");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        return stringBuilder.toString();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mqttClient.disconnect();
    }

    public void sendStartCommand(){
        mqttClient.publishWith().topic(CONFIG_TOPIC).qos(MqttQos.AT_MOST_ONCE).payload("gameStatus@round".getBytes()).send();
    }

    public void sendVote(String vote){
        String votePayload = GameController.getNickname()+"@"+vote;
        mqttClient.publishWith().topic(VOTE_TOPIC).qos(MqttQos.AT_MOST_ONCE).payload(votePayload.getBytes()).send();
    }

    public void setVampire(String payload){
        mqttClient.publishWith().topic(CONFIG_TOPIC).qos(MqttQos.AT_MOST_ONCE).payload(payload.getBytes()).send();
    }

    public class LocalBinder extends Binder {
        public ClientService getService() {
            return ClientService.this;
        }
    }

}
