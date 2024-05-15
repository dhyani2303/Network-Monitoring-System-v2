package org.motadata.database;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import org.motadata.api.APIServer;
import org.motadata.util.Utils;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Database {

    public static final Logger LOGGER = LoggerFactory.getLogger(Database.class);

    private static HashMap<String,Database> instanceMap = new HashMap<>();

    final ConcurrentMap<Long, JsonObject> DATA = new ConcurrentHashMap<>();


    private Database()
    {}

    public static void createDatabase(String name)
    {
        if (!(instanceMap.containsKey(name)))
        {
            instanceMap.put(name,new Database());

            LOGGER.info("New database is created for {}"+name);
        }
        else
        {
            LOGGER.info("Database with name {} already exists"+name);
        }


    }

    public static Database getDatabase(String name)
    {
        return instanceMap.getOrDefault(name, null);

    }

    public long create(JsonObject dataToPut)
    {
      var id = Utils.getNewId();

      DATA.put(id,dataToPut);

      return  id;

    }

    public JsonObject get(){

        JsonObject result = new JsonObject();

        DATA.forEach((key, value) ->
        {
            result.put(String.valueOf(key), value);
        });

        return result;
    }

    public JsonObject get(long id){

      var result = DATA.get(id);

        return result;
    }

    public boolean verify(long key)
    {
        return DATA.containsKey(key);

    }
}
