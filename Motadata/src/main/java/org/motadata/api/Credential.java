package org.motadata.api;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import org.motadata.database.Database;
import org.motadata.util.Constants;


public class Credential
{
    public static final Logger LOGGER = LoggerFactory.getLogger(Credential.class);

   public final static Database credentialDatabase = Database.getDatabase(Constants.CREDENTIALDATABASE);

    public static JsonObject createCredential(JsonObject credentialDetails)
    {
        JsonObject response = new JsonObject();

        if (credentialDetails.containsKey(Constants.USERNAME) && credentialDetails.containsKey(Constants.PASSWORD)) {

            if ((!(credentialDetails.getString(Constants.USERNAME).isEmpty())) && (!(credentialDetails.getString(Constants.PASSWORD).isEmpty()))) {

                var id = credentialDatabase.create(credentialDetails);

                response.put(Constants.ID, id);

                response.put(Constants.ERRORCODE, Constants.SUCCESSCODE);

                response.put(Constants.STATUS, Constants.SUCCESS);

                LOGGER.info("New Credential profile with id {} has been successfully created"+ id);

            } else {

                response.put(Constants.ERROR, "Empty credential fields");

                response.put(Constants.ERRORMESSAGE, "Username or password field is empty");

                response.put(Constants.ERRORCODE, Constants.EMPTYCREDENTIALFIELDS);

                response.put(Constants.STATUS, Constants.FAIL);

                LOGGER.info("Unable to create credential profile due to empty parameters");

            }

        } else {

            response.put(Constants.ERRORMESSAGE, "Username or password is not present");

            response.put(Constants.ERROR, "Empty credential");

            response.put(Constants.ERRORCODE, Constants.EMPTYCREDENTIALS);

            response.put(Constants.STATUS, Constants.FAIL);

            LOGGER.info("Unable to create credential profile dud to missing fields");


        }

        return response;
    }


    public static JsonObject getCredential()
    {
     var result = credentialDatabase.get();

     if (result.isEmpty()){

         LOGGER.info("Unable to get the credential details as there are no credential profiles");

         result.put(Constants.MESSAGE,"No credential profiles are present");
     }

     result.put(Constants.STATUS,Constants.SUCCESS);

     result.put(Constants.ERRORCODE,Constants.SUCCESSCODE);

        LOGGER.info("Retrieved the credential details successfully");

     return result;
    }
}
