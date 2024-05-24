package org.polleddatastore.utils;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class Util
{
    public static ConcurrentHashMap<String, Object> configMap = new ConcurrentHashMap<String,Object>();

    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

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
}
