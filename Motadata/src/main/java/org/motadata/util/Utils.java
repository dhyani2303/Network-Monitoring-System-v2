package org.motadata.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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

    public static JsonObject decode(String context)
    {
        var decodedBytes = Base64.getDecoder().decode(context);

        String bytes = new String(decodedBytes);

        return new JsonObject(bytes);

    }

    public static void main(String[] args) {

        var buffer = Buffer.buffer();

        buffer = buffer.appendString("eyJjcmVkZW50aWFsLmlkIjotMSwiY3JlZGVudGlhbC5wcm9maWxlcyI6W3siY3JlZGVudGlhbC5pZCI6MSwiaXAiOiIxNzIuMTYuOC4xMTMiLCJwYXNzd29yZCI6Ik1pbmRAMTIiLCJwb3J0IjoiNTk4NSIsInJlcXVlc3QudGltZW91dC5uYW5vc2Vjb25kcyI6MzAwMDAwMDAwMDAsInVzZXJuYW1lIjoiZGh2YW5pIn1dLCJlcnJvciI6W3siZXJyb3IuY29kZSI6IkNPTk5FQ1RJT04wMSIsImVycm9yLm1lc3NhZ2UiOiJodHRwIHJlc3BvbnNlIGVycm9yOiA0MDEgLSBpbnZhbGlkIGNvbnRlbnQgdHlwZSJ9XSwiaXAiOiIxNzIuMTYuOC4xMTMiLCJwb3J0IjoiNTk4NSIsInJlcXVlc3QudHlwZSI6ImRpc2NvdmVyeSIsInJlc3VsdCI6e30sInN0YXR1cyI6ImZhaWwifQ==||@@||eyJjcmVkZW50aWFsLmlkIjotMSwiY3JlZGVudGlhbC5wcm9maWxlcyI6W3siY3JlZGVudGlhbC5pZCI6MSwiaXAiOiIxNzIuMTYuOC4xMTMiLCJwYXNzd29yZCI6Ik1pbmRAMTIiLCJwb3J0IjoiNTk4NSIsInJlcXVlc3QudGltZW91dC5uYW5vc2Vjb25kcyI6MzAwMDAwMDAwMDAsInVzZXJuYW1lIjoiZGh2YW5pIn1dLCJlcnJvciI6W3siZXJyb3IuY29kZSI6IkNPTk5FQ1RJT04wMSIsImVycm9yLm1lc3NhZ2UiOiJodHRwIHJlc3BvbnNlIGVycm9yOiA0MDEgLSBpbnZhbGlkIGNvbnRlbnQgdHlwZSJ9XSwiaXAiOiIxNzIuMTYuOC4xMTMiLCJwb3J0IjoiNTk4NSIsInJlcXVlc3QudHlwZSI6ImRpc2NvdmVyeSIsInJlc3VsdCI6e30sInN0YXR1cyI6ImZhaWwifQ==");

        String[] bufferSplit = buffer.toString().split("\\|\\|@@\\|\\|");

        for(int i=0;i<bufferSplit.length;i++)
        {
            var result = decode(bufferSplit[i]);

            System.out.println(result);
        }



    }


}
