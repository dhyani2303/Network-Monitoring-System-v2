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
        var ipAddress = data.getString(Constants.IP_ADDRESS);

        ProcessBuilder processBuilder = new ProcessBuilder("fping", "-c", "3", "-q", ipAddress);

        processBuilder.redirectErrorStream(true);

        try
        {
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
                if (line.contains("/0%"))
                {
                    LOGGER.info("Device with IP address {} is up", ipAddress);

                    return true;
                } else
                {
                    LOGGER.info("Device with IP address {} is down", ipAddress);
                }
            }

        }
        catch (Exception exception)
        {
            LOGGER.error(exception.getCause().toString());
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

                } else
                {
                    LOGGER.warn("The output after splitting at seperator came to be null");
                }
            }

            if (!timeout)
            {
                process.destroyForcibly();

                LOGGER.warn("Process has been killed as it exceeded the timeout duration");

                return null;
            }
            return results;

        }
        catch (Exception exception)
        {
            LOGGER.error("Some exception has occurred",exception);

            return null;

        }
    }
}
