package org.motadata.api;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.motadata.database.Credential;
import org.motadata.database.Database;
import org.motadata.constants.Constants;
import org.motadata.database.Discovery;
import org.motadata.util.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Provision {

    public static final Logger LOGGER = LoggerFactory.getLogger(Provision.class);

    private static final org.motadata.database.Provision provisionDatabase = org.motadata.database.Provision.getProvision();

    private static final Discovery discoveryDatabase = Discovery.getDiscovery();

    private static final Credential credentialDatabase = Credential.getCredential();

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
            router.post(Constants.API_WITH_PARAMS).handler(this::provisionDevice);

            router.get(Constants.ROUTE_PATH).handler(this::getProvisionDevice);

            router.delete(Constants.API_WITH_PARAMS).handler(this::unprovisionDevice);

        }
        catch (Exception exception)
        {
            LOGGER.error("Some exception occurred in get router method",exception);

        }
        return router;

    }

    private void provisionDevice(RoutingContext context)
    {
        var response = new JsonObject();

        try
        {
            var discoveryId = context.pathParam(Constants.ID);

           if(discoveryDatabase.verify(Long.parseLong(discoveryId)))
           {
              var discoveryProfile =  discoveryDatabase.get(Long.parseLong(discoveryId));

              if (discoveryProfile.getLong(Constants.VALID_CREDENTIAL_ID)!=-1)
              {
                  if (credentialDatabase.verify(discoveryProfile.getLong(Constants.VALID_CREDENTIAL_ID)))
                  {
                      discoveryProfile.remove(Constants.CREDENTIAL_IDS);

                      discoveryProfile.remove(Constants.DISCOVERY_PROFILE_NAME);

                      var provisonDevices = provisionDatabase.get();

                      for (var provisionDevice : provisonDevices)
                      {

                          if (discoveryProfile.getString(Constants.IP_ADDRESS).equals(JsonObject.mapFrom(provisionDevice).getString(Constants.IP_ADDRESS)))
                          {
                              response = Handler.errorHandler("Already Provisioned", "The device with the given ip address is already provisioned", Constants.ALREADY_PROVISION);

                              response.put(Constants.IP_ADDRESS, discoveryProfile.getValue(Constants.IP_ADDRESS));

                              context.response().setStatusCode(400).end(response.encodePrettily());

                              LOGGER.info("Provision request served with response {} ", response);

                              return;
                          }
                      }

                      var provisionId = provisionDatabase.create(discoveryProfile);

                      response.put(Constants.STATUS, Constants.SUCCESS);

                      response.put(Constants.PROVISION_ID, provisionId);

                      response.put(Constants.ERROR_CODE, Constants.SUCCESS_CODE);

                      context.response().setStatusCode(200).end(response.encodePrettily());

                      LOGGER.info("Provision request served with response {} ", response);

                  }
                  else
                  {
                      response = Handler.errorHandler("Missing credential profile","Credential profile is already deleted",Constants.PROVISION_ERROR);

                      context.response().setStatusCode(400).end(response.encodePrettily());

                      LOGGER.warn("Unable to provision the device as the valid credential id is already deleted");
                  }
              }
              else
              {
                  response = Handler.errorHandler("Not yet Discovered","Device is not yet discovered",Constants.PROVISION_ERROR);

                  context.response().setStatusCode(400).end(response.encodePrettily());

                  LOGGER.info("Provision request served with response {} ",response);
              }
           }
           else
           {
               response = Handler.errorHandler("Invalid discovery Id","Discovery ID is invalid",Constants.INVALID_DISCOVERY_ID);

               LOGGER.info("Provision request served with response {} ",response);

               context.response().setStatusCode(400).end(response.encodePrettily());
           }

        }
        catch (Exception exception)
        {
            response = Handler.errorHandler("Exception occurred", exception.getMessage(), Constants.EXCEPTION);

            LOGGER.error("Some exception has occurred while provisioning the device",exception);

            context.response().setStatusCode(500).end(response.encodePrettily());
        }

    }

    private void getProvisionDevice(RoutingContext context)
    {
        var response = new JsonObject();
        try
        {
           var result= provisionDatabase.get();

           if (result.isEmpty())
           {
               response.put(Constants.MESSAGE,"No devices are provisioned yet");
           }

           response.put(Constants.STATUS,Constants.SUCCESS);

           response.put(Constants.ERROR_CODE,Constants.SUCCESS_CODE);

           response.put(Constants.RESULT,result);

           context.response().setStatusCode(200).end(response.encodePrettily());

           LOGGER.info("Get the provision device request has been serverd with response {}",response);

        }
        catch (Exception exception)
        {

            response = Handler.errorHandler("Exception occurred", exception.getMessage(), Constants.EXCEPTION);

            LOGGER.error("Some exception has occurred while getting  the proisioned device data",exception);

            context.response().setStatusCode(500).end(response.encodePrettily());
        }
    }
    private void unprovisionDevice(RoutingContext context)
    {
        var response = new JsonObject();

        try
        {
            var id = context.pathParam(Constants.ID);

            if (provisionDatabase.verify(Long.parseLong(id)))
            {
                if (provisionDatabase.delete(Long.parseLong(id)))
                {
                    response.put(Constants.STATUS,Constants.SUCCESS);

                    response.put(Constants.ERROR_CODE,Constants.SUCCESS_CODE);

                    response.put(Constants.MESSAGE,"Successfully unprovisioned the device");

                    context.response().setStatusCode(200).end(response.encodePrettily());

                    LOGGER.info("The device with provision id {} has been successfully unprovisioned",id);
                }
                else
                {
                    response = Handler.errorHandler("Failure in Deletion","Some error occurred while deleting the provisioned device",Constants.PROVISION_ERROR);

                    context.response().setStatusCode(400).end(response.encodePrettily());

                    LOGGER.warn("Unable to delete the provisioned device with id {} as the delete method of db returned false",id);
                }
            }
            else
            {
                response = Handler.errorHandler("Invalid Provision Id","Provision id does not exist",Constants.PROVISION_ERROR);

                context.response().setStatusCode(400).end(response.encodePrettily());

                LOGGER.warn("Unable to perform delete provision request as the id {} is invalid", id);
            }
        }
        catch (Exception exception)
        {
            response = Handler.errorHandler("Exception occurred", exception.getMessage(), Constants.EXCEPTION);

            LOGGER.error("Some exception has occurred while unprovisioning the device",exception);

            context.response().setStatusCode(500).end(response.encodePrettily());
        }
    }

}
