package org.motadata.api;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.motadata.database.Credential;
import org.motadata.constants.Constants;
import org.motadata.util.Handler;
import org.motadata.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Discovery
{

    public static final org.motadata.database.Discovery discoveryDatabase = org.motadata.database.Discovery.getDiscovery();

    public static final Credential credentialDatabase =Credential.getCredential();

    public static final Logger LOGGER = LoggerFactory.getLogger(Discovery.class);

    private Router router;

    private EventBus eventBus;

    public void init(Vertx vertx)
    {
        try
        {
            router = Router.router(vertx);

            router.route().handler(BodyHandler.create());

            eventBus = vertx.eventBus();
        }
        catch (Exception exception)
        {
            LOGGER.error("Some exception has occurred in init method",exception);
        }
    }

    public Router getRouter()
    {
        try
        {
            router.post(Constants.ROUTE_PATH).handler(this::createDiscovery);

            router.get(Constants.ROUTE_PATH).handler(this::getDiscoveries);

            router.get(Constants.API_WITH_PARAMS).handler(this::getDiscovery);

            router.put(Constants.API_WITH_PARAMS).handler(this::updateDiscovery);

            router.post(Constants.RUN_API).handler(this::discoveryRun);

            router.get(Constants.RUN_API_RESULT).handler(this::discoveryRunResult);

            router.delete(Constants.API_WITH_PARAMS).handler(this::deleteDiscovery);
        }
        catch (Exception exception)
        {
            LOGGER.error("Some exception has occurred in get Router method",exception);
        }
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

                LOGGER.warn("Post request  has been served for {} with result {}", Constants.DISCOVERY_API + Constants.ROUTE_PATH, response);

                context.response().setStatusCode(400).end(response.encodePrettily());

            }
            else
            {
                if (data.containsKey(Constants.IP_ADDRESS) && data.containsKey(Constants.PORT) && data.containsKey(Constants.CREDENTIAL_IDS) && data.containsKey(Constants.DISCOVERY_PROFILE_NAME) &&

                        (!(data.getString(Constants.IP_ADDRESS).isEmpty())) && (!(data.getJsonArray(Constants.CREDENTIAL_IDS).isEmpty())) && (Utils.validatePort(data.getString(Constants.PORT))) && (!(data.getString(Constants.DISCOVERY_PROFILE_NAME).isEmpty())))
                {
                    if (!discoveryDatabase.verify(Constants.DISCOVERY_PROFILE_NAME,data.getString(Constants.DISCOVERY_PROFILE_NAME)))
                    {
                        var credentialProfiles = data.getJsonArray(Constants.CREDENTIAL_IDS).copy();

                        for (var credentialId : credentialProfiles)
                        {
                            if (!credentialDatabase.verify(Long.parseLong(credentialId.toString())))
                            {
                                response = Handler.errorHandler("Wrong Credentials", "One of the credential Id is incorrect", Constants.INCORRECT_DISCOVERY);

                                LOGGER.warn("Creation of discovery profile failed because one of the credential Id is incorrect");

                                context.response().setStatusCode(400).end(response.encodePrettily());

                                return;
                            }
                        }

                        data.put(Constants.VALID_CREDENTIAL_ID,-1);

                        data.put(Constants.IS_DISCOVERED,false);

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

                    LOGGER.warn("Creation of discovery profile failed because either ip or port profile or discovery name is not present");
                }
            }

        }
        catch(Exception exception)
        {

        response = Handler.errorHandler("Exception occurred", exception.getMessage(), Constants.EXCEPTION);

        context.response().setStatusCode(500).end(response.encodePrettily());

        LOGGER.error("Post request  has been served for {}  with result {}", Constants.DISCOVERY_API + Constants.ROUTE_PATH, response);

        LOGGER.error("Exception occurred ", exception);
    }

}

    private void getDiscoveries(RoutingContext context)
    {
        LOGGER.info("Get request for {} has come", Constants.DISCOVERY_API + Constants.ROUTE_PATH);

        var response = new JsonObject();

        try {

            var result = discoveryDatabase.get();

            if (result != null)
            {
                if (result.isEmpty())
                {
                    LOGGER.warn("Unable to get the discovery details as there are no credential profiles");

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

            LOGGER.error("Get request  has been served for {}  with result {}", Constants.DISCOVERY_API + Constants.ROUTE_PATH, response);

            LOGGER.error("Exception occurred {}", exception.getMessage());

        }
    }

    private void getDiscovery(RoutingContext context)
    {
        LOGGER.info("Request for get discovery has arrived");

        var response = new JsonObject();

        try {
            var discoveryId = context.pathParam(Constants.ID);

            if (discoveryDatabase.verify(Long.parseLong(discoveryId)))
            {
                response = discoveryDatabase.get(Long.parseLong(discoveryId));

                if (response != null)
                {
                    response.put(Constants.STATUS, Constants.SUCCESS);

                    response.put(Constants.ERROR_CODE, Constants.SUCCESS_CODE);

                    LOGGER.info("Retrieved the discovery details successfully for id {}", discoveryId);

                    context.response().setStatusCode(200).end(response.encodePrettily());

                } else
                {
                    response = Handler.errorHandler("Failure", "Failed to get the discovery profile", Constants.EXCEPTION);

                    LOGGER.error("Some exception might have occurred while fetching the data as the result is null");

                    context.response().setStatusCode(500).end(response.encodePrettily());
                }
            }
            else
            {
                response = Handler.errorHandler("Invalid ID", "Id does not exist", Constants.INVALID_DISCOVERY_ID);

                context.response().setStatusCode(400).end(response.encodePrettily());

                LOGGER.warn("Unable to get the discovery details as there are no discovery profile with specific id");
            }
        }
        catch (Exception exception)
        {
            response = Handler.errorHandler("Exception occurred", exception.getMessage(), Constants.EXCEPTION);

            context.response().setStatusCode(500).end(response.encodePrettily());

            LOGGER.error("Exception occurred", exception);

            LOGGER.error("Get request  has been served for {}  with result {}", Constants.DISCOVERY_API + Constants.ROUTE_PATH, response);

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

                context.response().setStatusCode(400).end(response.encodePrettily());

                LOGGER.warn("Put request  has been served for {} with result {}", Constants.DISCOVERY_API + Constants.ROUTE_PATH, response);
            }
            else
            {
                if (discoveryDatabase.verify(Long.parseLong(id)))
                {
                    discoveryDatabase.update(data, Long.parseLong(id));

                    response.put(Constants.ERROR_CODE, Constants.SUCCESS_CODE);

                    response.put(Constants.MESSAGE, "Successfully updated the discovery profile");

                    response.put(Constants.STATUS, Constants.SUCCESS);

                    context.response().setStatusCode(200).end(response.encodePrettily());

                    LOGGER.info("Updation successful for the id {} and the changes are {}", id, data);
                }
                else
                {
                    response = Handler.errorHandler("Invalid discovery id", "Discovery id is invalid", Constants.INVALID_DISCOVERY_ID);

                    context.response().setStatusCode(400).end(response.encodePrettily());

                    LOGGER.warn("Unable to update discovery id as the discovery id {} is invalid", id);

                }
            }
        }
        catch (Exception exception)
        {
            response = Handler.errorHandler("Exception occurred", exception.getMessage(), Constants.EXCEPTION);

            context.response().setStatusCode(500).end(response.encodePrettily());

            LOGGER.error("Exception occurred ", exception);

            LOGGER.error("Put request  has been served for {}  with result {}", Constants.DISCOVERY_API + Constants.ROUTE_PATH, response);
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
                var discoveryProfileDetails = discoveryDatabase.get(Long.parseLong(id));

                if (!discoveryProfileDetails.getBoolean(Constants.IS_DISCOVERED))
                {
                    discoveryProfileDetails.put(Constants.ID,id);

                    eventBus.send(Constants.DISCOVERY_ADDRESS,discoveryProfileDetails);

                    response.put(Constants.STATUS, Constants.SUCCESS);

                    response.put(Constants.MESSAGE, "Discovery Run request has been received");

                    context.response().setStatusCode(200).end(response.encodePrettily());

                    LOGGER.info("Discovery run  request  has been successfully received");
                }
                else
                {
                    response = Handler.errorHandler("Already discovered","Device is already discovered",Constants.ALREADY_DISCOVERED);

                    context.response().setStatusCode(400).end(response.encodePrettily());

                    LOGGER.warn("Unable to discover the device with discovery id {} as it is already discovered",id);
                }
            }
            else
            {
                response = Handler.errorHandler("Invalid discovery id","Discovery Id does not exist",Constants.INCORRECT_DISCOVERY);

                context.response().setStatusCode(400).end(response.encodePrettily());

                LOGGER.warn("Discovery run failed as the discovery id {} is invalid", Long.parseLong(id));

            }

        }
        catch (Exception exception)
        {
            response = Handler.errorHandler("Exception occurred", exception.getMessage(), Constants.EXCEPTION);

            context.response().setStatusCode(500).end(response.encodePrettily());

            LOGGER.error("Exception occurred  in discovery run method ",exception);

            LOGGER.error("Discovery run request  has been served for {}  with result {}", Constants.DISCOVERY_API + Constants.RUN_API, response);

        }

    }

    private void discoveryRunResult(RoutingContext context)
    {
        var response = new JsonObject();
        try
        {
            var id = context.pathParam(Constants.ID);

            if (discoveryDatabase.verify(Long.parseLong(id)))
            {
                var result = discoveryDatabase.get(Long.parseLong(id));

                if (result.getLong(Constants.VALID_CREDENTIAL_ID)!=-1 && result.getBoolean(Constants.IS_DISCOVERED))
                {
                    response.put(Constants.STATUS,Constants.SUCCESS);

                    response.put(Constants.MESSAGE,"The device has been successfully discovered");

                    response.put(Constants.VALID_CREDENTIAL_ID,result.getValue(Constants.VALID_CREDENTIAL_ID));

                    context.response().setStatusCode(200).end(response.encodePrettily());

                    LOGGER.info("Get discovery run api has been served successfully with result {}",response);
                }
                else
                {
                   response= Handler.errorHandler("Failed Discovery","Failed to run discovery",Constants.FAILED_DISCOVERY);

                   context.response().setStatusCode(400).end(response.encodePrettily());

                    LOGGER.warn("Get discovery run api has been served successfully with result {}",response);

                }
            }
            else
            {
                response = Handler.errorHandler("Invalid discovery id","Discovery id does not exist",Constants.INVALID_DISCOVERY_ID);

                context.response().setStatusCode(400).end(response.encodePrettily());

                LOGGER.warn("Failed to get discovery run result as the id {} is invalid",id);


            }
        }
        catch (Exception exception)
        {
            response = Handler.errorHandler("Exception occurred", exception.getMessage(), Constants.EXCEPTION);

            context.response().setStatusCode(500).end(response.encodePrettily());

            LOGGER.error("Some exception occurred in discovey-run-result api ",exception);

        }
    }

    private void deleteDiscovery(RoutingContext context)
    {
        var response = new JsonObject();
        try
        {
            var id = context.pathParam(Constants.ID);

            if (discoveryDatabase.verify(Long.parseLong(id)))
            {
                if (discoveryDatabase.delete(Long.parseLong(id)))
                {
                    response.put(Constants.STATUS,Constants.SUCCESS);

                    response.put(Constants.MESSAGE,"Deletion of discovery profile is successful");

                    context.response().setStatusCode(200).end(response.encodePrettily());

                    LOGGER.info("Deletion of the discovery id {} is successful",Long.parseLong(id));

                }
                else
                {
                    response = Handler.errorHandler("Failed Deletion","Failed to delete discovery id",Constants.FAILED_DISCOVERY);

                    context.response().setStatusCode(400).end(response.encodePrettily());

                    LOGGER.warn("Deletion of discovery id {} failed as the delete operation in db returned false",id);
                }
            }
            else
            {
                response = Handler.errorHandler("Invalid Discovery ID","Discovery ID does not exist",Constants.INVALID_DISCOVERY_ID);

                context.response().setStatusCode(400).end(response.encodePrettily());

                LOGGER.warn("Deletion failed as the discovery id {} is invalid",Long.parseLong(id));

            }
        }
        catch (Exception exception)
        {
            response = Handler.errorHandler("Exception occurred", exception.getMessage(), Constants.EXCEPTION);

            context.response().setStatusCode(200).end(response.encodePrettily());

            LOGGER.error("Some exception occurred while deleting discovery",exception);
        }
    }

}

