package org.polleddatastore.utils;

import io.vertx.core.json.JsonObject;
import org.zeromq.SocketType;
import org.zeromq.ZContext;

import java.util.Base64;

public class Push {

    public static void main(String[] args)
    {
        try
        {
            ZContext context = new ZContext();

           var socket = context.createSocket(SocketType.PUSH);

           var pullSocket = context.createSocket(SocketType.PULL);

            socket.bind("tcp://localhost:5585");

            pullSocket.bind("tcp://localhost:5586");

            var object = new JsonObject().put("request.type","read.file").put("ip.address","192.168.12.30").put("timestamp","1585679400000");

            System.out.println(object.encodePrettily());

            socket.send(Base64.getEncoder().encodeToString(object.encodePrettily().getBytes()));

            new Thread(()->{

                while (true)
                {
                    var data = pullSocket.recv();

                    System.out.println(new String(data));
                }
            }).start();

        }
        catch (Exception exception)
        {
            System.out.println(exception);
        }

    }
}



