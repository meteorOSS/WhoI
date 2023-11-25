package com.meteor.whoi;

import com.comphenix.packetwrapper.WrapperPlayServerMap;
import com.comphenix.packetwrapper.WrapperPlayServerWindowItems;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.meteor.whoi.api.event.GameStartEvent;
import com.meteor.whoi.api.event.GameUpdateEvent;
import com.meteor.whoi.data.PokemonData;

import com.meteor.whoi.nms.Nms;
import com.meteor.whoi.nms.v1_12_R1;
import com.meteor.whoi.nms.v1_16_R3;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.TextComponent;

import net.minecraft.server.v1_12_R1.NonNullList;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_16_R3.MapIcon;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitTask;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.bukkit.event.EventPriority.LOWEST;
import static org.bukkit.event.EventPriority.MONITOR;


class WhoIThread implements Runnable{
    WhoIHandler whoIHandler;

    public WhoIThread(WhoIHandler whoIHandler) {
        this.whoIHandler = whoIHandler;
    }

    @Override
    public void run() {
        if(whoIHandler.getPokemonData()!=null&&!whoIHandler.getPokemonData().isAlreadyOpen()){
            long last = (System.currentTimeMillis()-whoIHandler.getPokemonData().getCreateTime())/1000/60;
            if(last>=whoIHandler.getPlugin().getConfig().getInt("setting.ans-bro",2)){
                whoIHandler.getPokemonData().setAlreadyOpen(true);
                Config.config.getMessageManager().getStringList("message.bro-ans").forEach(s->{
                    s = s.replace("@poke@",whoIHandler.getPlugin().getiStorage().getPokemon(whoIHandler.getPokemonData().getPokemonId()));
                    Bukkit.broadcastMessage(s);
                });
                whoIHandler.endGame();
                return;
            }
            whoIHandler.bro();
        }
    }
}

class TrSchedule{
    LocalDateTime startDate;
    LocalDateTime endDate;
    //分钟
    long delay;

    public TrSchedule(String text){
        String[] split = text.split(",");
        String[] startDateSplit = split[0].split(":");
        this.startDate = LocalDateTime.now();
        this.startDate = startDate.withHour(Integer.valueOf(startDateSplit[0]))
                .withMinute(Integer.valueOf(startDateSplit[1])).withSecond(Integer.valueOf(startDateSplit[2]));
        String[] endDateSplit = split[1].split(":");
        this.endDate = LocalDateTime.now();
        this.endDate = endDate.withHour(Integer.valueOf(endDateSplit[0]))
                .withMinute(Integer.valueOf(endDateSplit[1])).withSecond(Integer.valueOf(endDateSplit[2]));
        this.delay = Long.parseLong(split[2]);
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public long getDelay() {
        return delay;
    }
}

public class WhoIHandler extends AbstractListener<WhoI>{

    private List<String> gamePlayers;
    private Map<String, ItemStack> tempItemMap;
    private Map<String, ItemStack> lastCloseMap;
    private LocalDateTime nextTime;
    private BukkitTask bukkitTask;
    private PokemonData pokemonData;
    private WhoIThread whoIThread;
    private BukkitTask broTask;

    private Nms nms;

    public WhoIHandler(WhoI plugin) {
        super(plugin);
        this.gamePlayers = new Vector<>();
        this.tempItemMap = new ConcurrentHashMap<>();
        this.nextTime = LocalDateTime.now();
        this.lastCloseMap = new ConcurrentHashMap<>();
        File file = new File(plugin.getDataFolder()+"/last.yml");
        if(file.exists()){
            YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
            yamlConfiguration.getKeys(false).forEach(player->lastCloseMap.put(player,yamlConfiguration.getItemStack(player)));
        }
        Bukkit.getOnlinePlayers().forEach(player -> {
            if(lastCloseMap.containsKey(player.getName())){
                player.setItemInHand(lastCloseMap.get(player.getName()));
                lastCloseMap.remove(player.getName());
            }
        });
        try {
            String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            Class<?> aClass = Class.forName("com.meteor.whoi.nms." + version);
            plugin.getLogger().info("当前Nms版本:"+version);
            nms = (Nms) aClass.newInstance();
        }catch (Exception e){
            plugin.getLogger().info("检测到不兼容的版本..");
        }
        File dir = new File(plugin.getDataFolder()+"/hideimg/");
        if(!dir.exists())
            dir.mkdir();
       whoIThread = new WhoIThread(this);
        startRandomPokemon();
        super.register();
    }




    public void bro(){
        Config.config.getMessageManager().getStringList("message.bro.text")
                .forEach(s->{
                    if(s.equalsIgnoreCase("@format@")){
                        TextComponent textComponent = new TextComponent(Config.config.getMessageManager().getString("format.rich-mes.text"));
                        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/whoi wi"));
                        BaseComponent[] baseComponents = (new ComponentBuilder((Config.config.getMessageManager().getString("format.rich-mes.hover")))).create();
                        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,baseComponents);
                        textComponent.setHoverEvent(hoverEvent);
                        Bukkit.getOnlinePlayers().forEach(player -> player.spigot().sendMessage(textComponent));
                        return;
                    }
                    Bukkit.broadcastMessage(s);
                });
    }

    public void startRandomPokemon(){
        int week = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) -1;
        week = week<0?0:week;
        TrSchedule trSchedule = new TrSchedule(getPlugin().getConfig().getString("setting.time.schedule."+week,
                getPlugin().getConfig().getString("setting.time.schedule.other")));
        long lastM = getNextTimeM(nextTime,trSchedule);
        bukkitTask = Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
            randomPokemon();
            startRandomPokemon();
        }, lastM * 60 * 20);
    }

    public static void createHideImg(String pkm){
        BufferedImage read =null;
        try {
            read = ImageIO.read(new FileInputStream((WhoI.getPlugin(WhoI.class).getDataFolder()+"/image/@pkm@.png").replace("@pkm@",pkm)));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        int type = (read.getType() == 0) ? 2 : read.getType();
        BufferedImage resizedImage = new BufferedImage(128, 128, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(read, 0, 0, 128, 128, null);
        g.dispose();
        for(int x=0;x<128;x++){
            for(int y=0;y<128;y++){
                if(resizedImage.getRGB(x,y)!=0){
                    resizedImage.setRGB(x,y,Color.BLACK.getRGB());
                }
            }
        }
        try {
            ImageIO.write(resizedImage,"png",new File(WhoI.getPlugin(WhoI.class).getDataFolder()+"/hideimg/"+pkm+".png"));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }



    public void reload(){
        if(bukkitTask!=null)
            bukkitTask.cancel();
        startRandomPokemon();
    }

    public void randomPokemon() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin,()->{
            int r = (new Random()).nextInt(getPlugin().getConfig().getInt("pokemon-index-count"));
            boolean bl = plugin.getiStorage().initPokemonFile(r);
            if(!bl){
                plugin.getLogger().info("爬取网络资源出现异常,请检查网络,本次将使用本地资源");
                File file = new File(plugin.getDataFolder()+"/image");
                List<File> fileList = Arrays.asList(file.listFiles());
                Collections.shuffle(fileList);
                if(fileList.isEmpty()){
                    randomPokemon();
                    return;
                }else{
                    String pokemon = fileList.get(0).getName().replace(".png","");
                    r = plugin.getiStorage().getPokemonIndex(pokemon);
                    plugin.getLogger().info("使用本地资源:"+pokemon);
                }
            }
            this.pokemonData = new PokemonData(r,System.currentTimeMillis(),0L,false);
            if(broTask!=null)
                broTask.cancel();
            broTask = Bukkit.getScheduler().runTaskTimer(getPlugin(),whoIThread , plugin.getConfig().getInt("setting.bro-time", 20) * 20, plugin.getConfig().getInt("setting.bro-time", 20) * 20);
            Bukkit.getScheduler().runTask(plugin,()->{
                Bukkit.getServer().getPluginManager().callEvent(new GameStartEvent(plugin.getiStorage().getPokemon(pokemonData.getPokemonId())));
            });
            bro();
        });
    }

    public static long getLast(LocalDateTime currentTime,int hour, int minute) {
        LocalDateTime time = LocalDateTime.now();
        time = time.withHour(hour);
        time = time.withMinute(minute);
        time = time.withSecond(0);
        time = time.withDayOfMonth(currentTime.getDayOfMonth());
        time = time.withDayOfYear(currentTime.getDayOfYear());
        if (time.isBefore(currentTime))
            time = time.plusDays(1L);
        long re = time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return (re - currentTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()) / 1000L/60L;
    }

    public long getNextTimeM(LocalDateTime currentDate, TrSchedule trSchedule){
        LocalDateTime startDate = trSchedule.getStartDate().withDayOfMonth(currentDate.getDayOfMonth()).withDayOfYear(currentDate.getDayOfYear());
        LocalDateTime endDate = trSchedule.getEndDate().withDayOfMonth(currentDate.getDayOfMonth()).withDayOfYear(currentDate.getDayOfYear());
        if(endDate.isBefore(startDate))
            endDate = endDate.plusDays(1);
        if(currentDate.isAfter(endDate)){
            nextTime = startDate.plusDays(1).plusMinutes(trSchedule.getDelay());
            return getLast(currentDate,startDate.getHour(),startDate.getMinute())+trSchedule.getDelay();
        }
        if(currentDate.isBefore(startDate)){
            long rm = getLast(currentDate,startDate.getHour(),startDate.getMinute())+trSchedule.getDelay();
            nextTime = currentDate.plusMinutes(rm);
            return rm;
        }else if(currentDate.isAfter(startDate)){
            if(currentDate.plusMinutes(trSchedule.getDelay()).isAfter(endDate)){
                long rm = getLast(currentDate,startDate.getHour(),startDate.getMinute())+trSchedule.getDelay();
                nextTime = currentDate.plusMinutes(rm);
                return rm;
            }
            nextTime = currentDate.plusMinutes(trSchedule.getDelay());
            return trSchedule.getDelay();
        }
        return -1;
    }

    public BukkitTask getBukkitTask() {
        return bukkitTask;
    }

    private void quitGame(Player p){
        if(!gamePlayers.contains(p.getName()))
            return;
        p.updateInventory();
        tempItemMap.remove(p.getName());
        gamePlayers.remove(p.getName());
        p.sendTitle(Config.config.getMessageManager().getString("message.quit.title"),Config.config.getMessageManager().getString("message.quit.subtitle"));
    }

    public void joinGame(Player p){
        String pn = p.getName();
        if(gamePlayers.contains(pn))
            quitGame(p);
        ItemStack item = p.getInventory().getItem(4);
        if(item!=null&&item.getType()!=Material.AIR)
            tempItemMap.put(p.getName(),item);
        p.sendTitle(Config.config.getMessageManager().getString("message.join.title"),Config.config.getMessageManager().getString("message.join.subtitle"));
        showMap(p,plugin.getiStorage().getPokemon(getPokemonData().getPokemonId()),true);
        p.getInventory().setHeldItemSlot(4);
        gamePlayers.add(pn);
    }

    @EventHandler
    void onIn(PlayerInteractEntityEvent entityEvent){
        if(entityEvent.getRightClicked().getType()== EntityType.ITEM_FRAME){
            Player player = entityEvent.getPlayer();
            if(gamePlayers.contains(player.getName())){
                quitGame(player);
            }
        }
    }

    @EventHandler
    void onInt0(PlayerInteractEvent interactEvent){
        Player player = interactEvent.getPlayer();
        if(gamePlayers.contains(player.getName())){
            quitGame(player);
        }
    }

    @EventHandler
    void onOpen(InventoryOpenEvent openEvent){
        HumanEntity player = openEvent.getPlayer();
        if(gamePlayers.contains(player.getName())){
            openEvent.setCancelled(true);
            openEvent.getPlayer().sendMessage(Config.config.getMessageManager().getString("message.please-cancel"));
        }
    }

    @EventHandler
    void onJoinGame(PlayerChatEvent chatEvent){
        if(chatEvent.getMessage().matches(plugin.getConfig().getString("setting.chat-start","wi"))){
            chatEvent.setCancelled(true);
            if(chatEvent.getPlayer().getGameMode() != GameMode.SURVIVAL){
                chatEvent.getPlayer().sendMessage(Config.config.getMessageManager().getString("message.must-survival"));
                return;
            }
            if(getPokemonData()!=null&&!getPokemonData().isAlreadyOpen()){
                joinGame(chatEvent.getPlayer());
            }
        }
    }

    @EventHandler
    void startGame(GameStartEvent gameStartEvent){
        String title = Config.config.getMessageManager().getString("message.bro.title");
        String subtitle = Config.config.getMessageManager().getString("message.bro.subtitle");
        Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle(title,subtitle));
    }

    @EventHandler
    void onClick(InventoryClickEvent clickEvent){
        if(clickEvent.getCurrentItem()!=null){
            if(gamePlayers.contains(clickEvent.getWhoClicked().getName()))
                quitGame((Player) clickEvent.getWhoClicked());
        }
    }

    @EventHandler
    void onHeld(PlayerSwapHandItemsEvent swapHandItemsEvent){
        if(gamePlayers.contains(swapHandItemsEvent.getPlayer().getName()))
            swapHandItemsEvent.setCancelled(true);
    }

    @EventHandler
    void onHel(PlayerItemHeldEvent heldEvent){
        if(gamePlayers.contains(heldEvent.getPlayer().getName())||lastCloseMap.containsKey(heldEvent.getPlayer().getName())){
            heldEvent.setCancelled(true);
            heldEvent.getPlayer().sendMessage(Config.config.getMessageManager().getString("message.please-cancel"));
        }
    }

    @EventHandler
    void onDropGameItem(PlayerDropItemEvent dropItemEvent){
        Player player = dropItemEvent.getPlayer();
        if(gamePlayers.contains(player.getName())){
            dropItemEvent.setCancelled(true);
            dropItemEvent.getPlayer().sendMessage(Config.config.getMessageManager().getString("message.please-cancel"));
        }
    }



    @EventHandler
    void onCancel(PlayerChatEvent chatEvent){
        if(chatEvent.getMessage().equalsIgnoreCase("cancel")){
            Player player = chatEvent.getPlayer();
            if(gamePlayers.contains(player.getName()))
                quitGame(player);
        }
    }


    public boolean showMap(Player player,String pkm,boolean hide){
        ItemStack[] items = new ItemStack[46];
        for (int i = 0; i < 46; i++)
            items[i] = new ItemStack(Material.AIR);
        ItemStack itemStack = new ItemStack(Material.MAP);
        items[40] = itemStack;
        sendFakeItem(player,Arrays.asList(items));
        sendFakeMap(player,pkm,hide);
        return true;
    }

    public void sendFakeItem(Player player,List<ItemStack> itemStacks){
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.WINDOW_ITEMS);
        packetContainer.getIntegers().write(0,0);
        packetContainer.getItemListModifier().write(0,itemStacks);
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetContainer);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendFakeMap(Player player,String pkm,boolean hide){
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.MAP);
        packet.getBytes().write(0, (byte) 4);
        packet.getIntegers().write(0, 0);
        packet.getIntegers().write(1, 0);
        packet.getIntegers().write(2, 0);
        packet.getIntegers().write(3, 128);
        packet.getIntegers().write(4, 128);
        packet.getBooleans().write(0, true);
        if(nms instanceof v1_16_R3)
            packet.getBooleans().write(1,true);
        packet.getModifier().write((nms instanceof v1_12_R1)?3:4, nms.getMapIconArry());
        try {
            if (hide) {
                File file = new File(plugin.getDataFolder() + "/hideimg/" + pkm + ".png");
                if (!file.exists())
                    WhoIHandler.createHideImg(pkm);
            }
            FileInputStream fileInputStream = hide ? new FileInputStream(this.plugin.getDataFolder() + "/hideimg/" + pkm + ".png") : new FileInputStream(this.plugin.getDataFolder() + "/image/" + pkm + ".png");
            BufferedImage read = null;
            try {
                read = ImageIO.read(fileInputStream);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            int type = (read.getType() == 0) ? 2 : read.getType();
            BufferedImage resizedImage = new BufferedImage(128, 128, type);
            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(read, 0, 0, 128, 128, null);
            g.dispose();
            byte[] bytes = MapPalette.imageToBytes(resizedImage);
            packet.getByteArrays().write(0,bytes);
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendMap(){
        MapView mapView = Bukkit.createMap(Bukkit.getWorlds().get(0));
        MapRenderer renderer = new MapRenderer() {
            @Override
            public void render(MapView map, MapCanvas canvas, Player player) {
                // canvas.drawImage(0, 0, yourImage);
            }
        };

        for (MapRenderer mapRenderer : mapView.getRenderers()) {
            mapView.removeRenderer(mapRenderer);
        }

        mapView.addRenderer(renderer);
        Player player = Bukkit.getPlayer("xxj");
        player.sendMap(mapView);
    }



    private boolean updatePokemonData(PlayerChatEvent chatEvent,Player p,String ans){
        if(getPokemonData()==null||getPokemonData().isAlreadyOpen())
            return false;
        String pokemon = plugin.getiStorage().getPokemon(getPokemonData().getPokemonId());
        if(ans.matches(plugin.getConfig().getString("setting.ans-reg","@pkm@").replace("@pkm@",pokemon))){
            StringBuilder sb = new StringBuilder();
            for(int i=0;i<pokemon.length();++i)
                sb.append("*");
            chatEvent.setMessage(sb.toString());
            getPokemonData().setAlreadyOpen(true);
            Config.config.getMessageManager().getStringList("message.update")
                    .forEach(s -> {
                        s = s.replace("@p@",p.getName())
                                .replace("@as@",pokemon);
                        Bukkit.broadcastMessage(s);
                    });
            Bukkit.getOnlinePlayers().forEach(player -> {
                String title = Config.config.getMessageManager().getString("message.end.title");
                String subtitle = Config.config.getMessageManager().getString("message.end.subtitle");
                player.sendTitle(title.replace("@as@",pokemon),subtitle.replace("@as@",pokemon));
            });
            Bukkit.getServer().getPluginManager().callEvent(new GameUpdateEvent(p));
            endGame();
            return true;
        }
        return false;
    }

    public void giveReward(String p,String chatMessage){
        Bukkit.getScheduler().runTaskLater(plugin,()->{
            Config.config.lottery().getCmds()
                    .forEach(s->Bukkit.dispatchCommand(Bukkit.getConsoleSender(),s.replace("@p@",p)
                            .replace("@pkm@",chatMessage)));
        },20L);
    }

    public void endGame(){
        gamePlayers.forEach(gp->{
            Player player = Bukkit.getPlayerExact(gp);
            if(player!=null)
                showMap(player,plugin.getiStorage().getPokemon(getPokemonData().getPokemonId()),false);
        });
        Bukkit.getScheduler().runTaskLater(plugin,()->{
            Bukkit.getOnlinePlayers().forEach(player -> quitGame(player));
        },20L);
        if(broTask!=null)
            broTask.cancel();
    }

    @EventHandler
    void onQuit(PlayerQuitEvent quitEvent){
        if(gamePlayers.contains(quitEvent.getPlayer().getName())){
            quitGame(quitEvent.getPlayer());
        }
    }


    @EventHandler
    void onChat(PlayerChatEvent chatEvent){
        Player player = chatEvent.getPlayer();
        if(gamePlayers.contains(player.getName()))
            if(updatePokemonData(chatEvent,player,chatEvent.getMessage()))
                giveReward(player.getName(),chatEvent.getMessage());
    }

    public void close(){
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        tempItemMap.forEach((k,v)->{
            if(gamePlayers.contains(k))
                yamlConfiguration.set(k,v);
        });
        lastCloseMap.forEach((k,v)->yamlConfiguration.set(k,v));
        try {
            yamlConfiguration.save(new File(plugin.getDataFolder()+"/last.yml"));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public PokemonData getPokemonData() {
        return pokemonData;
    }
}
