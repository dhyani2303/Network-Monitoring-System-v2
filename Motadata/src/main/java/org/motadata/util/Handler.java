package org.motadata.util;

import io.vertx.core.json.JsonObject;
import org.motadata.constants.Constants;

public class Handler {

   public static JsonObject errorHandler(String error, String errorMessage, String errorCode)
    {
        JsonObject result = new JsonObject();

        result.put(Constants.ERROR,error);

        result.put(Constants.ERROR_MESSAGE,errorMessage);

        result.put(Constants.ERROR_CODE,errorCode);

        result.put(Constants.STATUS,Constants.FAIL);

        return result;
    }
}
