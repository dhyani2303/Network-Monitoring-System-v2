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

    public static long getNewId()
    {
        LOGGER.info("A request to generate a new id has come");

        return counter.incrementAndGet();
    }

    public static Future<Void> setConfig(Vertx vertx)
    {
        Promise<Void> promise = Promise.promise();

        try
        {
            vertx.fileSystem().readFile(System.getProperty("user.dir") + "/config/configuration.json", handler ->
            {
                if (handler.succeeded())
                {
                    var data = handler.result().toJsonObject();

                    for (var key : data.fieldNames())
                    {
                        configMap.put(key, data.getValue(key));
                    }

                    LOGGER.info("Set config method has run successfully");

                    promise.complete();
                }
                else
                {
                    LOGGER.error("Some error occurred in setConfig method {}",handler.cause().toString());

                    promise.fail(handler.cause());
                }
            });

        }
        catch (Exception exception)
        {
            LOGGER.error("Some exception occurred in setConfig method",exception);

            promise.fail("Exception occurred");
        }
        return promise.future();

    }

    public static Future<Void> writeToFile(Vertx vertx, JsonObject data)
    {
        Promise<Void> promise = Promise.promise();

        try
        {
            var ip = data.getString(Constants.IP_ADDRESS);

            var now = LocalDateTime.now();

            data.put(Constants.TIMESTAMP, now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            var fileName = configMap.get(Constants.RESULT_PATH) +ip + ".txt";

            var buffer = Buffer.buffer(data.encodePrettily());

            vertx.fileSystem().openBlocking(fileName, new OpenOptions().setAppend(true).setCreate(true)).write(buffer).onComplete(handler ->
            {
                LOGGER.info("Content has been written to the file named {}",fileName);

                promise.complete();

            }).onFailure(handler ->
            {
                LOGGER.warn("Error occurred while opening the file {}", handler.getCause().toString());

                promise.fail(handler.getCause());
            });

        }
        catch (Exception exception)
        {
            LOGGER.error("Some exception occurred in writeToFile method",exception);

             promise.fail("Exception occurred");
        }
        return promise.future();

    }


    public static boolean validatePort(String port)
    {
        try
        {
            if (port == null || port.isEmpty())
            {
                return false;
            }

            return Integer.parseInt(port) > 0 && Integer.parseInt(port) <= 65353 && port.matches("[0-9]+");
        }
        catch (Exception exception)
        {
            LOGGER.error("Exception occurred in validatePort method",exception);

            return false;

        }
    }
}
