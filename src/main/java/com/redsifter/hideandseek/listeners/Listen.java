package com.redsifter.hideandseek.listeners;

import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import com.redsifter.hideandseek.HideAndSeek;
import com.redsifter.hideandseek.utils.Game;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;

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
    public void onHit(PlayerInteractEvent event){
        if(HideAndSeek.playerInGame(event.getPlayer()) && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)){
            Player pl = event.getPlayer();
            //TEST INTERRACTION
            Entity target = getNearestEntityInsight(pl,3);
            if(target != null) {
                if (target instanceof Player) {
                    if (seekerFind(pl, (Player)target)) {
                        pl.sendMessage(ChatColor.DARK_PURPLE + "You found " + target.getName() + " !\n");
                        target.sendMessage(ChatColor.RED + "You were found by " + pl.getName() + " !\n");
                        removeFromGame((Player)target);
                    }
                } else if (target instanceof ArmorStand) {
                    if (target.getName().equals(ChatColor.GOLD + "[?]") && chestsAreAvailableFor(pl)) {
                        bonus(pl);
                        useChest((ArmorStand) target);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onArrowHitPlayer(ProjectileCollideEvent event){
        if(event.getEntity() instanceof SpectralArrow && event.getCollidedWith() instanceof Player) {
            if (HideAndSeek.playerInGame((Player)event.getCollidedWith())){
                Player p = (Player)event.getCollidedWith();
                if(event.getEntity().getShooter() instanceof Player){
                    if(gameHit(p,(Player)event.getEntity().getShooter())){
                        p.addPotionEffect(PotionEffectType.GLOWING.createEffect(100,1));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerManipulateArmorStand(PlayerArmorStandManipulateEvent event){
        if(HideAndSeek.playerInGame(event.getPlayer()) || event.getRightClicked().getName().equals(ChatColor.GOLD + "[?]")){
            event.setCancelled(true);
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
        p.sendMessage(ChatColor.GOLD + "[?!]|-----[BONUS]-----|[!?]");
        int b = (int)HideAndSeek.randDouble(0,3.5);
        if(p.getInventory().contains(Material.GLASS_BOTTLE)){
            p.getInventory().remove(Material.GLASS_BOTTLE);
        }
        switch(b){
            case 0:
                p.sendMessage(ChatColor.DARK_PURPLE + "[ENDERPEARL]");
                p.getInventory().setItemInMainHand(new ItemStack(Material.ENDER_PEARL));
                break;
            case 1:
                p.sendMessage(ChatColor.DARK_RED + "[CROSSBOW]");
                if( p.getInventory().contains(Material.CROSSBOW)){
                    p.getInventory().remove(Material.CROSSBOW);
                }
                p.getInventory().setItemInMainHand(new ItemStack(Material.CROSSBOW));
                p.getInventory().setItemInOffHand(new ItemStack(Material.SPECTRAL_ARROW,8));
                break;
            case 2:
                p.sendMessage(ChatColor.AQUA + "[SPEED POTION]");
                ItemStack potion = new ItemStack(Material.POTION,1);
                PotionMeta meta = (PotionMeta)potion.getItemMeta();
                meta.addCustomEffect(new PotionEffect( PotionEffectType.SPEED, 150, 5), true);
                meta.setDisplayName(ChatColor.AQUA + "SUPERSPEED");
                potion.setItemMeta(meta);
                p.getInventory().setItemInMainHand(potion);
                break;
            case 3:
                p.sendMessage(ChatColor.GOLD + "[I BELIEVE I CAN FLY]");
                p.setAllowFlight(true);
                p.setFlying(true);
                BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
                scheduler.scheduleSyncDelayedTask((Plugin)this, new Runnable() {
                    public void run() {
                        p.setAllowFlight(false);
                        p.setFlying(false);
                    }
                },  100L);
                break;
        }
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

    public boolean gameHit(Player p1, Player p2){
        for(Game g : HideAndSeek.games){
            if(g != null) {
                if(g.hasStarted && g.time <= g.timeset - 60) {
                    if (g.t2.players.contains(p1) && g.t1.players.contains(p2)) {
                        g.announcement(ChatColor.DARK_AQUA + "[!]" + ChatColor.DARK_GREEN + p2.getName() + ChatColor.DARK_AQUA + " has spotted " + ChatColor.RED + p1.getName());                        return true;
                    }
                    else if(g.t2.players.contains(p2) && g.t1.players.contains(p1)){
                        g.announcement(ChatColor.DARK_AQUA + "[!]" + ChatColor.RED + p2.getName() + ChatColor.DARK_AQUA + " has spotted " + ChatColor.DARK_GREEN + p1.getName());
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public boolean chestsAreAvailableFor(Player p){
        for(Game g : HideAndSeek.games){
            if(g.t1.players.contains(p) || g.t2.players.contains(p)){
                if(g.time <= g.timeset - 60){
                    if(g.lastBonus.containsKey(p)){
                        if(g.t1.players.contains(p)) {
                            if (g.lastBonus.get(p) - g.time >= 10) {
                                g.lastBonus.replace(p,g.time);
                                return true;
                            }
                            else{
                                p.sendMessage(ChatColor.GRAY + "[!]You must wait " + (10 - ((g.lastBonus.get(p)) - g.time)) + " more seconds before you can get your next bonus");
                                return false;
                            }
                        }
                        else if(g.t2.players.contains(p)){
                            if (g.lastBonus.get(p) - g.time >= 20) {
                                g.lastBonus.replace(p,g.time);
                                return true;
                            }
                            else{
                                p.sendMessage(ChatColor.GRAY + "[!]You must wait " + (20 - ((g.lastBonus.get(p)) - g.time)) + " more seconds before you can get your next bonus");
                                return false;
                            }
                        }
                    }
                    else{
                        g.lastBonus.put(p,g.time);
                    }
                    return true;
                }
                else {
                    p.sendMessage(ChatColor.GRAY + "[!]Mystery chests are not available yet");
                    return false;
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

    public static Entity getNearestEntityInsight(Player player, int range) {
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
                        (Block)sightBlock.get(i)).isPassable())
                    return Bukkit.getEntity(((Entity)entities.get(k)).getUniqueId());
            }
        }
        return null;
    }

}