package org.polleddatastore;

import io.vertx.core.Vertx;
import org.zeromq.SocketType;
import io.vertx.core.json.JsonObject;
import org.zeromq.ZContext;

public class Pusher
{

    public static void main(String[] args)
    {

        Vertx vertx = Vertx.vertx();

        try
        {
            ZContext context = new ZContext();

            var pusher = context.createSocket(SocketType.PUSH);

            pusher.bind("tcp://localhost:5585");

            Thread.sleep(100);

            for (int i=0;i<100;i++)
            {

                var object = new JsonObject();

                object.put("hello", i);

                pusher.send(object.encodePrettily(),0);

                System.out.println("data sent");



            }


        }
        catch (Exception e)
        {
            System.out.println(e);
        }


    }
}
