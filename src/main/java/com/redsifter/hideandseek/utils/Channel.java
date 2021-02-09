package com.redsifter.hideandseek.utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Channel {
    private final String name;
    private ArrayList<Player> list = new ArrayList<Player>();
    private ChatColor color;
    public Channel(String nm,ChatColor color) {
        name = nm;
        this.color = color;
    }

    public void addPlayer(Player pl) {
        if (!list.contains(pl)) {
            list.add(pl);
        }
    }

    public void remPlayer(Player pl) {
        if (list.contains(pl)) {
            list.remove(pl);
        }
    }

    public void flush(){
        list.clear();
    }

    public void broadcast(String msg){
        for(Player p : list){
            p.sendMessage(color + "[" + name + "]" + ChatColor.WHITE + msg);
        }
    }
}