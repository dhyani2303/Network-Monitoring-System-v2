package org.motadata;

import io.vertx.core.json.JsonObject;
import org.zeromq.SocketType;
import org.zeromq.ZContext;

import java.util.Base64;

public class example
{
    public static void main(String[] args) throws InterruptedException
    {
        var encodedContext = "WwogIHsKICAgICJpcC5hZGRyZXNzIjogIjE3Mi4xNi44LjExMyIsCiAgICJwb3J0IjogIjU5ODUiLCAKICAgICJ1c2VyLm5hbWUiOiAiZGh2YW5pIiwKICAgICAgInBhc3N3b3JkIjogIk1pbmRAMTIzIiwKICAgICAicmVxdWVzdC50eXBlIjogImNvbGxlY3QiCiAgfQpd";

        var zContext = new ZContext();

        var pushSocket = zContext.createSocket(SocketType.PUSH);

        pushSocket.bind("tcp://localhost:5587");

        Thread.sleep(4000);

        pushSocket.send(encodedContext);

        var pullSocket = zContext.createSocket(SocketType.PULL);

        pullSocket.bind("tcp://localhost:5588");

        new Thread(() ->
        {
           var message = new String(pullSocket.recv(0));

            System.out.println(message);

        }).start();
    }
}