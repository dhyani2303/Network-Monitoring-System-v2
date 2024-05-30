package org.polleddatastore;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.polleddatastore.constants.Constants;
import org.polleddatastore.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

public class ReadFile extends AbstractVerticle
{
    public static final Logger LOGGER = LoggerFactory.getLogger(ReadFile.class);

    public void start(Promise<Void> promise)
    {
        var eventBus = vertx.eventBus();

        try
        {
            ZContext context = new ZContext();

            var socket = context.createSocket(SocketType.ROUTER);

            socket.connect("tcp://localhost:5586");

            eventBus.<JsonObject>localConsumer(Constants.READ_ADDRESS,consumerHandler->
            {
                try
                {
                    var data = consumerHandler.body();

                    var contextResult = new JsonObject(new String(Base64.getDecoder().decode(data.getString("message"))));

                    var fileName = Util.configMap.get(Constants.RESULT_PATH) + contextResult.getString(Constants.IP_ADDRESS) + ".json";

                    vertx.executeBlocking(id->
                    {
                        try
                        {
                            vertx.fileSystem().readFile(fileName).onComplete(handler ->
                            {
                                if (handler.succeeded())
                                {
                                    var response = new JsonObject();

                                    for (var key : (handler.result().toJsonObject().fieldNames()))
                                    {
                                        if (Long.parseLong(key) >= Long.parseLong(contextResult.getString(Constants.TIMESTAMP)))
                                        {
                                            Date date = new Date(Long.parseLong(key));

                                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                                            String formattedDate = formatter.format(date);

                                            response.put(formattedDate,handler.result().toJsonObject().getJsonObject(key).getJsonObject(Constants.RESULT));

                                        }
                                    }

                                    System.out.println(response);

                                    System.out.println("Receiver side identity while swending : "+ data.getString("identity"));

                                    socket.send(data.getBinary("identity"), ZMQ.SNDMORE);

                                    socket.send(Base64.getEncoder().encodeToString(response.encode().getBytes()));
                                }
                                else
                                {
                                    System.out.println("Unable to read the file" + handler.cause());
                                }
                            });
                        }
                        catch (Exception exception)
                        {
                            LOGGER.error("Some exception occurred inside the executr blocking code",exception);
                        }
                    });
                }
                catch (Exception exception)
                {
                    LOGGER.error("Exception occurred in local consumer",exception);
                }

            });

             new Thread(() ->
                {
                    try
                    {
                        while (true)
                        {
                            var identity = socket.recv();

                            System.out.println("Received identity : "+ new String(identity));

                            var message = socket.recv();

                            eventBus.<JsonObject>send(Constants.READ_ADDRESS, new JsonObject().put("identity", identity).put("message", new String(message)));

                            //  eventBus.send(Constants.READ_ADDRESS, new String(message));
//
                        }
                    }
                    catch (Exception exception)
                    {
                        LOGGER.error("Some exception occurred in the another thread",exception);
                    }
                }).start();



        }
        catch (Exception exception)
        {
            LOGGER.error("Exception occurred",exception);

           promise.fail("Exception occurred");
        }

    }

//    public void stop(Promise<Void> stopPromise)
//    {
//        stopPromise.complete();
//    }
}
