package org.motadata.api;

import io.vertx.core.json.JsonObject;
import org.motadata.database.Database;
import org.motadata.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Discovery {

    public static final Database discoveryDatabase = Database.getDatabase(Constants.DISCOVERY_DATABASE);


    public static final Logger LOGGER = LoggerFactory.getLogger(Discovery.class);
    public static JsonObject createDiscovery(JsonObject discoveryDetails) {

        var result = new JsonObject();

        if (discoveryDetails.containsKey(Constants.IP) && discoveryDetails.containsKey(Constants.PORT) && discoveryDetails.containsKey(Constants.CREDENTIAL_IDS) && discoveryDetails.containsKey(Constants.NAME)) {

            if ((!(discoveryDetails.getString(Constants.IP).isEmpty())) && (!(discoveryDetails.getJsonArray(Constants.CREDENTIAL_IDS).isEmpty())) && (!(discoveryDetails.getString(Constants.PORT).isEmpty())) && (!(discoveryDetails.getString(Constants.NAME).isEmpty()))) {

                var discoveryNameVerification =  discoveryDatabase.verify(discoveryDetails.getString(Constants.NAME));

                if (!discoveryNameVerification) {

                    var credentialProfiles = discoveryDetails.getJsonArray(Constants.CREDENTIAL_IDS);

                    for (Object credentialId : credentialProfiles) {
                        if (!Credential.credentialDatabase.verify(Long.parseLong(credentialId.toString()))) {
                            result.put(Constants.ERROR, "Wrong Credentials");

                            result.put(Constants.ERROR_CODE, Constants.INCORRECT_CREDENTIAL);

                            result.put(Constants.ERROR_MESSAGE, "One of the credential Id is incorrect");

                            result.put(Constants.STATUS, Constants.FAIL);

                            LOGGER.info("Creation of discovery profile failed because one of the credential Id is incorrect");

                            return result;

                        }
                    }


                    var id = discoveryDatabase.create(discoveryDetails);

                    result.put(Constants.ID, id);

                    result.put(Constants.STATUS, Constants.SUCCESS);

                    result.put(Constants.ERROR_CODE, Constants.SUCCESS_CODE);

                    LOGGER.info("Discovery profile created  successfully with id {}", id);

                }
                else
                {
                    result.put(Constants.ERROR, "Duplicate Discovery Name");

                    result.put(Constants.ERROR_CODE, Constants.DUPLICATE_DISCOVERY_NAME);

                    result.put(Constants.ERROR_MESSAGE, "Discovery profile with the name already exists");

                    result.put(Constants.STATUS,Constants.FAIL);

                    LOGGER.info("Creation of discovery profile failed because discovery profile name already exists");

                }

            } else {
                result.put(Constants.ERROR, "Empty Discovery field");

                result.put(Constants.ERROR_CODE, Constants.EMPTY_DISCOVERY_FIELD);

                result.put(Constants.ERROR_MESSAGE, "IP or Port or Credential Profiles or discovery name is empty");

                result.put(Constants.STATUS,Constants.FAIL);

                LOGGER.info("Creation of discovery profile failed because either ip or port or credential field or discovery name is empty");


            }
        } else {
            result.put(Constants.ERROR, "Empty Discovery");

            result.put(Constants.ERROR_CODE, Constants.EMPTY_DISCOVERY);

            result.put(Constants.ERROR_MESSAGE, "IP or Port or Credential Profiles or discovery name is not present");

            result.put(Constants.STATUS,Constants.FAIL);

            LOGGER.info("Creation of discovery profile failed because either ip or port profile or discovery name is not present");

        }

        return result;
    }

    public static JsonObject getDiscovery()
    {
        var result = discoveryDatabase.get();

        if (result.isEmpty()){

            result.put(Constants.MESSAGE,"No Discovery profiles are present");

        }

        result.put(Constants.STATUS,Constants.SUCCESS);

        result.put(Constants.ERROR_CODE,Constants.SUCCESS_CODE);

        return result;
    }

    public static JsonObject getDiscovery(String id)
    {
        var result = new JsonObject();

        if (discoveryDatabase.verify(Long.parseLong(id))) {


            result = discoveryDatabase.get(Long.parseLong(id));

            result.put(Constants.STATUS, Constants.SUCCESS);

            result.put(Constants.ERROR_CODE, Constants.SUCCESS_CODE);

            LOGGER.info("Retrieved the discovery details successfully for id {}", id);
        }
        else
        {

            LOGGER.info("Unable to get the discovery details as there is no discovery profile with specific id");

            result.put(Constants.ERROR_MESSAGE,"Id does not exist");

            result.put(Constants.STATUS,Constants.FAIL);

            result.put(Constants.ERROR_CODE,Constants.INVALID_DISCOVERY_ID);

            result.put(Constants.ERROR_CODE,Constants.INVALID_DISCOVERY_ID);

            result.put(Constants.ERROR,"Invalid Id");


        }

        return result;
    }

    public static JsonObject updateDiscovery(JsonObject discoveryDetails,String id)
    {
        var response = new JsonObject();

        if (!discoveryDetails.isEmpty()) {
            var idVerification = discoveryDatabase.verify(Long.parseLong(id));

            if (idVerification) {
                discoveryDatabase.update(discoveryDetails, Long.parseLong(id));

                response.put(Constants.ERROR_CODE, Constants.SUCCESS_CODE);

                response.put(Constants.MESSAGE, "Successfully updated the discovery profile");

                response.put(Constants.STATUS, Constants.SUCCESS);

                LOGGER.info("Updation successful for the id {} and the changes are {}", id, discoveryDetails);


            } else {
                response.put(Constants.ERROR_MESSAGE, "Discovery id is invalid");

                response.put(Constants.ERROR, "Invalid discovery id");

                response.put(Constants.ERROR_CODE, Constants.INVALID_CREDENTIAL_ID);

                response.put(Constants.STATUS, Constants.FAIL);

                LOGGER.info("Unable to update discovery id as the discovery id {} is invalid", id);

            }

        }
        else
        {
            response.put(Constants.ERROR_MESSAGE, "Parameters are empty");

            response.put(Constants.ERROR, "Empty parameters");

            response.put(Constants.ERROR_CODE, Constants.EMPTY_DISCOVERY);

            response.put(Constants.STATUS, Constants.FAIL);

            LOGGER.info("Unable to update discovery id as parameters are empty");

        }
        return response;
    }

    public static JsonObject deleteDiscovery(String id)
    {

        var response = new JsonObject();

        var idVerification = discoveryDatabase.verify(Long.parseLong(id));

        if (idVerification)
        {
            var result = discoveryDatabase.delete(Long.parseLong(id));

            if (result)
            {
                response.put(Constants.ERROR_CODE, Constants.SUCCESS_CODE);

                response.put(Constants.MESSAGE, "Successfully deleted the discovery profile");

                response.put(Constants.STATUS, Constants.SUCCESS);

                LOGGER.info("Deletion successful of id {}",id);

            }
            else
            {
                response.put(Constants.ERROR_MESSAGE, "Device is already provisioned");

                response.put(Constants.ERROR, "Device Provisioned");

                response.put(Constants.ERROR_CODE, Constants.ALREADY_PROVISION);

                response.put(Constants.STATUS, Constants.FAIL);

                LOGGER.info("Unable to delete as the discovery id is already provisioned",id);
            }

        }
        else
        {
            response.put(Constants.ERROR_MESSAGE, "Id does not exist");

            response.put(Constants.ERROR, "Invalid Id");

            response.put(Constants.ERROR_CODE, Constants.INVALID_DISCOVERY_ID);

            response.put(Constants.STATUS, Constants.FAIL);

            LOGGER.info("Unable to delete as the discovery id is invalid id: {}",id);

        }
        return response;

    }
}


