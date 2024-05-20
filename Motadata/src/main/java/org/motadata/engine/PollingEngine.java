package org.motadata.engine;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.motadata.database.Database;
import org.motadata.constants.Constants;
import org.motadata.util.ProcessUtil;
import org.motadata.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollingEngine extends AbstractVerticle
{
    public static final Logger LOGGER = LoggerFactory.getLogger(PollingEngine.class);

    Database credentialDatabase = Database.getDatabase(Constants.CREDENTIAL_DATABASE);

    Database provisionDatabase = Database.getDatabase(Constants.PROVISION_DATABASE);

    public void start(Promise<Void> start)
    {
        var pollTime = Long.parseLong(Utils.configMap.get(Constants.POLL_TIME).toString());

        vertx.setPeriodic(pollTime, handler ->
        {
            var context = new JsonArray();

            try
            {
                if (!provisionDatabase.get().isEmpty())
                {
                    var provisionDevices = provisionDatabase.get();

                    for (var provisionedDevice : provisionDevices)
                    {
                        var entries = JsonObject.mapFrom(provisionedDevice);
                     //   if (ProcessUtil.checkAvailability(device))
                      //  {

                            var credentialDetails = credentialDatabase.get(Long.parseLong(entries.getValue(Constants.VALID_CREDENTIAL_ID).toString()));

                            entries.put(Constants.REQUEST_TYPE,Constants.COLLECT);

                          entries.put(Constants.USERNAME,credentialDetails.getString(Constants.USERNAME));

                          entries.put(Constants.PASSWORD,credentialDetails.getString(Constants.PASSWORD));

                            context.add(entries);

                            var outputs = ProcessUtil.spawnPluginEngine(context);

                            if (outputs != null)
                            {
                                for (var output : outputs)
                                {
                                    var jsonData = new JsonObject(output.toString());

                                    if (jsonData.getString(Constants.STATUS).equals(Constants.SUCCESS))
                                    {
                                        Utils.writeToFile(vertx, jsonData).onSuccess(v ->

                                                        LOGGER.trace("Content written to file {}", jsonData))

                                                .onFailure(err ->

                                                        LOGGER.error("Failed to write file", err));

                                    }
                                    else
                                    {
                                        LOGGER.error("Polling status is fail {}", jsonData);
                                    }
                                }
                            } else {
                                LOGGER.warn("Null has been return from plugin engine");
                            }
                       // } else {

                         //   LOGGER.info("Polling failed because the device with ip address {} is down",JsonObject.mapFrom(provisionedDevice).getString(Constants.IP_ADDRESS));
                       // }
                    }
                }
            }
            catch (Exception exception)
            {
                LOGGER.error("Some exception occurred ",exception);
            }

        });

    }

}
