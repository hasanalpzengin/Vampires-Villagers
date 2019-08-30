package com.hasanalpzengin.vampiresvillagers;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class VoteFragment extends Fragment {

    private View view;
    private ListView userList;
    private TextView countDown;
    private ArrayAdapter<String> adapter;
    private TimerRunnable timerThread;

    public VoteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        updateList();
        timerThread.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_vote, container, false);
        countDown = view.findViewById(R.id.countDown);
        userList = view.findViewById(R.id.userList);
        adapter = new ArrayAdapter<String>(getContext(), R.layout.list_white_text, R.id.text1);
        userList.setAdapter(adapter);
        userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                GameActivity.sendVote(adapter.getItem(i));
                Toast.makeText(getContext(), getString(R.string.voted), Toast.LENGTH_SHORT).show();
            }
        });
        timerThread = new TimerRunnable();
        return view;
    }

    private void updateList(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.clear();
                adapter.addAll(GameController.getPlayerList());
                adapter.notifyDataSetChanged();
            }
        });
    }

    class TimerRunnable extends Thread {
        int currentTime = (int) Math.floor(GameController.getRoundTime()/2);
        @Override
        public void run() {
            while(currentTime>=0){
                try {
                    int finalCurrentTime = currentTime;
                    getActivity().runOnUiThread(() -> countDown.setText(String.valueOf(finalCurrentTime)));
                    Thread.sleep(1000);
                    currentTime--;
                } catch (NullPointerException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ArrayList<String> voteResult = GameController.endVote();
            if (voteResult.size()>1){
                //start thread again
                this.interrupt();
                timerThread = new TimerRunnable();
                timerThread.start();
            }else{
                if (voteResult.get(0).equals(GameController.getNickname())){
                    GameController.dead();
                }else{
                    GameController.removePlayer(voteResult.get(0));
                    GameController.setGameStatus("round");
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timerThread.interrupt();
    }

}
