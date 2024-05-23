package org.motadata;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.motadata.api.APIServer;
import org.motadata.database.Database;

import org.motadata.constants.Constants;
import org.motadata.engine.DiscoveryEngine;
import org.motadata.engine.PollingEngine;
import org.motadata.util.Utils;
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

                                    vertx.deployVerticle(PollingEngine.class.getName()).onComplete(pollingEnginehHandler ->

                                            LOGGER.info("Polling Engine has been deployed")
                                    );

                                }
                        );

                    }));
        }
        catch (Exception exception)
        {
            LOGGER.error("Some exception occurred", exception);
        }
    }
}
