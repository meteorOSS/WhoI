package com.meteor.whoi.commands.sub;

import com.meteor.whoi.WhoI;
import com.meteor.whoi.commands.Icmd;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class Download extends Icmd {
    public Download(WhoI plugin) {
        super(plugin);
    }

    @Override
    public String label() {
        return "download";
    }

    @Override
    public String getPermission() {
        return "whoi.admin.download";
    }

    @Override
    public boolean playersOnly() {
        return false;
    }

    @Override
    public String usage() {
        return null;
    }

    @Override
    public void perform(CommandSender p0, String[] p1) {
        plugin.getiStorage().doGetPokemonIndex();
        int amount = Integer.parseInt(p1[1]);
        Bukkit.getScheduler().runTaskAsynchronously(plugin,()->{
            int errorAmount = 0;
            for(int i=1;i<=amount;++i){
                boolean cur = plugin.getiStorage().initPokemonFile(i);
                if(!cur)
                    errorAmount++;
            }
            plugin.getConfig().set("pokemon-index-count",amount+1);
            plugin.saveConfig();
            plugin.getLogger().info("已爬取完成,爬取失败资源数量:"+errorAmount);
        });
    }
}
