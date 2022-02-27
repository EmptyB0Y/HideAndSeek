package com.redsifter.hideandseek.utils;

import com.redsifter.hideandseek.HideAndSeek;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.*;

import java.util.ArrayList;


public class CustomTeam {
    public ArrayList<Player> players = new ArrayList<Player>();
    private int playernb = 0;
    private int nb;
    public Channel ch;
    public String name;
    public boolean full = false;

    public CustomTeam(int number, String nm){
        nb = number;
        name = nm;
        ChatColor color = ChatColor.GOLD;
        if(name.equals("HIDERS")){
            color = ChatColor.DARK_GREEN;
        }
        else if(name.equals("SEEKERS")){
            color = ChatColor.RED;
        }
        ch = new Channel(nm,color);
    }

    public void setPlayers(ArrayList<Player> lst){
        int iterator = 0;
        if(lst.size() < HideAndSeek.MAXPLAYERS){
            iterator = lst.size();
        }
        else{
            iterator = HideAndSeek.MAXPLAYERS;
        }
        for (int i = 0;i < iterator;i++){
            players.add(lst.get(i));
            playernb++;
            ch.addPlayer(lst.get(i));
        }
    }

    public boolean addPlayer(Player pl){
        if(!full) {
            players.add(pl);
            playernb++;
            ch.addPlayer(pl);
            if (playernb == HideAndSeek.MAXPLAYERS) {
                full = true;
            }
            return true;
        }
        return false;
    }

    public void remPlayer(String pl){
        players.remove(Bukkit.getPlayerExact(pl));
        playernb--;
        ch.remPlayer(Bukkit.getPlayerExact(pl));
    }

    public void flush(){
        players.clear();
        playernb = 0;
        ch.flush();
    }

    public void chat(String msg){
        ch.broadcast(msg);
    }
}