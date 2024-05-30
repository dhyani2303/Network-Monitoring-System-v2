package org.motadata;


import io.vertx.core.Vertx;
import org.motadata.api.APIServer;
import org.motadata.engine.DiscoveryEngine;
import org.motadata.engine.PollingEngine;
import org.motadata.util.Utils;
import org.motadata.zmq.Receiver;
import org.motadata.zmq.Sender;
import org.slf4j.LoggerFactory;

public class Bootstrap
{
    private static final Vertx vertx = Vertx.vertx();

    public static Vertx getVertx()
    {

        return vertx;
    }

    public static void main(String[] args)
    {

        var LOGGER = LoggerFactory.getLogger(Bootstrap.class);

        try
        {
            Utils.setConfig(vertx).onComplete(configHandler ->


                    vertx.deployVerticle(APIServer.class.getName()).onComplete(handler ->
                    {

                        LOGGER.info("API server has been deployed");

                        vertx.deployVerticle(DiscoveryEngine.class.getName()).onComplete(discoveryEngineHandler ->
                                {

                                    LOGGER.info("Discovery Engine has been deployed");

                                    vertx.deployVerticle(PollingEngine.class.getName()).onComplete(pollingEngineHandler ->
                                    {
                                        LOGGER.info("Polling Engine has been deployed");

                                        vertx.deployVerticle(Sender.class.getName()).onComplete(senderHandler ->
                                        {

                                            LOGGER.info("Sender verticle has been deployed");

                                            vertx.deployVerticle(Receiver.class.getName(), receiverHandler ->
                                            {

                                                LOGGER.info("Receiver verticle has been deployed");
                                            });
                                        });

                                    });

                                }
                        );

                    }
                    ));
        }
        catch (Exception exception)
        {
            LOGGER.error("Some exception occurred", exception);
        }
    }
}
