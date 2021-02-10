package com.redsifter.hideandseek.utils;

import com.redsifter.hideandseek.HideAndSeek;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class Game extends BukkitRunnable {
    public Team t1;
    public Team t2;
    public int nb;
    public int time;
    public int timeset;
    private HideAndSeek main;
    public boolean hasStarted = false;
    public Player owner;
    Location zone;
    public boolean full = false;
    private HashMap<Player,Location> limit = new HashMap<Player,Location>();

    public Game(Team team1, Team team2,int n,Player p,HideAndSeek hs){
        t1 = team1;
        t2 = team2;
        nb = n;
        owner = p;
        zone = p.getLocation();
        this.main = hs;
    }

    public void run(){
        Bukkit.broadcastMessage(String.valueOf(time));
        pushBack(t1);
        pushBack(t2);
        if(time == timeset/2){
            announcement(ChatColor.GOLD + "THE TIMER IS HALFWAY DONE !");
        }
        if(time == (timeset*0.1)){
            announcement(ChatColor.RED + "THE TIMER IS ALMOST DONE !");

        }
        if(!t1.players.isEmpty() && t2.players.isEmpty()){
            hidersVictory();
        }
        else if(t1.players.isEmpty() && !t2.players.isEmpty()){
            seekersVictory();
        }
        if(time == 0 && !t1.players.isEmpty()){
            hidersVictory();
        }
        time--;
    }

    public boolean start(int timer){
        if((t1.players.size() - t2.players.size() <= 3 || t1.players.size() - t2.players.size() >= -3) && (!t1.players.isEmpty() && !t2.players.isEmpty())) {
            this.time = timer;
            this.timeset = timer;
            this.runTaskTimer((Plugin) this.main, 0L, 20L);
            this.hasStarted = true;
            return true;
        }
        return false;
    }

    public boolean addPlayer(Player p,String t){
        if(t1.full && t2.full){
            full = true;
        }
        if(t.equals("h")){
            if(!t1.players.contains(p) && ((t2.players.size() - t1.players.size()) <= 3 || (t2.players.size() - t1.players.size() >= -3)) || full){
                if(!t1.full) {
                    t1.addPlayer(p);
                    return true;
                }
                else{
                    return false;
                }
            }
        }
        else if(t.equals("s")){
            if(!t2.players.contains(p) && ((t2.players.size() - t1.players.size()) <= 3 || (t2.players.size() - t1.players.size() >= -3)) || full){
                if(!t2.full) {
                    t2.addPlayer(p);
                    return true;
                }
                else{
                    return false;
                }
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

    public void announcement(String msg){
        t1.chat(msg);
        t2.chat(msg);
    }

    public void hidersVictory(){
        announcement(ChatColor.GOLD + "The hiders won ! Congratulations :\n");
        for(Player p : t1.players){
            announcement(ChatColor.DARK_GREEN + p.getName() + "\n");
        }
        main.cancelGame(nb);
    }

    public void seekersVictory(){
        announcement(ChatColor.GOLD + "The seekers won ! Congratulations :\n");
        for(Player p : t2.players){
            announcement(ChatColor.RED + p.getName() + "\n");
        }
        main.cancelGame(nb);
    }
    public void pushBack(Team t){
        for(Player p : t.players){
            if(p.getLocation().distance(zone) >= (t1.players.size() + t2.players.size())*20){
                Location l = new Location(limit.get(p).getWorld(),limit.get(p).getX(),limit.get(p).getY(),limit.get(p).getZ(),p.getLocation().getYaw(),p.getLocation().getPitch());
                p.teleport(l);
                p.sendMessage(ChatColor.RED + "You went too far, stay in the game zone.\n");
            }
            else{
                if(!limit.containsKey(p)) {
                    limit.put(p,p.getLocation());
                }
                else{
                    if(p.getLocation().distance(zone) < ((t1.players.size() + t2.players.size())*20)-10)
                    limit.replace(p,p.getLocation());
                }
            }
        }
    }
}
