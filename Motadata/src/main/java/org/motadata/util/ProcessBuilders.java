package org.motadata.util;

import io.vertx.core.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
                    LOGGER.info("Device with IP address {} is up",ipAddress);

                    return true;
                }
                else
                {
                    LOGGER.info("Device with IP address {} is down", ipAddress);
                }
            }

        } catch(IOException | InterruptedException e)
        {
           LOGGER.error(e.getCause().toString());

        }
        return false;

    }

    public static JsonArray spawnPluginEngine(JsonArray context)
    {
        var lengthOfContext = context.size();

        var resultArray = new JsonArray();

        var count = 0;

        String  encodedContext = Utils.encode(context);

        ProcessBuilder processBuilder = new ProcessBuilder(Constants.PLUGINPATH,encodedContext);

        processBuilder.redirectErrorStream(true);

        try
        {
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            process.waitFor();

            String line;

           var buffer = Buffer.buffer();

            while ((line=reader.readLine())!=null && count<=lengthOfContext)
            {
                buffer.appendString(line);

                if (line.contains("||@@||"))
                {
                    count++;
                }

            }

            String[] bufferSplit = buffer.toString().split("\\|\\|@@\\|\\|");

            for (String s : bufferSplit)
            {
                var result = Utils.decode(s);

                if (result!=null) {
                    resultArray.add(result);
                }

            }

        } catch(IOException | InterruptedException e)
        {
            LOGGER.error(e.getCause().toString());

        }


        return resultArray;


    }


}
