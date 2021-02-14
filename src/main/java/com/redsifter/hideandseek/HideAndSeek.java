package com.redsifter.hideandseek;

import com.redsifter.hideandseek.listeners.Listen;
import com.redsifter.hideandseek.utils.Game;
import com.redsifter.hideandseek.utils.CustomTeam;
import com.redsifter.hideandseek.utils.SimpleRandom;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nullable;
import java.util.ArrayList;

public final class HideAndSeek extends JavaPlugin {

    @Override
    public void onEnable() {
        System.out.println("Enabled HideAndSeek\n");
        this.getServer().getPluginManager().registerEvents(new Listen(), this);
    }

    @Override
    public void onDisable() {
        System.out.println("Disabled HideAndSeek\n");
    }
    public static int MAXPLAYERS = 10;
    public static int MAXSIZE = 3;
    public static Game[] games = new Game[MAXSIZE];


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            if(label.equals("hssetgame")){
                if(args.length > 2){
                    sender.sendMessage("Too much arguments...\n");
                    return false;
                }
                if(countGames() == MAXSIZE){
                    sender.sendMessage("Too much games started at this time...\n");
                    return false;
                }
                //ADD TEAMS
                CustomTeam t1 = new CustomTeam(1,"hiders");
                CustomTeam t2 = new CustomTeam(2,"seekers");
                if(args.length > 0) {
                    ArrayList<Player> lst = new ArrayList<Player>();
                    args[0] = args[0] + ',';
                    int length = args[0].length();
                    char[] destArray = new char[length];
                    String str = "";
                    int i = 0;
                    //ADD FIRST TEAM
                    if (args.length >= 1) {
                        args[0].getChars(0, length, destArray, 0);
                        str = "";
                        for (char t : destArray) {
                            if (t == ',') {
                                if (!playerInGame(Bukkit.getPlayerExact(str)) && Bukkit.getOnlinePlayers().contains(Bukkit.getPlayerExact(str)) && i < MAXPLAYERS) {
                                    lst.add(Bukkit.getPlayerExact(str));
                                    Bukkit.getPlayerExact(str).sendMessage(ChatColor.GREEN + "You were added to a new game of HideAndSeek ! (type /hsleave to leave)\n");
                                    str = "";
                                } else {
                                    sender.sendMessage(str + " is either already in a game, offline or can't join because the team is full (" + MAXPLAYERS + ") !\n");
                                    str = "";
                                }
                            } else {
                                str = str + t;
                            }
                            i++;
                        }
                        t1.setPlayers(lst);
                        lst.clear();
                        i = 0;
                    }
                    //ADD SECOND TEAM
                    if (args.length == 2) {
                        args[1] = args[1] + ',';
                        length = args[1].length();
                        destArray = new char[length];
                        args[1].getChars(0, length, destArray, 0);
                        str = "";
                        for (char t : destArray) {
                            if (t == ',') {
                                if (!playerInGame(Bukkit.getPlayerExact(str)) && !t1.players.contains(Bukkit.getPlayerExact(str)) && Bukkit.getOnlinePlayers().contains(Bukkit.getPlayerExact(str)) && i < MAXPLAYERS) {
                                    lst.add(Bukkit.getPlayerExact(str));
                                    Bukkit.getPlayerExact(str).sendMessage(ChatColor.GREEN + "You were added to a new game of HideAndSeek ! (type /hsleave to leave)\n");
                                    str = "";
                                } else {
                                    sender.sendMessage(str + " is either already in a game, in the other team, offline or can't join because the team is full (" + MAXPLAYERS + ") !\n");
                                    str = "";
                                }
                            } else {
                                str = str + t;
                            }
                            i++;
                        }
                        t2.setPlayers(lst);
                    }
                }
                if(!areaAvailableFor(((Player) sender).getLocation(),(t2.players.size()+t1.players.size())*30,(Player)sender)){
                    return false;
                }
                //INITALIZE GAME
                int game = 0;
                if(countGames() > 0){
                    game = countGames()+1;
                }
                games[game] = new Game(t1, t2, countGames()+1, (Player) sender, this);
                sender.sendMessage("You initalized game n° " + countGames() + " !\n");
            }
            else if(label.equals("hsjoin")) {
                if (args.length < 1) {
                    sender.sendMessage("Missing arguments...\n");
                    return false;
                }
                else if (args.length == 2) {
                    if (Integer.parseInt(args[1]) > 3 || Integer.parseInt(args[1]) < 1) {
                        sender.sendMessage("Not a valid game number (1 to 3)\n");
                        return false;
                    }

                    if (games[Integer.parseInt(args[1])-1] != null && !games[Integer.parseInt(args[1])].hasStarted && !games[Integer.parseInt(args[1])].full) {
                        if (args[0].equals("h")) {
                            if (games[Integer.parseInt(args[1])].addPlayer((Player) sender, "h")) {
                                sender.sendMessage("Successfuly joined game n° " + games[Integer.parseInt(args[1])-1].nb + " as hider !\n");
                                return true;
                            }
                        } else if (args[0].equals("s")) {
                            if (games[Integer.parseInt(args[1])].addPlayer((Player) sender, "s")) {
                                sender.sendMessage("Successfuly joined game n° " + games[Integer.parseInt(args[1])-1].nb + " as seeker !\n");
                                return true;
                            }
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "This game either has already started, is not set, or has no more room for you in the selected team !\n");
                        return false;
                    }
                }
                else if(args.length == 1){
                    if (args[0].equals("h")) {
                        if (countGames() >= 1) {
                            for (Game g : games) {
                                if (!g.hasStarted) {
                                    if (g.addPlayer((Player) sender, "h")) {
                                        sender.sendMessage("Successfuly joined game n° " + g.nb + " as hider !\n");
                                        return true;
                                    } else {
                                        sender.sendMessage("Failed to join game n° " + g.nb + "...");
                                    }
                                }
                            }
                            sender.sendMessage("Failed to join any games so far, try /hslistgames to see available games.\n");
                            return false;
                        }
                    } else if (args[0].equals("s")) {
                        if (countGames() >= 1) {
                            for (Game g : games) {
                                if (!g.hasStarted) {
                                    if (g.addPlayer((Player) sender, "s")) {
                                        sender.sendMessage("Successfuly joined game n°" + g.nb + " as seeker !\n");
                                        return true;
                                    } else {
                                        sender.sendMessage("Failed to join game n° " + g.nb + "...");
                                    }
                                }
                            }
                            sender.sendMessage("Failed to join any games so far, try /hslistgames to see available games.\n");
                            return false;
                        }
                    }
                    else{
                        sender.sendMessage("Choose between h and s\n");
                    }
                }
                else{
                    sender.sendMessage("Usage : hsjoin <(h | s)> [n°]");
                }
            }
            else if(label.equals("hsleave")){
                    if(countGames() >= 1){
                        for(Game g : games) {
                            if(g.t1.players.contains((Player) sender)){
                                if(g.remPlayer((Player)sender)){
                                    sender.sendMessage("Successfuly left game n°" + g.nb + " !\n");
                                    return true;
                                }
                            }
                            else if(g.t2.players.contains((Player) sender)){
                                if(g.remPlayer((Player)sender)){
                                    sender.sendMessage("Successfuly left game n°" + g.nb + " !\n");
                                    return true;
                                }
                            }
                            else{
                                sender.sendMessage("You aren't in any games...\n");
                                return false;
                            }

                        }
                    }
                    else{
                    sender.sendMessage("There aren't any games...\n");
                    return false;
                    }
            }
            else if(label.equals("hslistgames")){
                sender.sendMessage("There are " + countGames() + " games available games right now.\n");
            }
            else if(label.equals("hsstartgame")){
                if(args.length < 2){
                    sender.sendMessage("Missing arguments...\n");
                    return false;
                }
                else if(Integer.parseInt(args[1]) < 200 || Integer.parseInt(args[1]) > 1200){
                    sender.sendMessage("The time must be contained between 200 and 1200");
                    return false;
                }
                if(countGames() >= 1) {
                    if (games[Integer.parseInt(args[0]) - 1].owner != (Player) sender) {
                        sender.sendMessage("You are not the owner of this game...\n");
                        return false;
                    }
                    if (!games[Integer.parseInt(args[0]) - 1].start(Integer.parseInt(args[1]))) {
                        sender.sendMessage("Couldn't start game, difference between the two teams is too high !\n");
                        return false;
                    } else {
                        sender.sendMessage("Successfully started game n°" + games[Integer.parseInt(args[0]) - 1].nb + " !\n");
                        try {
                            setMysteryChests(games[Integer.parseInt(args[0]) - 1],true);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else{
                    sender.sendMessage("There is no game available at the moment or you didn't specify the right team option (h | s).\n");
                }
            }
            else if(label.equals("hscancelgame")){
                if(args.length != 1){
                    sender.sendMessage("Usage : /hscancelgame <n°>\n");
                    return false;
                }
                if(games[Integer.parseInt(args[0])-1].owner != (Player)sender){
                    sender.sendMessage("You are not the owner of this game...\n");
                    return false;
                }
                sender.sendMessage("Successfully cancelled game n°" + games[Integer.parseInt(args[0])-1].nb + " !\n");
                try {
                    setMysteryChests(games[Integer.parseInt(args[0])-1],false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                cancelGame(games[Integer.parseInt(args[0])-1].nb);
            }
            else if(label.equals("random")){
                sender.sendMessage(""+randDouble(Integer.parseInt(args[0]),Integer.parseInt(args[1])));
            }
        }
        return true;
    }

    public boolean areaAvailableFor(Location l, int size, @Nullable Player p){
        for(Game g : games) {
            if(g != null) {
                if (l.distance(g.zone) > size + g.SIZE) {
                    return true;
                }
                else{
                    p.sendMessage("There is no room for a game here ! Try to get a bit further from " + g.zone.getX() + " " + g.zone.getY() + " "  + g.zone.getZ());
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean playerInGame(Player pl){
        for(Game g : games){
            if(g != null) {
                if (g.t1.players.contains(pl) || g.t2.players.contains(pl)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int countGames(){
        int count = 0;
        for(int i = 0;i < games.length;i++){
            if(games[i] != null){
                count++;
            }
        }
        return count;
    }

    public void setMysteryChests(Game g,boolean set) throws InterruptedException {
        if(set){
            int[] random = generateRandom(200,-g.SIZE,g.SIZE);
            wait(10000);
            boolean keepOn = true;
            int n;
            for(n = 0; n < g.SIZE/10; n++) {
                do {
                    Location l = new Location(g.zone.getWorld(), random[n], random[n+1], random[n+2]);
                    Location under = new Location(g.zone.getWorld(), l.getX(), l.getY()-1, l.getZ());
                    boolean valid = false;
                    for (ArmorStand a : g.chests.keySet()) {
                        if(l.distance(a.getLocation()) < g.SIZE / 10){
                            valid = false;
                            break;
                        }
                        else{
                            valid = true;
                        }
                    }
                    if(valid){
                        System.out.println("Valid");
                        if ((under.getBlock().getType() != Material.AIR && under.getBlock().getType() != Material.WATER) && l.getBlock().getType() == Material.AIR) {
                            System.out.println("Building mystery chest");
                            ArmorStand z = (ArmorStand)g.zone.getWorld().spawnEntity(l, EntityType.ARMOR_STAND);
                            ItemStack skull = new ItemStack(Material.CHEST, 1, (byte) 3);
                            z.getEquipment().setHelmet(skull);
                            z.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,1000000,1));
                            z.setCustomNameVisible(true);
                            z.setCustomName(ChatColor.GOLD + "[?]MYSTERY CHEST[?]");
                            z.setInvulnerable(true);
                            z.setSmall(true);
                            g.chests.put(z,true);
                            keepOn = false;
                        }
                    }
                } while (keepOn);
            }

        }
        else{
            for(ArmorStand a: g.chests.keySet()){
                a.setHealth(0);
            }
        }
    }

    public static double randDouble(int min, int max) {
        SimpleRandom random = new SimpleRandom(9000);
        double multiplicator = random.nextInt() * 0.0001;
        System.out.println(multiplicator);
        return min + multiplicator * ((max - min));
    }

    public int[] generateRandom(int nbr,int min, int max){
        if(max <= 0){
            return null;
        }
        int[] random = new int[nbr];
        int i;
        for(i = 0;i < nbr;i++){
            randDouble(min,max);
        }
        return random;
    }

    public void cancelGame(int nb){
        if(games[nb-1] != null){
            if(games[nb-1].hasStarted) {
                games[nb - 1].cancel();
            }
            for(Player p : games[nb-1].t1.players){
                p.setGameMode(GameMode.SURVIVAL);
                p.setInvulnerable(false);
            }
            for(Player p : games[nb-1].t2.players){
                for (PotionEffect effect : p.getActivePotionEffects()) {
                    p.removePotionEffect(effect.getType());
                }
                p.setGameMode(GameMode.SURVIVAL);
                p.setInvulnerable(false);
            }
            games[nb-1].board.getTeam(String.valueOf(games[nb-1].nb)).unregister();
            games[nb-1].t1.flush();
            games[nb-1].t2.flush();
            games[nb-1].delScoreBoard();
            games[nb-1] = null;
            ArrayUtils.removeElement(games, games[nb-1]);

        }
    }
}