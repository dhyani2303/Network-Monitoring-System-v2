package org.motadata.api;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.motadata.Bootstrap;
import org.motadata.constants.Constants;
import org.motadata.database.Provision;
import org.motadata.util.Config;
import org.motadata.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

public class Polling
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Polling.class);

    private final Router router;

    private static final Provision provisionDatabase = Provision.getProvision();

    private final EventBus eventBus;

    private final JsonObject polledData = new JsonObject();

    public Polling()
    {
        router = Router.router(Bootstrap.getVertx());

        router.route().handler(BodyHandler.create());

        eventBus = Bootstrap.getVertx().eventBus();
    }

    public Router getRouter()
    {
        try
        {
            router.get(Constants.API_WITH_TWO_PARAMS).handler(this::getPolledData);

            router.get(Constants.API_WITH_PARAMS).handler(this::getAllPolledData);

        }
        catch (Exception exception)
        {
            LOGGER.error("Some exception occurred in get router method", exception);

        }

        return router;
    }

    private void getPolledData(RoutingContext context)
    {
        var response = new JsonObject();

        try
        {
            var id = context.pathParam(Constants.ID);

            var time = context.pathParam(Constants.TIME);

            if (provisionDatabase.verify(Long.parseLong(id)))
            {
                var currentTime = System.currentTimeMillis();

                var data = new JsonObject().put(Constants.TIMESTAMP, (currentTime - (Long.parseLong(time) * 60000))).put(Constants.IP_ADDRESS, provisionDatabase.get(Long.parseLong(id)).getString(Constants.IP_ADDRESS)).put(Constants.REQUEST_TYPE,"read.file");

                eventBus.send("db", Base64.getEncoder().encodeToString(data.encode().getBytes()));

                response.put(Constants.STATUS,Constants.SUCCESS);

                response.put(Constants.ERROR_CODE,Constants.SUCCESS_CODE);

                response.put(Constants.MESSAGE,"The output can be seen in log file");

                context.response().setStatusCode(200).end(response.encodePrettily());

                //cannot send the data in api as the datastorage is connected with zmq. in case of ui data can be sent using web socket

            }
            else
            {
                response = Utils.errorHandler("Invalid ID", "The id does not exist", Constants.INVALID_PROVISION_ID);

                context.response().setStatusCode(400).end(response.encodePrettily());

                LOGGER.warn("Get Polled Data method returned with status fail as the id did not exist");
            }

        }
        catch (Exception exception)
        {
            response = Utils.errorHandler("Internal Server Error", "Some problem occurred", Constants.EXCEPTION);

            context.response().setStatusCode(500).end(response.encodePrettily());

            LOGGER.error("Some exception occurred in getPolledData method", exception);
        }
    }

    private void getAllPolledData(RoutingContext context)
    {
        var response = new JsonObject();

        try
        {
            var id = context.pathParam(Constants.ID);

            if (provisionDatabase.verify(Long.parseLong(id)))
            {

                var data = new JsonObject().put(Constants.TIMESTAMP, Config.getDataDefaultTime()).put(Constants.IP_ADDRESS, provisionDatabase.get(Long.parseLong(id)).getString(Constants.IP_ADDRESS)).put(Constants.REQUEST_TYPE,"read.file");

                eventBus.send("db", Base64.getEncoder().encodeToString(data.encode().getBytes()));

                response.put(Constants.STATUS,Constants.SUCCESS);

                response.put(Constants.ERROR_CODE,Constants.SUCCESS_CODE);

                response.put(Constants.MESSAGE,"The output can be seen in log file");

                context.response().setStatusCode(200).end(response.encodePrettily());

                //cannot send the data in api as the datastorage is connected with zmq. in case of ui data can be sent using web socket

            }
            else
            {
                response = Utils.errorHandler("Invalid ID", "The id does not exist", Constants.INVALID_PROVISION_ID);

                context.response().setStatusCode(400).end(response.encodePrettily());

                LOGGER.warn("Get Polled Data method returned with status fail as the id did not exist while serving get all polled data method");
            }

        }
        catch (Exception exception)
        {
            response = Utils.errorHandler("Internal Server Error", "Some problem occurred", Constants.EXCEPTION);

            context.response().setStatusCode(500).end(response.encodePrettily());

            LOGGER.error("Some exception occurred in getAllPolledData method", exception);
        }

    }

}
