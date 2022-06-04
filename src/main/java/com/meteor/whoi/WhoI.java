package com.meteor.whoi;

import com.meteor.meteorlib.nbt.NBTItem;
import com.meteor.whoi.commands.CommandManager;
import com.meteor.whoi.storage.ExceptionColl;
import com.meteor.whoi.storage.IStorage;
import com.meteor.whoi.storage.MysqlStorage;
import com.meteor.whoi.storage.YamlStorage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public final class WhoI extends JavaPlugin{

    private IStorage iStorage;
    private WhoIHandler whoIHandler;
    private Metrics metrics;
    private CommandManager commandManager;




    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        boolean bl = true;
        if(bl){
            ExceptionColl.init(this);
            iStorage = getConfig().getBoolean("mysql-info.enable",false)?new MysqlStorage(this):new YamlStorage(this);
            metrics = new Metrics(this,15192);
            whoIHandler = new WhoIHandler(this);
            CosUtil.init(this);
            (commandManager = new CommandManager(this)).init();
            getCommand("whoi").setExecutor(commandManager);
            getLogger().info(this::getLogo);
            update();
            getLogger().info("已通过验证,使用问题联系qq2260483272");           Config.init(this);
        }else {
            getLogger().info("未通过验证，逻辑未正常执行，联系qq653440235购买cdk");
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getiStorage().close();
        ExceptionColl.getInstance.close();
        getWhoIHandler().close();
        getLogger().info("插件已卸载");
    }

    public WhoIHandler getWhoIHandler() {
        return whoIHandler;
    }

    private void update(){
        getLogger().info("正在进行更新验证");
        getLogger().info("当前插件版本: " + getDescription().getVersion());
        CosUtil.cosUtil.downloadFile("update.yml");
        File file = new File(getDataFolder()+"/update.yml");
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        getLogger().info("最新版本: "+yamlConfiguration.getString("version"));
        getLogger().info("更新内容:");
        yamlConfiguration.getStringList("update").forEach(s->{
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',s));
        });
        if(yamlConfiguration.getStringList("ads")!=null){
            yamlConfiguration.getStringList("ads").forEach(s->{
                Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',s));
            });
        }
        if(!getDescription().getVersion().equalsIgnoreCase(yamlConfiguration.getString("version"))){
            getLogger().info(" ");
            getLogger().info("当前插件并不是最新版，加群653440235获取更新");
        }
    }



    private String getLogo(){
        return " __          ___    _  ____ _____ ____   ___  \n" +
                " \\ \\        / / |  | |/ __ \\_   _|___ \\ / _ \\ \n" +
                "  \\ \\  /\\  / /| |__| | |  | || |   __) | | | |\n" +
                "   \\ \\/  \\/ / |  __  | |  | || |  |__ <| | | |\n" +
                "    \\  /\\  /  | |  | | |__| || |_ ___) | |_| |\n" +
                "     \\/  \\/   |_|  |_|\\____/_____|____(_)___/ \n" +
                "                                              \n" +
                "                                              ";
    }

    public IStorage getiStorage() {
        return iStorage;
    }
}
