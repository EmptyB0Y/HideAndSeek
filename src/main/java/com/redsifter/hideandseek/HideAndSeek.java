package com.redsifter.hideandseek;

import com.redsifter.hideandseek.listeners.Listen;
import com.redsifter.hideandseek.utils.Game;
import com.redsifter.hideandseek.utils.Team;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

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
                Team t1 = new Team(1,"hiders");
                Team t2 = new Team(2,"seekers");
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
                                if (!playerInGame(Bukkit.getPlayerExact(str)) && Bukkit.getOnlinePlayers().contains(Bukkit.getPlayerExact(str))) {
                                    lst.add(Bukkit.getPlayerExact(str));
                                    str = "";
                                } else {
                                    sender.sendMessage(str + " is already in a game or offline!\n");
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
                                if (!playerInGame(Bukkit.getPlayerExact(str)) && !t1.players.contains(Bukkit.getPlayerExact(str)) && Bukkit.getOnlinePlayers().contains(Bukkit.getPlayerExact(str))) {
                                    lst.add(Bukkit.getPlayerExact(str));
                                    str = "";
                                } else {
                                    sender.sendMessage(str + " is already in a game, in the other team or offline\n");
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

                    if (games[Integer.parseInt(args[1])-1] != null && !games[Integer.parseInt(args[1])].hasStarted) {
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
                        sender.sendMessage("This game has already started or is not set !\n");
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
                sender.sendMessage("Successfully cancelled game n°" + games[Integer.parseInt(args[0])-1].nb + " !\n");
                cancelGame(games[Integer.parseInt(args[0])-1].nb);
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
        System.out.println(count);
        return count;
    }

    public void cancelGame(int nb){
        if(games[nb-1] != null){
            if(!games[nb-1].isCancelled()){
                games[nb-1].cancel();
                games[nb-1].t1.flush();
                games[nb-1].t2.flush();
                games[nb-1] = null;
                ArrayUtils.removeElement(games, games[nb-1]);
            }
        }
    }
}