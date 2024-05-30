package org.motadata.util;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import org.motadata.Bootstrap;
import org.motadata.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.zeromq.SocketType;
import org.zeromq.ZContext;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.concurrent.TimeUnit;


public class ProcessUtil
{
    public static final Logger LOGGER = LoggerFactory.getLogger(ProcessUtil.class);

    public static Future<Boolean> checkAvailability(JsonObject data)
    {
        Promise<Boolean> promise = Promise.promise();

        Bootstrap.getVertx().executeBlocking(id ->
        {
            try
            {
                var ipAddress = data.getString(Constants.IP_ADDRESS);

                ProcessBuilder processBuilder = new ProcessBuilder("fping", "-c", "3", "-q", ipAddress);

                processBuilder.redirectErrorStream(true);

                Process process = processBuilder.start();

                var timeout = process.waitFor(Long.parseLong(Utils.configMap.get(Constants.PROCESS_TIMEOUT).toString()), TimeUnit.SECONDS);

                if (!timeout)
                {
                    process.destroyForcibly();

                    LOGGER.warn("Process has been killed as it exceeded the timeout duration");

                    promise.fail("Process has been killed as it exceeded the timeout duration");

                    return;

                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line;

                while ((line = reader.readLine()) != null)
                {
                    if (line.contains("xmt/rcv/%loss"))
                    {
                        var parts = line.split("=", 2);

                        if (parts.length == 2)
                        {
                            var lossPercentage = parts[1].split("/", 3);

                            var result = Integer.parseInt(lossPercentage[1].trim()) - Integer.parseInt(lossPercentage[0].trim());

                            if (result == 0)
                            {
                                LOGGER.info("Device with Ip address {} is up", ipAddress);

                                promise.complete(true);
                            }
                            else
                            {
                                LOGGER.info("Device with Ip address {} is down", ipAddress);

                                promise.fail("Device with Ip address" + ipAddress + "is down");

                                return;
                            }

                        }
                    }
                }

            }
            catch (Exception exception)
            {
                LOGGER.error("Some exception occurred in check availability method", exception);

                promise.fail("Some exception occurred inside execute blocking of check availability method");
            }

        });

        return promise.future();
    }

    public static Future<String[]> spawnPluginEngine(JsonArray context)
    {
        Promise<String[]> promise = Promise.promise();

        try
        {
            var contextLength = context.size();

            String encodedContext = Base64.getEncoder().encodeToString(context.toString().getBytes());

            Bootstrap.getVertx().executeBlocking(id ->
            {
                try
                {

                    var zContext = new ZContext();

                    var socket = zContext.createSocket(SocketType.PUSH);

                    socket.bind("tcp://localhost:5587");

                    socket.send(encodedContext);

                    ProcessBuilder processBuilder = new ProcessBuilder(Utils.configMap.get(Constants.PLUGIN_PATH).toString(), encodedContext);

                    processBuilder.redirectErrorStream(true);

                    Process process = processBuilder.start();

                    var timeout = process.waitFor(Long.parseLong(Utils.configMap.get(Constants.PROCESS_TIMEOUT).toString()), TimeUnit.SECONDS);

                    if (!timeout)
                    {
                        process.destroyForcibly();

                        LOGGER.warn("Process has been killed as it exceeded the timeout duration");

                        promise.fail("Process has been killed as it exceeded the timeout duration");

                        return;

                    }

                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    String line;

                    var buffer = Buffer.buffer();

                    var count = 0;

                    while ((line = reader.readLine()) != null && count <= contextLength)
                    {
                        buffer.appendString(line);

                        if (line.contains(Constants.SEPERATOR))
                        {
                            count++;
                        }
                    }

                    var outputs = buffer.toString().split(Constants.REGEX_SEPERATOR);

                    promise.complete(outputs);

                    LOGGER.info("Result is sent to the method that called the spawn plugin engine");

                }
                catch (Exception exception)
                {
                    LOGGER.error("Some exception occurred in executing block", exception);

                    promise.fail("Exception occurred inside execute blocking code");
                }
            });

        }
        catch (Exception exception)
        {
            LOGGER.error("Some exception has occurred", exception);

            promise.fail("Exception occurred in the spawnPlugin engine method");

        }

        return promise.future();
    }
}

