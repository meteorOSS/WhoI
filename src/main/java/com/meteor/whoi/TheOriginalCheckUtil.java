package com.meteor.whoi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TheOriginalCheckUtil {
    private final static String HTTP_DOMAIN = "https://admin.ljxmc.top/";
    private final static String VERIFY_SIGN = "/api/public/cdk/verifySign";
    private final static String PLUGIN_NAME = "WhoI";
    private final static String SECRET_KEY = "1525603231722508288";

    public static boolean theOriginalCheck(WhoI plugin) {
        String sign = plugin.getConfig().getString("cdk");
        int port = plugin.getServer().getPort();
        try {
            String urlStr = HTTP_DOMAIN + VERIFY_SIGN +
                    "?sign=" + sign +
                    "&port=" + port +
                    "&pluginName=" + PLUGIN_NAME +
                    "&secretKey=" + SECRET_KEY;
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder stringBuilder = new StringBuilder();
            String current;
            while ((current = in.readLine()) != null) {
                stringBuilder.append(current);
            }
            String verifySign = stringBuilder.toString();
            if ("true".equals(verifySign)) {
                plugin.getLogger().info("已通过验证,感谢使用。");
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            plugin.getLogger().info("可能因网络等原因不佳等原因验证失败,请稍后再试");
            return false;
        }
    }
}