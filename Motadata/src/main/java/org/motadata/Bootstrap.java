package org.motadata;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import org.motadata.api.APIServer;
import org.motadata.database.Database;
import org.motadata.engine.DiscoveryEngine;
import org.motadata.util.Constants;
import org.motadata.util.Utils;

public class Bootstrap {

    public static void main(String[] args) {


        Vertx vertx = Vertx.vertx();

        var verticleDeployementOptions = new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER);

        Database.createDatabase(Constants.DISCOVERYDATABASE);

        Database.createDatabase(Constants.CREDENTIALDATABASE);

        Database.createDatabase(Constants.VALID_CREDENTIALS);

        Utils.setConfig();

        vertx.deployVerticle(APIServer.class.getName()).onComplete(handler -> System.out.println("Successful"));

        vertx.deployVerticle(DiscoveryEngine.class.getName(),verticleDeployementOptions).onComplete(handler -> System.out.println("Discovery Engine deployed successfully"));

    }
}
