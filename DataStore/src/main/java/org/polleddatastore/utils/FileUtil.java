package org.polleddatastore.utils;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonObject;
import org.polleddatastore.Main;
import org.polleddatastore.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

public class FileUtil
{
    public static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    private static final Vertx vertx = Main.getVertx();

    private static EventBus eventBus = vertx.eventBus();


    public static void readFile(JsonObject context)
    {
        var response = new JsonObject();

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
                    try
                    {
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

                        context.put("result",response);
                        eventBus.send(Constants.READ_ADDRESS, Base64.getEncoder().encodeToString(context.encodePrettily().getBytes()));

                        LOGGER.trace("Content has been sent over event bus");

                    }
                    catch (Exception exception)
                    {
                        LOGGER.error("Some exception occurred inside the handler",exception);
                    }
//                    socket.send(context.getBinary("identity"), ZMQ.SNDMORE);
//
//                    socket.send(Base64.getEncoder().encodeToString(response.encode().getBytes()));
                }
                else
                {
                    LOGGER.warn("Unable to read the file" + handler.cause());

                    response.put("Error","Unable to read file");

                    eventBus.send(Constants.READ_ADDRESS,Base64.getEncoder().encodeToString(response.encodePrettily().getBytes()));

                }

            });
//                }
//                catch (Exception exception)
//                {
//                    LOGGER.error("Exception occurred in local consumer", exception);
//                }

            //    });

//            new Thread(() ->
//            {
//                try
//                {
//                    while (true)
//                    {
//                        var identity = socket.recv();
//
//                        System.out.println("Received identity : " + new String(identity));
//
//                        var message = socket.recv();
//
//                        eventBus.<JsonObject>send(Constants.READ_ADDRESS, new JsonObject().put("identity", identity).put("message", new String(message)));
//
//                    }
//                }
//                catch (Exception exception)
//                {
//                    LOGGER.error("Some exception occurred in the another thread", exception);
//                }
//            }).start();

        }
        catch (Exception exception)
        {
            LOGGER.error("Exception occurred", exception);
        }

    }
    public static void write(JsonObject context)
    {
        try
        {
            var file = Util.configMap.get(Constants.RESULT_PATH) + context.getString(Constants.IP_ADDRESS) + ".json";

            var asyncFile = vertx.fileSystem().openBlocking(file, new OpenOptions().setAppend(true).setCreate(true));

            var offset = asyncFile.getWritePos();

            Buffer buffer;

            if (offset == 0)
            {
                buffer = Buffer.buffer("{" + "\n")
                        .appendString("\"" + context.getString("timestamp") + "\":")
                        .appendBuffer(Buffer.buffer(context.encodePrettily()))
                        .appendString("\n" + "}");
            }
            else
            {
                offset = offset - 2;

                buffer = Buffer.buffer("," + "\n")
                        .appendString("\"" + context.getString("timestamp") + "\":")
                        .appendBuffer(Buffer.buffer(context.encodePrettily()))
                        .appendString("\n" + "}");

            }
            asyncFile.write(buffer, offset, handler ->
            {
                if (handler.succeeded())
                {
                    LOGGER.trace("File is successfully written");
                }
                else
                {
                    LOGGER.warn("Some error occurred in writing file {}",handler.cause().toString());
                }

            });
            asyncFile.close();
        }
        catch (Exception exception)
        {
            LOGGER.error("Exception occurred", exception);
        }

    }


}
