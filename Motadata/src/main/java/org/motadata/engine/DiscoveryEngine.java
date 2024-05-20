package org.motadata.engine;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.motadata.database.Database;
import org.motadata.constants.Constants;
import org.motadata.util.ProcessUtil;

import java.util.jar.JarOutputStream;

public class DiscoveryEngine extends AbstractVerticle {

    public static final Database discoveryDatabase = Database.getDatabase(Constants.DISCOVERY_DATABASE);

    public static final Database credentialDatabase = Database.getDatabase(Constants.CREDENTIAL_DATABASE);

    public static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryEngine.class);

    public void start(Promise<Void> promise)
    {
        var eventBus = vertx.eventBus();

        eventBus.localConsumer(Constants.DISCOVERY_ADDRESS, this::handler);

        promise.complete();

    }

    public void handler(Message<String> message)
    {
        try
        {
            var discoveryId = message.body();

            var discoveryProfileDetails = discoveryDatabase.get(Long.parseLong(discoveryId));

            if (ProcessUtil.checkAvailability(discoveryProfileDetails))
            {
                var context = new JsonArray();

                var  credentialIds =  discoveryProfileDetails.getJsonArray(Constants.CREDENTIAL_IDS);

                var credentialProfiles = new JsonArray();
                for (var credentialId : credentialIds)
                {
                    var credentialDetails = credentialDatabase.get(Long.parseLong(credentialId.toString()));

                    System.out.println(credentialDetails);

                    credentialDetails.put(Constants.CREDENTIAL_ID, credentialId);

                    credentialProfiles.add(credentialDetails);
                }

                discoveryProfileDetails.put(Constants.CREDENTIAL_PROFILES, credentialProfiles);

                discoveryProfileDetails.put(Constants.REQUEST_TYPE, Constants.DISCOVERY);

                context.add(discoveryProfileDetails);

                var results = ProcessUtil.spawnPluginEngine(context);

                if (results != null)
                {
                    var contextResult = results.getJsonObject(0);

                    if (contextResult.getString(Constants.STATUS).equals(Constants.SUCCESS))
                    {
                        contextResult.remove(Constants.CREDENTIAL_PROFILES);

                        contextResult.remove(Constants.ERROR);

                        contextResult.remove(Constants.STATUS);

                        contextResult.remove(Constants.RESULT);

                        System.out.println(contextResult);

                        discoveryDatabase.update(contextResult,Long.parseLong(discoveryId));

                        LOGGER.info("Discovery ran successfully");
                    }
                    else
                    {
                        LOGGER.info("Discovery failed  errors found are {}: ",contextResult.getJsonArray(Constants.ERROR) );
                    }
                }
                else
                {
                    LOGGER.info("The output of spawning process builder came out to be null as the output did not come in limited time");

                }

            }
        }
        catch (Exception exception)
        {
            LOGGER.error("Some exception occurred in the handler method",exception);
        }



    }

}
