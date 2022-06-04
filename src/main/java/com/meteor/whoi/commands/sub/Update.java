package com.meteor.whoi.commands.sub;

import com.meteor.whoi.Config;
import com.meteor.whoi.WhoI;
import com.meteor.whoi.commands.Icmd;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class Update extends Icmd {
    public Update(WhoI plugin) {
        super(plugin);
    }

    @Override
    public String label() {
        return "update";
    }

    @Override
    public String getPermission() {
        return "whoi.admin.update";
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
        Bukkit.getScheduler().runTaskAsynchronously(plugin,()->plugin.getiStorage().doGetPokemonIndex());
        p0.sendMessage(Config.config.getMessageManager().getString("message.update-index"));
    }
}
