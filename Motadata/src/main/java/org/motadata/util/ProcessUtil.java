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

import java.io.BufferedReader;
import java.io.IOException;
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
                            var lossPercentage = parts[1].split("/",3);

                            var result = Integer.parseInt(lossPercentage[1].trim()) - Integer.parseInt(lossPercentage[0].trim());

                            if (result == 0)
                            {
                                LOGGER.info("Device with Ip address {} is up", ipAddress);

                                promise.complete(true);
                            }
                            else
                            {
                                LOGGER.info("Device with Ip address {} is down", ipAddress);

                                promise.fail("Device with Ip address" + ipAddress +"is down");

                                return;
                            }

                        }
                    }
                }

            }
            catch (Exception exception)
            {
                LOGGER.error("Some exception occurred in check availabilty method",exception);

                promise.fail("Some exception occurred inside execute blocking of check availability method");
            }

        });

        return promise.future();
    }

//    public static Future<Boolean> checkAvailability(JsonObject data)
//    {
//        Promise<Boolean> promise = Promise.promise();
//
//            Bootstrap.getVertx().executeBlocking(id ->
//            {
//                try
//                {
//                    var ipAddress = data.getString(Constants.IP_ADDRESS);
//
//                    ProcessBuilder processBuilder = new ProcessBuilder("fping", "-c", "3", "-q", ipAddress);
//
//                    processBuilder.redirectErrorStream(true);
//
//                    Process process = processBuilder.start();
//
//                    var timeout = process.waitFor(Long.parseLong(Utils.configMap.get(Constants.PROCESS_TIMEOUT).toString()), TimeUnit.SECONDS);
//
//                    if (!timeout)
//                    {
//                        process.destroyForcibly();
//
//                        LOGGER.warn("Process has been killed as it exceeded the timeout duration");
//
//                        return false;
//                    }
//
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//
//                    String line;
//
//                    while ((line = reader.readLine()) != null)
//                    {
//                        if (line.contains("xmt/rcv/%loss"))
//                        {
//                            var parts = line.split("=", 2);
//
//                            if (parts.length == 2)
//                            {
//                                var result = Integer.parseInt(parts[1]) - Integer.parseInt(parts[0]);
//
//                                if (result == 0)
//                                {
//                                    LOGGER.info("Device with Ip address {} is up", ipAddress);
//
//                                    promise.complete(true);
//
//                                }
//                                else
//                                {
//                                    LOGGER.info("Device with Ip address {} is down", ipAddress);
//
//                                    promise.fail("fail");
//                                }
//                            }
//                        }
//                    }
//                }
//                catch (Exception exception)
//                {
//                    promise.fail("false");
//
//                    return promise.future();
//                }
//            });
//    }
//
//
//
//    public static boolean checkAvailability(JsonObject data)
//    {
//        try
//        {
//            var ipAddress = data.getString(Constants.IP_ADDRESS);
//
//            ProcessBuilder processBuilder = new ProcessBuilder("fping", "-c", "3", "-q", ipAddress);
//
//            processBuilder.redirectErrorStream(true);
//
//            Process process = processBuilder.start();
//
//            var timeout = process.waitFor(Long.parseLong(Utils.configMap.get(Constants.PROCESS_TIMEOUT).toString()), TimeUnit.SECONDS);
//
//            if (!timeout)
//            {
//                process.destroyForcibly();
//
//                LOGGER.warn("Process has been killed as it exceeded the timeout duration");
//
//                return false;
//            }
//
//            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//
//            String line;
//
//            while ((line = reader.readLine()) != null)
//            {
//                if (line.contains("xmt/rcv/%loss"))
//                {
//                    var parts = line.split("=", 2);
//
//                    if (parts.length == 2)
//                    {
//                        var lossPercentage = parts[1].split("%")[0].trim();
//
//                        var loss = Integer.parseInt(lossPercentage.split("/")[2]);
//
//                        var result = loss != 100;
//
//                        if (result)
//                        {
//                            LOGGER.info("Device with Ip address {} is up", ipAddress);
//
//                            return true;
//
//                        }
//                        else
//                        {
//                            LOGGER.info("Device with Ip address {} is down", ipAddress);
//
//                            return false;
//                        }
//                    }
//                }
//            }
//
//        }
//        catch (Exception exception)
//        {
//            LOGGER.error("Some exception occurred in check availability method ", exception);
//
//            return false;
//        }
//        return false;
//
//    }

    public static Future<JsonArray> spawnPluginEngine(JsonArray context)
    {
        Promise<JsonArray> promise = Promise.promise();

        try
        {
            var contextLength = context.size();

            var results = new JsonArray();

            String encodedContext = Base64.getEncoder().encodeToString(context.toString().getBytes());

            Bootstrap.getVertx().executeBlocking(id->
            {
                try
                {
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

                        for (var output : outputs)
                        {
                            if (output != null && !output.trim().isEmpty())
                            {
                                var result = new JsonObject(new String(Base64.getDecoder().decode(output)));

                                results.add(result);

                                LOGGER.info("Decoding of the data is successful");

                            }
                            else
                            {
                                LOGGER.warn("The output after splitting at separator came to be null");

                                promise.fail("The output after splitting at separator came to be null");

                                return;
                            }
                        }

                        promise.complete(results);

                        LOGGER.info("Result is sent to the method that called the spawn plugin engine");

                }
                catch (Exception exception)
                {
                    LOGGER.error("Some exception occurred in executing block",exception);

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

