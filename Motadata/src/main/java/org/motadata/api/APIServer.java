package org.motadata.api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import org.motadata.util.Utils;
import org.motadata.util.Constants;

import java.nio.Buffer;

public class APIServer extends AbstractVerticle {

    public static final Logger LOGGER = LoggerFactory.getLogger(APIServer.class);
    @Override
    public void start(Promise<Void> promise) {

        var port = (Integer) Utils.configMap.get(Constants.HTTPPORT);

        var host = (String) Utils.configMap.get(Constants.HTTPHOST);

        var httpServerOptions = new HttpServerOptions().setPort(port).setHost(host);

        var httpServer = vertx.createHttpServer(httpServerOptions);

        var router = Router.router(vertx);

        var eb = vertx.eventBus();

        router.post(Constants.CREDENTIALAPI).handler(ctx -> {

            ctx.request().bodyHandler(buffer -> {

                if (buffer.length() <= 0) {

                    var result = new JsonObject();

                    result.put(Constants.ERRORCODE, Constants.EMPTYBODY);

                    result.put(Constants.ERRORMESSAGE, "Request body is empty");

                    result.put(Constants.STATUS, Constants.FAIL);

                    result.put(Constants.ERROR, "Empty Body");

                    ctx.response().setStatusCode(400).end(result.toBuffer());


                } else {
                    try {
                        var credentialDetails = buffer.toJsonObject();

                        var result = Credential.createCredential(credentialDetails);

                        if (result.getString(Constants.STATUS).equals(Constants.FAIL)) {

                            ctx.response().setStatusCode(400).end(result.toBuffer());
                        } else {

                            ctx.json(result);
                        }
                    } catch (Exception exception) {

                        var response = new JsonObject();

                        response.put(Constants.STATUS,Constants.FAIL);

                        response.put(Constants.ERROR,"JSON ERROR");

                        response.put(Constants.ERRORCODE,Constants.INVALIDJSON);

                        response.put(Constants.ERRORMESSAGE,"Invalid JSON Format");

                        ctx.response().setStatusCode(400).end(response.toBuffer());

                        LOGGER.error(exception.getCause());
                    }
                }
            });


        });

        router.get(Constants.CREDENTIALAPI).handler(ctx -> {

            var result = Credential.getCredential();

            ctx.json(result);

        });
        router.put(Constants.CREDENTIALAPI).handler(ctx -> {


        });
        router.delete(Constants.CREDENTIALAPI).handler(ctx -> {


        });

        router.post(Constants.DISCOVERYAPI).handler(ctx -> {

            ctx.request().bodyHandler(buffer -> {

                if (buffer.length() <= 0) {

                    var result = new JsonObject();

                    result.put(Constants.ERRORCODE, Constants.EMPTYBODY);

                    result.put(Constants.ERRORMESSAGE, "Request body is empty");

                    result.put(Constants.STATUS, Constants.FAIL);

                    result.put(Constants.ERROR, "Empty Body");

                    ctx.response().setStatusCode(400).end(result.toBuffer());


                } else {

                    try {
                        var result = Discovery.createDiscovery(buffer.toJsonObject());

                        if (result.getString(Constants.STATUS).equals(Constants.FAIL)) {

                            ctx.response().setStatusCode(400).end(result.toBuffer());
                        }
                        else
                        {

                            ctx.json(result);
                        }

                    }catch (Exception exception) {

                        var response = new JsonObject();

                        response.put(Constants.STATUS,Constants.FAIL);

                        response.put(Constants.ERROR,"JSON ERROR");

                        response.put(Constants.ERRORCODE,Constants.INVALIDJSON);

                        response.put(Constants.ERRORMESSAGE,"Invalid JSON Format");

                        ctx.response().setStatusCode(400).end(response.toBuffer());

                        LOGGER.error(exception.getCause());
                    }


                }
            });

        });
        router.get(Constants.DISCOVERYAPI).handler(ctx -> {

            var result =Discovery.getDiscovery();

            ctx.json(result);

        });
        router.put(Constants.DISCOVERYAPI).handler(ctx -> {


        });
        router.delete(Constants.DISCOVERYAPI).handler(ctx -> {


        });
        router.post(Constants.DISCOVERYRUNAPI).handler(ctx -> {

            ctx.request().bodyHandler(buffer -> {

                if (buffer.length() <= 0) {

                    var result = new JsonObject();

                    result.put(Constants.ERRORCODE, Constants.EMPTYBODY);

                    result.put(Constants.ERRORMESSAGE, "Request body is empty");

                    result.put(Constants.STATUS, Constants.FAIL);

                    result.put(Constants.ERROR, "Empty Body");

                    ctx.response().setStatusCode(400).end(result.toBuffer());


                } else {

                    try {
                        var dataToSend = buffer.toJsonObject();

                      eb.request(Constants.DISCOVERYADDRESS,dataToSend,handler->{

                          if (handler.succeeded())
                          {
                              System.out.println(handler.result().body());

                              ctx.response().end(handler.result().body().toString());
                          }
                      });
                    }catch (Exception exception) {

                        var response = new JsonObject();

                        response.put(Constants.STATUS,Constants.FAIL);

                        response.put(Constants.ERROR,"JSON ERROR");

                        response.put(Constants.ERRORCODE,Constants.INVALIDJSON);

                        response.put(Constants.ERRORMESSAGE,"Invalid JSON Format");

                        ctx.response().setStatusCode(400).end(response.toBuffer());

                        LOGGER.error(exception.getCause());
                    }


                }
            });

        });

        router.post(Constants.PROVISION).handler(ctx -> {


        });


        httpServer.requestHandler(router).listen(8000);


        promise.complete();


    }
}
