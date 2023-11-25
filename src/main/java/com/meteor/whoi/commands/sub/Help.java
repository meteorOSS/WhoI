package com.meteor.whoi.commands.sub;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.meteor.whoi.Config;
import com.meteor.whoi.WhoI;
import com.meteor.whoi.commands.Icmd;
import net.minecraft.server.v1_12_R1.MapIcon;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PacketPlayOutMap;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapPalette;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;

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
