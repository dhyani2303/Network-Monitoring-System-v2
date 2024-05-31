package org.motadata.engine;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.motadata.database.Credential;
import org.motadata.constants.Constants;
import org.motadata.database.Provision;
import org.motadata.util.ProcessUtil;
import org.motadata.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;

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
            var pollTime = Long.parseLong(Utils.configMap.get(Constants.POLL_TIME).toString());

            var zContext = new ZContext();

            var socket = zContext.createSocket(SocketType.PUSH);

            socket.bind(Utils.configMap.get(Constants.ZMQ_ADDRESS).toString());

            vertx.setPeriodic(pollTime, handler ->
            {
                var context = new JsonArray();

                    if (!Provision.getProvision().get().isEmpty())
                    {
                        var provisionDevices = provisionDatabase.get();

                        for (var provisionedDevice : provisionDevices)
                        {
                            var entries = JsonObject.mapFrom(provisionedDevice);

                            ProcessUtil.checkAvailability(entries).onComplete(asyncResult ->
                            {
                                if (asyncResult.succeeded())
                                {
                                    if (!Credential.getCredential().get(Long.parseLong(entries.getValue(Constants.VALID_CREDENTIAL_ID).toString())).isEmpty())
                                    {
                                        var credentialDetails = credentialDatabase.get(Long.parseLong(entries.getValue(Constants.VALID_CREDENTIAL_ID).toString()));

                                        entries.put(Constants.REQUEST_TYPE, Constants.COLLECT);

                                        entries.put(Constants.USERNAME, credentialDetails.getString(Constants.USERNAME));

                                        entries.put(Constants.PASSWORD, credentialDetails.getString(Constants.PASSWORD));

                                        context.add(entries);

                                        vertx.eventBus().send(Constants.SEND_ADDRESS,context);

                                        vertx.eventBus().<JsonObject>localConsumer(Constants.COLLECT_ADDRESS,fetchHandler->
                                        {
                                            System.out.println("Data" +fetchHandler.body());
                                            socket.send(Base64.getEncoder().encodeToString(fetchHandler.body().encode().getBytes()));

                                            LOGGER.trace("Content has been sent over zmq {}", fetchHandler.body());

                                        });
//                                        ProcessUtil.spawnPluginEngine(context).onComplete(pluginEngineHandler ->
//                                        {
//                                            if (pluginEngineHandler.succeeded())
//                                            {
//                                                var outputs = pluginEngineHandler.result();
//
//                                                    for (var output : outputs)
//                                                    {
//                                                        socket.send(output);
//
//                                                        LOGGER.trace("Content has been sent over zmq {}", output);
//                                                    }
//                                            }
//                                            else
//                                            {
//                                                LOGGER.warn("Failure occurred from spawnPluginEngine method {}", pluginEngineHandler.cause().toString());
//                                            }
//                                        });
                                    }
                                    else
                                    {
                                        LOGGER.warn("Unable to fetch the credential details of valid credential id {}", entries.getString(Constants.VALID_CREDENTIAL_ID));
                                    }

                                }
                                else
                                {
                                    LOGGER.warn("Polling failed from the check availability method reason: {}", asyncResult.cause().toString());
                                }
                            });
                        }
                    }


            });
            start.complete();
        }
        catch (Exception exception)
        {
            LOGGER.error("Some exception has occurred",exception);

            start.fail("exception");
        }

    }

    public void stop(Promise<Void> promise)
    {

        promise.complete();
    }

}
