package com.redsifter.hideandseek.utils;

import com.redsifter.hideandseek.HideAndSeek;
import net.minecraft.server.v1_16_R3.EntityArmorStand;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_16_R3.PacketPlayOutSpawnEntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomTeam {
    public ArrayList<Player> players = new ArrayList<Player>();
    private int playernb = 0;
    private int nb;
    public Channel ch;
    public String name;
    public boolean full = false;
    public Map<ArrayList<UUID>,EntityArmorStand> hiddenmap = new HashMap<ArrayList<UUID>,EntityArmorStand>();

    public CustomTeam(int number, String nm){
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

    public void chat(String msg){
        ch.broadcast(msg);
    }
}