package com.redsifter.hideandseek.listeners;

import com.redsifter.hideandseek.HideAndSeek;
import com.redsifter.hideandseek.utils.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

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

}
