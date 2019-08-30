package com.hasanalpzengin.vampiresvillagers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceControl;
import android.widget.FrameLayout;

import com.hasanalpzengin.vampiresvillagers.broker.MQTTService;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import io.moquette.BrokerConstants;

public class GameActivity extends AppCompatActivity {
    private String serverIP;
    private SharedPreferences sharedPreferences;
    private static FrameLayout fragmentLayout;
    private static FragmentTransaction transaction;
    private static ClientService cService;
    private boolean mBound = false;
    private boolean isHost = false;
    private MQTTService mService;
    private static LobbyFragment lobbyFragment = null;
    private static RoundFragment roundFragment = null;
    private static VoteFragment voteFragment = null;
    private static DeadFragment deadFragment = null;
    private static GameActivity activity;

    @Override
    protected void onStart() {
        super.onStart();
        org.apache.log4j.BasicConfigurator.configure();
        //this.bindService(new Intent(this, MQTTService.class), mConnection, BIND_IMPORTANT);
        //this.bindService(new Intent(this, ClientService.class), cConnection, BIND_IMPORTANT);
    }

    private ServiceConnection cConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            cService = ((ClientService.LocalBinder) service).getService();
            // MainActivity.this.updateStartedStatus();
        }

        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = ((MQTTService.LocalBinder) service).getService();
            mBound = ((MQTTService.LocalBinder) service).getServerStatus();
            // MainActivity.this.updateStartedStatus();
        }

        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(cConnection);
        cService.stopSelf();
        if (isHost) {
            unbindService(mConnection);
            mService.stopSelf();
        }
    }

    private Properties defaultConfig() {
        Properties props = new Properties();
        props.setProperty(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME, this.getExternalFilesDir(null).getAbsolutePath() + File.separator + BrokerConstants.DEFAULT_MOQUETTE_STORE_H2_DB_FILENAME);
        props.setProperty(BrokerConstants.PORT_PROPERTY_NAME, "1883");
        props.setProperty(BrokerConstants.NEED_CLIENT_AUTH, "false");
        props.setProperty(BrokerConstants.HOST_PROPERTY_NAME, serverIP);
        props.setProperty(BrokerConstants.WEB_SOCKET_PORT_PROPERTY_NAME, String.valueOf(BrokerConstants.WEBSOCKET_PORT));
        return props;
    }

    public void startBrokerService(){
        if (mBound && mService != null) {
            Log.i("MainActivity", "Service already running");
            return;
        }

        GameController.resetPlayers();

        Intent serviceIntent = new Intent(this, MQTTService.class);

        Bundle bundle = new Bundle();
        bundle.putSerializable("config", defaultConfig());
        serviceIntent.putExtras(bundle);
        startService(serviceIntent);
        this.bindService(serviceIntent, mConnection, BIND_IMPORTANT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        init();
    }

    private void init() {
        //server
        serverIP = getIntent().getStringExtra("serverIP");
        sharedPreferences = getSharedPreferences("game", MODE_PRIVATE);
        //fragment
        fragmentLayout = findViewById(R.id.fragmentLayout);
        transaction = getSupportFragmentManager().beginTransaction();
        isHost = getIntent().getBooleanExtra("isHost", false);
        //fragments
        lobbyFragment = new LobbyFragment(serverIP, isHost);
        voteFragment = new VoteFragment();
        roundFragment = new RoundFragment();
        deadFragment = new DeadFragment();

        transaction.add(lobbyFragment, "lobbyFragment");
        transaction.add(voteFragment, "voteFragment");
        transaction.add(roundFragment, "roundFragment");
        transaction.addToBackStack(null);

        activity = this;

        if (isHost){
            startBrokerService();
        }

        startClientService();
        //open first fragment
        GameController.setNickname(sharedPreferences.getString("nickname", ""));
        GameController.setGameStatus("lobby");
    }

    private void startClientService() {
        Intent serviceIntent = new Intent(this, ClientService.class);
        Bundle bundle = new Bundle();
        bundle.putString("serverUri", serverIP);
        bundle.putString("nickname", sharedPreferences.getString("nickname", ""));
        bundle.putBoolean("isHost", isHost);
        serviceIntent.putExtras(bundle);
        startService(serviceIntent);
        this.bindService(serviceIntent, cConnection, BIND_IMPORTANT);
    }

    public static void switchFragment(){
        transaction = activity.getSupportFragmentManager().beginTransaction();
        switch (GameController.getGameStatus()){
            case "lobby":{
                transaction.replace(fragmentLayout.getId(), lobbyFragment);
                transaction.commitAllowingStateLoss();
                break;
            }
            case "round": {
                transaction.replace(fragmentLayout.getId(), roundFragment);
                transaction.commitAllowingStateLoss();
                break;
            }
            case "vote": {
                transaction.replace(fragmentLayout.getId(), voteFragment);
                transaction.commitAllowingStateLoss();
                break;
            }
            case "dead": {
                cService.stopSelf();
                transaction.replace(fragmentLayout.getId(), deadFragment);
                transaction.commitAllowingStateLoss();
                break;
            }
        }
    }

    public static void updateLobby(){
        if(GameActivity.lobbyFragment!=null) {
            GameActivity.lobbyFragment.updatePlayerList();
        }
    }

    public static void sendStartCommand(){
        cService.sendStartCommand();
    }

    public static void setVampire(String nickname){
        String payload = "setVampire"+"@"+nickname;
        cService.setVampire(payload);
    }

    public static void sendVote(String vote){
        cService.sendVote(vote);
    }
}
