package com.redsifter.hideandseek;

import com.redsifter.hideandseek.listeners.Listen;
import com.redsifter.hideandseek.utils.FileManager;
import com.redsifter.hideandseek.utils.Game;
import com.redsifter.hideandseek.utils.CustomTeam;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Team;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public final class HideAndSeek extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(new Listen(this), this);
    }

    @Override
    public void onDisable() {
        for (Game g : HideAndSeek.games){
            if(g != null) {
                try {
                    cancelGame(g.nb + 1);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static int MAXPLAYERS = 10;
    public static int MAXSIZE = 3;
    public static Game[] games = new Game[MAXSIZE];
    public static FileManager fm = new FileManager();

    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String label, @Nonnull String[] args) {
        if (sender instanceof Player) {
            switch (label) {
                case "hssetgame":
                    if (args.length > 2) {
                        sender.sendMessage("Too much arguments...\n");
                        return false;
                    }
                    if (countGames() == MAXSIZE) {
                        sender.sendMessage("Too much games started at this time...\n");
                        return false;
                    }
                    //ADD TEAMS
                    CustomTeam t1 = new CustomTeam(1, "hiders");
                    CustomTeam t2 = new CustomTeam(2, "seekers");
                    if (args.length > 0) {
                        ArrayList<Player> lst = new ArrayList<>();
                        args[0] = args[0] + ',';
                        int length = args[0].length();
                        char[] destArray = new char[length];
                        String str = "";
                        int i = 0;
                        //ADD FIRST TEAM
                        if (args.length == 1) {
                            args[0].getChars(0, length, destArray, 0);
                            str = "";
                            for (char t : destArray) {
                                if (t == ',') {
                                    System.out.println(str.trim());
                                    if (!playerInGame(Bukkit.getPlayerExact(str.trim())) && Bukkit.getOnlinePlayers().contains(Bukkit.getPlayerExact(str.trim())) && i < MAXPLAYERS) {
                                        lst.add(Bukkit.getPlayerExact(str.trim()));
                                        Bukkit.getPlayerExact(str.trim()).sendMessage(ChatColor.GREEN + "You were added to a new game of HideAndSeek ! (type /hsleave to leave)\n");
                                        str = "";
                                    } else {
                                        sender.sendMessage(str + " is either already in a game, offline or can't join because the team is full (" + MAXPLAYERS + ") !\n");
                                        str = "";
                                    }
                                }
                                else {
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
                                    System.out.println(str);
                                    if (!playerInGame(Bukkit.getPlayerExact(str.trim())) && !t1.players.contains(Bukkit.getPlayerExact(str.trim())) && Bukkit.getOnlinePlayers().contains(Bukkit.getPlayerExact(str.trim())) && i < MAXPLAYERS) {
                                        lst.add(Bukkit.getPlayerExact(str.trim()));
                                        Bukkit.getPlayerExact(str.trim()).sendMessage(ChatColor.GREEN + "You were added to a new game of HideAndSeek ! (type /hsleave to leave)\n");
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
                    if (!areaAvailableFor(((Player) sender).getLocation(), (t2.players.size() + t1.players.size()) * 30, (Player) sender)) {
                        return false;
                    }
                    //INITALIZE GAME
                    int game = 0;
                    if (countGames() > 0) {
                        game = countGames() + 1;
                    }
                    games[game] = new Game(t1, t2, game, (Player) sender, this);
                    System.out.println(games[game].players);
                    System.out.println(games[game].t1.players);
                    System.out.println(games[game].t2.players);

                    sender.sendMessage("You initalized game n° " + countGames() + " !\n");

                    break;
                case "hsjoin":
                    if (args.length < 1) {
                        sender.sendMessage("Missing arguments...\n");
                        return false;
                    } else if (args.length == 2) {

                        if (Integer.parseInt(args[1]) > 3 || Integer.parseInt(args[1]) < 1) {
                            sender.sendMessage("Not a valid game number (1 to "+MAXSIZE+")\n");
                            return false;
                        }

                        Player p = (Player) sender;
                        if (games[(Integer.parseInt(args[1]) - 1)] == null) {
                            return false;
                        }
                        if (!games[(Integer.parseInt(args[1]) - 1)].hasStarted && !games[(Integer.parseInt(args[1]) - 1)].full && !games[(Integer.parseInt(args[1]) - 1)].players.contains(p)) {
                            if (args[0].equals("h")) {
                                if (games[(Integer.parseInt(args[1]) - 1)].addPlayer((Player) sender, "h")) {
                                    sender.sendMessage("Successfuly joined game n° " + games[Integer.parseInt(args[1])].nb + " as hider !\n");
                                    return true;
                                }
                            } else if (args[0].equals("s")) {
                                if (games[(Integer.parseInt(args[1]) - 1)].addPlayer((Player) sender, "s")) {
                                    sender.sendMessage("Successfuly joined game n° " + games[Integer.parseInt(args[1])].nb + " as seeker !\n");
                                    return true;
                                }
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "This game either has already started, is not set, or has no more room for you in the selected team !\n");
                            return false;
                        }
                    } else if (args.length == 1) {
                        Player p = (Player) sender;
                        if (countGames() >= 1) {

                            if (args[0].equals("h")) {
                                for (Game g : games) {
                                    if (g != null) {
                                        if (!g.hasStarted && !g.full && !g.players.contains(p)) {

                                            if (g.addPlayer((Player) sender, "h")) {
                                                sender.sendMessage("Successfuly joined game n° " + (g.nb + 1) + " as hider !\n");
                                                return true;
                                            } else {
                                                sender.sendMessage("Failed to join game n° " + (g.nb + 1) + "...");
                                            }

                                        }
                                    }
                                }
                                sender.sendMessage("Failed to join any games so far, try /hslistgames to see available games.\n");
                                return false;

                            } else if (args[0].equals("s")) {
                                for (Game g : games) {
                                    if (g != null) {
                                        if (!g.hasStarted && !g.full && !g.players.contains(p)) {

                                            if (g.addPlayer((Player) sender, "s")) {
                                                sender.sendMessage("Successfuly joined game n°" + (g.nb + 1) + " as seeker !\n");
                                                return true;
                                            } else {
                                                sender.sendMessage("Failed to join game n° " + (g.nb + 1) + "...");
                                            }
                                        }
                                    }
                                }
                                sender.sendMessage("Failed to join any games so far, try /hslistgames to see available games.\n");
                                return false;
                            } else {
                                sender.sendMessage("Choose between [h] (hider) and [s] (seekers)\n");
                            }
                        }
                    } else {
                        sender.sendMessage("Usage : hsjoin <(h | s)> [n°]");
                    }

                    break;
                case "hsleave":
                    if (countGames() >= 1) {
                        for (Game g : games) {
                            if (g.t1.players.contains((Player) sender)) {
                                if (g.remPlayer((Player) sender)) {
                                    sender.sendMessage("Successfuly left game n°" + (g.nb + 1) + " !\n");
                                    return true;
                                }
                            } else if (g.t2.players.contains((Player) sender)) {
                                if (g.remPlayer((Player) sender)) {
                                    sender.sendMessage("Successfuly left game n°" + (g.nb + 1) + " !\n");
                                    return true;
                                }
                            } else {
                                sender.sendMessage("You aren't in any games...\n");
                                return false;
                            }

                        }
                    } else {
                        sender.sendMessage("There aren't any games...\n");
                        return false;
                    }
                    break;
                case "hslistgames":
                    sender.sendMessage("There are " + countGames() + " games available games right now.\n");
                    break;
                case "hsstartgame":
                    if (args.length < 3) {
                        sender.sendMessage("Missing arguments...\n");
                        return false;
                    } else if (Integer.parseInt(args[1]) < 200 || Integer.parseInt(args[1]) > 1200) {
                        sender.sendMessage("The time must be contained between 200 and 1200\n");
                        return false;
                    }
                    if (Integer.parseInt(args[2]) < 120 || Integer.parseInt(args[2]) > 800) {
                        sender.sendMessage("The limit must be contained between 120 and 800\n");
                        return false;
                    }
                    if (countGames() >= 1) {
                        if (games[Integer.parseInt(args[0]) - 1].owner != (Player) sender) {
                            sender.sendMessage("You are not the owner of this game...\n");
                            return false;
                        }
                        try {
                            if (!games[Integer.parseInt(args[0]) - 1].start(Integer.parseInt(args[1]), Integer.parseInt(args[2]))) {
                                sender.sendMessage("Couldn't start game, difference between the two teams is too high !\n");
                                return false;
                            } else {
                                sender.sendMessage("Successfully started game n°" + (games[Integer.parseInt(args[0]) - 1].nb + 1) + " !\n");
                                setMysteryChests(games[Integer.parseInt(args[0]) - 1], true);
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else {
                        sender.sendMessage("There is no game available at the moment or you didn't specify the right team option (h | s).\n");
                    }
                    break;
                case "hscancelgame":
                    if (args.length != 1) {
                        sender.sendMessage("You must specify a number\n");
                        return false;
                    }
                    if (games[Integer.parseInt(args[0]) - 1].owner != (Player) sender) {
                        sender.sendMessage("You are not the owner of this game...\n");
                        return false;
                    }
                    sender.sendMessage("Successfully cancelled game n°" + (games[Integer.parseInt(args[0]) - 1].nb + 1) + " !\n");
                    try {
                        cancelGame(games[Integer.parseInt(args[0]) - 1].nb + 1);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case "hsspawn":
                    if(playerInGame((Player) sender)) {
                        for (Game g : games) {
                            if (g != null) {
                                if (g.players.contains((Player) sender)) {
                                    sender.sendMessage(ChatColor.DARK_PURPLE + "Teleporting you to this game spawn in 5 seconds");
                                    BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
                                    scheduler.scheduleSyncDelayedTask(this, new Runnable() {
                                        public void run() {
                                            ((Player) sender).teleport(g.zone);
                                            if(g.t1.players.contains((Player) sender)){
                                                g.announcement(ChatColor.DARK_PURPLE + "[!] " + ChatColor.DARK_GREEN + sender.getName() + ChatColor.DARK_PURPLE + " TELEPORTED TO GAME SPAWN [!]");

                                            }else {
                                                g.announcement(ChatColor.DARK_PURPLE + "[!] " + ChatColor.RED + sender.getName() + ChatColor.DARK_PURPLE + " TELEPORTED TO GAME SPAWN [!]");
                                            }
                                            sender.sendMessage(ChatColor.DARK_PURPLE + "Teleporting you to this game spawn...");
                                        }
                                    }, 100L);
                                    return true;
                                }
                            }
                        }
                    }
                    else{
                        sender.sendMessage(ChatColor.RED + "You are not in any game...");
                    }
                    break;
                case "hslistpl":
                    if(playerInGame((Player) sender)) {
                        for (Game g : games) {
                            if (g != null) {
                                if (g.players.contains((Player) sender)) {
                                    sender.sendMessage(ChatColor.DARK_PURPLE + "PLAYERS IN GAME N°"+(g.nb+1)+" : ");
                                    for(Player p : g.t1.players){
                                        sender.sendMessage((ChatColor.DARK_GREEN + p.getName()));
                                    }
                                    for(Player p : g.t2.players){
                                        sender.sendMessage((ChatColor.RED + p.getName()));
                                    }
                                    return true;
                                }
                            }
                        }
                    }
                    else{
                        sender.sendMessage(ChatColor.RED + "You are not in any game...");
                    }
                    break;
                case "random":
                    if (args.length != 2) {
                        return false;
                    } else if (Integer.parseInt(args[0]) > 10 && Integer.parseInt(args[0]) > 20) {
                        return false;
                    }
                    ArrayList<Location> random = randLocations(((Player) sender).getLocation(), Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                    for (Location l : random) {
                        ArmorStand a = (ArmorStand) ((Player) sender).getWorld().spawnEntity(l, EntityType.ARMOR_STAND);
                        ItemStack skull = new ItemStack(Material.CHEST, 1);
                        a.getEquipment().setHelmet(skull);
                        a.setInvisible(true);
                        a.setCustomNameVisible(true);
                        a.setCustomName(ChatColor.GOLD + "[?]");
                        a.setInvulnerable(true);
                        a.setSmall(true);
                        a.setSilent(true);
                    }
                    break;
                case "saveinv": {
                    try {
                        fm.reloadConfig();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        int count = 0;
                        try {
                            fm.getConfig().set(p.getUniqueId().toString() + ".inventory", null);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        for (ItemStack it : p.getInventory()) {
                            if (it != null) {
                                count++;
                                try {
                                    fm.getConfig().set(p.getUniqueId().toString() + ".inventory." + count, it);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        try {
                            fm.getConfig().set(p.getUniqueId().toString() + ".count", count);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        fm.saveConfig();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
                }
                case "loadinv": {
                    ArrayList<ItemStack> inv = new ArrayList<ItemStack>();
                    try {
                        fm.reloadConfig();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    int nbr = 0;
                    try {
                        nbr = fm.getConfig().getInt(Bukkit.getPlayerExact(args[0]).getUniqueId().toString() + ".count");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    for (int i = 1; i <= nbr; i++) {
                        try {
                            inv.add(fm.getConfig().getItemStack(Bukkit.getPlayerExact(args[0]).getUniqueId().toString() + ".inventory." + i));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }

                    for (ItemStack it : inv) {
                        if (it != null) {
                            ((Player) sender).getInventory().addItem(it);

                        }
                    }
                    break;
                }
            }

        }
        return true;
    }

    public static void saveinv(Player p) throws FileNotFoundException {
        fm.reloadConfig();
        int count = 0;
        fm.getConfig().set(p.getUniqueId().toString() + ".inventory", null);
        for (ItemStack it : p.getInventory()) {
            if (it != null) {
                count++;
                fm.getConfig().set(p.getUniqueId().toString() + ".inventory." + count, it);
            }
        }
        fm.getConfig().set(p.getUniqueId().toString() + ".count", count);
        try {
            fm.saveConfig();
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadinv(Player p) throws FileNotFoundException {
        if (fm.getConfig().contains(p.getUniqueId().toString())) {

            ArrayList<ItemStack> inv = new ArrayList<ItemStack>();
            fm.reloadConfig();
            int nbr = fm.getConfig().getInt(p.getUniqueId().toString() + ".count");
            for (int i = 1; i <= nbr; i++) {
                inv.add(fm.getConfig().getItemStack(p.getUniqueId().toString() + ".inventory." + i));
            }
            fm.getConfig().set(p.getUniqueId().toString() + ".inventory", null);

            for (ItemStack it : inv) {
                if (it != null) {
                    p.getInventory().addItem(it);

                }
            }
        }
    }

    public boolean areaAvailableFor(Location l, int size, @Nullable Player p){
        for(Game g : games) {
            if(g != null) {
                if (l.distance(g.zone) <= size + g.SIZE) {
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
                if (g.players.contains(pl)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int countGames(){
        int count = 0;
        for (Game game : games) {
            if (game != null) {
                count++;
            }
        }
        return count;
    }

    public static void setMysteryChests(Game g, boolean set){
        if(set) {
            ArrayList<Location> random = randLocations(g.zone,g.SIZE*0.1,(int)(g.SIZE*0.01)+6);
            for(Location l : random) {
                if(l.distance(g.zone) <= g.SIZE) {
                    ArmorStand a = (ArmorStand) (l).getWorld().spawnEntity(l, EntityType.ARMOR_STAND);
                    ItemStack skull = new ItemStack(Material.CHEST, 1);
                    a.getEquipment().setHelmet(skull);
                    a.setInvisible(true);
                    a.setCustomNameVisible(true);
                    a.setCustomName(ChatColor.GOLD + "[?]");
                    a.setInvulnerable(true);
                    a.setSmall(true);
                    a.setSilent(true);
                    g.chests.put(a, true);
                }
            }
        }
        else{
            for(ArmorStand a: g.chests.keySet()){
                a.setHealth(0);
                a.remove();
            }
            g.chests.clear();
        }
    }

    public static double randDouble(double min, double max) {
        return min + Math.random() * ((max - min));
    }

    public static ArrayList<Location> randLocations(Location l, double radius, int density){
        Random random = new Random();
        Location[][] Matrice = new Location[density*2][density*2];
        float yaw = 0;
        float pitch = 0;
        Matrice[0][0] = new Location(l.getWorld(),l.getX()-(density*radius),l.getY(),l.getZ()-(density*radius),yaw,pitch);
        int i;
        int j;

        for(i = 0;i < density*2;i++){
            if(i != 0) {
                Matrice[i][0] = new Location(l.getWorld(), Matrice[i - 1][0].getX() + radius, Matrice[i - 1][0].getY(), Matrice[i - 1][0].getZ(), yaw, pitch);
            }
            for(j = 1;j < Matrice[i].length; j++){
                Matrice[i][j] = new Location(l.getWorld(), Matrice[i][j - 1].getX(), Matrice[i][j - 1].getY(), Matrice[i][j - 1].getZ() + radius, yaw, pitch);
                //System.out.println("Line " + i + " Column "+ j+ "/" + Matrice[i].length  + " : " + Matrice[i][j]);

            }
        }

        ArrayList<Location> randLocs = new ArrayList<Location>();
        for(i = 0;i < Matrice.length;i++){
            for(j = 0;j < Matrice[i].length;j++){
                Matrice[i][j].setX(Matrice[i][j].getX()+randDouble(-5,5));
                Matrice[i][j].setY(Matrice[i][j].getY()+randDouble(-5, radius*10));
                Matrice[i][j].setZ(Matrice[i][j].getZ()+randDouble(-5,5));
                if(random.nextBoolean() && Matrice[i][j].getBlock().getType() == Material.AIR){
                    randLocs.add(Matrice[i][j]);
                }
            }
        }
        for(Location loc : randLocs){
            loc.setYaw((float)randDouble(-179,179));
        }
    return randLocs;
    }

    public static void cancelGame(int nb) throws FileNotFoundException {
        if(games[nb-1] != null){
            if(games[nb-1].hasStarted) {
                games[nb - 1].cancel();
            }
            for(Player p : games[nb-1].t1.players){
                p.getInventory().clear();
                loadinv(p);
                /*if (games[nb - 1].savedInventories.get(p) != null) {

                    if (!games[nb - 1].savedInventories.get(p).isEmpty()) {
                        for (ItemStack it : games[nb - 1].savedInventories.get(p).getStorageContents()) {
                            if (it != null) {
                                p.getInventory().addItem(it);
                            }
                        }
                    }
                }*/

                p.setGameMode(GameMode.SURVIVAL);
                p.setInvulnerable(false);
            }
            for(Player p : games[nb-1].t2.players){
                p.getInventory().clear();
                for (PotionEffect effect : p.getActivePotionEffects()) {
                    p.removePotionEffect(effect.getType());
                }
                loadinv(p);
                /*if (games[nb - 1].savedInventories.size() > 0) {
                    if (!games[nb - 1].savedInventories.get(p).isEmpty()) {
                        for (ItemStack it : games[nb - 1].savedInventories.get(p).getStorageContents()) {
                            if (it != null) {
                                p.getInventory().addItem(it);
                            }
                        }
                    }
                }*/

                p.setGameMode(GameMode.SURVIVAL);
                p.setInvulnerable(false);
            }
            setMysteryChests(games[nb-1],false);
            for(Team t : games[nb-1].board.getTeams()) {
                t.unregister();
            }
            games[nb-1].t1.flush();
            games[nb-1].t2.flush();
            games[nb-1].delScoreBoard();
            games[nb-1] = null;
            ArrayUtils.removeElement(games, games[nb-1]);
            System.gc();
        }
    }
}