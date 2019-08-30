package com.hasanalpzengin.vampiresvillagers;


import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;


/**
 * A simple {@link Fragment} subclass.
 */
public class RoundFragment extends Fragment {

    private View view;
    private ImageView classImage;
    private TextView classText;
    private TextView timeText;
    private TextView dayText;
    private TimerRunnable timer;

    public RoundFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        //start timer thread
        timer = new TimerRunnable();
        timer.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_round, container, false);
        classText = view.findViewById(R.id.classText);
        String className = GameController.isVampire() ? "Vampire" : "Villager";
        classText.setText(view.getContext().getString(R.string.you_are)+" "+className);
        classImage = view.findViewById(R.id.classImage);
        dayText = view.findViewById(R.id.dayText);
        GameController.dayCount++;
        dayText.setText(getString(R.string.day)+":"+GameController.dayCount);
        Drawable image = GameController.isVampire() ? view.getContext().getDrawable(R.drawable.vampire) : view.getContext().getDrawable(R.drawable.villager);
        classImage.setImageDrawable(image);
        timeText = view.findViewById(R.id.countDown);

        return view;
    }

    class TimerRunnable extends Thread {
        @Override
        public void run() {
            int currentTime = GameController.getRoundTime();
            while(currentTime>=0){
                try {
                    int finalCurrentTime = currentTime;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timeText.setText(String.valueOf(finalCurrentTime));
                        }
                    });
                    Thread.sleep(1000);
                    currentTime--;
                } catch (NullPointerException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            GameController.setGameStatus("vote");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.interrupt();
    }
}
