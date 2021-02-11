package com.redsifter.hideandseek.utils;


import com.redsifter.hideandseek.HideAndSeek;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.*;

public class Game extends BukkitRunnable {
    public Team t1;
    public Team t2;
    public int nb;
    public int time;
    public int timeset;
    private HideAndSeek main;
    public boolean hasStarted = false;
    public Player owner;
    public Location zone;
    public boolean full = false;
    private HashMap<Player,Location> limit = new HashMap<Player,Location>();
    public HashMap<ArmorStand,Boolean> chests = new HashMap<ArmorStand,Boolean>();
    public int SIZE;
    //SCOREBOARD
    private ScoreboardManager manager = Bukkit.getScoreboardManager();
    private Scoreboard board = manager.getMainScoreboard();
    private Objective timer = board.registerNewObjective("timer"+nb, "test", "TIMER");

    public Game(Team team1, Team team2,int n,Player p,HideAndSeek hs){
        t1 = team1;
        t2 = team2;
        nb = n;
        owner = p;
        zone = p.getLocation();
        this.main = hs;
        SIZE = ((t1.players.size() + t2.players.size())*30);
    }

    public void run(){
        updateScoreBoard();
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
        else if(t1.players.isEmpty() && t2.players.isEmpty() && time > 0){
            main.cancelGame(nb);
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
            //setMysteryChests(true);
            for(Player p : t1.players){
                setScoreBoard(p);
            }
            for(Player p : t2.players){
                setScoreBoard(p);
            }
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
        //setMysteryChests(false);
        main.cancelGame(nb);
    }

    public void seekersVictory(){
        announcement(ChatColor.GOLD + "The seekers won ! Congratulations :\n");
        for(Player p : t2.players){
            announcement(ChatColor.RED + p.getName() + "\n");
        }
        //setMysteryChests(false);
        main.cancelGame(nb);
    }
    public void pushBack(Team t){
        for(Player p : t.players){
            if(p.getLocation().distance(zone) >= SIZE){
                Location l = new Location(limit.get(p).getWorld(),limit.get(p).getX(),limit.get(p).getY(),limit.get(p).getZ(),p.getLocation().getYaw(),p.getLocation().getPitch());
                p.teleport(l);
                p.sendMessage(ChatColor.RED + "You went too far, stay in the game zone.\n");
            }
            else{
                if(!limit.containsKey(p)) {
                    limit.put(p,p.getLocation());
                }
                else{
                    if(p.getLocation().distance(zone) < SIZE -10)
                    limit.replace(p,p.getLocation());
                }
            }
        }
    }

    public void setScoreBoard(Player p) {
        this.timer.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.timer.setDisplayName(ChatColor.DARK_PURPLE + "H&S");
        Score score = this.timer.getScore(ChatColor.DARK_GREEN + "TIMER");
        score.setScore(this.time);
        p.setScoreboard(this.board);
    }

    public void updateScoreBoard() {
        for(Player p : t1.players) {
            Score score = this.timer.getScore(ChatColor.DARK_GREEN + "TIMER");
            score.setScore(this.time);
            p.setScoreboard(this.board);
        }
        for(Player p : t2.players) {
            Score score = this.timer.getScore(ChatColor.DARK_GREEN + "TIMER");
            score.setScore(this.time);
            p.setScoreboard(this.board);
        }
    }

    public void delScoreBoard() {
        this.timer.unregister();
        this.board.clearSlot(DisplaySlot.SIDEBAR);
    }

    public void setMysteryChests(boolean set){
        if(set){
            Random rand = new Random();
            char[] lst = new char[3];
            int i = 0;
            double rx = 0;
            double ry = 0;
            double rz = 0;
            boolean keepOn = true;

            for(int n = 0;n < SIZE/10;n++) {
                do {
                    while (i != 3) {
                        double r = rand.nextDouble();
                        if (r < 5) {
                            lst[i] = '-';
                            i++;
                        } else if (r > 5) {
                            lst[i] = '+';
                            i++;
                        } else {

                        }
                    }
                    if (lst[0] == '+') {
                        rx = zone.getX() + rand.nextDouble() * SIZE;
                    } else {
                        rx = zone.getX() - rand.nextDouble() * SIZE;
                    }
                    if (lst[1] == '+') {
                        ry = zone.getY() + rand.nextDouble() * SIZE;
                    } else {
                        ry = zone.getY() - rand.nextDouble() * SIZE;
                    }
                    if (lst[2] == '+') {
                        rz = zone.getZ() + rand.nextDouble() * SIZE;
                    } else {
                        rz = zone.getZ() - rand.nextDouble() * SIZE;
                    }
                    Location l = new Location(zone.getWorld(), rx, ry, rz);
                    Location under = new Location(zone.getWorld(), rx, ry-1, rz);
                    boolean valid = false;
                    for (ArmorStand a : chests.keySet()) {
                        if(l.distance(a.getLocation()) < SIZE / 10){
                            valid = false;
                            break;
                        }
                        else{
                            valid = true;
                        }
                    }
                    if(valid){
                        if ((under.getBlock().getType() != Material.AIR && under.getBlock().getType() != Material.WATER) && l.getBlock().getType() == Material.AIR) {
                            ArmorStand z = (ArmorStand)zone.getWorld().spawnEntity(l, EntityType.ARMOR_STAND);
                            z.setInvisible(true);
                            z.setCustomNameVisible(true);
                            z.setCustomName(ChatColor.GOLD + "[?]MYSTERY CHEST[?]");
                            z.setInvulnerable(true);
                            z.setSmall(true);
                            chests.put(z,true);
                            keepOn = false;
                        }
                    }
                } while (keepOn);
            }

        }
        else{
            for(ArmorStand a: chests.keySet()){
                a.setHealth(0);
            }
        }
    }

    public void useChest(Entity en){
        if(en instanceof ArmorStand){
            if(en.getName().equals("[?]MYSTERY CHEST[?]")){
                chests.replace((ArmorStand) en,false);
                ((ArmorStand) en).setHealth(0);
            }
        }
    }
}
