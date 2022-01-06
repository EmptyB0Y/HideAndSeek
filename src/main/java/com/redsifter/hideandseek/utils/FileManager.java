package com.redsifter.hideandseek.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;

public class FileManager {
    private FileConfiguration dataConfig = null;
    private File configFile;

    public FileManager(){
    saveDefaultConfig();
    }

    public void reloadConfig() throws FileNotFoundException {
        if (this.configFile == null) {
            this.configFile = new File ("plugins/HideAndSeek/", "inventories.yml");
        }
        this.dataConfig= YamlConfiguration.loadConfiguration(this.configFile);
        FileInputStream defaultStream = new FileInputStream("plugins/HideAndSeek/inventories.yml");
        if(defaultStream != null){
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            this.dataConfig.setDefaults(defaultConfig);
        }
    }

    public FileConfiguration getConfig() throws FileNotFoundException {
        if(this.dataConfig == null){
            reloadConfig();
        }
        return this.dataConfig;
    }

    public void saveConfig() throws IOException {
        if(this.dataConfig == null || this.configFile == null){
            return;
        }
        this.getConfig().save(this.configFile);
    }

    public void saveDefaultConfig(){
        if(this.configFile == null || !this.configFile.exists()){
            this.configFile = new File("plugins/HideAndSeek/", "inventories.yml");
        }
    }


    /*public void saveInventories(String fileName,String node,ArrayList<Player> data) throws IOException {
        /*File dir = new File("plugins\\HideAndSeek");
        if(!dir.exists()){
            dir.mkdir();
        }
        if (!fileName.endsWith(".yml")) {
            fileName = fileName + ".yml";
        }
        config = new File(main.getDataFolder(),fileName);
        if (!config.exists()) {
            config.createNewFile();
        }

        YamlConfiguration yml = YamlConfiguration.loadConfiguration(config);
        InputStream defaultStream = main.getResource(fileName);
        if(defaultStream != null){
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            config.setDefaults(defaultConfig);
        }
        for(Player p : data){
            String path = node + "."+p.getName();
            System.out.println(path);
            yml.createSection(path);
            for(ItemStack it : p.getInventory()) {
                yml.set(path, it);
            }
        }
        yml.save(config);
    }

    public Inventory loadInventories(String filePath, UUID uuid) {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ItemStack[] items = (ItemStack[]) config.get("Inventories"+uuid.toString());
        Inventory data = null;
        for(ItemStack it : items){
            data.addItem(it);
        }
        return data;
    }
    public Location loadLocation(String filePath,String node, String name) {
        if (!filePath.endsWith(".yml")) {
            filePath = filePath + ".yml";
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        Location data = config.getLocation(node+"."+name);
        return data;
    }*/
}
