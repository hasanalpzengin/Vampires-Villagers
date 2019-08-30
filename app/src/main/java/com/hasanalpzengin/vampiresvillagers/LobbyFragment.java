package com.hasanalpzengin.vampiresvillagers;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class LobbyFragment extends Fragment {
    private TextView ipText;
    private ListView playerList;
    private Button startButton;
    private View view;
    private String ip;
    private ArrayAdapter<String> playerAdapter;
    private boolean isCreator;

    public LobbyFragment(String ip, boolean isCreator){
        this.ip = ip;
        this.isCreator=isCreator;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_lobby, container, false);
        init();
        return view;

    }

    private void init() {
        ipText = view.findViewById(R.id.ipText);
        ipText.setText(ip);
        playerList = view.findViewById(R.id.playerList);
        playerAdapter = new ArrayAdapter<String>(getContext(), R.layout.list_white_text, R.id.text1);
        playerList.setAdapter(playerAdapter);
        startButton = view.findViewById(R.id.startGameButton);
        startButton.setEnabled(isCreator);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GameController.getVampireCount()*2<=GameController.getPlayerList().size()){
                    if(isCreator){
                        GameController.defineClass();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    GameActivity.sendStartCommand();
                    GameController.setGameStatus("round");
                }else{
                    Toast.makeText(getContext(), "Player Amount >= Vampire Amount x 2", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void updatePlayerList(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playerAdapter.clear();
                playerAdapter.addAll(GameController.getPlayerList());
                playerAdapter.notifyDataSetChanged();
            }
        });
    }

}
