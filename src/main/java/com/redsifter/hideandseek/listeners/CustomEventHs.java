package com.redsifter.hideandseek.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class CustomEventHs extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private String name;

    public CustomEventHs(String name, Player player) {
        this.name = name;
        this.player = player;
    }

    public String getName() {
        return this.name;
    }

    public Player getPlayer(){
        return this.player;
    }
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
