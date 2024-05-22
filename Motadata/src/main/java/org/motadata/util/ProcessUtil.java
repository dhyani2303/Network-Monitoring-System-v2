package org.motadata.util;

import io.vertx.core.buffer.Buffer;
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

    public static boolean checkAvailability(JsonObject data)
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

                return false;
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
                        var lossPercentage = parts[1].split("%")[0].trim();

                        var loss = Integer.parseInt(lossPercentage.split("/")[2]);

                        var result = loss != 100;

                        if (result)
                        {
                            LOGGER.info("Device with Ip address {} is up", ipAddress);

                            return true;

                        }
                        else
                        {
                            LOGGER.info("Device with Ip address {} is down", ipAddress);

                            return false;
                        }
                    }
                }
            }

        }
        catch (Exception exception)
        {
            LOGGER.error("Some exception occurred in check availability method ", exception);

            return false;
        }
        return false;

    }

    public static JsonArray spawnPluginEngine(JsonArray context)
    {
        try
        {
            var contextLength = context.size();

            var results = new JsonArray();

            var count = 0;

            String encodedContext = Base64.getEncoder().encodeToString(context.toString().getBytes());

            ProcessBuilder processBuilder = new ProcessBuilder(Utils.configMap.get(Constants.PLUGIN_PATH).toString(), encodedContext);

            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            var timeout = process.waitFor(Long.parseLong(Utils.configMap.get(Constants.PROCESS_TIMEOUT).toString()), TimeUnit.SECONDS);

            if (!timeout)
            {
                process.destroyForcibly();

                LOGGER.warn("Process has been killed as it exceeded the timeout duration");

                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;

            var buffer = Buffer.buffer();

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
                if (output != null)
                {
                    var result = new JsonObject(new String(Base64.getDecoder().decode(output)));

                    results.add(result);

                    LOGGER.info("Decoding of the data is successful");

                }
                else
                {
                    LOGGER.warn("The output after splitting at seperator came to be null");
                }
            }

            LOGGER.info("Result is sent to the method that called the spawn plugin engine");

            return results;

        }
        catch (Exception exception)
        {
            LOGGER.error("Some exception has occurred", exception);

            return null;

        }
    }
}
