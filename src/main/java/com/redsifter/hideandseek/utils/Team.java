package com.redsifter.hideandseek.utils;

import com.redsifter.hideandseek.HideAndSeek;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;

public class Team {
    public ArrayList<Player> players = new ArrayList<Player>();
    private int playernb = 0;
    private int nb;
    public Channel ch;
    public String name;
    public boolean full = false;

    public Team(int number,String nm){
        nb = number;
        name = nm;
        ChatColor color = ChatColor.GOLD;
        if(name.equals("hiders")){
            color = ChatColor.DARK_GREEN;
        }
        else if(name.equals("seekers")){
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

    public void setPlayerNamesVisibility(boolean z){
        for(Player p : players){
            p.setCustomNameVisible(z);
        }
    }

    public void chat(String msg){
        ch.broadcast(msg);
    }
}