package org.motadata.engine;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.motadata.database.Database;
import org.motadata.util.Constants;
import org.motadata.util.ProcessBuilders;
import org.motadata.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollingEngine extends AbstractVerticle {

    public static final Logger LOGGER = LoggerFactory.getLogger(PollingEngine.class);

    Database credentialDatabase = Database.getDatabase(Constants.CREDENTIALDATABASE);

    Database discoveryDatabase = Database.getDatabase(Constants.DISCOVERYDATABASE);

    public void start(Promise<Void> start) {


        var pollTime = Long.parseLong(Utils.configMap.get(Constants.POLL_TIME).toString());

        var batchSize = Integer.parseInt(Utils.configMap.get(Constants.BATCH_SIZE).toString());

        vertx.setPeriodic(pollTime, handler -> {

            var context = new JsonArray();

            var result = new JsonObject();

            var provisionedDevices = Database.getProvisionedDevices();

            if (!provisionedDevices.isEmpty()) {

                if (provisionedDevices.size()<batchSize) {

                    for (Object id : provisionedDevices) {

                        var discoveryDetails = discoveryDatabase.get(Long.parseLong(id.toString()));

                        var available = ProcessBuilders.checkAvailability(discoveryDetails);

                        if (available) {
                            var credentialId = Database.getCredentialId(Long.parseLong(id.toString()));

                            var credentialDetails = credentialDatabase.get(credentialId);

                            discoveryDetails.put(Constants.USERNAME, credentialDetails.getString(Constants.USERNAME));

                            discoveryDetails.put(Constants.PASSWORD, credentialDetails.getString(Constants.PASSWORD));

                            discoveryDetails.put(Constants.REQUEST_TYPE, Constants.COLLECT);

                            context.add(discoveryDetails);

                            var outputs = ProcessBuilders.spawnPluginEngine(context);


                            System.out.println(outputs);

                        } else {
                            result.put(Constants.ERROR, "Device Availability");

                            result.put(Constants.ERRORMESSAGE, "Device is down for a while");

                            result.put(Constants.STATUS, Constants.FAIL);

                            result.put(Constants.ERRORCODE, Constants.INCORRECTDISCOVERY);

                            LOGGER.info("Discovery failed because the device with ip address {} is down", discoveryDetails.getString(Constants.IP));
                        }

                    }
                }
                else
                {
                    //create batch size
                }
            }
        });

    }

}
