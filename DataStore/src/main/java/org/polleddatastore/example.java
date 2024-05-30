package org.polleddatastore;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.polleddatastore.constants.Constants;
import org.polleddatastore.utils.Util;
import org.zeromq.SocketType;
import org.zeromq.ZContext;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;

public class example
{
    public static void main(String[] args)
    {
//        Vertx vertx = Vertx.vertx();
//
//        String desiredTimestamp = "1717045200000";
//
//        try
//        {
//            vertx.fileSystem().readFile("result/172.16.8.113.json").onComplete(handler->{
//
//                if (handler.succeeded())
//                {
//                   for(var key:(handler.result().toJsonObject().fieldNames()))
//                   {
//                       var keytoLong = Long.parseLong(key);
//
//                       if (keytoLong>=Long.parseLong(desiredTimestamp))
//                       {
//                           System.out.println(handler.result().toJsonObject().getValue(key));
//                       }
//
//
//                   }
//                }
//                else
//                {
//                    System.out.println("Unable to read the file"+handler.cause());
//                }
//            });
//
//
//
//        }
//        catch (Exception exception)
//        {
//            System.out.println(exception.getMessage());
//        }
//
        try
        {
            var context = new ZContext();

            var socket = context.createSocket(SocketType.DEALER);

            var data = new JsonObject();

            data.put(Constants.TIMESTAMP,"1717045200000");

            data.put(Constants.IP_ADDRESS,"172.16.8.113");

            socket.bind("tcp://localhost:5586");

            socket.send(Base64.getEncoder().encodeToString(data.encode().getBytes()));

            new Thread(() ->
            {
                while (true)
                {
                    var bytes = socket.recv();

                    var string = new String(bytes);

                    var result = Base64.getDecoder().decode(bytes);

                    System.out.println("Result: "+ new String(result) );
                }
            }).start();

        }
        catch (Exception exception)
        {
            System.out.println(exception.fillInStackTrace());
        }


    }
}
//
//var asyncFile = vertx.fileSystem().openBlocking("result/192.168.2.230.json", new OpenOptions().setCreate(false));
//
//            asyncFile.read(buffer, 0, 0, 1024 * 3, handler ->
//        {
//        if (handler.succeeded())
//        {
//        System.out.println(buffer);
//                }
//                        else
//                        {
//                        System.out.println(handler.cause().toString());
//        }
//
//        });
