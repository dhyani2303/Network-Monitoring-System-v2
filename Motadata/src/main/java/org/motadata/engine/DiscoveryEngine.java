package org.motadata.engine;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import org.motadata.database.Credential;
import org.motadata.database.Discovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.motadata.database.Database;
import org.motadata.constants.Constants;
import org.motadata.util.ProcessUtil;

import java.util.jar.JarOutputStream;

public class DiscoveryEngine extends AbstractVerticle {

    public static final Discovery discoveryDatabase = Discovery.getDiscovery();

    public static final Credential credentialDatabase = Credential.getCredential();

    public static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryEngine.class);

    public void start(Promise<Void> promise)
    {
        var eventBus = vertx.eventBus();

        eventBus.localConsumer(Constants.DISCOVERY_ADDRESS, this::handler);

        promise.complete();
    }

    public void handler(Message<JsonObject> message)
    {
        try
        {
            var discoveryProfileDetails = message.body();

            ProcessUtil.checkAvailability(discoveryProfileDetails).onComplete(handler->
            {
                if (handler.succeeded())
                {
                    var context = new JsonArray();

                    var  credentialIds =  discoveryProfileDetails.getJsonArray(Constants.CREDENTIAL_IDS).copy();

                    var credentialProfiles = new JsonArray();

                    for (var credentialId : credentialIds)
                    {
                        var credentialDetails = credentialDatabase.get(Long.parseLong(credentialId.toString()));

                        credentialDetails.put(Constants.CREDENTIAL_ID, credentialId);

                        credentialProfiles.add(credentialDetails);
                    }

                    discoveryProfileDetails.put(Constants.CREDENTIAL_PROFILES, credentialProfiles);

                    discoveryProfileDetails.put(Constants.REQUEST_TYPE, Constants.DISCOVERY);

                    context.add(discoveryProfileDetails);

                    ProcessUtil.spawnPluginEngine(context).onComplete(pluginHandler->{

                        if (pluginHandler.succeeded())
                        {
                            var results = pluginHandler.result();

                            var contextResult = results.getJsonObject(0);

                            if (contextResult.getString(Constants.STATUS).equals(Constants.SUCCESS))
                            {
                                contextResult.remove(Constants.CREDENTIAL_PROFILES);

                                contextResult.remove(Constants.ERROR);

                                contextResult.remove(Constants.STATUS);

                                contextResult.remove(Constants.RESULT);

                                contextResult.remove(Constants.REQUEST_TYPE);

                                contextResult.put(Constants.IS_DISCOVERED,true);

                                discoveryDatabase.update(contextResult, Long.parseLong(discoveryProfileDetails.getValue(Constants.ID).toString()));

                                LOGGER.info("Discovery ran successfully for id {}", discoveryProfileDetails.getValue(Constants.ID));
                            }
                            else
                            {
                                LOGGER.info("Discovery failed  for the id {} errors found are {}: ", discoveryProfileDetails.getValue(Constants.ID),contextResult.getJsonArray(Constants.ERROR));
                            }

                        }
                        else
                        {
                            LOGGER.warn("Failure occurred in spawn plugin engine method {}",pluginHandler.cause().toString());
                        }

                    });
                }
                else
                {
                    LOGGER.warn("Check availability method failed {}",handler.cause().toString());
                }
            });
        }
        catch (Exception exception)
        {
            LOGGER.error("Some exception occurred in the handler method",exception);
        }

    }

    public void stop(Promise<Void> promise)
    {
        promise.complete();

    }
}
