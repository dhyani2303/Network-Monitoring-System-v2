package org.polledcontextstore;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.polledcontextstore.constants.Constants;
import org.polledcontextstore.utils.Util;
import org.polleddatastore.Main;
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

public class ReadFile
{
    public static final Logger LOGGER = LoggerFactory.getLogger(ReadFile.class);

    public static void readFile(JsonObject context)
    {
        var vertx = Main.getVertx();

        var eventBus = vertx.eventBus();

        try
        {
//            ZContext context = new ZContext();
//
//            var socket = context.createSocket(SocketType.ROUTER);
//
//            socket.connect(Util.configMap.get(Constants.ZMQ_READ_ADDRESS).toString());

            //   eventBus.<JsonObject>localConsumer(Constants.READ_ADDRESS, consumerHandler ->
            // {
//                try
//                {

            var fileName = Util.configMap.get(Constants.RESULT_PATH) + context.getString(Constants.IP_ADDRESS) + ".json";

            vertx.fileSystem().readFile(fileName).onComplete(handler ->
            {
                if (handler.succeeded())
                {
                    var response = new JsonObject();

                    for (var key : (handler.result().toJsonObject().fieldNames()))
                    {
                        if (Long.parseLong(key) >= Long.parseLong(context.getString(Constants.TIMESTAMP)))
                        {
                            Date date = new Date(Long.parseLong(key));

                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                            String formattedDate = formatter.format(date);

                            response.put(formattedDate, handler.result().toJsonObject().getJsonObject(key).getJsonObject(Constants.RESULT));

                        }
                    }

//                    socket.send(context.getBinary("identity"), ZMQ.SNDMORE);
//
//                    socket.send(Base64.getEncoder().encodeToString(response.encode().getBytes()));
                }
                else
                {
                    LOGGER.warn("Unable to read the file" + handler.cause());
                }

            });
//                }
//                catch (Exception exception)
//                {
//                    LOGGER.error("Exception occurred in local consumer", exception);
//                }

            //    });

            new Thread(() ->
            {
                try
                {
                    while (true)
                    {
                        var identity = socket.recv();

                        System.out.println("Received identity : " + new String(identity));

                        var message = socket.recv();

                        eventBus.<JsonObject>send(Constants.READ_ADDRESS, new JsonObject().put("identity", identity).put("message", new String(message)));

                    }
                }
                catch (Exception exception)
                {
                    LOGGER.error("Some exception occurred in the another thread", exception);
                }
            }).start();

        }
        catch (Exception exception)
        {
            LOGGER.error("Exception occurred", exception);

        }

    }

}
