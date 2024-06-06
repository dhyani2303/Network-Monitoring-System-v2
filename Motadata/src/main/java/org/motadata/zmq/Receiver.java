package org.motadata.zmq;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.motadata.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import java.util.Base64;


public class Receiver extends AbstractVerticle
{
    public static final Logger LOGGER = LoggerFactory.getLogger(Receiver.class);

    private final ZContext zContext = new ZContext();

    private final ZMQ.Socket socket = zContext.createSocket(SocketType.PULL);

    public void start(Promise<Void> promise)
    {
        var eventBus = vertx.eventBus();
        try
        {
            socket.bind(config().getString(Constants.RECEIVER_ADDRESS));

            eventBus.<String>localConsumer(Constants.RECEIVE_ADDRESS, handler ->
            {
                if (!handler.body().isEmpty())
                {
                    var response = new JsonObject(new String(Base64.getDecoder().decode(handler.body())));

                    var requestType = response.getString(Constants.REQUEST_TYPE);

                    switch (requestType)
                    {
                        case Constants.COLLECT ->
                        {
                            eventBus.send(Constants.COLLECT_ADDRESS, response);

                            LOGGER.info("The request type is collect and message is sent over event bus ");
                        }
                        case Constants.DISCOVERY ->
                        {
                            eventBus.send(Constants.DISCOVERY_DATA_ADDRESS, response);

                            LOGGER.info("The request type is discovery and message is sent over event bus ");
                        }
                        case Constants.READ_FILE ->
                        {
                            eventBus.send("read.data.address", response);

                            LOGGER.info("The request type is read file and message is sent over event bus ");
                        }
                    }
                }
            });

            new Thread(() ->
            {
                try
                {
                    while (!Thread.currentThread().isInterrupted())
                    {
                        var message = socket.recv(0);

                        eventBus.send(Constants.RECEIVE_ADDRESS, new String(message));

                        LOGGER.trace("New message is received");
                    }
                }
                catch (Exception exception)
                {
                    if (zContext.isClosed())
                    {
                        LOGGER.info("Context is closed");
                    }
                    else
                    {
                        LOGGER.error("Exception occurred inside the new thread ", exception);
                    }

                }
            }).start();

            promise.complete();
        }
        catch (Exception exception)
        {
            LOGGER.error("some exception occurred", exception);

            promise.fail("exception occurred");

        }
    }
    public void stop(Promise<Void> promise)
    {
        try
        {
            if ((!zContext.isClosed()))
            {
                LOGGER.debug("Closing zmq context");

                zContext.close();
            }
            if (socket != null)
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
