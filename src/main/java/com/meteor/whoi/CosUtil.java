package com.meteor.whoi;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;

import java.io.File;

public class CosUtil {
    private WhoI plugin;
    private COSClient client;

    private String bukketName = "meteor-1307367050";

    public static CosUtil cosUtil;

    private CosUtil(WhoI plugin){
        this.plugin = plugin;
        COSCredentials credentials = new BasicCOSCredentials("AKIDEUcXIvUtALH2oa4oQvNj3Tp7Z7ZwERYZ","2xnZDjAXp4HQWYciqzALWhwALTgtpnlg");
        ClientConfig clientConfig = new ClientConfig(new Region("ap-guangzhou"));
//        clientConfig.setHttpProtocol(HttpProtocol.https);
        this.client = new COSClient(credentials,clientConfig);
    }


    public static void init(WhoI plugin){
        cosUtil = new CosUtil(plugin);
    }

    public void downloadFile(String file){
        File downFile = new File(plugin.getDataFolder()+"/"+file);
        GetObjectRequest objectRequest = new GetObjectRequest(bukketName,file);
        ObjectMetadata objectMetadata = client.getObject(objectRequest,downFile);
    }

    public void download(String pokemon){
        pokemon = pokemon+".png";
        File downFile = new File(plugin.getDataFolder()+"/image/"+pokemon);
        GetObjectRequest objectRequest = new GetObjectRequest(bukketName,pokemon);
        ObjectMetadata objectMetadata = client.getObject(objectRequest,downFile);
    }

    public String upload(String pokemon){
        pokemon = pokemon+".png";
        File file = new File(plugin.getDataFolder()+"/image/"+pokemon);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bukketName,pokemon,file);
        PutObjectResult putObjectResult = client.putObject(putObjectRequest);
        return putObjectResult.getETag();
    }
}
