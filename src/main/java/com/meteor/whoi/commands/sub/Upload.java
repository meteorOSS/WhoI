package com.meteor.whoi.commands.sub;

import com.meteor.whoi.CosUtil;
import com.meteor.whoi.WhoI;
import com.meteor.whoi.commands.Icmd;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;

public class Upload extends Icmd {
    public Upload(WhoI plugin) {
        super(plugin);
    }

    @Override
    public String label() {
        return "upload";
    }

    @Override
    public String getPermission() {
        return "whoi.admin";
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

        Bukkit.getScheduler().runTaskAsynchronously(plugin,()->{
            File file = new File(plugin.getDataFolder()+"/image/");
            plugin.getLogger().info("开始上传资源");
            int i = 0;
            for (File listFile : file.listFiles()) {
                String pokemon = listFile.getName().replace(".png","");
                CosUtil.cosUtil.upload(pokemon);
                i++;
            }
            plugin.getLogger().info("上传资源完成，数:"+i);

        });
    }
}
