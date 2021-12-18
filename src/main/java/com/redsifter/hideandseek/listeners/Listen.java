package com.redsifter.hideandseek.listeners;

import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import com.redsifter.hideandseek.HideAndSeek;
import com.redsifter.hideandseek.utils.FileManager;
import com.redsifter.hideandseek.utils.Game;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Listen implements Listener {

    private HideAndSeek main;
    public Listen(HideAndSeek hs){
        this.main = hs;
    }

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
                        target.getWorld().playSound(target.getLocation(), Sound.BLOCK_CHAIN_BREAK,5,5);
                        target.getWorld().spawnParticle(Particle.SMOKE_LARGE,target.getLocation(),5);
                    }
                }
            }
            //THERMO-SIGNAL BONUS
            else if(pl.getInventory().getItemInMainHand().getType() == Material.NETHER_STAR){
                pl.getInventory().remove(Material.NETHER_STAR);
                for(Game g : HideAndSeek.games){
                    if(g != null){
                        if(g.t1.players.contains(pl)){
                            for(Player s : g.t2.players){
                                s.addPotionEffect(PotionEffectType.GLOWING.createEffect(200,1));
                            }
                        }
                        else{
                            pl.teleport(g.zone);
                            for(Player h : g.t1.players){
                                h.addPotionEffect(PotionEffectType.GLOWING.createEffect(200,1));
                            }
                        }
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
    public void onPlayerSwapItem(PlayerSwapHandItemsEvent event){
        if(HideAndSeek.playerInGame(event.getPlayer())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event){
        if(HideAndSeek.playerInGame(event.getPlayer())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPickUpItem(PlayerAttemptPickupItemEvent event){
        if(HideAndSeek.playerInGame(event.getPlayer())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDragItem(InventoryDragEvent event){
        if(event.getInventory().getType() == InventoryType.PLAYER){
            for(HumanEntity p : event.getViewers()){
                if(HideAndSeek.playerInGame((Player)p)){
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event){
        for(Game g : HideAndSeek.games){
            if (g != null) {
                if (g.owner.equals(event.getPlayer())) {
                    g.announcement(ChatColor.RED + "[!] The game was cancelled because the owner left without starting it [!]");
                    HideAndSeek.cancelGame(g.nb + 1);
                    return;
                }
            }
        }
        if(HideAndSeek.playerInGame(event.getPlayer())){
            removeFromGame(event.getPlayer());
            //HideAndSeek.saveinv(event.getPlayer());
        }
    }

    /*@EventHandler
    public void onConnect(PlayerJoinEvent event){
        HideAndSeek.loadinv(event.getPlayer());
    }*/
    @EventHandler
    public void onPlayerSit(VehicleEnterEvent event){
        if(event.getEntered() instanceof Player && event.getVehicle() instanceof Arrow){
            Player pl = (Player) event.getEntered();
            System.out.println(pl.getName() + " tried to sit");
            if(HideAndSeek.playerInGame(pl)){
                event.setCancelled(true);
                pl.sendActionBar(ChatColor.RED + "[!]You can't sit during a game");
            }
        }
    }

    @EventHandler
    public void onPlayerStarve(FoodLevelChangeEvent event){
        if(HideAndSeek.playerInGame((Player)event.getEntity())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerTrip(PlayerMoveEvent event){
        if(HideAndSeek.playerInGame(event.getPlayer())){
            if (event.getPlayer().getLocation().getY() < 0 || event.getPlayer().getLocation().getBlock().getType() == Material.LAVA){
                for (Game g : HideAndSeek.games){
                    if (g != null) {
                        if (g.players.contains(event.getPlayer())) {
                            event.getPlayer().teleport(g.zone);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event){
        if(HideAndSeek.playerInGame(event.getPlayer())){
            for (Game g : HideAndSeek.games){
                if (g.players.contains(event.getPlayer())){
                    event.getPlayer().teleport(g.zone);
                }
            }
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

    public void bonus(Player p) {
        p.sendMessage(ChatColor.GOLD + "[?!]|-----[BONUS]-----|[!?]");
        double b = HideAndSeek.randDouble(0, 33);
        if (p.getInventory().contains(Material.GLASS_BOTTLE)) {
            p.getInventory().remove(Material.GLASS_BOTTLE);
        }
        if (0 <= b && b <= 7) {
            p.sendMessage(ChatColor.DARK_PURPLE + "[ENDERPEARL]");
            p.sendMessage(ChatColor.ITALIC + "[?]THROW : Teleports you to the landing location");
            p.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
        }
        else if (7 < b && b <= 19) {
            p.sendMessage(ChatColor.YELLOW + "[SPECTRAL ARROWS]");
            p.sendMessage(ChatColor.ITALIC + "[?]SHOOT : The person you shot will glow for a small amount of time");
            p.sendMessage(ChatColor.ITALIC + "[!]Works only on opposing team members");
            if (p.getInventory().contains(Material.CROSSBOW)) {
                p.getInventory().remove(Material.CROSSBOW);
            }
            p.getInventory().addItem(new ItemStack(Material.CROSSBOW));
            p.getInventory().addItem(new ItemStack(Material.SPECTRAL_ARROW, 5));
        }
        else if (19 < b && b <= 25) {
            p.sendMessage(ChatColor.AQUA + "[SPEED POTION]");
            p.sendMessage(ChatColor.ITALIC + "[?]DRINK : Makes you go super fast");
            ItemStack potion = new ItemStack(Material.POTION, 1);
            PotionMeta meta = (PotionMeta) potion.getItemMeta();
            meta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 150, 5), true);
            meta.setDisplayName(ChatColor.AQUA + "SUPERSPEED");
            potion.setItemMeta(meta);
            p.getInventory().addItem(potion);
        }
        else if (25 < b && b <= 27) {
            p.sendMessage(ChatColor.GOLD + "[I BELIEVE I CAN FLY]");
            p.sendMessage(ChatColor.ITALIC + "[?]INSTANT : Enables you to fly");
            p.setAllowFlight(true);
            p.setFlying(true);
            BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
            scheduler.scheduleSyncDelayedTask(main, new Runnable() {
                public void run() {
                    p.setAllowFlight(false);
                    p.setFlying(false);
                }
            }, 100L);
        }
        else if (27 < b && b <= 29){
            p.sendMessage(ChatColor.DARK_PURPLE + "[RADAR]");
            p.sendMessage(ChatColor.ITALIC + "[?]HOLD : Reveal the nearest opposing team member's position");
            p.sendMessage(ChatColor.ITALIC + "[!]55 blocks ahead or less");
            ItemStack compass = new ItemStack(Material.COMPASS);
            ItemMeta meta = compass.getItemMeta();
            meta.setDisplayName(ChatColor.DARK_PURPLE + "RADAR");
            compass.setItemMeta(meta);
            if(p.getInventory().contains(compass)){
                p.getInventory().remove(compass);
            }
            p.getInventory().addItem(compass);
            BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
            scheduler.scheduleSyncDelayedTask(main, new Runnable() {
                public void run() {
                    p.getInventory().remove(compass);
                }
            }, 1000L);
        }
        else if (29 < b && b <= 32) {
            p.sendMessage(ChatColor.GRAY + "[STEALTH POTION]");
            p.sendMessage(ChatColor.ITALIC + "[?]DRINK : Makes you invisible");
            ItemStack potion = new ItemStack(Material.POTION, 1);
            PotionMeta meta = (PotionMeta) potion.getItemMeta();
            meta.addCustomEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 250, 1), true);
            meta.setDisplayName(ChatColor.GRAY + "STEALTH");
            meta.setColor(Color.AQUA);
            potion.setItemMeta(meta);
            p.getInventory().addItem(potion);
        }
        else if (32 < b && b <= 33) {
            p.sendMessage(ChatColor.GOLD + "THERMO-SIGNAL");
            p.sendMessage(ChatColor.ITALIC + "[?]LEFT-CLICK : Reveal all the opposing team members's positions");
            p.sendMessage(ChatColor.ITALIC + "[!]Teleport you to game spawn if you are a seeker");
            ItemStack netherstar = new ItemStack(Material.NETHER_STAR);
            ItemMeta meta = netherstar.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + "THERMO-SIGNAL");
            netherstar.setItemMeta(meta);
            p.getInventory().addItem(netherstar);
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
                g.remPlayer(pl);
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