package org.motadata.database;

import java.util.concurrent.ConcurrentHashMap;

public class Provision extends AbstractDatabase
{
    private static Provision provision;

    private Provision()
    {
        super(new ConcurrentHashMap<>());
    }

    public static Provision getProvision()
    {
        if (provision==null)
        {
            provision = new Provision();
        }

        return provision;
    }
}
