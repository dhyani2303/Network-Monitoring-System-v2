package org.motadata;


import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
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

        var pluginEngineSender = new JsonObject().put("sender.address","tcp://localhost:5587").put("event.type","plugin");

        var pluginEngineReceiver = new JsonObject().put("receiver.address","tcp://localhost:5588").put("event.type","plugin");

        var fileWriteSender = new JsonObject().put("sender.address","tcp://localhost:5585").put("event.type","db");

        var fileReadReceiver = new JsonObject().put("receiver.address","tcp://localhost:5586").put("event.type","db");


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

                                        vertx.deployVerticle(Sender.class.getName(),new DeploymentOptions().setConfig(pluginEngineSender)).onComplete(senderHandler ->
                                        {

                                            LOGGER.info("Sender verticle has been deployed");

                                            vertx.deployVerticle(Receiver.class.getName(), new DeploymentOptions().setConfig(pluginEngineReceiver),receiverHandler ->
                                            {

                                                LOGGER.info("Receiver verticle has been deployed");
                                            });
                                        });

                                        vertx.deployVerticle(Sender.class.getName(),new DeploymentOptions().setConfig(fileWriteSender)).onComplete(sendHandler->{

                                            LOGGER.info("Another sender handler has been deployed");

                                            vertx.deployVerticle(Receiver.class.getName(),new DeploymentOptions().setConfig(fileReadReceiver)).onComplete(receiveHandler->{

                                                LOGGER.info("Another receiver handler has been deployed");
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
