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

    private final ConcurrentMap<Long, JsonObject> items = new ConcurrentHashMap<>();

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
            LOGGER.error("Exception occurred in createDatabase method",exception);
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
            LOGGER.error("Some exception occurred in function create() ",exception);

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
            LOGGER.error("Some exception occurred while serving in the function get() ",exception);

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
            LOGGER.error("Exception occurred in get(id) method",exception);

            return null;

        }
    }

    public void update(JsonObject updateData,long id)
    {
        try
        {
            var previousData = items.get(id);

            var updatedData = updateData.getMap();

            var keySet = updatedData.keySet();

            for (var key : keySet)
            {
                previousData.put(key, updatedData.get(key));
            }
        }
        catch (Exception exception)
        {
            LOGGER.error("Exception occurred in update method",exception);
        }
    }

    public boolean delete(long id)
    {
        try
        {
            items.remove(id);

            return true;
        }
        catch (Exception exception)
        {
            LOGGER.info("Some exception has occurred in delete method",exception);

            return false;
        }
    }

    public boolean verify(long key)
    {
        try
        {
            LOGGER.info("Verification for the key {} is served", key);

            return items.containsKey(key);
        }
        catch (Exception exception)
        {
            LOGGER.error("Exception occurred in verify method",exception);

            return false;

        }
    }

    public boolean verify(String field,String name)
    {
        try
        {
            var values = items.values();

            for (var value : values)
            {
                if (name.equals(value.getString(field)))
                {
                    return true;
                }

            }
            return false;

        }
        catch (Exception exception)
        {
            LOGGER.error("Exception occurred in verify(String) method ",exception);

            return false;
        }
    }
}
