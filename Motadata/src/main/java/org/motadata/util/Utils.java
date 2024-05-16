package org.motadata.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


public class Utils
{
    public static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

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

                configMap = mapper.readValue(new File("/home/dhyani/Documents/Network-Monitoring-System-v2/Motadata/src/main/java/org/motadata/config/configuration.json"), ConcurrentHashMap.class);

            LOGGER.info("Successful");


        }
        catch (IOException e)
        {
           LOGGER.error(e.getCause().toString());
        }

    }



    public static String encode(JsonArray context)
    {
       return Base64.getEncoder().encodeToString(context.toString().getBytes());


    }

    public static JsonObject decode(String context)
    {

        if (context!=null) {

            var decodedBytes = Base64.getDecoder().decode(context);

            String bytes = new String(decodedBytes);
            
            var jsonObject = new JsonObject(bytes);

            return jsonObject;
        }
        else
        {
            return null;
        }

    }





}
