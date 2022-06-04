package com.meteor.whoi;

import com.meteor.meteorlib.nbt.NBTItem;
import com.meteor.whoi.api.event.GameStartEvent;
import com.meteor.whoi.api.event.GameUpdateEvent;
import com.meteor.whoi.data.PokemonData;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitTask;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


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

       whoIThread = new WhoIThread(this);

//        randomPokemon();
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
        int week = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) -1;
        week = week<0?0:week;
        TrSchedule trSchedule = new TrSchedule(getPlugin().getConfig().getString("setting.time.schedule."+week,
                getPlugin().getConfig().getString("setting.time.schedule.other")));
        long lastM = getNextTimeM(nextTime,trSchedule);
        bukkitTask = Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
            randomPokemon();
            startRandomPokemon();
        }, lastM * 60 * 20);
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
            Bukkit.getServer().getPluginManager().callEvent(new GameStartEvent(plugin.getiStorage().getPokemon(pokemonData.getPokemonId())));
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
        p.setItemInHand(tempItemMap.getOrDefault(p.getName(),new ItemStack(Material.AIR)));
        gamePlayers.remove(p.getName());
        p.sendTitle(Config.config.getMessageManager().getString("message.quit.title"),Config.config.getMessageManager().getString("message.quit.subtitle"));
    }

    public void joinGame(Player p){
        String pn = p.getName();
        if(gamePlayers.contains(pn))
            quitGame(p);
        ItemStack itemInHand = p.getItemInHand();
        if(itemInHand!=null){
            tempItemMap.put(pn,itemInHand);
        }
        ItemStack itemStack = new ItemStack(Material.EMPTY_MAP);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(Config.config.getMessageManager().getString("format.item.name"));
        itemMeta.setLore(Config.config.getMessageManager().getStringList("format.item.lore"));
        itemStack.setItemMeta(itemMeta);
        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setBoolean("isGameItem",true);
        p.setItemInHand(nbtItem.getItem());
        p.sendTitle(Config.config.getMessageManager().getString("message.join.title"),Config.config.getMessageManager().getString("message.join.subtitle"));
        gamePlayers.add(pn);
    }

    @EventHandler
    void onIn(PlayerInteractEntityEvent entityEvent){
        if(entityEvent.getRightClicked().getType()== EntityType.ITEM_FRAME){
            Player player = entityEvent.getPlayer();
            if(gamePlayers.contains(player.getName())){
                entityEvent.setCancelled(true);
                player.sendMessage(Config.config.getMessageManager().getString("message.please-cancel"));
            }

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
                clickEvent.setCancelled(true);
        }
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


    @EventHandler
    void onDrawMap(MapInitializeEvent initializeEvent){
        MapRenderer mapRenderer = new MapRenderer() {
            @Override
            public void render(MapView map, MapCanvas canvas, Player player) {
                if(gamePlayers.contains(player.getName())){
                    File file = new File(plugin.getDataFolder()+"/image/"+plugin.getiStorage().getPokemon(getPokemonData().getPokemonId())+".png");
                    BufferedImage read = null;
                    try {
                        read = ImageIO.read(file);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    canvas.drawImage(0,0, MapPalette.resizeImage(read));
                    if(!pokemonData.isAlreadyOpen()){
                        for (int x = 0; x < 126; x++) {
                            for (int y = 0; y < 126; y++) {
                                if(canvas.getPixel(x,y)!=0)
                                    canvas.setPixel(x,y,MapPalette.BROWN);
                            }
                        }
                    }
                }
            }
        };
        initializeEvent.getMap().getRenderers().clear();
        initializeEvent.getMap().addRenderer(mapRenderer);
        initializeEvent.getMap().setScale(MapView.Scale.FARTHEST);
    }


    private boolean updatePokemonData(PlayerChatEvent chatEvent,Player p,String ans){
        if(getPokemonData()==null||getPokemonData().isAlreadyOpen())
            return false;
        String pokemon = plugin.getiStorage().getPokemon(getPokemonData().getPokemonId());
//        if(plugin.getConfig().getString("setting.ans-reg")!=null){
//            pokemon = plugin.getConfig().getString("setting.ans-reg").replace("@pkm@",pokemon);
//        }
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

    public void giveReward(String p){
        Bukkit.getScheduler().runTaskLater(plugin,()->{
            Config.config.lottery().getCmds()
                    .forEach(s->Bukkit.dispatchCommand(Bukkit.getConsoleSender(),s.replace("@p@",p)));
        },20L);
    }

    public void endGame(){
        Bukkit.getScheduler().runTaskLater(plugin,()->{
            Bukkit.getOnlinePlayers().forEach(player -> quitGame(player));
        },10L);
        if(broTask!=null)
            broTask.cancel();
    }

    @EventHandler
    void onQuit(PlayerJoinEvent joinEvent){
        Player player = joinEvent.getPlayer();
        String pn = player.getName();
        Bukkit.getScheduler().runTaskLater(plugin,()->{
                if(gamePlayers.contains(pn))
                    quitGame(player);
                if(lastCloseMap.containsKey(pn)){
                    player.setItemInHand(lastCloseMap.get(pn));
                    lastCloseMap.remove(pn);
                }
            },20L);
    }



    @EventHandler
    void onChat(PlayerChatEvent chatEvent){
        Player player = chatEvent.getPlayer();
        if(gamePlayers.contains(player.getName()))
            if(updatePokemonData(chatEvent,player,chatEvent.getMessage()))
                giveReward(player.getName());
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
