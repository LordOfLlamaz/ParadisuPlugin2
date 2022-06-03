package me.jakedadream.ParadisuPlugin;


import me.jakedadream.ParadisuPlugin.items.ItemCommands;
import me.jakedadream.ParadisuPlugin.items.models.modelscroller.ModelScrollerEvents;
import me.jakedadream.ParadisuPlugin.playerdata.PlayerDataEvents;
import me.jakedadream.ParadisuPlugin.commands.EssentialCommands;
import me.jakedadream.ParadisuPlugin.commands.ParadisuCommands;
import me.jakedadream.ParadisuPlugin.events.*;
import me.jakedadream.ParadisuPlugin.items.SpinningCoins;
import me.jakedadream.ParadisuPlugin.items.ToyEvents;
import me.jakedadream.ParadisuPlugin.items.models.ModelCommands;
import me.jakedadream.ParadisuPlugin.items.models.ModelGiveInv;
import me.jakedadream.ParadisuPlugin.items.models.ModelItemManager;
import me.jakedadream.ParadisuPlugin.shops.ShopCommands;
import me.jakedadream.ParadisuPlugin.shops.ShopGuis;
import me.jakedadream.ParadisuPlugin.util.DatabaseConnection;
import me.jakedadream.ParadisuPlugin.warps.TeleportationCommands;
import me.jakedadream.ParadisuPlugin.warps.WarpCommands;
import me.jakedadream.ParadisuPlugin.warps.WarpsDataHandler;
import me.jakedadream.ParadisuPlugin.util.TimeZone;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import cloud.commandframework.CommandManager;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.paper.PaperCommandManager;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.function.Function;

import javax.sql.DataSource;

public class ParadisuMain extends JavaPlugin {


    /*
     _____                    _ _             _____  _             _
    |  __ \                  | (_)           |  __ \| |           (_)
    | |__) |_ _ _ __ __ _  __| |_ ___ _   _  | |__) | |_   _  __ _ _ _ __
    |  ___/ _` | '__/ _` |/ _` | / __| | | | |  ___/| | | | |/ _` | | '_ \
    | |  | (_| | | | (_| | (_| | \__ \ |_| | | |    | | |_| | (_| | | | | |
    |_|   \__,_|_|  \__,_|\__,_|_|___/\__,_| |_|    |_|\__,_|\__, |_|_| |_|
                                                              __/ |
                                                             |___/
        __                 __      __           ___        ______      __
       / /_  __  __       / /___ _/ /_____     ( _ )      / ____/_  __/ /_____
      / __ \/ / / /  __  / / __ `/ //_/ _ \   / __ \/|   / /   / / / / __/ __ \
     / /_/ / /_/ /  / /_/ / /_/ / ,< /  __/  / /_/  <   / /___/ /_/ / /_/ /_/ /
    /_.___/\__, /   \____/\__,_/_/|_|\___/   \____/\/   \____/\__, /\__/\____/
          /____/                                             /____/        */



    public static String CommandPrefix() { String cmdprefix = "\uE016 "; return cmdprefix; }
    public static String CommandEmph() { String cmdemph = "§x§f§d§d§0§2§3"; return cmdemph; }
    public static String NoPermsMessage() { String nopermsmsg = "\uE016 §fYou do not have permission to use that command."; return nopermsmsg; }
    public static String NoArgsMessage() { String noargsmsg = "\uE016 §fNot enough arguments provided."; return noargsmsg; }
    public static String PlayerCooldownMessage() { String cooldownmsg = "\uE016 §fYou are currently on a §x§f§d§d§0§2§3cooldown§f." ; return cooldownmsg; }

    private static DataSource dataSource;
    public static CommandManager<CommandSender> manager;
    public static AnnotationParser<CommandSender> annotationParser;

    @Override
    public void onEnable() {

        
        createEnvFiles();
        saveEnvConfig();

        try {
            dataSource = DatabaseConnection.initParadisuSQLCon();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            manager = new PaperCommandManager<>(this, CommandExecutionCoordinator.simpleCoordinator(), Function.identity(), Function.identity());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        annotationParser = new AnnotationParser<>(
            manager,
            CommandSender.class,
            parameters -> SimpleCommandMeta.empty() 
        );

        

        annotationParser.parse(new WarpCommands());
        annotationParser.parse(new EssentialCommands());
        annotationParser.parse(new ParadisuCommands());
        annotationParser.parse(new ItemCommands());



        // =================
        // TPING COMMANDS
        // =================
        getCommand("tphere").setExecutor(new TeleportationCommands());
        getCommand("tp").setExecutor(new TeleportationCommands());
        getCommand("cordstp").setExecutor(new TeleportationCommands());
        //
        // =================
        // WARPS COMMANDS
        // =================
        

        WarpsDataHandler.updateWarpData();

        // =================
        // MODEL COMMANDS
        // =================
        getCommand("mgive").setExecutor(new ModelCommands());
        getCommand("hgive").setExecutor(new ModelCommands());
        getCommand("mhat").setExecutor(new ModelCommands());
        
        // =================
        // SHOP GUI COMMANDS
        // =================
        //
        getCommand("reloadshops").setExecutor(new ShopCommands());
        getCommand("getshop").setExecutor(new ShopCommands());
        //
        // =================
        // SHOP GUI COMMANDS
        // =================
        //
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        createShopGuiFiles();
        saveShopGuiConfig();

        ModelItemManager.updateModelData();
        ModelGiveInv.createAllInvs();
        ShopGuis.initShops();
        TimeZone.setJapanTime();

        // =================
        // EVENTS
        // =================
        getServer().getPluginManager().registerEvents(new ToyEvents(), this);
        getServer().getPluginManager().registerEvents(new SnwEvents(), this);
        getServer().getPluginManager().registerEvents(new GuiListeners(), this);
        getServer().getPluginManager().registerEvents(new PlayerDataEvents(), this);
        getServer().getPluginManager().registerEvents(new ModelScrollerEvents(), this);
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "\n" + ChatColor.GREEN +
                "|------------------------------|\n" + ChatColor.GREEN +
                "| [Paradisu] Plugin now Active |\n" + ChatColor.GREEN +
                "|------------------------------|");

        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                SpinningCoins.spineffect();

            }
        }, 0L, 2L);

    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Paradisu] Plugin is now disabled.");
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Paradisu] Goodbye!");
    }

    private static File envValues;
    private static FileConfiguration envConfig;
    
    private static File sourceShopGuiFile;
    private static FileConfiguration fileShopGuiConfig;

    public void createEnvFiles(){
        envValues = new File(getDataFolder(), "env.yml");
        if (!envValues.exists()) {
            try {
                envValues.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        envConfig = YamlConfiguration.loadConfiguration(envValues);
    }


    public void createShopGuiFiles(){
        sourceShopGuiFile = new File(getDataFolder(), "shopgui.yml");

        if (!sourceShopGuiFile.exists()) {
            sourceShopGuiFile.getParentFile().mkdirs();
            saveResource("shopgui.yml", false);
        }

        fileShopGuiConfig = new YamlConfiguration();

        try {
            fileShopGuiConfig.load(sourceShopGuiFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }


    public static FileConfiguration getEnvConfig(){
        return envConfig;
    }
    
    public static FileConfiguration getFileShopGuiConfig() {return fileShopGuiConfig; }


    public static void saveEnvConfig(){
        try {
            envConfig.save(envValues);
        } catch (IOException e){
            System.out.println("couldn't save file");
        }
    }
   
    public static void saveShopGuiConfig() {
        try {
            fileShopGuiConfig.save(sourceShopGuiFile);
        } catch (IOException e) {
            System.out.println("couldn't save file");
        }
    }

//    public static void reloadEnvConfig(){
//        envConfig = YamlConfiguration.loadConfiguration(envValues);
//    }

    public static void reloadShopGuiConfig() { fileShopGuiConfig = YamlConfiguration.loadConfiguration(sourceShopGuiFile); }
    public static DataSource getDBCon(){return dataSource;}
}
