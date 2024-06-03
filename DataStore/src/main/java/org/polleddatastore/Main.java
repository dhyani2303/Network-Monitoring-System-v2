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

    private static final Vertx vertx = Vertx.vertx();

    public static  Vertx getVertx()
    {
        return vertx;

    }

    public static void main(String[] args)
    {
        var eventBus = vertx.eventBus();

        try
        {
            Util.setConfig(vertx).onComplete(setConfigHandler ->
            {
                if (setConfigHandler.succeeded())
                {
                    LOGGER.info("Configuration has been set");

//                    vertx.deployVerticle(WriteFile.class.getName()).onComplete(handler->{
//
//                        if (handler.succeeded())
//                        {
//                            LOGGER.info("Write file verticle has been deployed successfully");
//                        }
//                        else
//                        {
//                            LOGGER.warn("Unable to deploy write file verticle");
//                        }
//                    });
//
//                    vertx.deployVerticle(ReadFile.class.getName()).onComplete(readHandler->{
//
//                        if (readHandler.succeeded())
//                        {
//                            LOGGER.info("Read file verticle has been deployed successfully");
//                        }
//                        else
//                        {
//                            LOGGER.warn("Failed to deploy the read file verticle");
//                        }
//                    })
                    eventBus.localConsumer(Constants.WRITE_ADDRESS,handler->
                    {
                        var message = handler.body().toString();

                        var contextResult = new JsonObject(new String(Base64.getDecoder().decode(message)));

                        if (contextResult.getString("request.type").equals("read.file"))
                        {
                            org.polledcontextstore.ReadFile.readFile(contextResult);
                        }


                    });

                    ZContext context = new ZContext();

                    var pullSocket = context.createSocket(SocketType.PULL);

                    pullSocket.connect(Util.configMap.get(Constants.ZMQ_WRITE_ADDRESS).toString());

                    new Thread(() ->
                    {
                        while (true)
                        {
                            var message = pullSocket.recv();

                            eventBus.send(Constants.WRITE_ADDRESS, new String(message));

                        }
                    });
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

