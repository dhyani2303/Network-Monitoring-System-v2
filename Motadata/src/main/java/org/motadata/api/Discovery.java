package org.motadata.api;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.motadata.database.Database;
import org.motadata.constants.Constants;
import org.motadata.util.Handler;
import org.motadata.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Discovery {

    public static final Database discoveryDatabase = Database.getDatabase(Constants.DISCOVERY_DATABASE);

    public static final Database credentialDatabase = Database.getDatabase(Constants.CREDENTIAL_DATABASE);

    public static final Logger LOGGER = LoggerFactory.getLogger(Discovery.class);

//    public static JsonObject deleteDiscovery(String id)
//    {
//
//        var response = new JsonObject();
//
//        var idVerification = discoveryDatabase.verify(Long.parseLong(id));
//
//        if (idVerification)
//        {
//            var result = discoveryDatabase.delete(Long.parseLong(id));
//
//            if (result)
//            {
//                response.put(Constants.ERROR_CODE, Constants.SUCCESS_CODE);
//
//                response.put(Constants.MESSAGE, "Successfully deleted the discovery profile");
//
//                response.put(Constants.STATUS, Constants.SUCCESS);
//
//                LOGGER.info("Deletion successful of id {}",id);
//
//            }
//            else
//            {
//                response.put(Constants.ERROR_MESSAGE, "Device is already provisioned");
//
//                response.put(Constants.ERROR, "Device Provisioned");
//
//                response.put(Constants.ERROR_CODE, Constants.ALREADY_PROVISION);
//
//                response.put(Constants.STATUS, Constants.FAIL);
//
//                LOGGER.info("Unable to delete as the discovery id is already provisioned",id);
//            }
//
//        }
//        else
//        {
//            response.put(Constants.ERROR_MESSAGE, "Id does not exist");
//
//            response.put(Constants.ERROR, "Invalid Id");
//
//            response.put(Constants.ERROR_CODE, Constants.INVALID_DISCOVERY_ID);
//
//            response.put(Constants.STATUS, Constants.FAIL);
//
//            LOGGER.info("Unable to delete as the discovery id is invalid id: {}",id);
//
//        }
//        return response;
//
//    }
    private Router router;

    private EventBus eventBus;

    public void setRouter(Vertx vertx)
    {
        router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        eventBus = vertx.eventBus();
    }

    public Router getRouter()
    {
        router.post(Constants.ROUTE_PATH).handler(this::createDiscovery);

        router.get(Constants.ROUTE_PATH).handler(this::getDiscoveries);

        router.get(Constants.API_WITH_PARAMS).handler(this::getDiscovery);

        router.put(Constants.API_WITH_PARAMS).handler(this::updateDiscovery);

        router.post(Constants.RUN_API).handler(this::discoveryRun);


        return router;
    }

    private void createDiscovery(RoutingContext context)
    {
        LOGGER.info("Post request for  {} has come", Constants.DISCOVERY_API + Constants.ROUTE_PATH);

        var data = context.body().asJsonObject();

        var response = new JsonObject();

        try
        {
            if (data.isEmpty())
            {
                response = Handler.errorHandler("Empty Body", "Request body is empty", Constants.EMPTY_BODY);

                LOGGER.info("Post request  has been served for {} with result {}", Constants.DISCOVERY_API + Constants.ROUTE_PATH, response);

                context.response().setStatusCode(400).end(response.encodePrettily());

            }
            else
            {
                if (data.containsKey(Constants.IP_ADDRESS) && data.containsKey(Constants.PORT) && data.containsKey(Constants.CREDENTIAL_IDS) && data.containsKey(Constants.NAME) &&
                        (!(data.getString(Constants.IP_ADDRESS).isEmpty())) && (!(data.getJsonArray(Constants.CREDENTIAL_IDS).isEmpty())) && (!(data.getString(Constants.PORT).isEmpty())) && (!(data.getString(Constants.NAME).isEmpty())))
                {
                    if (!discoveryDatabase.verify(data.getString(Constants.NAME)))
                    {
                        var credentialProfiles = data.getJsonArray(Constants.CREDENTIAL_IDS);

                        for (var credentialId : credentialProfiles)
                        {
                            if (!credentialDatabase.verify(Long.parseLong(credentialId.toString())))
                            {
                                response = Handler.errorHandler("Wrong Credentials", "One of the credential Id is incorrect", Constants.INCORRECT_DISCOVERY);

                                LOGGER.info("Creation of discovery profile failed because one of the credential Id is incorrect");

                                context.response().setStatusCode(400).end(response.encodePrettily());

                                return;
                            }
                        }

                        data.put(Constants.VALID_CREDENTIAL_ID,"");

                        var id = discoveryDatabase.create(data);

                        response.put(Constants.DISCOVERY_ID, id);

                        response.put(Constants.STATUS, Constants.SUCCESS);

                        response.put(Constants.ERROR_CODE, Constants.SUCCESS_CODE);

                        LOGGER.info("Discovery profile created  successfully with id {}", id);

                        context.response().setStatusCode(200).end(response.encodePrettily());

                    }
                    else
                    {
                        response = Handler.errorHandler("Duplicate Discovery Name", "Discovery profile with the name already exists", Constants.DUPLICATE_DISCOVERY_NAME);

                        context.response().setStatusCode(400).end(response.encodePrettily());

                        LOGGER.info("Creation of discovery profile failed because discovery profile name already exists");
                    }

                }
                else
                {
                    response = Handler.errorHandler("Invalid Context", "Some parameters are empty of missing", Constants.INCORRECT_DISCOVERY);

                    context.response().setStatusCode(400).end(response.encodePrettily());

                    LOGGER.info("Creation of discovery profile failed because either ip or port profile or discovery name is not present");
                }
            }

        }
        catch(Exception exception)
        {

        response = Handler.errorHandler("Exception occurred", exception.getMessage(), Constants.EXCEPTION);

        context.response().setStatusCode(500).end(response.encodePrettily());

        LOGGER.info("Post request  has been served for {}  with result {}", Constants.DISCOVERY_API + Constants.ROUTE_PATH, response);

        LOGGER.error("Exception occurred {}", exception.getMessage());
    }

}

    private void getDiscoveries(RoutingContext context)
    {
        LOGGER.info("Get request for {} has come", Constants.DISCOVERY_API + Constants.ROUTE_PATH);

        var response = new JsonObject();

        try {
            System.out.println(discoveryDatabase.get().isEmpty());

            var result = discoveryDatabase.get();

            if (result != null)
            {
                if (result.isEmpty())
                {
                    LOGGER.info("Unable to get the discovery details as there are no credential profiles");

                    response.put(Constants.MESSAGE, "No discovery profiles are present");
                }

                response.put(Constants.RESULT, result);

                response.put(Constants.STATUS, Constants.SUCCESS);

                response.put(Constants.ERROR_CODE, Constants.SUCCESS_CODE);

                LOGGER.info("Get request for {} has been served with result {}", Constants.DISCOVERY_API, response);

                context.response().setStatusCode(200).end(response.encodePrettily());
            }
            else
            {
                response = Handler.errorHandler("Failure", "Failed to get the credential profiles", Constants.EXCEPTION);

                LOGGER.error("Some exception might have occurred while fetching the data as the result is null");

                context.response().setStatusCode(500).end(response.encodePrettily());

            }
        } catch (Exception exception)
        {
            response = Handler.errorHandler("Exception occurred", exception.getMessage(), Constants.EXCEPTION);

            context.response().setStatusCode(500).end(response.encodePrettily());

            LOGGER.info("Get request  has been served for {}  with result {}", Constants.DISCOVERY_API + Constants.ROUTE_PATH, response);

            LOGGER.error("Exception occurred {}", exception.getMessage());

        }
    }

    private void getDiscovery(RoutingContext context)
    {
        var response = new JsonObject();

        try {
            var discoveryId = context.pathParam(Constants.ID);


            if (discoveryDatabase.verify(Long.parseLong(discoveryId))) {
                response = discoveryDatabase.get(Long.parseLong(discoveryId));

                if (response != null) {
                    response.put(Constants.STATUS, Constants.SUCCESS);

                    response.put(Constants.ERROR_CODE, Constants.SUCCESS_CODE);

                    LOGGER.info("Retrieved the discovery details successfully for id {}", discoveryId);

                    context.response().setStatusCode(200).end(response.encodePrettily());

                } else {
                    response = Handler.errorHandler("Failure", "Failed to get the discovery profile", Constants.EXCEPTION);

                    LOGGER.error("Some exception might have occurred while fetching the data as the result is null");

                    context.response().setStatusCode(500).end(response.encodePrettily());
                }
            } else {
                response = Handler.errorHandler("Invalid ID", "Id does not exist", Constants.INVALID_DISCOVERY_ID);

                context.response().setStatusCode(400).end(response.encodePrettily());

                LOGGER.info("Unable to get the discovery details as there are no discovery profile with specific id");
            }
        }
        catch (Exception exception)
        {
            response = Handler.errorHandler("Exception occurred", exception.getMessage(), Constants.EXCEPTION);

            context.response().setStatusCode(500).end(response.encodePrettily());

            LOGGER.error("Exception occurred {}", exception.getMessage());

            LOGGER.info("Get request  has been served for {}  with result {}", Constants.DISCOVERY_API + Constants.ROUTE_PATH, response);

        }

    }

    private void updateDiscovery(RoutingContext context)
    {
        LOGGER.info("Put request for  {} has come", Constants.DISCOVERY_API + Constants.ROUTE_PATH);

        var id = context.pathParam(Constants.ID);

        var data = context.body().asJsonObject();

        var response = new JsonObject();

        try
        {
            if (data.isEmpty())
            {
                response = Handler.errorHandler("Empty Body", "Request body is empty", Constants.EMPTY_BODY);

                LOGGER.info("Put request  has been served for {} with result {}", Constants.DISCOVERY_API + Constants.ROUTE_PATH, response);

                context.response().setStatusCode(400).end(response.encodePrettily());
            }
            else
            {
                if (discoveryDatabase.verify(Long.parseLong(id)))
                {
                    discoveryDatabase.update(data, Long.parseLong(id));

                    response.put(Constants.ERROR_CODE, Constants.SUCCESS_CODE);

                    response.put(Constants.MESSAGE, "Successfully updated the discovery profile");

                    response.put(Constants.STATUS, Constants.SUCCESS);

                    LOGGER.info("Updation successful for the id {} and the changes are {}", id, data);

                    context.response().setStatusCode(200).end(response.encodePrettily());

                }
                else
                {
                    response = Handler.errorHandler("Invalid credential id", "Discovery id is invalid", Constants.INVALID_DISCOVERY_ID);

                    context.response().setStatusCode(400).end(response.encodePrettily());

                    LOGGER.info("Unable to update discovery id as the discovery id {} is invalid", id);

                }
            }
        }
        catch (Exception exception)
        {
            response = Handler.errorHandler("Exception occurred", exception.getMessage(), Constants.EXCEPTION);

            context.response().setStatusCode(500).end(response.encodePrettily());

            LOGGER.error("Exception occurred {}", exception.getMessage());

            LOGGER.info("Put request  has been served for {}  with result {}", Constants.DISCOVERY_API + Constants.ROUTE_PATH, response);

        }
    }

    private void discoveryRun(RoutingContext context)
    {
        var response = new JsonObject();

        try
        {
            var id = context.pathParam(Constants.ID);

            if (discoveryDatabase.verify(Long.parseLong(id)))
            {
                eventBus.send(Constants.DISCOVERY_ADDRESS, id);

                response.put(Constants.STATUS, Constants.SUCCESS);

                response.put(Constants.MESSAGE, "Discovery Run request has been received");

                context.response().setStatusCode(200).end(response.encodePrettily());
            }
            else
            {
                response = Handler.errorHandler("Invalid discovery id","Discovery Id does not exist",Constants.INCORRECT_DISCOVERY);

                context.response().setStatusCode(400).end(response.encodePrettily());

            }

        }
        catch (Exception exception)
        {
            response = Handler.errorHandler("Exception occurred", exception.getMessage(), Constants.EXCEPTION);

            context.response().setStatusCode(500).end(response.encodePrettily());

            LOGGER.error("Exception occurred  in discovery run method {}",exception.getMessage());

            LOGGER.info("Discovery run request  has been served for {}  with result {}", Constants.DISCOVERY_API + Constants.RUN_API, response);

        }

    }

}

