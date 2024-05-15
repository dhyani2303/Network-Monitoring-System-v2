package org.motadata.util;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProcessBuilders {

    public static final Logger LOGGER = LoggerFactory.getLogger(ProcessBuilders.class);

    public static boolean checkAvailability(JsonObject data)
    {

        var ipAddress = data.getString(Constants.IP);

        ProcessBuilder processBuilder = new ProcessBuilder("fping", "-c", "3", "-q", ipAddress);

        processBuilder.redirectErrorStream(true);
        try
        {
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            process.waitFor();

            String line;

            while ((line=reader.readLine())!=null)
            {
                if (line.contains("/0%"))
                {
                    LOGGER.info("Device with IP address {} is up"+ ipAddress);

                    return true;
                }
                else
                {
                    LOGGER.info("Device with IP address {} is down"+ ipAddress);
                }
            }

        } catch(IOException | InterruptedException e)
        {
           LOGGER.error(e.getCause());

        }
        return false;

    }

    public static boolean spawnPluginEngine(JsonArray context)
    {

        String  encodedContext = Utils.encode(context);

        System.out.println(encodedContext);

        ProcessBuilder processBuilder = new ProcessBuilder(Constants.PLUGINPATH,encodedContext);

        processBuilder.redirectErrorStream(true);
        try
        {
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            process.waitFor();

            String line;

            while ((line=reader.readLine())!=null)
            {
                System.out.println(line);

            }

        } catch(IOException | InterruptedException e)
        {
            LOGGER.error(e.getCause());

        }

return true;

    }


}
