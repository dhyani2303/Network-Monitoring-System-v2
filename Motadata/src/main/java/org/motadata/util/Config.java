package org.motadata.util;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.motadata.Bootstrap;
import org.motadata.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config
{
    public static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

    private static JsonObject config = new JsonObject();

    public static Future<Void> setConfig()
    {
        Promise<Void> promise = Promise.promise();

        try
        {
           Bootstrap.getVertx().fileSystem().readFile(System.getProperty("user.dir") + "/config/configuration.json", handler ->
            {
                if (handler.succeeded())
                {

                    config = handler.result().toJsonObject().copy();

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

    public static String getHost()
    {
        return config.getString(Constants.HTTP_HOST);
    }

    public static int getPort()
    {
        return config.getInteger(Constants.HTTP_PORT);
    }

    public static long getPollTime()
    {
        return config.getLong(Constants.POLL_TIME);
    }
    public static long getProcessTimeout()
    {
        return config.getLong(Constants.PROCESS_TIMEOUT);
    }

    public static String getDataDefaultTime()
    {
        return config.getString(Constants.DATA_DEFAULT_TIME);
    }
    public static String getZMQPluginSenderAddress()
    {
        return config.getString(Constants.ZMQ_PLUGIN_SENDER_ADDRESS);
    }
    public static String getZMQPluginReceiverAddress()
    {
        return config.getString(Constants.ZMQ_PLUGIN_RECEIVER_ADDRESS);
    }
    public static String getZMQDBSenderAddress()
    {
        return config.getString(Constants.ZMQ_DB_SENDER_ADDRESS);
    }

    public static String getZMQDBReceiverAddress()
    {
        return config.getString(Constants.ZMQ_DB_RECEIVER_ADDRESS);
    }


}
