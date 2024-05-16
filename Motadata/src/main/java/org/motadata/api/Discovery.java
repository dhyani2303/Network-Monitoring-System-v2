package org.motadata.api;

import io.vertx.core.json.JsonObject;
import org.motadata.database.Database;
import org.motadata.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Discovery {

    public static final Database discoveryDatabase = Database.getDatabase(Constants.DISCOVERYDATABASE);


    public static final Logger LOGGER = LoggerFactory.getLogger(Discovery.class);
    public static JsonObject createDiscovery(JsonObject discoveryDetails) {

        var result = new JsonObject();

        if (discoveryDetails.containsKey(Constants.IP) && discoveryDetails.containsKey(Constants.PORT) && discoveryDetails.containsKey(Constants.CREDENTIAL_IDS)) {

            if ((!(discoveryDetails.getString(Constants.IP).isEmpty())) && (!(discoveryDetails.getJsonArray(Constants.CREDENTIAL_IDS).isEmpty())) && (!(discoveryDetails.getString(Constants.PORT).isEmpty()))) {

                var credentialProfiles = discoveryDetails.getJsonArray(Constants.CREDENTIAL_IDS);

                for (Object credentialId : credentialProfiles)
                {
                    if (!Credential.credentialDatabase.verify(Long.parseLong(credentialId.toString())))
                    {
                        result.put(Constants.ERROR, "Wrong Credentials");

                        result.put(Constants.ERRORCODE, Constants.INCORRECTCREDENTIAL);

                        result.put(Constants.ERRORMESSAGE, "One of the credential Id is incorrect");

                        result.put(Constants.STATUS,Constants.FAIL);

                        LOGGER.info("Creation of discovery profile failed because one of the credential Id is incorrect");

                        return  result;

                    }
                }

                var id =discoveryDatabase.create(discoveryDetails);

                result.put(Constants.ID,id);

                result.put(Constants.STATUS,Constants.SUCCESS);

                result.put(Constants.ERRORCODE,Constants.SUCCESSCODE);

                LOGGER.info("Discovery profile created  successfully with id {}",id);


            } else {
                result.put(Constants.ERROR, "Empty Discovery field");

                result.put(Constants.ERRORCODE, Constants.EMPTYDISCVOERYFIELD);

                result.put(Constants.ERRORMESSAGE, "IP or Port or Credential Profiles are empty");

                result.put(Constants.STATUS,Constants.FAIL);

                LOGGER.info("Creation of discovery profile failed because either ip or port or credential field is empty");


            }
        } else {
            result.put(Constants.ERROR, "Empty Discovery");

            result.put(Constants.ERRORCODE, Constants.EMPTYDISCOVERY);

            result.put(Constants.ERRORMESSAGE, "IP or Port or Credential Profiles are not present");

            result.put(Constants.STATUS,Constants.FAIL);

            LOGGER.info("Creation of discovery profile failed because either ip or port profile is not present");

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

        result.put(Constants.ERRORCODE,Constants.SUCCESSCODE);

        return result;
    }


}
