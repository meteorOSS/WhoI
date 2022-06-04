package com.meteor.whoi.storage;

import com.meteor.meteorlib.mysql.data.KeyValue;
import com.meteor.whoi.CosUtil;
import com.meteor.whoi.WhoI;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YamlStorage implements IStorage{


    private WhoI plugin;

    private YamlConfiguration yamlConfiguration;
    private YamlConfiguration indexYamlConfiguraton;

    public YamlStorage(WhoI plugin){
        this.plugin = plugin;
        File file = new File(plugin.getDataFolder()+"/index.yml");
        if(!file.exists())
            plugin.saveResource("index.yml",false);
        this.yamlConfiguration = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public String getPokemon(int key) {
        return this.yamlConfiguration.getString(String.valueOf(key));
    }

    @Override
    public int getPokemonIndex(String key) {
        int index = 0;
        for(String ykey : yamlConfiguration.getKeys(false)){
            if(yamlConfiguration.getString(ykey).equalsIgnoreCase(key))
                index = Integer.parseInt(ykey);
        }
        return index;
    }

    @Override
    public boolean initPokemonFile(int id) {
        String pindex = getPokemon(id);
        File file = new File(plugin.getDataFolder()+"/image/"+pindex+".png");
        if(!file.exists())
            CosUtil.cosUtil.download(getPokemon(id));
        return true;
    }

    @Override
    public void close(){
        try {
            this.yamlConfiguration.save(new File(plugin.getDataFolder()+"/index.yml"));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Override
    public String getTrPokemonIndex(int key){
        return indexYamlConfiguraton.getString(String.valueOf(key));
    }

    @Override
    public void doGetPokemonIndex() {
        CloseableHttpClient aDefault = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("https://wiki.52poke.com/wiki/%E5%AE%9D%E5%8F%AF%E6%A2%A6%E5%88%97%E8%A1%A8%EF%BC%88%E6%8C%89%E7%AC%AC%E5%85%AD%E4%B8%96%E4%BB%A3%E8%8E%B7%E5%BE%97%E6%96%B9%E5%BC%8F%EF%BC%89");
        CloseableHttpResponse execute = null;
        this.yamlConfiguration = new YamlConfiguration();
        this.indexYamlConfiguraton = new YamlConfiguration();
        try {
            plugin.getLogger().info("连接网络以爬取索引");
            execute = aDefault.execute(httpGet);
            if(execute.getStatusLine().getStatusCode()==200){
                plugin.getLogger().info("成功访问wiki.52poke.com,正在爬取索引");
                String htmlText = EntityUtils.toString(execute.getEntity());
                String regStr = "<td>([0-9]*)\n" +
                        "</td>\n" +
                        "<td><span class=\".*\" title=\"\"></span>\n" +
                        "</td>\n" +
                        "<td><a href=\".*\" title=\"(.*)\">.*</a><br />\n" +
                        "</td>";
                Pattern pattern = Pattern.compile(regStr);
                Matcher matcher = pattern.matcher(htmlText);
                int indexAmount = 1;
                while (matcher.find()){
                    this.yamlConfiguration.set(String.valueOf(indexAmount),matcher.group(2));
                    indexAmount++;
                }
                plugin.getLogger().info("爬取到"+indexAmount+"条宝可梦索引，已更新数据库");
                plugin.getConfig().set("pokemon-index-count",indexAmount);
                plugin.saveConfig();
                for(int i=1;i<=3;++i)
                    initPokemonFile(i);
            }else{
                plugin.getLogger().info("未能成功爬取索引，请咨询作者以解决该问题");
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }finally {
            if(execute!=null) {
                try {
                    execute.close();
                    aDefault.close();
                    plugin.getLogger().info("已关闭网络资源,后续可使用指令更新索引库");
                    plugin.getLogger().info("图片资源来自 https://wiki.52poke.com/ 神奇宝贝中文百科");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }
}
