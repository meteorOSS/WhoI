package com.meteor.whoi.commands;


import com.meteor.whoi.WhoI;
import com.meteor.whoi.commands.sub.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class CommandManager implements CommandExecutor, TabCompleter {
    private WhoI plugin;
    private Map<String, Icmd> commands;
    public CommandManager(WhoI plugin){
        this.plugin = plugin;
    }

    public void init(){
        this.commands = new HashMap<>();
        register(new Help(plugin));
        register(new Reload(plugin));
        register(new Update(plugin));
        register(new Start(plugin));
        register(new Wi(plugin));
        register(new Upload(plugin));
        register(new Download(plugin));
    }

    public void register(Icmd cmd){
        this.commands.put(cmd.label(),cmd);
    }
    public static List<String> getSugg(final String arg, final List<String> source) {
        if (source == null) {
            return null;
        }
        final List<String> ret = new ArrayList<String>();
        final List<String> sugg = new ArrayList<String>(source);
        StringUtil.copyPartialMatches(arg, (Iterable)sugg, (Collection)ret);
        Collections.sort(ret);
        return ret;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Icmd icmd = commands.get("help");
        if (args.length > 0 && this.commands.containsKey(args[0])) {
            icmd = commands.get(args[0]);
        }
        icmd.execute(sender,args);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(!(sender instanceof Player)){
            return null;
        }
        if(args.length<=0){
            return null;
        }
        if(args.length==1){
            List<String> sugg = new ArrayList<>();
            for(Icmd c : commands.values()){
                if (c.hasPerm(sender)) {
                    sugg.remove(c.label());
                }
            }
            return getSugg(args[0],sugg);
        }
        Icmd icmd = commands.get(args[0]);
        if(icmd==null){
            return new ArrayList<>(commands.keySet());
        }
        List<String> list = icmd.getTab((Player)sender,args.length-1,args);
        return getSugg(args[args.length-1],list);
    }
}
