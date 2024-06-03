package org.motadata.api;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.motadata.constants.Constants;
import org.motadata.database.Provision;
import org.motadata.util.Handler;
import org.motadata.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;

import java.time.LocalTime;
import java.util.Base64;

public class Polling
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Polling.class);

    private Router router;

    private static final Provision provisionDatabase = Provision.getProvision();

    private Vertx vertx;


    public void init(Vertx vertx)
    {
        try
        {
            this.vertx = vertx;

            router = Router.router(vertx);

            router.route().handler(BodyHandler.create());
        }
        catch (Exception exception)
        {
            LOGGER.error("Some exception occurred in init method",exception);
        }
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
            LOGGER.error("Some exception occurred in get router method",exception);

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

                var contextToSend = new JsonObject().put(Constants.TIMESTAMP,(currentTime - (Long.parseLong(time)*60000))).put(Constants.IP_ADDRESS,provisionDatabase.get(Long.parseLong(id)).getString(Constants.IP_ADDRESS));

                try
                {

                  var result=  collectData(contextToSend);

                  if (result.containsKey(Constants.RESULT))
                  {
                      response.put(Constants.STATUS, Constants.SUCCESS);

                      response.put(Constants.ERROR_CODE, Constants.SUCCESS_CODE);

                      response.put(Constants.RESULT, result.getValue(Constants.RESULT));

                      context.response().setStatusCode(200).end(response.encodePrettily());

                  }
                  else
                  {
                      response = Handler.errorHandler("Internal Server Error","Some problem occurred",Constants.EXCEPTION);

                      context.response().setStatusCode(500).end(response.encodePrettily());

                      LOGGER.error("The output of collect method came out to be null.");

                  }

                }
                catch (Exception exception)
                {
                    response = Handler.errorHandler("Internal Server Error","Some problem occurred",Constants.EXCEPTION);

                    context.response().setStatusCode(500).end(response.encodePrettily());

                    LOGGER.error("Some exception occurred inside the zContext block",exception);
                }
            }
            else
            {
                response = Handler.errorHandler("Invalid ID","The id does not exist",Constants.INVALID_PROVISION_ID);

                context.response().setStatusCode(400).end(response.encodePrettily());

                LOGGER.warn("Get Polled Data method returned with status fail as the id did not exist");
            }

        }
        catch (Exception exception)
        {
            response = Handler.errorHandler("Internal Server Error","Some problem occurred",Constants.EXCEPTION);

            context.response().setStatusCode(500).end(response.encodePrettily());

            LOGGER.error("Some exception occurred in getPolledData method",exception);
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

                var contextToSend = new JsonObject().put(Constants.TIMESTAMP,Utils.configMap.get(Constants.DATA_DEFAULT_TIME)).put(Constants.IP_ADDRESS,provisionDatabase.get(Long.parseLong(id)).getString(Constants.IP_ADDRESS));

                try
                {
                    var result=  collectData(contextToSend);

                    if (result!=null)
                    {

                        response.put(Constants.STATUS, Constants.SUCCESS);

                        response.put(Constants.ERROR_CODE, Constants.SUCCESS_CODE);

                        response.put(Constants.RESULT, result);

                        context.response().setStatusCode(200).end(response.encodePrettily());


                    }
                    else
                    {
                        response = Handler.errorHandler("Internal Server Error","Some problem occurred",Constants.EXCEPTION);

                        context.response().setStatusCode(500).end(response.encodePrettily());

                        LOGGER.error("The output of collect method came out to be null while serving get all polled data.");

                    }

                }
                catch (Exception exception)
                {
                    response = Handler.errorHandler("Internal Server Error","Some problem occurred",Constants.EXCEPTION);

                    context.response().setStatusCode(500).end(response.encodePrettily());

                    LOGGER.error("Some exception occurred inside the zContext block",exception);
                }
            }
            else
            {
                response = Handler.errorHandler("Invalid ID","The id does not exist",Constants.INVALID_PROVISION_ID);

                context.response().setStatusCode(400).end(response.encodePrettily());

                LOGGER.warn("Get Polled Data method returned with status fail as the id did not exist while serving get all polled data method");
            }

        }
        catch (Exception exception)
        {
            response = Handler.errorHandler("Internal Server Error","Some problem occurred",Constants.EXCEPTION);

            context.response().setStatusCode(500).end(response.encodePrettily());

            LOGGER.error("Some exception occurred in getAllPolledData method",exception);
        }

    }



    private JsonObject collectData(JsonObject context)
    {
        try
        {
            context.put(Constants.REQUEST_TYPE,"read.file");

            vertx.eventBus().send("db",Base64.getEncoder().encodeToString(context.encode().getBytes()));

            vertx.eventBus().<JsonObject>localConsumer("read.data.address",handler->{

                System.out.println(handler.body());

                context.put(Constants.RESULT,handler.body());

            });
//            var zContext = new ZContext();
//
//            var socket = zContext.createSocket(SocketType.DEALER);
//
//            socket.bind("tcp://localhost:5586");
//
//            var encodedContext = Base64.getEncoder().encodeToString(context.encode().getBytes());
//
//            socket.send(encodedContext);
//
//            var result = new JsonObject(new String(Base64.getDecoder().decode( socket.recv())));
//
//            socket.close();
//
//            return result;

        }
        catch (Exception exception)
        {
        LOGGER.error("Some exception occurred inside the collect data method",exception);
        }

        return context;
    }

}
