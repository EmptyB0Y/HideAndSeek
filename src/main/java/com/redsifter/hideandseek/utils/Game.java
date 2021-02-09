package com.redsifter.hideandseek.utils;

import com.redsifter.hideandseek.HideAndSeek;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Game extends BukkitRunnable {
    public Team t1;
    public Team t2;
    public int nb;
    public int time;
    private HideAndSeek main;
    public boolean hasStarted = false;
    public Player owner;

    public Game(Team team1, Team team2,int n,Player p,HideAndSeek hs){
        t1 = team1;
        t2 = team2;
        nb = n;
        owner = p;
        this.main = hs;
    }

    public void run(){
        Bukkit.broadcastMessage(String.valueOf(time));
        if(time == 0){
            t1.flush();
            t2.flush();
            main.cancelGame(nb);
        }
        time--;
    }

    public boolean start(int timer){
        if((t1.players.size() - t2.players.size() <= 3 || t1.players.size() - t2.players.size() >= -3) && (!t1.players.isEmpty() && !t2.players.isEmpty())) {
            this.time = timer;
            this.runTaskTimer((Plugin) this.main, 0L, 20L);
            this.hasStarted = true;
            return true;
        }
        return false;
    }

    public boolean addPlayer(Player p,String t){
        if(t.equals("h")){
            if(!t1.players.contains(p) && ((t2.players.size() - t1.players.size()) <= 3 || (t2.players.size() - t1.players.size() >= -3))){
                t1.addPlayer(p);
                return true;
            }
        }
        else if(t.equals("s")){
            if(!t2.players.contains(p) && ((t2.players.size() - t1.players.size()) <= 3 || (t2.players.size() - t1.players.size() >= -3))){
                t2.addPlayer(p);
                return true;
            }
        }
        return false;
    }

    public boolean remPlayer(Player p){
        if(t1.players.contains(p)){
                t1.remPlayer(p.getName());
                return true;

        }
        else if(t2.players.contains(p)){
                t2.remPlayer(p.getName());
                return true;
            }
        return false;
    }
}
