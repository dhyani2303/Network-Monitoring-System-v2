package org.motadata.database;


import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.motadata.util.Constants;
import org.motadata.util.Utils;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Database {

    public static final Logger LOGGER = LoggerFactory.getLogger(Database.class);

    private static HashMap<String,Database> instanceMap = new HashMap<>();

    private static ConcurrentMap<Long,Long> validCredentials = new ConcurrentHashMap<>();

    private static ConcurrentHashSet<Long> provisionedDevices = new ConcurrentHashSet<>();

    final ConcurrentMap<Long, JsonObject> DATA = new ConcurrentHashMap<>();


    private Database()
    {}

    public static void createDatabase(String name)
    {
        if (!(instanceMap.containsKey(name)))
        {
            Database database = new Database();

            instanceMap.put(name,database);

            LOGGER.info("New database is created for {}",name);
        }
        else
        {
            LOGGER.info("Database with name {} already exists",name);
        }


    }

    public static void addValidCredentials(Long discoveryId,Long credentialId)
    {
        validCredentials.put(discoveryId,credentialId);
    }

    public static boolean verifyId(Long id, String requestType)
    {
        if (requestType.equals(Constants.VERIFY_DISCOVERY_ID)) {

            return validCredentials.containsKey(id);
        }
     if (requestType.equals(Constants.VERIFY_PROVISION))
        {

            return provisionedDevices.contains(id);

        }
      return false;
    }


    public static void addProvisionDevice(long discoveryId)
    {
        provisionedDevices.add(discoveryId);
    }

    public static JsonArray getProvisionedDevices()
    {

        var devices = new JsonArray();

        for (long id : provisionedDevices)
        {
            devices.add(id);

        }

        return devices;
    }

    public static long getCredentialId(long discoveryId)
    {
        return validCredentials.get(discoveryId);
    }




    public static Database getDatabase(String name)
    {
        LOGGER.info("Get database request has been served for database {}",name);

        return instanceMap.get(name);

    }


    public long create(JsonObject dataToPut)
    {
      var id = Utils.getNewId();

      DATA.put(id,dataToPut);

        LOGGER.info("Data added to database and id is {}",id);

      return  id;

    }

    public JsonObject get(){

        JsonObject result = new JsonObject();

        DATA.forEach((key, value) ->
        {
            result.put(String.valueOf(key), value);
        });

        LOGGER.info("Get all the data has been served");

        return result.copy();

    }

    public JsonObject get(long id){

        var result = new JsonObject();

        result = DATA.get(id);

        LOGGER.info("Get the data for id {} has been served",id);

        return result.copy();
    }

    public boolean verify(long key)
    {
        LOGGER.info("Verification for the key {} is served",key);

        return DATA.containsKey(key);

    }
}
