package com.meteor.whoi.commands.sub;

import com.meteor.whoi.Config;
import com.meteor.whoi.WhoI;
import com.meteor.whoi.commands.Icmd;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Wi extends Icmd {
    public Wi(WhoI plugin) {
        super(plugin);
    }

    @Override
    public String label() {
        return "wi";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean playersOnly() {
        return true;
    }

    @Override
    public String usage() {
        return null;
    }

    @Override
    public void perform(CommandSender p0, String[] p1) {

        Player player = (Player) p0;
        if(player.getGameMode() != GameMode.SURVIVAL){
            player.sendMessage(Config.config.getMessageManager().getString("message.must-survival"));
            return;
        }

        if(plugin.getWhoIHandler().getPokemonData()!=null&&!plugin.getWhoIHandler().getPokemonData().isAlreadyOpen())
            plugin.getWhoIHandler().joinGame((Player)p0);
    }
}
