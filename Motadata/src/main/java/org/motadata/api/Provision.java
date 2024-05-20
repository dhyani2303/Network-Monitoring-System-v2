package org.motadata.api;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.motadata.database.Database;
import org.motadata.constants.Constants;
import org.motadata.util.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Provision {

    public static final Logger LOGGER = LoggerFactory.getLogger(Provision.class);

    public static final Database provisionDatabase = Database.getDatabase(Constants.PROVISION_DATABASE);

    public static final Database discoveryDatabase = Database.getDatabase(Constants.DISCOVERY_DATABASE);
//    public static JsonObject provisionDevice(JsonObject context)
//    {
//        var result = new JsonObject();
//
//        if (context.containsKey(Constants.DISCOVERY_ID))
//        {
//            if (!(context.getString(Constants.DISCOVERY_ID).isEmpty()))
//            {
//                if (Database.exist(Long.parseLong(context.getString(Constants.DISCOVERY_ID)),Constants.VERIFY_DISCOVERY_ID))
//                {
//                    if (!(Database.exist(Long.parseLong(context.getString(Constants.DISCOVERY_ID)),Constants.VERIFY_PROVISION)))
//                    {
//                        Database.addProvisionDevice(Long.parseLong(context.getString(Constants.DISCOVERY_ID)));
//
//                        result.put(Constants.MESSAGE, "Device has been provisioned successfully");
//
//                        result.put(Constants.ERROR_CODE, Constants.SUCCESS_CODE);
//
//                        result.put(Constants.STATUS,Constants.SUCCESS);
//
//                        LOGGER.info("Device with id {} is provisioned",context.getString(Constants.DISCOVERY_ID));
//                    }
//                    else
//                    {
//                        result.put(Constants.ERROR, "Already Provisioned");
//
//                        result.put(Constants.ERROR_CODE, Constants.PROVISION_ERROR);
//
//                        result.put(Constants.ERROR_MESSAGE, "The device is already provisioned");
//
//                        result.put(Constants.STATUS,Constants.FAIL);
//
//                        LOGGER.info("Cannot provision as the device is already provisioned");
//
//                    }
//
//                }
//                else
//                {
//                    result.put(Constants.ERROR, "Discovery Not Done");
//
//                    result.put(Constants.ERROR_CODE, Constants.PROVISION_ERROR);
//
//                    result.put(Constants.ERROR_MESSAGE, "The device is not discovered yet");
//
//                    result.put(Constants.STATUS,Constants.FAIL);
//
//                    LOGGER.info("Cannot provision as The device is not discovered yet");
//
//                }
//
//            }
//            else
//            {
//                result.put(Constants.ERROR, "Empty Discovery Field");
//
//                result.put(Constants.ERROR_CODE, Constants.EMPTY_PROVISION_FIELD);
//
//                result.put(Constants.ERROR_MESSAGE, "Discovery ID is empty");
//
//                result.put(Constants.STATUS,Constants.FAIL);
//
//                LOGGER.info("Cannot provision as the discovery id is empty");
//
//            }
//        }
//        else
//        {
//            result.put(Constants.ERROR, "Empty Provision");
//
//            result.put(Constants.ERROR_CODE, Constants.EMPTY_PROVISION);
//
//            result.put(Constants.ERROR_MESSAGE, "Discovery ID is not present");
//
//            result.put(Constants.STATUS,Constants.FAIL);
//
//            LOGGER.info("Cannot provision as the discovery id is not present");
//        }
//
//        return result;
//    }

    private Router router;

    public void init(Vertx vertx)
    {
        router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
    }

    public Router getRouter()
    {
        router.post(Constants.API_WITH_PARAMS).handler(this::provisionDevice);

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
                  discoveryProfile.remove(Constants.CREDENTIAL_IDS);

                  discoveryProfile.remove(Constants.DISCOVERY_PROFILE_NAME);

                  var provisonDevices = provisionDatabase.get();

                  for (var provisionDevice : provisonDevices) {

                      if (discoveryProfile.getString(Constants.IP_ADDRESS).equals(JsonObject.mapFrom(provisionDevice).getString(Constants.IP_ADDRESS))) {

                          response = Handler.errorHandler("Already Provisioned", "The device with the given ip address is already provisioned", Constants.ALREADY_PROVISION);

                          response.put(Constants.IP_ADDRESS, discoveryProfile.getValue(Constants.IP_ADDRESS));

                          context.response().setStatusCode(400).end(response.encodePrettily());

                          LOGGER.info("Provision request served with response {} ",response);

                          return;
                      }
                  }

                  var provisionId = provisionDatabase.create(discoveryProfile);

                  response.put(Constants.STATUS, Constants.SUCCESS);

                  response.put(Constants.PROVISION_ID, provisionId);

                  response.put(Constants.ERROR_CODE, Constants.SUCCESS_CODE);

                  context.response().setStatusCode(200).end(response.encodePrettily());

                  LOGGER.info("Provision request served with response {} ",response);

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

            LOGGER.error("Some exception has occurred",exception);

            context.response().setStatusCode(500).end(response.encodePrettily());
        }

    }

}
