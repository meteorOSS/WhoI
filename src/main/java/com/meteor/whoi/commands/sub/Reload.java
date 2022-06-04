package com.meteor.whoi.commands.sub;

import com.meteor.whoi.Config;
import com.meteor.whoi.WhoI;
import com.meteor.whoi.commands.Icmd;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class Reload extends Icmd {
    public Reload(WhoI plugin) {
        super(plugin);
    }

    @Override
    public String label() {
        return "reload";
    }

    @Override
    public String getPermission() {
        return "whoi.admin.reload";
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
        Config.config.reload();
        plugin.getWhoIHandler().reload();
        p0.sendMessage(Config.config.getMessageManager().getString("message.reload"));
    }
}
