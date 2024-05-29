package org.polleddatastore;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonObject;
import org.polleddatastore.constants.Constants;
import org.polleddatastore.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;


public class Main
{
    public static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args)
    {
        try
        {
            Vertx vertx = Vertx.vertx();

            var eventBus = vertx.eventBus();

            Util.setConfig(vertx).onComplete(setConfigHandler ->
            {
                if (setConfigHandler.succeeded())
                {
                    LOGGER.info("Configuration has been set");

                    eventBus.localConsumer("address", consumerHandler ->
                    {
                        var message = consumerHandler.body().toString();

                        var contextResult = new JsonObject(new String(Base64.getDecoder().decode(message)));

                        var file = Util.configMap.get(Constants.RESULT_PATH) + contextResult.getString(Constants.IP_ADDRESS) + ".json";

                        if (contextResult.getString(Constants.STATUS).equals(Constants.SUCCESS))
                        {
                            vertx.executeBlocking(id ->
                            {
                                vertx.fileSystem().open(file, new OpenOptions().setAppend(true).setCreate(true), openHandler ->
                                {
                                    if (openHandler.succeeded())
                                    {
                                        var offset = openHandler.result().getWritePos();

                                        Buffer buffer;

                                        if (openHandler.result().getWritePos() == 0)
                                        {
                                            buffer = Buffer.buffer("[")
                                                    .appendBuffer(Buffer.buffer(contextResult.encodePrettily()))
                                                    .appendString("]");
                                        }
                                        else
                                        {
                                            offset = offset - 1;

                                            buffer = Buffer.buffer(",")
                                                    .appendBuffer(Buffer.buffer(contextResult.encodePrettily()))
                                                    .appendString("]");

                                        }
                                        openHandler.result().write(buffer, offset, wHandler ->
                                        {
                                            LOGGER.info("File is successfully written");

                                        });
                                        openHandler.result().close();

                                    }
                                    else
                                    {
                                        LOGGER.warn("Unable to write the content to the file");
                                    }
                                });

                            });
                        }
                        else
                        {
                            LOGGER.warn("Data has status failed {}", contextResult);
                        }
                    });

                    try
                    {
                        ZContext context = new ZContext();

                        var socket = context.createSocket(SocketType.PULL);

                        socket.connect(Util.configMap.get(Constants.ZMQ_ADDRESS).toString());

                        new Thread(() ->
                        {

                            while (true)
                            {
                                var message = socket.recv();

                                eventBus.send("address", new String(message));

                            }
                        }).start();

                    }
                    catch (Exception exception)
                    {
                        System.out.println(exception);
                    }

                }
                else
                {
                    LOGGER.warn("Unable to set the config");

                }
            });
        }
        catch (Exception exception)
        {
            LOGGER.error("Some exception occurred", exception);
        }

    }

}

