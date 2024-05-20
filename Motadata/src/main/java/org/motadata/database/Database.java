package org.motadata.database;


import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.motadata.constants.Constants;
import org.motadata.util.Utils;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Database {

    public static final Logger LOGGER = LoggerFactory.getLogger(Database.class);

    private static final HashMap<String,Database> instances = new HashMap<>();

    final ConcurrentMap<Long, JsonObject> items = new ConcurrentHashMap<>();

    private Database()
    {}

    public static void createDatabase(String name)
    {
        try
        {
            if (!(instances.containsKey(name)))
            {
                Database database = new Database();

                instances.put(name, database);

                LOGGER.info("New database is created for {}", name);
            }
            else
            {
                LOGGER.info("Database with name {} already exists", name);
            }
        }
        catch (Exception exception)
        {
            LOGGER.error("Exception occurred in createDatabase method {}",exception.getCause().toString());
        }
    }

    public static Database getDatabase(String name)
    {
        LOGGER.info("Get database request has been served for database {}",name);

        return instances.get(name);

    }

    public long create(JsonObject object)
    {
        try
        {
            var id = Utils.getNewId();

            items.put(id, object);

            LOGGER.info("Data added to database and id is {}", id);

            return id;
        }
        catch (Exception exception)
        {
            LOGGER.error("Some exception occurred in function create () {}",exception.getCause().toString());

            return -1;
        }
    }

    public JsonArray get()
    {
        try
        {
            var result = new JsonArray();

            items.forEach((key, value) ->
            {
                value.put(Constants.ID, key);

                result.add(value);
            });

            LOGGER.info("Get all the data has been served");

            return result.copy();
        }
        catch (Exception exception)
        {
            LOGGER.error("Some exception occurred while serving in the function get() {}",exception.getCause().toString());

            return null;
        }

    }

    public JsonObject get(long id)
    {
        try
        {
            var result = new JsonObject();

            result = items.get(id);

            LOGGER.info("Get the data for id {} has been served", id);

            return result.copy();
        }
        catch (Exception exception)
        {
            LOGGER.error("Exception occurred in get(id) method {}",exception.getCause().toString());

            return null;

        }
    }

    public void update(JsonObject updateData,long id)
    {
        try {
            var previousData = items.get(id);

            var updatedData = updateData.getMap();

            var keySet = updatedData.keySet();

            for (var key : keySet) {
                previousData.put(key, updatedData.get(key));
            }

        }
        catch (Exception exception)
        {
            LOGGER.error("Exception occurred in update method {}",exception.getCause().toString());


        }    }

//    public boolean delete(long id)
//    {
//        //for discovery id present in provisioned devices
//        if (provisionedDevices.contains(id))
//        {
//            return false;
//        }
//        else
//        {
//          //to delete credential id
//            var discoveredDevices = validCredentials.entrySet();
//
//            for ( var validCredential : discoveredDevices)
//            {
//                if (validCredential.getValue().equals(id))
//                {
//                    if (provisionedDevices.contains(validCredential.getKey()))
//                    {
//                        return false;
//                    }
//                    else
//                    {
//                        validCredentials.remove(validCredential.getKey());
//
//                        items.remove(id);
//
//                        return true;
//                    }
//                }
//
//            }
//            //to delete discovery id
//            if (validCredentials.containsKey(id) )
//            {
//                validCredentials.remove(id);
//            }
//            items.remove(id);
//
//            return true;
//
//
//        }
//    }

    public boolean verify(long key)
    {
        try
        {
            LOGGER.info("Verification for the key {} is served", key);

            return items.containsKey(key);
        }
        catch (Exception exception)
        {
            LOGGER.error("Exception occurred in verify method {}",exception.getCause().toString());

            return false;

        }
    }

    public boolean verify(String name)
    {
        try
        {
            var values = items.values();

            for (var value : values)
            {
                if (name.equals(value.getString(Constants.NAME)))
                {
                    return true;
                }

            }
            return false;

        }
        catch (Exception exception)
        {
            LOGGER.error("Exception occurred in verify(String) method {}",exception.getCause().toString());

            return false;

        }


    }
}
