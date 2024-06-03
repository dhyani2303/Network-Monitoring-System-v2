package org.motadata.zmq;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.motadata.constants.Constants;
import org.motadata.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;

import java.util.Base64;

public class Receiver extends AbstractVerticle
{
    public static final Logger LOGGER = LoggerFactory.getLogger(Receiver.class);

    public void start(Promise<Void> promise)
    {
        var eventBus = vertx.eventBus();
        try
        {
            var zContext = new ZContext();

            var socket = zContext.createSocket(SocketType.PULL);

            socket.bind(config().getString("receiver.address"));

            eventBus.<String>localConsumer(Constants.RECEIVE_ADDRESS,handler->
            {
                if(!handler.body().isEmpty())
                {
                    var response = new JsonObject(new String(Base64.getDecoder().decode(handler.body())));

                    var requestType = response.getString(Constants.REQUEST_TYPE);

                    if (requestType.equals(Constants.COLLECT))
                    {
                        eventBus.send(Constants.COLLECT_ADDRESS, response);

                        LOGGER.info("The request type is collect and message is sent over event bus " + response);

                    }
                    else if (requestType.equals(Constants.DISCOVERY))
                    {
                        eventBus.send(Constants.DISCOVERY_DATA_ADDRESS, response);

                        LOGGER.info("The request type is discovery and message is sent over event bus " + response);

                    }
                    else if(requestType.equals("read.file"))
                    {
                        eventBus.send("read.data.address",response);

                    }

                }


            });

            new Thread(()->
            {
                while (true)
                {
                    var message = socket.recv(0);

                    eventBus.send(Constants.RECEIVE_ADDRESS,new String(message));

                    LOGGER.info("New message is received "+ new String(message));

                }
            }).start();

            promise.complete();
        }
        catch (Exception exception)
        {
            LOGGER.error("some exception occurred",exception);

            promise.fail("exception occurred");

        }
    }
}
