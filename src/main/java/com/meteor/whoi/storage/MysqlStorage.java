package com.meteor.whoi.storage;

import com.meteor.meteorlib.mysql.FastMySQLStorage;
import com.meteor.meteorlib.mysql.column.Column;
import com.meteor.meteorlib.mysql.data.KeyValue;
import com.meteor.whoi.CosUtil;
import com.meteor.whoi.WhoI;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.map.MapPalette;
import sun.net.www.http.HttpClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MysqlStorage implements IStorage{

    private WhoI plugin;
    private FastMySQLStorage fastMySQLStorage;
    private final String TABLE_NAME  = "PokemonImageIndexes";

    public MysqlStorage(WhoI plugin){
        this.plugin = plugin;
        fastMySQLStorage = new FastMySQLStorage(plugin,plugin.getConfig().getConfigurationSection("mysql-info"));
        fastMySQLStorage.enable();
        Column[] columns = {Column.of("pkey", Column.Integer.INT,true,25),
        Column.of("pindex",Column.Char.CHAR,30)};
        fastMySQLStorage.createTable(TABLE_NAME,columns);
        if(fastMySQLStorage.get(TABLE_NAME,"pkey",1,"pindex")==null){
            plugin.getLogger().info("索引库不存在，开始爬取..请保持网络畅通");
            Bukkit.getScheduler().runTaskAsynchronously(plugin,()->{
                doGetPokemonIndex();
            });
        }
        plugin.getLogger().info("存储方式: mysql");
    }

    @Override
    public String getPokemon(int key){
        return fastMySQLStorage.get(TABLE_NAME,"pkey",key,"pindex");
    }

    @Override
    public int getPokemonIndex(String key) {
        return fastMySQLStorage.get(TABLE_NAME,"pindex",key,"pkey");
    }

    @Override
    public boolean initPokemonFile(int id){
        String pindex = getPokemon(id);
        File file = new File(plugin.getDataFolder()+"/image/"+pindex+".png");
        HttpGet httpGet = null;
        CloseableHttpClient aDefault = null;
        CloseableHttpResponse execute = null;
        if(!file.exists()){
            CosUtil.cosUtil.download(getPokemon(id));
//            httpGet = new HttpGet("https://wiki.52poke.com/wiki/"+pindex);
//            aDefault = HttpClients.createDefault();
//            try {
//                execute = aDefault.execute(httpGet);
//                if(execute.getStatusLine().getStatusCode()==200){
//                    plugin.getLogger().info("正在爬取宝可梦"+pindex+"相关资源文件");
//                    String htmlText = EntityUtils.toString(execute.getEntity());
//                    String reg = "<a href=\"/(.*)\" class=\"image\">";
//                    Pattern pattern = Pattern.compile(reg);
//                    Matcher matcher = pattern.matcher(htmlText);
//                    while (matcher.find()){
//                        httpGet = new HttpGet("https://wiki.52poke.com/"+matcher.group(1));
//                        break;
//                    }
//                    execute = aDefault.execute(httpGet);
//                    htmlText = EntityUtils.toString(execute.getEntity());
//                    reg = "\\| <a href=\"(.*)\" class=\"mw-thumbnail-link\">480×480像素</a>";
//                    pattern = Pattern.compile(reg);
//                    matcher = pattern.matcher(htmlText);
//                    URL url = null;
//                    while (matcher.find())
//                    {
//                        url = new URL("https:"+matcher.group(1));
//                    }
//                    if(url==null){
//                        reg = "<div class=\"fullImageLink\" id=\"file\"><a href=\"(.*)\"><img";
//                        pattern = Pattern.compile(reg);
//                        matcher = pattern.matcher(htmlText);
//                        while (matcher.find())
//                            url = new URL("https:"+matcher.group(1));
//                    }
//                    HttpURLConnection connection = ((HttpURLConnection) url.openConnection());
//                    connection.setRequestProperty(
//                            "User-Agent",
//                            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
//                    BufferedImage read = ImageIO.read(connection.getInputStream());
//                    ImageIO.write(read,"PNG",file);
//                    plugin.getLogger().info("已爬取相关资源文件存于:"+file.toString());
//                }
//                return true;
//            } catch (Exception exception) {
//                plugin.getLogger().info("爬取资源产生了一个异常，已存于error.yml");
//                exception.printStackTrace();
//                ExceptionColl.getInstance.collError(exception.getMessage());
//                return false;
//            }finally {
//                try {
//                    if(aDefault!=null)
//                        aDefault.close();
//                    if(execute!=null)
//                        execute.close();
//                } catch (IOException ioException) {
//                    ioException.printStackTrace();
//                }
//            }
        }
        return true;
    }

    @Override
    public void doGetPokemonIndex(){
        CloseableHttpClient aDefault = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("https://wiki.52poke.com/wiki/%E5%AE%9D%E5%8F%AF%E6%A2%A6%E5%88%97%E8%A1%A8%EF%BC%88%E6%8C%89%E7%AC%AC%E5%85%AD%E4%B8%96%E4%BB%A3%E8%8E%B7%E5%BE%97%E6%96%B9%E5%BC%8F%EF%BC%89");
        CloseableHttpResponse execute = null;
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
                    String pindex = matcher.group(2);
                    KeyValue[] keyValues = {new KeyValue("pkey",String.valueOf(indexAmount)),new KeyValue("pindex",pindex)};
                    fastMySQLStorage.put(TABLE_NAME,keyValues);
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

    @Override
    public String getTrPokemonIndex(int key) {
        return fastMySQLStorage.get(TABLE_NAME,"pkey",key,"pindext");
    }

    @Override
    public void close() {
        fastMySQLStorage.disable();
    }
}
