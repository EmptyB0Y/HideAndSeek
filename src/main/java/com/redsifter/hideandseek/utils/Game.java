package com.redsifter.hideandseek.utils;

import com.redsifter.hideandseek.HideAndSeek;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.*;

public class Game extends BukkitRunnable {
    public CustomTeam t1;
    public CustomTeam t2;
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
    public HashMap<Player,Integer> lastBonus = new HashMap<Player,Integer>();
    public int SIZE;
    //SCOREBOARD
    private ScoreboardManager manager = Bukkit.getScoreboardManager();
    public Scoreboard board = manager.getMainScoreboard();
    private Objective timer = board.registerNewObjective("timer"+nb, "test", "TIMER");
    private int cursor = 0;
    public Game(CustomTeam team1, CustomTeam team2,int n,Player p,HideAndSeek hs){
        t1 = team1;
        t2 = team2;
        nb = n;
        owner = p;
        zone = p.getLocation();
        this.main = hs;
    }

    public void run(){
        /*for(Player p : lastBonus.keySet()){
            int i = lastBonus.get(p);
            if(i > 0){
                lastBonus.replace(p,i--);
            }
        }*/
        updateScoreBoard();
        pushBack(t1);
        pushBack(t2);
        cursor++;
        if(cursor == 5){
            playersLocations();
            cursor = 0;
        }
        if(time == timeset - 60){
            announcement(ChatColor.DARK_RED + "[!]THE SEEKERS ARE UNLEASHED[!]");
            announcement(ChatColor.GOLD + "[!]THE MYSTERY CHESTS ARE AVAILABLE[!]");
        }
        if(time == timeset/2){
            announcement(ChatColor.GOLD + "[!]THE TIMER IS HALFWAY DONE[!]");
        }
        if(time == (timeset*0.1)){
            announcement(ChatColor.RED + "[!]THE TIMER IS ALMOST DONE[!]");

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

    public boolean start(int timer,int limit){
        if((t1.players.size() - t2.players.size() <= 3 || t1.players.size() - t2.players.size() >= -3) && (!t1.players.isEmpty() && !t2.players.isEmpty())) {
            this.time = timer;
            this.timeset = timer;
            this.runTaskTimer((Plugin) this.main, 0L, 20L);
            this.hasStarted = true;
            this.SIZE = limit;
            board.registerNewTeam(String.valueOf(nb));
            board.getTeam(String.valueOf(nb)).setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            for(Player p : t1.players){
                board.getTeam(String.valueOf(nb)).addEntry(p.getName());
                p.teleport(zone);
                p.setInvulnerable(true);
                p.setFoodLevel(20);
                p.setGameMode(GameMode.ADVENTURE);
                setScoreBoard(p);
                p.sendMessage(ChatColor.DARK_GREEN + "[!]You have 60 seconds to hide before the seekers get unleashed !\n");
            }
            for(Player p : t2.players){
                p.teleport(zone);
                p.setInvulnerable(true);
                p.setFoodLevel(20);
                p.setGameMode(GameMode.ADVENTURE);
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20*60, 200));
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20*60, 200));
                setScoreBoard(p);
                p.sendMessage(ChatColor.DARK_GRAY + "[!]You have to wait 60 seconds before you can chase hiders !\n");

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
        main.cancelGame(nb);
    }

    public void seekersVictory(){
        announcement(ChatColor.GOLD + "The seekers won ! Congratulations :\n");
        for(Player p : t2.players){
            announcement(ChatColor.RED + p.getName() + "\n");
        }
        main.cancelGame(nb);
    }
    public void pushBack(CustomTeam t){
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

    public void playersLocations(){
        boolean check = false;
        for(Player p1 : t1.players){
            for(Player p2 : t2.players){
                if(p1.getLocation().distance(p2.getLocation()) <= 20 && p1.getLocation().distance(p2.getLocation()) > 10){
                    if(!check) {
                        p1.sendActionBar(ChatColor.AQUA + "[!]A seeker is nearby, careful !");
                        check = true;
                    }
                    p2.sendActionBar(ChatColor.AQUA + "[!]A hider is nearby !");
                }
                else if(p1.getLocation().distance(p2.getLocation()) <= 10){
                    if(!check) {
                        p1.sendActionBar(ChatColor.DARK_PURPLE + "[!]A seeker is very close, careful !");
                        check = true;
                    }
                    p2.sendActionBar(ChatColor.DARK_PURPLE + "[!]A hider is very close !");
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

    public void useChest(Entity en){
        if(en instanceof ArmorStand){
            if(en.getName().equals(ChatColor.GOLD + "[?]")){
                chests.replace((ArmorStand) en,false);
                ((ArmorStand) en).setHealth(0);
                en.remove();
            }
        }
    }
}
