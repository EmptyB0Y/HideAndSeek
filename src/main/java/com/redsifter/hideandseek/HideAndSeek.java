package com.redsifter.hideandseek;

import com.redsifter.hideandseek.listeners.Listen;
import com.redsifter.hideandseek.utils.FileManager;
import com.redsifter.hideandseek.utils.Game;
import com.redsifter.hideandseek.utils.CustomTeam;
import net.md_5.bungee.api.chat.*;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public final class HideAndSeek extends JavaPlugin {

    public static int MAXPLAYERS = 10;
    public static int MAXNUMBER = 3;
    public static int MINTIME = 200;
    public static int MAXTIME = 1060;
    public static int MINLIMIT = 120;
    public static int MAXLIMIT = 300;
    public static Game[] games = new Game[MAXNUMBER];
    public static FileManager fm;
    public static FileManager config;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(new Listen(this), this);
        getDataFolder().mkdir();
        try {
            fm =  new FileManager("inventories.yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Initializing config
        this.getLogger().info("Initializing config");
        this.saveDefaultConfig();

        if(this.getConfig().contains("MAXPLAYERS")){
            if(this.getConfig().get("MAXPLAYERS") != null) {
                MAXPLAYERS = this.getConfig().getInt("MAXPLAYERS");
            }
        }
        this.getLogger().info("MAXPLAYERS : "+MAXPLAYERS);

        if(this.getConfig().contains("MAXNUMBER")){
            if(this.getConfig().get("MAXNUMBER") != null) {
                MAXNUMBER = this.getConfig().getInt("MAXNUMBER");
            }
        }
        this.getLogger().info("MAXNUMBER : "+MAXNUMBER);

        if(this.getConfig().contains("MINTIME")){
            if(this.getConfig().get("MINTIME") != null) {
                MINTIME = this.getConfig().getInt("MINTIME");
            }
        }
        this.getLogger().info("MINTIME : "+MINTIME);

        if(this.getConfig().contains("MAXTIME")){
            if(this.getConfig().get("MAXTIME") != null) {
                MAXTIME = this.getConfig().getInt("MAXTIME");
            }
        }
        this.getLogger().info("MAXTIME : "+MAXTIME);

        if(this.getConfig().contains("MINLIMIT")){
            if(this.getConfig().get("MINLIMIT") != null) {
                MINLIMIT = this.getConfig().getInt("MINLIMIT");
            }
        }
        this.getLogger().info("MINLIMIT: "+MINLIMIT);

        if(this.getConfig().contains("MAXLIMIT")){
            if(this.getConfig().get("MAXLIMIT") != null) {
                MAXLIMIT = this.getConfig().getInt("MAXLIMIT");
            }
        }
        this.getLogger().info("MAXLIMIT : "+MAXLIMIT);

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

    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String label, @Nonnull String[] args) {
        if (sender instanceof Player) {
            switch (label) {
                case "hssetgame":
                    if (args.length > 1) {
                        sender.sendMessage("Too much arguments...\n");
                        return false;
                    }
                    if (countGames() == MAXNUMBER) {
                        sender.sendMessage("Too much games started at this time...\n");
                        return false;
                    }
                    //ADD TEAMS
                    CustomTeam t1 = new CustomTeam(1, "HIDERS");
                    CustomTeam t2 = new CustomTeam(2, "SEEKERS");

                    //INITALIZE GAME
                    int game = 0;
                    if (countGames() > 0) {
                        game = countGames() + 1;
                    }

                    games[game] = new Game(t1, t2, game, (Player) sender, this);
                    if(args.length > 0) {
                        if(args[0].equals("predator")){
                            games[game].mode = "predator";
                            sender.sendMessage("You initalized game n° " + countGames() + " in predator mode !\n");
                            return true;
                        }
                    }

                    sender.sendMessage("You initalized game n° " + countGames() + " !\n");
                    TextComponent rand = new TextComponent("[JOIN AS RANDOM]");
                    rand.setColor(net.md_5.bungee.api.ChatColor.GRAY);
                    rand.setBold(true);
                    rand.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/hsjoin "+(game+1)+" r"));
                    TextComponent hider = new TextComponent("[JOIN AS HIDER]");
                    hider.setColor(net.md_5.bungee.api.ChatColor.DARK_GREEN);
                    hider.setBold(true);
                    hider.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/hsjoin "+(game+1)+" h"));
                    TextComponent seeker = new TextComponent("[JOIN AS SEEKER]");
                    seeker.setColor(net.md_5.bungee.api.ChatColor.RED);
                    seeker.setBold(true);
                    seeker.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/hsjoin "+(game+1)+" s"));
                    TextComponent startgame = new TextComponent("[START GAME]");
                    startgame.setColor(net.md_5.bungee.api.ChatColor.DARK_PURPLE);
                    startgame.setBold(true);
                    startgame.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/hsstartgame "+(game+1)+" 560 120"));

                    for(Player pl : Bukkit.getOnlinePlayers()) {
                        pl.sendMessage(ChatColor.GOLD+"A NEW GAME OF HIDE AND SEEK HAS BEEN SET BY "+ChatColor.DARK_PURPLE+sender.getName()+ChatColor.GOLD+" :");
                        pl.sendMessage(rand);
                        pl.sendMessage(hider);
                        pl.sendMessage(seeker);
                    }
                    sender.sendMessage(ChatColor.DARK_PURPLE+"}-----------{");
                    sender.sendMessage(startgame);
                    sender.sendMessage("(default value hsstartgame "+(game+1)+" 560 120)");
                    sender.sendMessage(ChatColor.DARK_PURPLE+"}-----------{");
                    break;
                case "hsjoin":
                    if (args.length < 1) {
                        sender.sendMessage("Missing arguments...\n");
                        return false;
                    } else if (args.length == 2) {

                        if (Integer.parseInt(args[0]) > MAXNUMBER || Integer.parseInt(args[0]) < 1) {
                            sender.sendMessage("Not a valid game number (1 to "+MAXNUMBER+")\n");
                            return false;
                        }

                        Player p = (Player) sender;
                        if (games[(Integer.parseInt(args[0]) - 1)] == null) {
                            return false;
                        }
                        if (!games[(Integer.parseInt(args[0]) - 1)].hasStarted && !games[(Integer.parseInt(args[0]) - 1)].full && !games[(Integer.parseInt(args[0]) - 1)].players.contains(p)) {
                            if (args[1].equals("h")) {
                                if (games[(Integer.parseInt(args[0]) - 1)].addPlayer((Player) sender, "h")) {
                                    sender.sendMessage("Successfuly joined game n° " + (games[Integer.parseInt(args[0]) - 1].nb + 1) + " as hider !\n");
                                    return true;
                                }
                                else {
                                    sender.sendMessage("Failed to join game n° " + (games[(Integer.parseInt(args[0]) - 1)].nb + 1) + "...");
                                }
                            } else if (args[1].equals("s")) {
                                if (games[(Integer.parseInt(args[0]) - 1)].addPlayer((Player) sender, "s")) {
                                    sender.sendMessage("Successfuly joined game n° " + (games[Integer.parseInt(args[0]) - 1].nb + 1) + " as seeker !\n");
                                    return true;
                                }
                                else {
                                    sender.sendMessage("Failed to join game n° " + (games[(Integer.parseInt(args[0]) - 1)].nb + 1) + "...");
                                }
                            }
                            else if (args[1].equals("r")) {
                                if (games[(Integer.parseInt(args[0]) - 1)].addPlayer((Player) sender, "r")) {
                                    sender.sendMessage("Successfuly joined game n°" + (games[(Integer.parseInt(args[0]) - 1)].nb + 1) + " as random !\n");
                                    return true;
                                }
                                else {
                                    sender.sendMessage("Failed to join game n° " + (games[(Integer.parseInt(args[0]) - 1)].nb + 1) + "...");
                                }
                            }

                        }
                        else {
                            sender.sendMessage(ChatColor.RED + "This game either has already started, is not set, or has no more room for you in the selected team !\n");
                            return false;
                        }
                    } else if (args.length == 1) {
                        Player p = (Player) sender;
                        if (countGames() >= 1) {
                            for (Game g : games) {
                                if (g != null) {
                                    if (!g.hasStarted && !g.full && !g.players.contains(p)) {
                                        if (args[0].equals("h")) {
                                            if (g.addPlayer((Player) sender, "h")) {
                                                sender.sendMessage("Successfuly joined game n° " + (g.nb + 1) + " as hider !\n");
                                                return true;
                                            }
                                            else {
                                                sender.sendMessage("Failed to join game n° " + (g.nb + 1) + "...");
                                            }

                                        }
                                        else if (args[0].equals("s")) {

                                            if (g.addPlayer((Player) sender, "s")) {
                                                sender.sendMessage("Successfuly joined game n°" + (g.nb + 1) + " as seeker !\n");
                                                return true;
                                            }
                                            else {
                                                sender.sendMessage("Failed to join game n° " + (g.nb + 1) + "...");
                                            }
                                        }
                                        else if (args[0].equals("r")) {
                                            if (g.addPlayer((Player) sender, "r")) {
                                                sender.sendMessage("Successfuly joined game n°" + (g.nb + 1) + " as random !\n");
                                                return true;
                                            }
                                            else {
                                                sender.sendMessage("Failed to join game n° " + (g.nb + 1) + "...");
                                            }
                                        }
                                        else {
                                            sender.sendMessage("Choose between [h] (hider), [s] (seeker) and [r] (random)\n");
                                        }
                                    }
                                }
                            }
                            sender.sendMessage("Failed to join any games so far, try /hslistgames to see available games.\n");
                            return false;
                        }
                    } else {
                        sender.sendMessage("Usage : hsjoin <(h | s)> [n°]");
                    }

                    break;
                case "hsleave":
                    if (countGames() >= 1) {
                        for (Game g : games) {
                            if(g != null) {
                                if (g.players.contains((Player) sender) || g.specs.contains((Player) sender)) {
                                    try {
                                        if (g.remPlayer((Player) sender, false)) {
                                            sender.sendMessage("Successfuly left game n°" + (g.nb + 1) + " !\n");
                                            return true;
                                        }
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    sender.sendMessage("You aren't in any games...\n");
                                    return false;
                                }
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
                    }
                    else if (Integer.parseInt(args[1]) < MINTIME || Integer.parseInt(args[1]) > MAXTIME) {
                        sender.sendMessage("The timer must be contained between "+MINTIME+" and "+MAXTIME);
                        return false;
                    }
                    if (Integer.parseInt(args[2]) < MINLIMIT || Integer.parseInt(args[2]) > MAXLIMIT) {
                        sender.sendMessage("The limit must be contained between "+MINLIMIT+ " and "+MAXLIMIT);
                        return false;
                    }
                    else if(!areaAvailableFor(games[Integer.parseInt(args[0]) - 1].zone,Integer.parseInt(args[2]),(Player) sender)){
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
                                if(games[Integer.parseInt(args[0]) - 1].mode.equals("normal")) {
                                    setMysteryChests(games[Integer.parseInt(args[0]) - 1], true);
                                }
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
                                                g.announcement(ChatColor.DARK_PURPLE + "[!] " + ChatColor.DARK_GREEN + sender.getName() + ChatColor.DARK_PURPLE + " TELEPORTED TO GAME SPAWN [!]",false);

                                            }else {
                                                g.announcement(ChatColor.DARK_PURPLE + "[!] " + ChatColor.RED + sender.getName() + ChatColor.DARK_PURPLE + " TELEPORTED TO GAME SPAWN [!]",false);
                                            }
                                            sender.sendMessage(ChatColor.DARK_PURPLE + "Teleporting you to this game spawn...");
                                        }
                                    }, 100L);
                                    return true;
                                }
                                else if(g.specs.contains((Player) sender)){
                                    ((Player) sender).teleport(g.zone);
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
                                if (g.players.contains((Player) sender) || g.specs.contains((Player) sender)){
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
            try {
                fm.saveConfig();
            } catch (
                    IOException e) {
                e.printStackTrace();
            }
            for (ItemStack it : inv) {
                if (it != null) {
                    p.getInventory().addItem(it);

                }
            }
        }
    }

    public boolean areaAvailableFor(Location l, int size, Player p){
        for(Game g : games) {
            if(g != null) {
                if (l.distance(g.zone) <= size + g.SIZE && g.owner != p) {
                    p.sendMessage("There is no room for a game here ! Try to get a bit further from " + g.zone.getX() + " " + g.zone.getY() + " " + g.zone.getZ());
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

            for(Player p : games[nb-1].specs){
                p.teleport(games[nb-1].zone);
                p.setGameMode(GameMode.SURVIVAL);
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