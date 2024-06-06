package org.motadata.engine;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.motadata.database.Credential;
import org.motadata.constants.Constants;
import org.motadata.database.Provision;
import org.motadata.util.Config;
import org.motadata.util.ProcessUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Base64;


public class PollingEngine extends AbstractVerticle
{
    public static final Logger LOGGER = LoggerFactory.getLogger(PollingEngine.class);

    private final Credential credentialDatabase = Credential.getCredential();

    private final Provision provisionDatabase = Provision.getProvision();

    public void start(Promise<Void> start)
    {
        try
        {
            var pollTime = Config.getPollTime();

            vertx.eventBus().<JsonObject>localConsumer(Constants.COLLECT_ADDRESS, fetchHandler ->
            {
                fetchHandler.body().put(Constants.REQUEST_TYPE, "write.file");

                vertx.eventBus().send("db", Base64.getEncoder().encodeToString(fetchHandler.body().encode().getBytes()));

                LOGGER.trace("Content has been sent over zmq");

            });

            vertx.eventBus().<JsonObject>localConsumer("read.data.address",handler->
            {
                LOGGER.trace("Data of ip {} is as follows: {}",handler.body().getString(Constants.IP_ADDRESS),handler.body().getJsonObject(Constants.RESULT).encodePrettily());

            });


            vertx.setPeriodic(pollTime, handler ->
            {
                var context = new JsonArray();

                var provisionDevices = provisionDatabase.get();

                for (var index = 0; index < provisionDevices.size(); index++)
                {
                    var device = provisionDevices.getJsonObject(index);

                    ProcessUtil.checkAvailability(device).onComplete(asyncResult ->
                    {
                        if (asyncResult.succeeded())
                        {
                            if (!Credential.getCredential().get(Long.parseLong(device.getValue(Constants.VALID_CREDENTIAL_ID).toString())).isEmpty())
                            {
                                var credentialDetails = credentialDatabase.get(Long.parseLong(device.getValue(Constants.VALID_CREDENTIAL_ID).toString()));

                                device.put(Constants.REQUEST_TYPE, Constants.COLLECT);

                                device.put(Constants.USERNAME, credentialDetails.getString(Constants.USERNAME));

                                device.put(Constants.PASSWORD, credentialDetails.getString(Constants.PASSWORD));

                                context.add(device);

                                vertx.eventBus().send("plugin", Base64.getEncoder().encodeToString(context.encode().getBytes()));

                            }
                            else
                            {
                                LOGGER.warn("Unable to fetch the credential details of valid credential id {}", device.getString(Constants.VALID_CREDENTIAL_ID));
                            }

                        }
                        else
                        {
                            LOGGER.warn("Polling failed from the check availability method reason: {}", asyncResult.cause().toString());
                        }
                    });
                }

            });


            start.complete();
        }
        catch (Exception exception)
        {
            LOGGER.error("Some exception has occurred", exception);

            start.fail("exception");
        }
    }

    public void stop(Promise<Void> promise)
    {
        try
        {
            promise.complete();

            LOGGER.info("stop method is called");
        }
        catch (Exception exception)
        {
            LOGGER.error("Some exception occurred", exception);

            promise.fail(exception);

        }

    }

}
