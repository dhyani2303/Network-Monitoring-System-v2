package org.motadata.zmq;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import org.motadata.constants.Constants;
import org.motadata.util.ProcessUtil;
import org.motadata.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;

import java.util.Base64;

public class Sender extends AbstractVerticle
{
    public static final Logger LOGGER = LoggerFactory.getLogger(Sender.class);

    public void start(Promise<Void> promise)
    {
        var eventBus = vertx.eventBus();
        try
        {
            var zContext = new ZContext();

            var socket = zContext.createSocket(SocketType.PUSH);

            socket.bind(Utils.configMap.get(Constants.ZMQ_SENDER_ADDRESS).toString());

            eventBus.<JsonArray>localConsumer(Constants.SEND_ADDRESS, handler->{

                String encodedContext = Base64.getEncoder().encodeToString(handler.body().toString().getBytes());

                socket.send(encodedContext);

            });

            promise.complete();
        }
        catch (Exception exception)
        {
            LOGGER.error("Some exception occurred",exception);

            promise.fail("exception occurred");

        }
    }

    public void stop(Promise<Void> stopPromise)
    {
        stopPromise.complete();
    }

}
