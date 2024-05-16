package org.motadata.api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import org.motadata.util.Utils;
import org.motadata.util.Constants;


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

        router.post(Constants.CREDENTIALAPI).handler(ctx -> ctx.request().bodyHandler(buffer -> {

            if (buffer.length() <= 0) {


              var result= errorHandler("Empty Body","Request body is empty",Constants.EMPTYBODY);

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

                    var result = errorHandler("JSON ERROR","Invalid JSON Format",Constants.INVALIDJSON);

                    ctx.response().setStatusCode(400).end(result.toBuffer());

                   LOGGER.error(exception.getCause().toString());
                }
            }
        }));

        router.get(Constants.CREDENTIALAPI).handler(ctx -> {

            var result = Credential.getCredential();

            ctx.json(result);

        });
        router.put(Constants.CREDENTIALAPI).handler(ctx -> {


        });
        router.delete(Constants.CREDENTIALAPI).handler(ctx -> {


        });

        router.post(Constants.DISCOVERYAPI).handler(ctx -> ctx.request().bodyHandler(buffer -> {


            if (buffer.length() <= 0) {


                var result= errorHandler("Empty Body","Request body is empty",Constants.EMPTYBODY);

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


                    var result = errorHandler("JSON ERROR","Invalid JSON Format",Constants.INVALIDJSON);

                    ctx.response().setStatusCode(400).end(result.toBuffer());

                    LOGGER.error(exception.getCause().toString());
                }


            }
        }));
        router.get(Constants.DISCOVERYAPI).handler(ctx -> {

            var result =Discovery.getDiscovery();

            ctx.json(result);

        });
        router.put(Constants.DISCOVERYAPI).handler(ctx -> {


        });
        router.delete(Constants.DISCOVERYAPI).handler(ctx -> {


        });
        router.post(Constants.DISCOVERYRUNAPI).handler(ctx -> ctx.request().bodyHandler(buffer -> {

            if (buffer.length() <= 0) {

                var result= errorHandler("Empty Body","Request body is empty",Constants.EMPTYBODY);

                ctx.response().setStatusCode(400).end(result.toBuffer());


            } else {

                try {
                    var dataToSend = buffer.toJsonObject();

                  eb.request(Constants.DISCOVERYADDRESS,dataToSend,handler->{

                      if (handler.succeeded())
                      {
                          ctx.response().end(handler.result().body().toString());
                      }
                  });
                }catch (Exception exception) {

                    var result = errorHandler("JSON ERROR","Invalid JSON Format",Constants.INVALIDJSON);

                    ctx.response().setStatusCode(400).end(result.toBuffer());

                    LOGGER.error(exception.getCause().toString());
                }


            }
        }));

        router.post(Constants.PROVISION).handler(ctx -> {

            ctx.request().bodyHandler(buffer -> {


                if (buffer.length() <= 0) {

                    var result = errorHandler("Empty Body", "Request body is empty", Constants.EMPTYBODY);

                    ctx.response().setStatusCode(400).end(result.toBuffer());

                } else {

                    try {
                        var dataToSend = buffer.toJsonObject();

                     var result=   Provision.provisionDevice(dataToSend);

                     if (result.getString(Constants.STATUS).equals(Constants.FAIL))
                     {
                         ctx.response().setStatusCode(400).end(result.toBuffer());

                     }
                     else
                     {
                         ctx.json(result);

                     }


                    } catch (Exception exception) {

                        var result = errorHandler("JSON ERROR", "Invalid JSON Format", Constants.INVALIDJSON);

                        ctx.response().setStatusCode(400).end(result.toBuffer());

                        LOGGER.error(exception.getCause().toString());
                    }


                }
            });
        });


        httpServer.requestHandler(router).listen();


        promise.complete();


    }

     JsonObject errorHandler(String error,String errorMessage,String errorCode)
     {
         JsonObject result = new JsonObject();

         result.put(Constants.ERROR,error);

         result.put(Constants.ERRORMESSAGE,errorMessage);

         result.put(Constants.ERRORCODE,errorCode);

         result.put(Constants.STATUS,Constants.FAIL);


         return result;
     }
}
