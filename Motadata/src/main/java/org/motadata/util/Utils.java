package org.motadata.util;



import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.motadata.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


public class Utils
{
    public static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    public static ConcurrentHashMap<String, Object> configMap = new ConcurrentHashMap<String,Object>();

    private static final AtomicLong counter = new AtomicLong(0);

    public static long getNewId(){

        return counter.incrementAndGet();
    }

    public static Future<Void> setConfig(Vertx vertx)
    {
        Promise<Void> promise = Promise.promise();

        vertx.fileSystem().readFile(System.getProperty("user.dir") + "/config/configuration.json",handler->{

            if (handler.succeeded())
            {
                var data = handler.result().toJsonObject();

                for (var key : data.fieldNames()) {

                    configMap.put(key, data.getValue(key));
                }

                promise.complete();
            }
            else
            {
                LOGGER.error(handler.cause().toString());

                promise.fail(handler.cause());
            }
        });


        return promise.future();

    }

    public static Future<Void> writeToFile(Vertx vertx, JsonObject data)
    {
        Promise<Void> promise = Promise.promise();

        var ip = data.getString(Constants.IP_ADDRESS);

        var now = LocalDateTime.now();

        data.put(Constants.TIMESTAMP, now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        var fileName = ip + ".txt";

        var buffer = Buffer.buffer(data.encodePrettily());

        vertx.fileSystem().openBlocking(fileName,new OpenOptions().setAppend(true).setCreate(true)).write(buffer).onComplete(handler->
                {
                    LOGGER.info("Content written to file");

                    promise.complete();

                }).onFailure(handler->
                {
                    LOGGER.warn("Error occurred while opening the file {}",handler.getCause().toString());

                    promise.fail(handler.getCause());
                });

        return promise.future();
    }

    public static String encode(JsonArray context)
    {
       return Base64.getEncoder().encodeToString(context.toString().getBytes());
    }

    public static JsonObject decode(String context)
    {

        if (context!=null) {

            var decodedBytes = Base64.getDecoder().decode(context);
            
            return new JsonObject(new String(decodedBytes));

        }
        else
        {
            return null;
        }

    }



}
