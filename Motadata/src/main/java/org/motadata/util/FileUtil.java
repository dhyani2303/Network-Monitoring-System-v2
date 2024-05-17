package org.motadata.util;

import java.time.LocalDateTime;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;

public class FileUtil {

    public static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    public static Future<Void> writeToFile(Vertx vertx, JsonObject data) {


        Promise<Void> promise = Promise.promise();

        var ip = data.getString(Constants.IP);

        var now = LocalDateTime.now();

        data.put(Constants.TIMESTAMP, now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        var fileName = ip + ".json";

        System.out.println(fileName);

        var buffer = Buffer.buffer(data.encodePrettily());

        var fileSystem = vertx.fileSystem();

        fileSystem.openBlocking(fileName,new OpenOptions().setAppend(true).setCreate(true))
                .write(buffer).onComplete(handler->
                {
                    LOGGER.info("Content written to file");

                    promise.complete();

                }).onFailure(handler->{


                    LOGGER.warn("Error occured while opening the file {}",handler.getCause());
                    promise.fail(handler.getCause());
                });





        return promise.future();
    }

}

