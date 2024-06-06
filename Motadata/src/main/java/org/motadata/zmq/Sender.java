package org.motadata.zmq;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.motadata.constants.Constants;
import org.motadata.util.ProcessUtil;
import org.motadata.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Base64;

public class Sender extends AbstractVerticle
{
    public static final Logger LOGGER = LoggerFactory.getLogger(Sender.class);

    public ZContext zContext = new ZContext();

    public ZMQ.Socket socket = zContext.createSocket(SocketType.PUSH);


    public void start(Promise<Void> promise)
    {
        var eventBus = vertx.eventBus();
        try
        {

            socket.bind(config().getString(Constants.SENDER_ADDRESS));

            eventBus.<String>localConsumer(config().getString(Constants.EVENT_TYPE), handler->{

                LOGGER.trace("New message to send has arrived");

                    socket.send(handler.body(),1);

                    LOGGER.trace("message has been sent to socket");
            });

            promise.complete();
        }
        catch (Exception exception)
        {
            LOGGER.error("Some exception occurred",exception);

            promise.fail("exception occurred");

        }
    }

    public void stop(Promise<Void> promise)
    {
        try
        {
            if ((!zContext.isClosed())&& zContext!=null)
            {
                LOGGER.debug("Closing zmq context");

                zContext.close();
            }
            if (socket!=null)
            {
                LOGGER.debug("Closing socket");

                socket.close();
            }
            promise.complete();

            LOGGER.info("stop method is called");
        }
        catch (Exception exception)
        {
            LOGGER.error("Some exception occurred", exception);

            promise.fail(exception);

        }

    }


}
