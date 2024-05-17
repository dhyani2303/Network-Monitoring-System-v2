package org.motadata.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.json.JsonObject;
import org.motadata.database.Database;
import org.motadata.util.Constants;


public class Credential
{
    public static final Logger LOGGER = LoggerFactory.getLogger(Credential.class);

   public final static Database credentialDatabase = Database.getDatabase(Constants.CREDENTIAL_DATABASE);

    public static JsonObject createCredential(JsonObject credentialDetails)
    {
        JsonObject response = new JsonObject();

        if (credentialDetails.containsKey(Constants.USERNAME) && credentialDetails.containsKey(Constants.PASSWORD) && credentialDetails.containsKey(Constants.NAME)) {

            if ((!(credentialDetails.getString(Constants.USERNAME).isEmpty())) && (!(credentialDetails.getString(Constants.PASSWORD).isEmpty())) && (!(credentialDetails.getString(Constants.NAME).isEmpty()))) {

                var credentialNameVerification = credentialDatabase.verify(credentialDetails.getString(Constants.NAME));

                if(!credentialNameVerification) {

                    var id = credentialDatabase.create(credentialDetails);

                    response.put(Constants.ID, id);

                    response.put(Constants.ERROR_CODE, Constants.SUCCESS_CODE);

                    response.put(Constants.STATUS, Constants.SUCCESS);

                    LOGGER.info("New Credential profile with id {} has been successfully created", id);
                }
                else
                {
                    response.put(Constants.ERROR, "Duplicate credential name");

                    response.put(Constants.ERROR_MESSAGE, "Credential profile with given name already exists");

                    response.put(Constants.ERROR_CODE, Constants.DUPLICATE_CREDENTIAL_NAME);

                    response.put(Constants.STATUS, Constants.FAIL);

                    LOGGER.info("Unable to create credential profile due to duplicate credential name");
                }
            } else {

                response.put(Constants.ERROR, "Empty credential fields");

                response.put(Constants.ERROR_MESSAGE, "Username or password field  or credential name is empty");

                response.put(Constants.ERROR_CODE, Constants.EMPTY_CREDENTIAL_FIELDS);

                response.put(Constants.STATUS, Constants.FAIL);

                LOGGER.info("Unable to create credential profile due to empty parameters");

            }

        } else {

            response.put(Constants.ERROR_MESSAGE, "Username or password or credential name  is not present");

            response.put(Constants.ERROR, "Empty credential");

            response.put(Constants.ERROR_CODE, Constants.EMPTY_CREDENTIALS);

            response.put(Constants.STATUS, Constants.FAIL);

            LOGGER.info("Unable to create credential profile due to missing parameters");


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

     result.put(Constants.ERROR_CODE,Constants.SUCCESS_CODE);

        LOGGER.info("Retrieved the credential details successfully");

     return result;
    }

    public static JsonObject getCredential(String id)
    {
        var result = new JsonObject();

        if (credentialDatabase.verify(Long.parseLong(id))) {


            result = credentialDatabase.get(Long.parseLong(id));

            result.put(Constants.STATUS, Constants.SUCCESS);

            result.put(Constants.ERROR_CODE, Constants.SUCCESS_CODE);

            LOGGER.info("Retrieved the credential details successfully for id {}", id);
        }
        else
        {

                LOGGER.info("Unable to get the credential details as there are no credential profile with specific id");

                result.put(Constants.ERROR_MESSAGE,"Id does not exist");

                result.put(Constants.STATUS,Constants.FAIL);

                result.put(Constants.ERROR_CODE,Constants.INVALID_CREDENTIAL_ID);

                result.put(Constants.ERROR_CODE,Constants.INVALID_CREDENTIAL_ID);

                result.put(Constants.ERROR,"Invalid Id");


        }

        return result;
    }

    public static JsonObject updateCredential(JsonObject credentialDetails,String id)
    {
        var response = new JsonObject();

        if (!credentialDetails.isEmpty()) {
            var idVerification = credentialDatabase.verify(Long.parseLong(id));

            if (idVerification) {
                credentialDatabase.update(credentialDetails, Long.parseLong(id));

                response.put(Constants.ERROR_CODE, Constants.SUCCESS_CODE);

                response.put(Constants.MESSAGE, "Successfully updated the credential profile");

                response.put(Constants.STATUS, Constants.SUCCESS);

                LOGGER.info("Updation successful for the id {} and the changes are {}", id, credentialDetails);


            } else {
                response.put(Constants.ERROR_MESSAGE, "Credential id is invalid");

                response.put(Constants.ERROR, "Invalid credential id");

                response.put(Constants.ERROR_CODE, Constants.INVALID_CREDENTIAL_ID);

                response.put(Constants.STATUS, Constants.FAIL);

                LOGGER.info("Unable to update credential id as the credential id {} is invalid", id);

            }
        }
        else
        {
            response.put(Constants.ERROR_MESSAGE, "Parameters are empty");

            response.put(Constants.ERROR, "Empty parameters");

            response.put(Constants.ERROR_CODE, Constants.EMPTY_CREDENTIALS);

            response.put(Constants.STATUS, Constants.FAIL);

            LOGGER.info("Unable to update credential id as parameters are empty");
        }
        return response;
    }

    public static JsonObject deleteCredential(String id)
    {

        var response = new JsonObject();

        var idVerification = credentialDatabase.verify(Long.parseLong(id));

        if (idVerification)
        {
            var result = credentialDatabase.delete(Long.parseLong(id));

            if (result)
            {
                response.put(Constants.ERROR_CODE, Constants.SUCCESS_CODE);

                response.put(Constants.MESSAGE, "Successfully deleted the credential profile");

                response.put(Constants.STATUS, Constants.SUCCESS);

                LOGGER.info("Deletion successful of id {}",id);

            }
            else
            {
                response.put(Constants.ERROR_MESSAGE, "Device is already provisioned");

                response.put(Constants.ERROR, "Device Provisioned");

                response.put(Constants.ERROR_CODE, Constants.ALREADY_PROVISION);

                response.put(Constants.STATUS, Constants.FAIL);

                LOGGER.info("Unable to delete as the credential id is already provisioned",id);
            }

        }
        else
        {
            response.put(Constants.ERROR_MESSAGE, "Id does not exist");

            response.put(Constants.ERROR, "Invalid Id");

            response.put(Constants.ERROR_CODE, Constants.INVALID_CREDENTIAL_ID);

            response.put(Constants.STATUS, Constants.FAIL);

            LOGGER.info("Unable to delete as the credential id is invalid id: {}",id);

        }

        return response;
    }
}
