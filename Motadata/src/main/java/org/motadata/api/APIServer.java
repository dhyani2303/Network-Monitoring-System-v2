package org.motadata.api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.ext.web.Router;

import org.motadata.util.Utils;
import org.motadata.constants.Constants;


public class APIServer extends AbstractVerticle
{

    public static final Logger LOGGER = LoggerFactory.getLogger(APIServer.class);

    @Override
    public void start(Promise<Void> promise)
    {

        try
        {
            LOGGER.info("API server has been started");

            var port = Integer.parseInt(Utils.configMap.get(Constants.HTTP_PORT).toString());

            var host = Utils.configMap.get(Constants.HTTP_HOST).toString();

            var httpServerOptions = new HttpServerOptions().setPort(port).setHost(host);

            var httpServer = vertx.createHttpServer(httpServerOptions);

            var router = Router.router(vertx);

            var credential = new Credential();

            var discovery = new Discovery();

            var provision = new Provision();

            credential.init(vertx);

            discovery.init(vertx);

            provision.init(vertx);

            router.route(Constants.CREDENTIAL_API).subRouter(credential.getRouter());

            router.route(Constants.DISCOVERY_API).subRouter(discovery.getRouter());

            router.route(Constants.PROVISION_API).subRouter(provision.getRouter());

            httpServer.requestHandler(router).listen();

            LOGGER.info("Subrouter of credential,discovery and provision api have been deployed");

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
        }
        catch (Exception exception)
        {
            LOGGER.error("Some exception occurred", exception);

        }
    }

}
