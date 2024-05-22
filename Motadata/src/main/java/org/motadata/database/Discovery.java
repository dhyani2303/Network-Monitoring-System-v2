package org.motadata.database;

import io.vertx.core.json.JsonObject;

import java.util.concurrent.ConcurrentHashMap;

public class Discovery extends AbstractDatabase
{
    private static Discovery discovery;

    private Discovery()
    {
       super(new ConcurrentHashMap<>());
    }

    public static Discovery getDiscovery()
    {
        if (discovery == null)
        {
            discovery = new Discovery();
        }
        return discovery;

    }
}
