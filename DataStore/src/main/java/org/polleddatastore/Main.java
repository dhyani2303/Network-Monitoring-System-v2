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

                        System.out.println(message);

                        var contextResult = new JsonObject(new String(Base64.getDecoder().decode(message)));

                        var file = Util.configMap.get(Constants.RESULT_PATH) + contextResult.getString(Constants.IP_ADDRESS) + ".json";

                        if (contextResult.getString(Constants.STATUS).equals(Constants.SUCCESS))
                        {

                            vertx.executeBlocking(id ->
                            {
                                System.out.println("Inside execute blocking handler" + Thread.currentThread().getName());

                                vertx.fileSystem().open(file, new OpenOptions().setAppend(true).setCreate(true), openHandler ->
                                {
                                    if (openHandler.succeeded())
                                    {
                                        var asyncFile = openHandler.result();

                                        asyncFile.lock().onComplete(asyncFileLock ->
                                        {
                                            if (asyncFileLock.succeeded())
                                            {
                                                var offset = openHandler.result().getWritePos();

                                                System.out.println(openHandler.result().getWritePos() + " Message length: " + message.length());

                                                Buffer buffer;

                                                if (openHandler.result().getWritePos() == 0)
                                                {
                                                    buffer = Buffer.buffer("[")
                                                            .appendBuffer(Buffer.buffer(contextResult.encodePrettily()))
                                                            .appendString("]");

                                                    System.out.println("Offset " + openHandler.result().getWritePos());

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

                                                    System.out.println("Content is written to file");

                                                    LOGGER.info("File is successfully written");

                                                });
                                                openHandler.result().close();
                                            }
                                        });

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
                            LOGGER.warn("Data has status failed {}",contextResult);
                        }
                    });



                    try
                    {
                        ZContext context = new ZContext();

                        var socket = context.createSocket(SocketType.PULL);

                        System.out.println(Util.configMap.get(Constants.ZMQ_ADDRESS).toString());

                        socket.connect(Util.configMap.get(Constants.ZMQ_ADDRESS).toString());

                        new Thread(() ->
                        {

                            while (true)
                            {
                                var message = socket.recv();

                                eventBus.send("address", new String(message));

                            }
                        }).start();

//
                        //   Buffer buffer = Buffer.buffer(message);


//                        while (!Thread.currentThread().isInterrupted())
//                        {
//
//                            byte[] message = socket.recv(0);
//
//                            Buffer buffer = Buffer.buffer(message);
//
//
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

//             vertx.fileSystem().openBlocking("File1.txt", new OpenOptions().setCreate(true).setAppend(true))
//                     .write(buffer).onComplete(handelr->{
//
//                            if (handelr.succeeded())
//                            {
//                                System.out.println("Content written to file");
//
//                            }
//                            else
//                            {
//                                System.out.println("Failed");
//                            }
//
//                        });


//
//                  System.out.println(new String(message));
//
//                    fileWriter.write(new String(message));
//
//                    offset = message.length;
