package org.motadata.util;




import io.vertx.core.json.JsonObject;
import org.motadata.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.atomic.AtomicLong;


public class Utils
{
    public static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    private static final AtomicLong counter = new AtomicLong(0);

    public static long getNewId()
    {
        LOGGER.info("A request to generate a new id has come");

        return counter.incrementAndGet();
    }


    public static boolean validatePort(String port)
    {
        try
        {
            if (port == null || port.isEmpty())
            {
                return false;
            }

            return Integer.parseInt(port) > 0 && Integer.parseInt(port) <= 65353 && port.matches("[0-9]+");
        }
        catch (Exception exception)
        {
            LOGGER.error("Exception occurred in validatePort method",exception);

            return false;

        }
    }

    public static JsonObject errorHandler(String error, String errorMessage, String errorCode)
    {
        var result = new JsonObject();

        result.put(Constants.ERROR, error);

        result.put(Constants.ERROR_MESSAGE, errorMessage);

        result.put(Constants.ERROR_CODE, errorCode);

        result.put(Constants.STATUS, Constants.FAIL);

        return result;
    }


}
