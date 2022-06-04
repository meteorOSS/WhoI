package com.meteor.whoi.commands.sub;

import com.meteor.whoi.WhoI;
import com.meteor.whoi.commands.Icmd;
import org.bukkit.command.CommandSender;

public class Start extends Icmd {
    public Start(WhoI plugin) {
        super(plugin);
    }

    @Override
    public String label() {
        return "start";
    }

    @Override
    public String getPermission() {
        return "whoi.admin.start";
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
        plugin.getWhoIHandler().getBukkitTask().cancel();
        plugin.getWhoIHandler().randomPokemon();
        plugin.getWhoIHandler().startRandomPokemon();
    }
}
