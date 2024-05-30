package org.polleddatastore;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonObject;
import org.polleddatastore.constants.Constants;
import org.polleddatastore.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;

import java.util.Base64;

public class WriteFile extends AbstractVerticle
{
    public static final Logger LOGGER = LoggerFactory.getLogger(WriteFile.class);

    public void start(Promise<Void> promise)
    {
        var eventBus = vertx.eventBus();
        try
        {
            eventBus.localConsumer(Constants.WRITE_ADDRESS, consumerHandler ->
            {
                var message = consumerHandler.body().toString();

                var contextResult = new JsonObject(new String(Base64.getDecoder().decode(message)));

                var file = Util.configMap.get(Constants.RESULT_PATH) + contextResult.getString(Constants.IP_ADDRESS) + ".json";

                    vertx.executeBlocking(id ->
                    {
                        try
                        {
                            var asyncFile = vertx.fileSystem().openBlocking(file, new OpenOptions().setAppend(true).setCreate(true));

                            var offset = asyncFile.getWritePos();

                            Buffer buffer;

                            if (offset == 0)
                            {
                                buffer = Buffer.buffer("{" + "\n")
                                        .appendString("\"" + contextResult.getString("timestamp") + "\":")
                                        .appendBuffer(Buffer.buffer(contextResult.encodePrettily()))
                                        .appendString("\n" + "}");
                            }
                            else
                            {
                                offset = offset - 2;

                                buffer = Buffer.buffer("," + "\n")
                                        .appendString("\"" + contextResult.getString("timestamp") + "\":")
                                        .appendBuffer(Buffer.buffer(contextResult.encodePrettily()))
                                        .appendString("\n" + "}");

                            }
                            asyncFile.write(buffer, offset, wHandler ->
                            {
                                LOGGER.info("File is successfully written");

                            });
                            asyncFile.close();
                        }
                        catch (Exception exception)
                        {
                            LOGGER.error("Some exception occurred inside execute blocking",exception);
                        }

                    });

            });

            try
            {
                ZContext context = new ZContext();

                var socket = context.createSocket(SocketType.PULL);

                socket.connect(Util.configMap.get(Constants.ZMQ_WRITE_ADDRESS).toString());

                new Thread(() ->
                {
                    while (true)
                    {
                        var message = socket.recv();

                        eventBus.send(Constants.WRITE_ADDRESS, new String(message));

                    }
                }).start();

            }
            catch (Exception exception)
            {
                System.out.println(exception);
            }

            promise.complete();

        }
        catch (Exception exception)
        {
            LOGGER.error("Exception occurred",exception);

            promise.fail("exception occurred");

        }

    }

    public void stop(Promise<Void> stopPromise)
    {
        stopPromise.complete();
    }
}
