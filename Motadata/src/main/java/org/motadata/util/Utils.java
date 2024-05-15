package org.motadata.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


public class Utils
{
    public static ConcurrentHashMap configMap = new ConcurrentHashMap<String,Object>();

    static AtomicLong counter = new AtomicLong(0);

    public static long getNewId(){

        return counter.incrementAndGet();


    }

    public static void setConfig()
    {
        try
        {
            ObjectMapper mapper = new ObjectMapper();

            configMap= mapper.readValue(new File("/home/dhyani/Documents/Network-Monitoring-System-v2/Motadata/src/main/java/org/motadata/config/configuration.json"), ConcurrentHashMap.class);

        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

    }

    public static String encode(JsonArray context)
    {
       var result= Base64.getEncoder().encodeToString(context.toString().getBytes());

       return result;
    }



}
