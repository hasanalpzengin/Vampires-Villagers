package com.hasanalpzengin.vampiresvillagers;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button createButton, joinButton, howtoButton, creditButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initListeners();
    }

    private void init(){
        createButton = findViewById(R.id.createButton);
        joinButton = findViewById(R.id.joinButton);
        howtoButton = findViewById(R.id.howtoButton);
        creditButton = findViewById(R.id.creditButton);
    }

    private void initListeners(){
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createGameActivity = new Intent(getApplicationContext(), CreateGameActivity.class);
                startActivity(createGameActivity);
            }
        });

        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createGameActivity = new Intent(getApplicationContext(), JoinGameActivity.class);
                startActivity(createGameActivity);
            }
        });

        howtoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), HowToActivity.class);
                startActivity(intent);
            }
        });

        creditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.hasanalpzengin.com"));
                startActivity(browserIntent);
            }
        });
    }
}
