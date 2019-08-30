package com.hasanalpzengin.vampiresvillagers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.InetAddresses;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.text.Layout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class JoinGameActivity extends AppCompatActivity {

    private TextInputLayout serverInput, nickInput;
    private Button connectButton;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);
        init();
        initListeners();
    }


    private void init(){
        sharedPreferences = getSharedPreferences("game",MODE_PRIVATE);
        serverInput = findViewById(R.id.ipAddress);
        connectButton = findViewById(R.id.connectButton);
        nickInput = findViewById(R.id.nickText);

        if(sharedPreferences.getString("nickname", null)!=null){
            nickInput.getEditText().setText(sharedPreferences.getString("nickname", "Empty"));
        }
    }

    private void initListeners() {
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nickInput.getEditText().getText().toString().length()<=0){
                    nickInput.setError(getString(R.string.error_no_nick));
                    nickInput.setErrorEnabled(true);
                    return;
                }
                if(checkConnection()){
                    try {
                        Thread.sleep(3000);
                        sharedPreferences.edit().putString("nickname", nickInput.getEditText().getText().toString()).commit();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Intent gameScreen = new Intent(getApplicationContext(), GameActivity.class);
                    gameScreen.putExtra("serverIP", serverInput.getEditText().getText().toString());
                    gameScreen.putExtra("isHost", false);
                    startActivity(gameScreen);
                }
            }
        });

        serverInput.getEditText().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                serverInput.setErrorEnabled(false);
                return false;
            }
        });

        nickInput.getEditText().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                nickInput.setErrorEnabled(false);
                return false;
            }
        });

    }

    private boolean checkConnection() {
        String serverIP = serverInput.getEditText().getText().toString();
        if(serverIP==null | serverIP.length()<=0){
            serverInput.setError(getString(R.string.error_valid_ip));
            serverInput.setErrorEnabled(true);
            return false;
        }
        try {
            Object address = InetAddress.getByName(serverIP);
            return address instanceof Inet4Address;
        } catch (IOException | NullPointerException | NetworkOnMainThreadException e) {
            serverInput.setError(getString(R.string.error_server));
            serverInput.setErrorEnabled(true);
            Log.e("JoinGameActivity", "Connection Error: ", e);
        }
        return false;
    }
}
