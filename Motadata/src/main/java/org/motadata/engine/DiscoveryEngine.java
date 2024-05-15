package org.motadata.engine;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.motadata.database.Database;
import org.motadata.util.Constants;
import org.motadata.util.ProcessBuilders;

public class DiscoveryEngine extends AbstractVerticle
{
    public static final Database discoveryDatabase = Database.getDatabase(Constants.DISCOVERYDATABASE);

    public static final Database credentialDatabase = Database.getDatabase(Constants.CREDENTIALDATABASE);

    public static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryEngine.class);

    public void start(Promise<Void> promise)
    {
        var eb = vertx.eventBus();

        eb.localConsumer(Constants.DISCOVERYADDRESS,this :: handler);

        promise.complete();

    }


    public void handler(Message<JsonObject> requestedData)
    {
        JsonObject result = new JsonObject();

        if (requestedData.body().containsKey(Constants.DISCOVERYID))
        {
            if (!(requestedData.body().getString(Constants.DISCOVERYID).isEmpty()))
            {
                var discoveryId = requestedData.body().getString(Constants.DISCOVERYID);

                boolean verificationResult = discoveryDatabase.verify(Long.parseLong(discoveryId));

                if (verificationResult)
                {
                    var discoveryProfileDetails = discoveryDatabase.get(Long.parseLong(discoveryId));

                    var availabilityResult = ProcessBuilders.checkAvailability(discoveryProfileDetails);

                    if (availabilityResult)
                    {
                        JsonArray context = new JsonArray();

                        JsonArray credentialProfiles = new JsonArray();

                        var credentialIds = discoveryProfileDetails.getJsonArray(Constants.CREDENTIAL_IDS);

                        for (Object credentialId : credentialIds)
                        {
                            var credentialDetails = credentialDatabase.get(Long.parseLong(credentialId.toString()));

                            credentialDetails.put(Constants.CREDENTIALID,credentialId);

                            credentialProfiles.add(credentialDetails);

                        }

                        discoveryProfileDetails.put(Constants.CREDENTIAL_PROFILES,credentialProfiles);

                        discoveryProfileDetails.put(Constants.REQUEST_TYPE,Constants.DISCOVERY);

                        discoveryProfileDetails.remove(Constants.CREDENTIAL_IDS);

                        context.add(discoveryProfileDetails);

                     var value = ProcessBuilders.spawnPluginEngine(context);

                     requestedData.reply(value);

                    }
                    else
                    {
                        result.put(Constants.ERROR,"Device Availability");

                        result.put(Constants.ERRORMESSAGE,"Device is down for a while");

                        result.put(Constants.STATUS,Constants.FAIL);

                        result.put(Constants.ERRORCODE,Constants.INCORRECTDISCOVERY);

                        LOGGER.info("Discovery failed because the device with ip address {} is down"+ discoveryProfileDetails.getString(Constants.IP));
                    }

                }
                else
                {
                    result.put(Constants.ERROR,"Invalid Discovery Id");

                    result.put(Constants.ERRORMESSAGE,"Discovery Id does not exist");

                    result.put(Constants.STATUS,Constants.FAIL);

                    result.put(Constants.ERRORCODE,Constants.INCORRECTDISCOVERY);

                    LOGGER.info("Discovery failed because the discovery id {} does not exist"+ discoveryId);

                }

            }
            else
            {
                result.put(Constants.ERROR,"Empty field");

                result.put(Constants.ERRORMESSAGE,"Discovery Id is  empty");

                result.put(Constants.STATUS,Constants.FAIL);

                result.put(Constants.ERRORCODE,Constants.EMPTYDISCVOERYFIELD);

                LOGGER.info("Discovery failed because the discovery id is empty");

            }

        }
        else
        {
            result.put(Constants.ERROR,"Empty context");

            result.put(Constants.ERRORMESSAGE,"Discovery Ids are not present");

            result.put(Constants.STATUS,Constants.FAIL);

            result.put(Constants.ERRORCODE,Constants.EMPTYDISCOVERY);

            LOGGER.info("Discovery failed because context provided is empty");

        }

    }

}
