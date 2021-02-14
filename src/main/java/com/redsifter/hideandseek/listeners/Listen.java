package com.redsifter.hideandseek.listeners;

import com.redsifter.hideandseek.HideAndSeek;
import com.redsifter.hideandseek.utils.Game;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;

public class Listen implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
        Player pl = event.getPlayer();
        for(Game g : HideAndSeek.games){
            if(g != null) {
                if (g.t1.players.contains(pl)) {
                    event.setCancelled(true);
                    g.t1.chat(event.getMessage());
                } else if (g.t2.players.contains(pl)) {
                    event.setCancelled(true);
                    g.t2.chat(event.getMessage());
                }
            }
        }
    }

    @EventHandler
    public void onHit(PlayerInteractEntityEvent event){
        if(HideAndSeek.playerInGame(event.getPlayer())){
            Player pl = event.getPlayer();
            //TEST INTERRACTION
            Player target = getNearestPlayerInSight(pl,5);
            if(target != null) {
                if (seekerFind(pl, target)) {
                    pl.sendMessage(ChatColor.DARK_PURPLE + "You found " + target.getName() + " !\n");
                    target.sendMessage(ChatColor.RED + "You were found by " + pl.getName() + " !\n");
                    removeFromGame(target);
                }
            }
            /*else if(event.getRightClicked().getType() == EntityType.ARMOR_STAND){
                Entity en = event.getRightClicked();
                if(en.getName().equals("[?]MYSTERY CHEST[?]")){
                    bonus(pl);
                    useChest((ArmorStand)en);
                }
            }*/
        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event){
        if(HideAndSeek.playerInGame(event.getPlayer())){
            removeFromGame(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerStarve(FoodLevelChangeEvent event){
        if(HideAndSeek.playerInGame((Player)event.getEntity())){
            event.setCancelled(true);
        }
    }

    public void useChest(ArmorStand en){
        for(Game g : HideAndSeek.games){
            if(g != null) {
                if (g.chests.containsKey(en)) {
                    g.useChest(en);
                }
            }
        }
    }

    public void bonus(Player p){

    }

    public boolean seekerFind(Player p1, Player p2){
        for(Game g : HideAndSeek.games){
            if(g != null) {
                if ((g.t2.players.contains(p1) && g.t1.players.contains(p2)) && g.hasStarted && g.time <= g.timeset - 60) {
                    g.announcement(ChatColor.DARK_PURPLE + "[!]" + p2.getName() + " has been found by " + p1.getName());
                    return true;
                }
            }
        }
        return false;
    }

    public void removeFromGame(Player pl) {
        for (Game g : HideAndSeek.games) {
            if (g != null) {
                if (g.t1.players.contains(pl)) {
                    g.t1.players.remove(pl);
                }
                else if (g.t2.players.contains(pl)) {
                    g.t2.players.remove(pl);
                }
            }
        }
    }

    public static Player getNearestPlayerInSight(Player player, int range) {
        ArrayList<Entity> entities = (ArrayList<Entity>)player.getNearbyEntities(range, range, range);
        ArrayList<Block> sightBlock = (ArrayList<Block>)player.getLineOfSight(null, range);
        ArrayList<Location> sight = new ArrayList<>();
        int i;
        for (i = 0; i < sightBlock.size(); i++)
            sight.add(((Block)sightBlock.get(i)).getLocation());
        for (i = 0; i < sight.size(); i++) {
            for (int k = 0; k < entities.size(); k++) {
                if (Math.abs(((Entity)entities.get(k)).getLocation().getX() - ((Location)sight.get(i)).getX()) < 1.3D &&
                        Math.abs(((Entity)entities.get(k)).getLocation().getY() - ((Location)sight.get(i)).getY()) < 1.5D &&
                        Math.abs(((Entity)entities.get(k)).getLocation().getZ() - ((Location)sight.get(i)).getZ()) < 1.3D && (
                        (Block)sightBlock.get(i)).isPassable() &&
                        entities.get(k) instanceof Player)
                    return Bukkit.getPlayerExact(((Entity)entities.get(k)).getName());
            }
        }
        return null;
    }
}