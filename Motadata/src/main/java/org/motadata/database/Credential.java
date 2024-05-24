package org.motadata.database;

import java.util.concurrent.ConcurrentHashMap;


public class Credential extends AbstractDatabase
{
    private static Credential credential;

    private Credential()
    {
        super(new ConcurrentHashMap<>());
    }

    public static Credential getCredential()
    {

        if (credential == null)
        {
            credential = new Credential();
        }
        return credential;
    }

}

