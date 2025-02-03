package com.hss01248.webviewspider;

import android.content.Context;
import android.util.Log;

import androidx.startup.Initializer;

import com.blankj.utilcode.util.LogUtils;
import com.hss01248.aop.network.hook.OkhttpAspect;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Dns;
import okhttp3.OkHttpClient;

public class DnsHook implements OkhttpAspect.OkhttpHook , Initializer<String> {


    static Map<String,List<InetAddress>> hosts = new HashMap<>();

    static {
        try {
            hosts.put("66img.cc", Arrays.asList(new InetAddress[]{InetAddress.getByAddress(
                    new byte[]{(byte) 172, (byte) 235, (byte) 174, (byte) 249}),
                    InetAddress.getByAddress(new byte[]{(byte) 172, (byte) 233,45,40})}) );
        }catch (Throwable throwable){
            LogUtils.w(throwable);
        }

    }
    @Override
    public void beforeBuild(OkHttpClient.Builder builder) {
        builder.dns(new Dns() {
            @Override
            public List<InetAddress> lookup(String hostname) throws UnknownHostException {

                try {
                    InetAddress[] allByName = InetAddress.getAllByName(hostname);
                    if(allByName !=null && allByName.length>0){
                        List<InetAddress> list = new ArrayList<>();
                        for (int i = 0; i < allByName.length; i++) {
                            list.add(allByName[i]);
                        }
                        return list;
                    }
                }catch (UnknownHostException throwable){

                }

                LogUtils.i("没有解析到dns,从hosts中取:"+hostname);
                if(hosts.containsKey(hostname)){
                    return hosts.get(hostname);
                }
                throw new UnknownHostException(hostname);
            }
        });

    }

    @Override
    public String create(Context context) {
        Log.d("init","DnsHook.init start");
        OkhttpAspect.addHook(new DnsHook());
        return "DnsHook";
    }

    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {

        return new ArrayList<>();
    }
}
