package com.hasanalpzengin.vampiresvillagers;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.LocalOnlyHotspotCallback;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.hasanalpzengin.vampiresvillagers.broker.MQTTService;
import com.hasanalpzengin.vampiresvillagers.broker.ServerInstance;
import com.shawnlin.numberpicker.NumberPicker;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Properties;

import io.moquette.BrokerConstants;

public class CreateGameActivity extends AppCompatActivity {

    private Button createButton;
    private TextInputLayout nickLayout;
    private NumberPicker roundTimePicker, vampireCountPicker;
    private WifiManager wifiManager;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_game);
        init();
        initListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void init() {
        preferences = getSharedPreferences("game",MODE_PRIVATE);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        createButton = findViewById(R.id.createButton);
        nickLayout = findViewById(R.id.nickText);
        roundTimePicker = findViewById(R.id.roundTimePicker);
        vampireCountPicker = findViewById(R.id.vampireCountPicker);

        //set nick if exist
        if(preferences.getString("nickname", null)!=null){
            nickLayout.getEditText().setText(preferences.getString("nickname", "Empty"));
        }
    }

    private void initListeners() {
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGame();
            }
        });

        nickLayout.getEditText().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                nickLayout.setErrorEnabled(false);
                return false;
            }
        });
    }

    private String getIpAddress(){
        int ipAddressInt = wifiManager.getConnectionInfo().getIpAddress();
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddressInt = Integer.reverseBytes(ipAddressInt);
        }
        byte[] ipAddress = BigInteger.valueOf(ipAddressInt).toByteArray();
        InetAddress address = null;
        try {
            address = InetAddress.getByAddress(ipAddress);
            return address.getHostAddress();
        } catch (UnknownHostException e) {
            Log.e("CreateGameActivity", "Ip Address Parse Error : ", e);
            return null;
        }
    }

    private void startGame(){
        String nick = nickLayout.getEditText().getText().toString();
        if(nick==null || nick.length()==0){
            nickLayout.setError(getText(R.string.error_no_nick));
            nickLayout.setErrorEnabled(true);
            return;
        }

        int roundTime = roundTimePicker.getValue();
        int vampireCount = vampireCountPicker.getValue();

        if(!checkIsWifiConnected()){
            hotspotOperation();
            return;
        }
        //save nickname
        SharedPreferences.Editor preferenceEditor = preferences.edit();
        preferenceEditor.putString("nickname", nick);
        preferenceEditor.putInt("roundTime", roundTime);
        preferenceEditor.putInt("vampireCount", vampireCount);
        preferenceEditor.commit();

        GameController.setRoundTime(roundTime);
        GameController.setVampireCount(vampireCount);

        Intent intent = new Intent(getApplicationContext(), GameActivity.class);
        String ip = getIpAddress();
        if (ip==null){
            Toast.makeText(this, getString(R.string.error_valid_ip), Toast.LENGTH_SHORT).show();
            return;
        }
        intent.putExtra("serverIP", ip);
        intent.putExtra("isHost", true);
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createHotspot(){
        wifiManager.startLocalOnlyHotspot(new LocalOnlyHotspotCallback(){
            @Override
            public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                super.onStarted(reservation);
                Log.d("CreateGameActivity", "Wifi Hotspot Created");
            }

            @Override
            public void onStopped() {
                super.onStopped();
                Log.d("CreateGameActivity", "Wifi Hotspot Stopped");
            }

            @Override
            public void onFailed(int reason) {
                super.onFailed(reason);
                Log.d("CreateGameActivity", "Wifi Hotspot Failed: "+reason);
            }
        }, new Handler());
    }

    private void hotspotOperation(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createHotspot();
        }else{
            Log.d("CreateGameActivity", "Hotspot operation should be done manually");
            Toast.makeText(this, "You must create hotspot or connect to a wifi before creating game", Toast.LENGTH_SHORT).show();
            startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), 0);
        }
    }

    private boolean checkIsWifiConnected(){
        return wifiManager.getConnectionInfo().getSSID()!=null;
    }
}
