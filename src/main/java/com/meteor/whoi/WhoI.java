package com.meteor.whoi;

import com.meteor.whoi.commands.CommandManager;
import com.meteor.whoi.storage.ExceptionColl;
import com.meteor.whoi.storage.IStorage;
import com.meteor.whoi.storage.YamlStorage;
import org.bukkit.plugin.java.JavaPlugin;

public final class WhoI extends JavaPlugin{

    private IStorage iStorage;
    private WhoIHandler whoIHandler;
    private Metrics metrics;
    private CommandManager commandManager;


    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
//        TheOriginalCheckUtil.theOriginalCheck(this)
        boolean vek = true;
        if(vek){
            ExceptionColl.init(this);
            iStorage = new YamlStorage(this);
            metrics = new Metrics(this,15192);
            whoIHandler = new WhoIHandler(this);
            (commandManager = new CommandManager(this)).init();
            getCommand("whoi").setExecutor(commandManager);
            getLogger().info(this::getLogo);
            Config.init(this);
            getLogger().info("插件问题反馈群: 653440235");
        }else {
//            getLogger().info("未通过验证，逻辑未正常执行，联系qq653440235购买cdk");
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




    private String getLogo(){
        return "                                                              \n" +
                "                                                              \n" +
                "                          .---.  ,---,                        \n" +
                "                         /. ./|,--.' |                ,--,    \n" +
                "                     .--'.  ' ;|  |  :       ,---.  ,--.'|    \n" +
                "                    /__./ \\ : |:  :  :      '   ,'\\ |  |,     \n" +
                "                .--'.  '   \\' .:  |  |,--. /   /   |`--'_     \n" +
                "               /___/ \\ |    ' '|  :  '   |.   ; ,. :,' ,'|    \n" +
                "               ;   \\  \\;      :|  |   /' :'   | |: :'  | |    \n" +
                "                \\   ;  `      |'  :  | | |'   | .; :|  | :    \n" +
                "                 .   \\    .\\  ;|  |  ' | :|   :    |'  : |__  \n" +
                "                  \\   \\   ' \\ ||  :  :_:,' \\   \\  / |  | '.'| \n" +
                "                   :   '  |--\" |  | ,'      `----'  ;  :    ; \n" +
                "                    \\   \\ ;    `--''                |  ,   /  \n" +
                "                     '---\"                           ---`-'   \n" +
                "version: 2.3.6  author: meteor 接中小型插件,MOD定制 qq2260483272\n" +
                "                                                              ";
    }

    public IStorage getiStorage() {
        return iStorage;
    }
}
