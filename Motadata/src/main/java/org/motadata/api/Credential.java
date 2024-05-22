package org.motadata.api;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.motadata.constants.Constants;
import org.motadata.database.Database;
import org.motadata.database.Provision;
import org.motadata.util.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Credential {

    private static final Logger LOGGER = LoggerFactory.getLogger(Credential.class);

    private final static org.motadata.database.Credential credentialDatabase = org.motadata.database.Credential.getCredential();

    private final static Provision provisionDatabase = Provision.getProvision();

    private Router router;

    public void init(Vertx vertx)
    {
        try
        {
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
            router.post(Constants.ROUTE_PATH).handler(this::createCredential);

            router.get(Constants.ROUTE_PATH).handler(this::getCredentials);

            router.get(Constants.API_WITH_PARAMS).handler(this::getCredential);

            router.put(Constants.API_WITH_PARAMS).handler(this::updateCredential);

            router.delete(Constants.API_WITH_PARAMS).handler(this::deleteCredential);
        }
        catch (Exception exception)
        {
            LOGGER.error("Some exception occurred in get router method",exception);
        }
        return router;
    }

    private void createCredential(RoutingContext context)
    {
        LOGGER.info("Post request for  {} has come", Constants.CREDENTIAL_API + Constants.ROUTE_PATH);

        var response = new JsonObject();

        try
        {
            var data = context.body().asJsonObject();

            if (data.isEmpty())
            {
                response = Handler.errorHandler("Empty Body", "Request body is empty", Constants.EMPTY_BODY);

                LOGGER.warn("Post request  has been served for {} with result {}", Constants.CREDENTIAL_API + Constants.ROUTE_PATH, response);

                context.response().setStatusCode(400).end(response.encodePrettily());

            }
            else
            {
                if (data.containsKey(Constants.USERNAME) && data.containsKey(Constants.PASSWORD) && data.containsKey(Constants.CREDENTIAL_PROFILE_NAME) &&

                            (!(data.getString(Constants.USERNAME).isEmpty())) && (!(data.getString(Constants.PASSWORD).isEmpty())) && (!(data.getString(Constants.CREDENTIAL_PROFILE_NAME).isEmpty())))
                {
                    if (!credentialDatabase.verify(Constants.CREDENTIAL_PROFILE_NAME,data.getString(Constants.CREDENTIAL_PROFILE_NAME)))
                        {
                            var id = credentialDatabase.create(data);

                            if (id != -1)
                            {
                                response.put(Constants.CREDENTIAL_ID, id);

                                response.put(Constants.ERROR_CODE, Constants.SUCCESS_CODE);

                                response.put(Constants.STATUS, Constants.SUCCESS);

                                LOGGER.info("New Credential profile with id {} has been successfully created", id);

                                context.response().setStatusCode(200).end(response.encodePrettily());
                            }
                            else
                            {
                                response = Handler.errorHandler("Failure", "Failed to create credential profile", Constants.EXCEPTION);

                                context.response().setStatusCode(500).end(response.encodePrettily());

                                LOGGER.error("Some exception might have occurred in create method of db as the id received is -1");
                            }

                        }
                        else
                        {
                            response = Handler.errorHandler("Duplicate credential name", "Credential profile with given name already exists", Constants.DUPLICATE_CREDENTIAL_NAME);

                            LOGGER.warn("Unable to create credential profile due to duplicate credential name");

                            context.response().setStatusCode(400).end(response.encodePrettily());
                        }

                    }
                    else
                    {
                        response = Handler.errorHandler("Missing Parameters", "Some of the parameters are either missing or empty", Constants.INCORRECT_CREDENTIAL);

                        context.response().setStatusCode(400).end(response.encodePrettily());

                        LOGGER.warn("Post request  has been served for {}  with result {}", Constants.CREDENTIAL_API + Constants.ROUTE_PATH, response);
                    }
            }
        }
        catch (Exception exception)
        {
            response = Handler.errorHandler("Exception occurred", exception.getMessage(), Constants.EXCEPTION);

            context.response().setStatusCode(500).end(response.encodePrettily());

            LOGGER.error("Post request  has been served for {}  with result {}", Constants.CREDENTIAL_API + Constants.ROUTE_PATH, response);

            LOGGER.error("Exception occurred in create credential method", exception);
        }
    }

    private void getCredentials(RoutingContext context)
    {
        LOGGER.info("Get request for {} has come", Constants.CREDENTIAL_API + Constants.ROUTE_PATH);

        var response = new JsonObject();

        try
        {
            var result = credentialDatabase.get();

            if (result != null)
            {
                if (result.isEmpty())
                {
                    LOGGER.info("Unable to get the credential details as there are no credential profiles");

                    response.put(Constants.MESSAGE, "No credential profiles are present");
                }

                response.put(Constants.RESULT, result);

                response.put(Constants.STATUS, Constants.SUCCESS);

                response.put(Constants.ERROR_CODE, Constants.SUCCESS_CODE);

                LOGGER.info("Get request for {} has been served with result {}", Constants.CREDENTIAL_API, response);

                context.response().setStatusCode(200).end(response.encodePrettily());
            }
            else
            {
                response = Handler.errorHandler("Failure", "Failed to get the credential profiles", Constants.EXCEPTION);

                LOGGER.error("Some exception might have occurred while fetching the data as the result is null");

                context.response().setStatusCode(500).end(response.encodePrettily());

            }
        }
        catch (Exception exception)
        {
            response = Handler.errorHandler("Exception occurred", exception.getMessage(), Constants.EXCEPTION);

            context.response().setStatusCode(500).end(response.encodePrettily());

            LOGGER.error("Get request  has been served for {}  with result {}", Constants.CREDENTIAL_API + Constants.ROUTE_PATH, response);

            LOGGER.error("Exception occurred in get credentials method", exception);

        }
    }

    private void getCredential(RoutingContext context)
    {
        var response = new JsonObject();

        var credentialId = context.pathParam(Constants.ID);

        try
        {
            if (credentialDatabase.verify(Long.parseLong(credentialId)))
            {
                response = credentialDatabase.get(Long.parseLong(credentialId));

                if (response != null)
                {
                    response.put(Constants.STATUS, Constants.SUCCESS);

                    response.put(Constants.ERROR_CODE, Constants.SUCCESS_CODE);

                    LOGGER.info("Retrieved the credential details successfully for id {}", credentialId);

                    context.response().setStatusCode(200).end(response.encodePrettily());
                }
                else
                {
                    response = Handler.errorHandler("Failure", "Failed to get the credential profile", Constants.EXCEPTION);

                    LOGGER.error("Some exception might have occurred while fetching the data as the result is null");

                    context.response().setStatusCode(500).end(response.encodePrettily());
                }
            }
            else
            {
                response = Handler.errorHandler("Invalid ID", "Id does not exist", Constants.INVALID_CREDENTIAL_ID);

                context.response().setStatusCode(400).end(response.encodePrettily());

                LOGGER.warn("Unable to get the credential details as there are no credential profile with specific id");
            }
        }
        catch (Exception exception)
        {
            response = Handler.errorHandler("Exception occurred", exception.getMessage(), Constants.EXCEPTION);

            context.response().setStatusCode(500).end(response.encodePrettily());

            LOGGER.error("Get request  has been served for {}  with result {}", Constants.CREDENTIAL_API + Constants.ROUTE_PATH, response);

            LOGGER.error("Exception occurred in get credential method for id {}",credentialId, exception);

        }
    }

    private void updateCredential(RoutingContext context)
    {
        LOGGER.info("Put request for  {} has come", Constants.CREDENTIAL_API + Constants.ROUTE_PATH);

        var id = context.pathParam(Constants.ID);

        var response = new JsonObject();

        try
        {
            var data = context.body().asJsonObject();

            if (data.isEmpty())
            {
                response = Handler.errorHandler("Empty Body", "Request body is empty", Constants.EMPTY_BODY);

                LOGGER.warn("Put request  has been served for {} with result {}", Constants.CREDENTIAL_API + Constants.ROUTE_PATH, response);

                context.response().setStatusCode(400).end(response.encodePrettily());
            }
            else
            {
                if (credentialDatabase.verify(Long.parseLong(id)))
                {
                    credentialDatabase.update(data, Long.parseLong(id));

                    response.put(Constants.ERROR_CODE, Constants.SUCCESS_CODE);

                    response.put(Constants.MESSAGE, "Successfully updated the credential profile");

                    response.put(Constants.STATUS, Constants.SUCCESS);

                    LOGGER.info("Updation successful for the id {} and the changes are {}", id, data);

                    context.response().setStatusCode(200).end(response.encodePrettily());

                }
                else
                {
                    response = Handler.errorHandler("Invalid credential id", "Credential id is invalid", Constants.INVALID_CREDENTIAL_ID);

                    context.response().setStatusCode(400).end(response.encodePrettily());

                    LOGGER.warn("Unable to update credential id as the credential id {} is invalid", id);

                }
            }

        }
        catch (Exception exception)
        {
            response = Handler.errorHandler("Exception occurred", exception.getMessage(), Constants.EXCEPTION);

            context.response().setStatusCode(500).end(response.encodePrettily());

            LOGGER.error("Put request  has been served for {}  with result {}", Constants.CREDENTIAL_API + Constants.ROUTE_PATH, response);

            LOGGER.error("Exception occurred in update credential method", exception);
        }
    }

    private void deleteCredential(RoutingContext context)
    {
        LOGGER.info("Delete request for  {} has come", Constants.CREDENTIAL_API + Constants.ROUTE_PATH);

        var id = context.pathParam(Constants.ID);

        var response  = new JsonObject();
        try
        {
            if (credentialDatabase.verify(Long.parseLong(id)))
            {
                var details = provisionDatabase.get();

                for (var provisionDevice : details)
                {
                    var device = JsonObject.mapFrom(provisionDevice);

                    if (device.getValue(Constants.VALID_CREDENTIAL_ID).toString().equals(id))
                    {
                        response = Handler.errorHandler("Already in use","The credential id is already in use with ip address",Constants.CREDENTIAL_ERROR);

                        response.put(Constants.IP_ADDRESS,device.getValue(Constants.IP_ADDRESS));

                        context.response().setStatusCode(403).end(response.encodePrettily());

                        LOGGER.warn("Unable to delete the credential profile as it is used in provisioning the device");

                        return;
                    }
                }


                if (credentialDatabase.delete(Long.parseLong(id)))
                {
                   response.put(Constants.STATUS,Constants.SUCCESS);

                   response.put(Constants.ERROR_CODE,Constants.SUCCESS_CODE);

                   response.put(Constants.MESSAGE,"Deletion successful");

                   LOGGER.info("Deletion of credential profile with id {} is successful",id);

                   context.response().setStatusCode(200).end(response.encodePrettily());
                }
                else
                {

                    response = Handler.errorHandler("Failure in Deletion","Some error occurred while deleting the provisioned device",Constants.PROVISION_ERROR);

                    context.response().setStatusCode(400).end(response.encodePrettily());

                    LOGGER.warn("Unable to delete the provisioned device with id {} as the delete method of db returned false",id);                }

            }
            else
            {
                response = Handler.errorHandler("Invalid credential id","Credential id does not exist",Constants.INVALID_CREDENTIAL_ID);

                context.response().setStatusCode(400).end(response.encodePrettily());

                LOGGER.warn("Unable to delete the credential profile as the id {} is invalid ",id);

            }

        }
        catch (Exception exception)
        {
            response = Handler.errorHandler("Exception occurred", exception.getMessage(), Constants.EXCEPTION);

            context.response().setStatusCode(500).end(response.encodePrettily());

            LOGGER.error("Delete request  has been served for {}  with result {}", Constants.CREDENTIAL_API + Constants.ROUTE_PATH, response);

            LOGGER.error("Exception occurred in delete method", exception);
        }

    }


}


