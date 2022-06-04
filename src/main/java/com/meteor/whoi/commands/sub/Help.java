package com.meteor.whoi.commands.sub;

import com.meteor.whoi.Config;
import com.meteor.whoi.WhoI;
import com.meteor.whoi.commands.Icmd;
import org.bukkit.command.CommandSender;

public class Help extends Icmd {
    public Help(WhoI plugin) {
        super(plugin);
    }

    @Override
    public String label() {
        return "help";
    }

    @Override
    public String getPermission() {
        return "whoi.use.help";
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
        Config.config.getMessageManager().getStringList("message.help").forEach(s->p0.sendMessage(s));
    }
}
