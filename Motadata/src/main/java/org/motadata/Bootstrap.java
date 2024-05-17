package org.motadata;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.motadata.api.APIServer;
import org.motadata.database.Database;
import org.motadata.engine.DiscoveryEngine;
import org.motadata.engine.PollingEngine;
import org.motadata.util.Constants;
import org.motadata.util.Utils;
import org.slf4j.LoggerFactory;

public class Bootstrap {

    public static void main(String[] args) {


        var LOGGER = LoggerFactory.getLogger(Bootstrap.class);

        Vertx vertx = Vertx.vertx(new VertxOptions().setInternalBlockingPoolSize(10));

        var verticleDeployementOptions = new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER);

        Database.createDatabase(Constants.DISCOVERY_DATABASE);

        Database.createDatabase(Constants.CREDENTIAL_DATABASE);

        Utils.setConfig();

        vertx.deployVerticle(APIServer.class.getName()).onComplete(handler->

                LOGGER.info("API server has been deployed"));

        vertx.deployVerticle(DiscoveryEngine.class.getName(),verticleDeployementOptions).onComplete(handler ->

            LOGGER.info("Discovery Engine has been deployed")
        );

        vertx.deployVerticle(PollingEngine.class.getName(),verticleDeployementOptions).onComplete(handler->{

            LOGGER.info("Polling Engine has been deployed");

        });

    }
}
