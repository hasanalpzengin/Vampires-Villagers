package com.hasanalpzengin.vampiresvillagers;


import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class GameController {
    private static HashMap<String, String> playerVote = new HashMap<>();
    private static ArrayList<String> playerList = new ArrayList<String>();
    private static String nickname;
    public static int dayCount = 0;
    private static String gameStatus;
    private static int roundTime;
    private static boolean vampire;
    private static int vampireCount;

    public static boolean isVampire() {
        return vampire;
    }

    public static void setGameStatus(String gameStatus) {
        GameController.gameStatus = gameStatus;
        GameActivity.switchFragment();
    }

    public static void resetPlayers(){
        playerVote.clear();
        playerList.clear();
    }

    public static HashMap<String, String> getPlayerVote() {
        return playerVote;
    }

    public static String getGameStatus() {
        return gameStatus;
    }

    public static void setVampire(boolean vampire) {
        GameController.vampire = vampire;
    }

    public static String getNickname() {
        return nickname;
    }

    public static void setNickname(String nickname) {
        GameController.nickname = nickname;
    }

    public static void setVampireCount(int vampireCount) {
        GameController.vampireCount = vampireCount;
    }

    public static int getVampireCount() {
        return vampireCount;
    }

    public static int getRoundTime() {
        return roundTime;
    }

    public static void setRoundTime(int roundTime) {
        GameController.roundTime = roundTime;
    }

    public static void addPlayer(String nickname){
        if(!isPlayerExist(nickname)){
            playerList.add(nickname);
        }
    }

    public static void removePlayer(String nickname){
        if(isPlayerExist(nickname)){
            playerVote.remove(nickname);
            playerList.remove(nickname);
        }
    }

    public static void vote(String nickname, String vote){
        if (isPlayerExist(vote) && isPlayerExist(nickname)){
            playerVote.put(nickname, vote);
        }
    }

    public static ArrayList<String> getPlayerList() {
        return playerList;
    }

    public static boolean isPlayerExist(String nickname){
        return playerList.contains(nickname);
    }

    public static ArrayList<String> endVote(){
        AtomicInteger minVote = new AtomicInteger();
        ArrayList<String> result = new ArrayList<>();
        HashMap<String, String> votes = getPlayerVote();
        HashMap<String, Integer> resultMap = new HashMap<>();
        votes.forEach((K, V)->{
            if(resultMap.containsKey(V)){
                int value = resultMap.get(V);
                resultMap.put(V, value+1);
            }else{
                resultMap.put(V, 1);
            }
        });
        resultMap.forEach((K, V)->{
            if(minVote.get()<V){
                result.clear();
                minVote.set(V);
                result.add(K);
            }else if (minVote.get()==V){
                result.add(K);
            }
        });
        return result;
    }


    public static void dead() {
        setGameStatus("dead");
    }

    public static void defineClass() {
        Random rand = new Random();
        ArrayList<String> copyPlayerList = (ArrayList<String>) playerList.clone();
        ArrayList<String> vampires = new ArrayList<>();
        for (int i=0; i<getVampireCount(); i++){
            int randomIndex = rand.nextInt(copyPlayerList.size());
            vampires.add(copyPlayerList.get(randomIndex));
            copyPlayerList.remove(randomIndex);
        }

        for (String nickname : vampires) {
            GameActivity.setVampire(nickname);
        }
    }

    public static void resetVotes(){
        playerVote = new HashMap<>();
    }
}
