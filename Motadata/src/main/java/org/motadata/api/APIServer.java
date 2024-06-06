package org.motadata.api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import org.motadata.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.ext.web.Router;
import org.motadata.constants.Constants;


public  class APIServer extends AbstractVerticle
{
    public static final Logger LOGGER = LoggerFactory.getLogger(APIServer.class);

    @Override
    public void start(Promise<Void> promise)
    {
        try
        {
            LOGGER.info("API server has been started");

            var port = Config.getPort();

            var host = Config.getHost();

            var httpServerOptions = new HttpServerOptions().setPort(port).setHost(host);

            var httpServer = vertx.createHttpServer(httpServerOptions);

            var router = Router.router(vertx);

            var credential = new Credential();

            var discovery = new Discovery();

            var provision = new Provision();

            var polling = new Polling();

            router.route(Constants.CREDENTIAL_API).subRouter(credential.getRouter());

            router.route(Constants.DISCOVERY_API).subRouter(discovery.getRouter());

            router.route(Constants.PROVISION_API).subRouter(provision.getRouter());

            router.route(Constants.POLLING_API).subRouter(polling.getRouter());

            httpServer.requestHandler(router).listen();

            LOGGER.info("Sub router of credential,discovery,polling and provision api have been deployed");

            promise.complete();

        }
        catch (Exception exception)
        {
            LOGGER.error("Some exception occurred", exception);

            promise.fail(promise.future().cause());
        }

    }

    public void stop(Promise<Void> promise)
    {
        try
        {
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
