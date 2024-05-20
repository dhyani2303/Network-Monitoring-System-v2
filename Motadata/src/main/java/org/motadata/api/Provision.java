package org.motadata.api;

import io.vertx.core.json.JsonObject;
import org.motadata.database.Database;
import org.motadata.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Provision {

    public static final Logger LOGGER = LoggerFactory.getLogger(Provision.class);

    public static JsonObject provisionDevice(JsonObject context)
    {
        var result = new JsonObject();

        if (context.containsKey(Constants.DISCOVERY_ID))
        {
            if (!(context.getString(Constants.DISCOVERY_ID).isEmpty()))
            {
                if (Database.exist(Long.parseLong(context.getString(Constants.DISCOVERY_ID)),Constants.VERIFY_DISCOVERY_ID))
                {
                    if (!(Database.exist(Long.parseLong(context.getString(Constants.DISCOVERY_ID)),Constants.VERIFY_PROVISION)))
                    {
                        Database.addProvisionDevice(Long.parseLong(context.getString(Constants.DISCOVERY_ID)));

                        result.put(Constants.MESSAGE, "Device has been provisioned successfully");

                        result.put(Constants.ERROR_CODE, Constants.SUCCESS_CODE);

                        result.put(Constants.STATUS,Constants.SUCCESS);

                        LOGGER.info("Device with id {} is provisioned",context.getString(Constants.DISCOVERY_ID));
                    }
                    else
                    {
                        result.put(Constants.ERROR, "Already Provisioned");

                        result.put(Constants.ERROR_CODE, Constants.PROVISION_ERROR);

                        result.put(Constants.ERROR_MESSAGE, "The device is already provisioned");

                        result.put(Constants.STATUS,Constants.FAIL);

                        LOGGER.info("Cannot provision as the device is already provisioned");

                    }

                }
                else
                {
                    result.put(Constants.ERROR, "Discovery Not Done");

                    result.put(Constants.ERROR_CODE, Constants.PROVISION_ERROR);

                    result.put(Constants.ERROR_MESSAGE, "The device is not discovered yet");

                    result.put(Constants.STATUS,Constants.FAIL);

                    LOGGER.info("Cannot provision as The device is not discovered yet");

                }

            }
            else
            {
                result.put(Constants.ERROR, "Empty Discovery Field");

                result.put(Constants.ERROR_CODE, Constants.EMPTY_PROVISION_FIELD);

                result.put(Constants.ERROR_MESSAGE, "Discovery ID is empty");

                result.put(Constants.STATUS,Constants.FAIL);

                LOGGER.info("Cannot provision as the discovery id is empty");

            }
        }
        else
        {
            result.put(Constants.ERROR, "Empty Provision");

            result.put(Constants.ERROR_CODE, Constants.EMPTY_PROVISION);

            result.put(Constants.ERROR_MESSAGE, "Discovery ID is not present");

            result.put(Constants.STATUS,Constants.FAIL);

            LOGGER.info("Cannot provision as the discovery id is not present");
        }

        return result;
    }
}
