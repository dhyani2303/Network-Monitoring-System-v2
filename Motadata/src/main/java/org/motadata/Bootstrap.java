package org.motadata;


import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.motadata.api.APIServer;
import org.motadata.constants.Constants;
import org.motadata.engine.DiscoveryEngine;
import org.motadata.engine.PollingEngine;
import org.motadata.util.Config;
import org.motadata.zmq.Receiver;
import org.motadata.zmq.Sender;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

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

        var deploymentIds = new ArrayList<String>();

        try
        {
            Config.setConfig().onComplete(configHandler ->
            {
                var pluginEngineSender = new JsonObject().put(Constants.SENDER_ADDRESS, Config.getZMQPluginSenderAddress()).put(Constants.EVENT_TYPE, Constants.PLUGIN);

                var pluginEngineReceiver = new JsonObject().put(Constants.RECEIVER_ADDRESS, Config.getZMQPluginReceiverAddress()).put(Constants.EVENT_TYPE, Constants.PLUGIN);

                var fileWriteSender = new JsonObject().put(Constants.SENDER_ADDRESS, Config.getZMQDBSenderAddress()).put(Constants.EVENT_TYPE, Constants.DB);

                var fileReadReceiver = new JsonObject().put(Constants.RECEIVER_ADDRESS, Config.getZMQDBReceiverAddress()).put(Constants.EVENT_TYPE, Constants.DB);

                vertx.deployVerticle(APIServer.class.getName()).onComplete(handler ->
                        {
                            LOGGER.info("API server has been deployed");

                            deploymentIds.add(handler.result());

                            vertx.deployVerticle(DiscoveryEngine.class.getName()).onComplete(discoveryEngineHandler ->
                                    {
                                        LOGGER.info("Discovery Engine has been deployed");

                                        deploymentIds.add(discoveryEngineHandler.result());

                                        vertx.deployVerticle(PollingEngine.class.getName()).onComplete(pollingEngineHandler ->
                                        {
                                            LOGGER.info("Polling Engine has been deployed");

                                            deploymentIds.add(pollingEngineHandler.result());

                                            vertx.deployVerticle(Sender.class.getName(), new DeploymentOptions().setConfig(pluginEngineSender)).onComplete(senderHandler ->
                                            {

                                                LOGGER.info("Sender verticle has been deployed");

                                                deploymentIds.add(senderHandler.result());

                                                vertx.deployVerticle(Receiver.class.getName(), new DeploymentOptions().setConfig(pluginEngineReceiver), receiverHandler ->
                                                {

                                                    LOGGER.info("Receiver verticle has been deployed");

                                                    deploymentIds.add(receiverHandler.result());

                                                });
                                            });

                                            vertx.deployVerticle(Sender.class.getName(), new DeploymentOptions().setConfig(fileWriteSender)).onComplete(sendHandler ->
                                            {

                                                LOGGER.info("Another sender handler has been deployed");

                                                deploymentIds.add(sendHandler.result());

                                                vertx.deployVerticle(Receiver.class.getName(), new DeploymentOptions().setConfig(fileReadReceiver)).onComplete(receiveHandler ->
                                                {
                                                    LOGGER.info("Another receiver handler has been deployed");

                                                    deploymentIds.add(receiveHandler.result());

                                                });
                                            });

                                        });

                                    }
                            );

                        }

                );
            });

            Runtime.getRuntime().addShutdownHook(new Thread(() ->

                    deploymentIds.forEach(vertx::undeploy)
            ));
        }

        catch (Exception exception)
        {
            LOGGER.error("Some exception occurred", exception);
        }
    }
}
