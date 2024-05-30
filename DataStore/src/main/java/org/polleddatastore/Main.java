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

    static Vertx vertx = Vertx.vertx();

    public static void main(String[] args)
    {
        try
        {
            Util.setConfig(vertx).onComplete(setConfigHandler ->
            {
                if (setConfigHandler.succeeded())
                {
                    LOGGER.info("Configuration has been set");

                    vertx.deployVerticle(WriteFile.class.getName()).onComplete(handler->{

                        if (handler.succeeded())
                        {
                            LOGGER.info("Write file verticle has been deployed successfully");
                        }
                        else
                        {
                            LOGGER.warn("Unable to deploy write file verticle");
                        }
                    });

                    vertx.deployVerticle(ReadFile.class.getName()).onComplete(readHandler->{

                        if (readHandler.succeeded())
                        {
                            LOGGER.info("Read file verticle has been deployed successfully");
                        }
                        else
                        {
                            LOGGER.warn("Failed to deploy the read file verticle");
                        }
                    });
//                    eventBus.localConsumer("address", consumerHandler ->
//                    {
//                        var message = consumerHandler.body().toString();
//
//                        var contextResult = new JsonObject(new String(Base64.getDecoder().decode(message)));
//
//                        var file = Util.configMap.get(Constants.RESULT_PATH) + contextResult.getString(Constants.IP_ADDRESS) + ".json";
//
//                        if (contextResult.getString(Constants.STATUS).equals(Constants.SUCCESS))
//                        {
//                            vertx.executeBlocking(id ->
//                            {
//                                var asyncFile = vertx.fileSystem().openBlocking(file, new OpenOptions().setAppend(true).setCreate(true));
//
//                                var offset = asyncFile.getWritePos();
//
//                                Buffer buffer;
//
//                                if (offset == 0)
//                                {
//                                    buffer = Buffer.buffer("{" + "\n")
//                                            .appendString("\"" + contextResult.getString("timestamp") + "\":")
//                                            .appendBuffer(Buffer.buffer(contextResult.encodePrettily()))
//                                            .appendString("\n" + "}");
////
//                                }
//                                else
//                                {
//                                    offset = offset - 1;
//
//                                    buffer = Buffer.buffer("," + "\n")
//                                            .appendString("\"" + contextResult.getString("timestamp") + "\":")
//                                            .appendBuffer(Buffer.buffer(contextResult.encodePrettily()))
//                                            .appendString("\n" + "}");
//
//                                }
//                                asyncFile.write(buffer, offset, wHandler ->
//                                {
//                                    LOGGER.info("File is successfully written");
//
//                                });
//                                asyncFile.close();
//
//                            });
//                        }
//                        else
//                        {
//                            LOGGER.warn("Data has status failed {}", contextResult);
//                        }
//                    });
//
//                    try
//                    {
//                        ZContext context = new ZContext();
//
//                        var socket = context.createSocket(SocketType.PULL);
//
//                        socket.connect(Util.configMap.get(Constants.ZMQ_ADDRESS).toString());
//
//                        new Thread(() ->
//                        {
//
//                            while (true)
//                            {
//                                var message = socket.recv();
//
//                                eventBus.send("address", new String(message));
//
//                            }
//                        }).start();
//
//                    }
//                    catch (Exception exception)
//                    {
//                        System.out.println(exception);
//
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

