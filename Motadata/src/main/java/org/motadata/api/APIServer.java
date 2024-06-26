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

        LOGGER.info("API server has been started");

        var port = (Integer) Utils.configMap.get(Constants.HTTP_PORT);

        var host = (String) Utils.configMap.get(Constants.HTTP_HOST);

        var httpServerOptions = new HttpServerOptions().setPort(port).setHost(host);

        var httpServer = vertx.createHttpServer(httpServerOptions);

        var router = Router.router(vertx);

        var eb = vertx.eventBus();

        router.post(Constants.CREDENTIAL_API).handler(ctx -> ctx.request().bodyHandler(buffer -> {

            LOGGER.info("Post request for  {} has come",Constants.CREDENTIAL_API);

            if (buffer.length() <= 0) {

              var result= errorHandler("Empty Body","Request body is empty",Constants.EMPTY_BODY);

                LOGGER.info("Post request  has been served for {} with result {}",Constants.CREDENTIAL_API,result);

                ctx.response().setStatusCode(400).end(result.toBuffer());


            } else {
                try {
                    var credentialDetails = buffer.toJsonObject();

                    var result = Credential.createCredential(credentialDetails);

                    if (result.getString(Constants.STATUS).equals(Constants.FAIL)) {

                        LOGGER.info("Post request  has been served for {} with result {}",Constants.CREDENTIAL_API,result);

                        ctx.response().setStatusCode(400).end(result.toBuffer());
                    } else {

                        LOGGER.info("Post request  has been served for {} with result {}",Constants.CREDENTIAL_API,result);


                        ctx.json(result);
                    }
                } catch (Exception exception) {

                    var result = errorHandler("JSON ERROR","Invalid JSON Format",Constants.INVALID_JSON);

                    ctx.response().setStatusCode(400).end(result.toBuffer());

                    LOGGER.info("Post request  has been served for {} with result {}",Constants.CREDENTIAL_API,result);

                   LOGGER.error(exception.getCause().toString());
                }
            }
        }));

        router.get(Constants.CREDENTIAL_API).handler(ctx -> {

            LOGGER.info("Get request for {} has come",Constants.CREDENTIAL_API);

            var result = Credential.getCredential();

            LOGGER.info("Get request for {} has been served with result {}",Constants.CREDENTIAL_API,result);

            ctx.json(result);

        });

        router.get(Constants.CREDENTIAL_API_WITH_PARAMS).handler(ctx -> {

            LOGGER.info("Get request for {} has come",Constants.CREDENTIAL_API_WITH_PARAMS);

            var id = ctx.pathParam(Constants.ID);

            var result = Credential.getCredential(id);

            if (result.getString(Constants.STATUS).equals(Constants.FAIL)) {

                LOGGER.info("Get request for {} has been served with result {}",Constants.CREDENTIAL_API_WITH_PARAMS,result);

                ctx.response().setStatusCode(400).end(result.toBuffer());
            } else {

                LOGGER.info("Get request for {} has been served with result {}",Constants.CREDENTIAL_API_WITH_PARAMS,result);

                ctx.json(result);
            }

        });

        router.put(Constants.CREDENTIAL_API_WITH_PARAMS).handler(ctx -> {

            LOGGER.info("Put request for {} has come",Constants.CREDENTIAL_API_WITH_PARAMS);

            var id = ctx.pathParam(Constants.ID);

            ctx.request().bodyHandler(buffer->{

                if (buffer.length() <= 0) {

                    var result= errorHandler("Empty Body","Request body is empty",Constants.EMPTY_BODY);

                    LOGGER.info("Put request for {} has been served with result {}",Constants.CREDENTIAL_API_WITH_PARAMS,result);

                    ctx.response().setStatusCode(400).end(result.toBuffer());

                }
                else
                {

                    try {
                        var credentialDetails = buffer.toJsonObject();

                        var result = Credential.updateCredential(credentialDetails,id);

                        if (result.getString(Constants.STATUS).equals(Constants.FAIL)) {

                            LOGGER.info("Put request for {} has been served with result {}",Constants.CREDENTIAL_API_WITH_PARAMS,result);

                            ctx.response().setStatusCode(400);

                            ctx.json(result);
                        } else {

                            LOGGER.info("Put request for {} has been served with result {}",Constants.CREDENTIAL_API_WITH_PARAMS,result);


                            ctx.json(result);
                        }
                    } catch (Exception exception) {

                        var result = errorHandler("JSON ERROR","Invalid JSON Format",Constants.INVALID_JSON);

                        LOGGER.info("Put request for {} has been served with result {}",Constants.CREDENTIAL_API_WITH_PARAMS,result);

                        ctx.response().setStatusCode(400).end(result.toBuffer());

                        LOGGER.error(exception.getCause().toString());
                    }
                }

            });

        });

        router.delete(Constants.CREDENTIAL_API_WITH_PARAMS).handler(ctx -> {

            LOGGER.info("Delete request for {} has come",Constants.CREDENTIAL_API_WITH_PARAMS);


            var id = ctx.pathParam(Constants.ID);

            var result = Credential.deleteCredential(id);

            if (result.getString(Constants.STATUS).equals(Constants.FAIL))
            {
                LOGGER.info("Delete request for {} has been served with result {}",Constants.CREDENTIAL_API_WITH_PARAMS,result);

                ctx.response().setStatusCode(400);

                ctx.json(result);
            }
            else
            {
                LOGGER.info("Delete request for {} has been served with result {}",Constants.CREDENTIAL_API_WITH_PARAMS,result);

                ctx.json(result);
            }



        });

        router.post(Constants.DISCOVERY_API).handler(ctx -> ctx.request().bodyHandler(buffer -> {

            LOGGER.info("Post request has come for {}",Constants.DISCOVERY_API);

            if (buffer.length() <= 0) {


                var result= errorHandler("Empty Body","Request body is empty",Constants.EMPTY_BODY);

                LOGGER.info("Post request  has been served for {} with result {}",Constants.DISCOVERY_API,result);

                ctx.response().setStatusCode(400).end(result.toBuffer());


            } else {

                try {
                    var result = Discovery.createDiscovery(buffer.toJsonObject());

                    if (result.getString(Constants.STATUS).equals(Constants.FAIL)) {

                        LOGGER.info("Post request  has been served for {} with result {}",Constants.DISCOVERY_API,result);

                        ctx.response().setStatusCode(400).end(result.toBuffer());
                    }
                    else
                    {
                        LOGGER.info("Post request  has been served for {} with result {}",Constants.DISCOVERY_API,result);


                        ctx.json(result);
                    }

                }catch (Exception exception) {


                    var result = errorHandler("JSON ERROR","Invalid JSON Format",Constants.INVALID_JSON);

                    LOGGER.info("Post request  has been served for {} with result {}",Constants.DISCOVERY_API,result);

                    ctx.response().setStatusCode(400).end(result.toBuffer());

                    LOGGER.error(exception.getCause().toString());
                }


            }
        }));

        router.get(Constants.DISCOVERY_API).handler(ctx -> {

            LOGGER.info("Get request has come for {}",Constants.DISCOVERY_API);
            var result =Discovery.getDiscovery();

            LOGGER.info("Get request  has been served for {} with result {}",Constants.DISCOVERY_API,result);

            ctx.json(result);

        });

        router.get(Constants.DISCOVERY_API_WITH_PARAMS).handler(ctx -> {

            LOGGER.info("Get request has come for {}",Constants.DISCOVERY_API_WITH_PARAMS);

            var id = ctx.pathParam(Constants.ID);

            var result = Discovery.getDiscovery(id);

            if (result.getString(Constants.STATUS).equals(Constants.FAIL)) {

                LOGGER.info("Get request  has been served for {} with result {}",Constants.DISCOVERY_API_WITH_PARAMS,result);

                ctx.response().setStatusCode(400).end(result.toBuffer());
            }
            else
            {
                LOGGER.info("Get request  has been served for {} with result {}",Constants.DISCOVERY_API_WITH_PARAMS,result);

                ctx.json(result);
            }

        });

        router.put(Constants.DISCOVERY_API_WITH_PARAMS).handler(ctx -> {

            LOGGER.info("Put request has come for {}",Constants.DISCOVERY_API_WITH_PARAMS);

            var id = ctx.pathParam(Constants.ID);

            ctx.request().bodyHandler(buffer->{

                if (buffer.length() <= 0) {

                    var result= errorHandler("Empty Body","Request body is empty",Constants.EMPTY_BODY);

                    LOGGER.info("Put request  has been served for {} with result {}",Constants.DISCOVERY_API_WITH_PARAMS,result);

                    ctx.response().setStatusCode(400).end(result.toBuffer());

                }
                else
                {

                    try {
                        var discoveryDetails = buffer.toJsonObject();

                        var result = Discovery.updateDiscovery(discoveryDetails,id);

                        if (result.getString(Constants.STATUS).equals(Constants.FAIL)) {

                            LOGGER.info("Put request  has been served for {} with result {}",Constants.DISCOVERY_API_WITH_PARAMS,result);

                            ctx.response().setStatusCode(400).end(result.toBuffer());
                        }
                        else
                        {
                            LOGGER.info("Put request  has been served for {} with result {}",Constants.DISCOVERY_API_WITH_PARAMS,result);

                            ctx.json(result);
                        }
                    } catch (Exception exception) {

                        var result = errorHandler("JSON ERROR","Invalid JSON Format",Constants.INVALID_JSON);

                        LOGGER.info("Put request  has been served for {} with result {}",Constants.DISCOVERY_API_WITH_PARAMS,result);

                        ctx.response().setStatusCode(400).end(result.toBuffer());

                        LOGGER.error(exception.getCause().toString());
                    }
                }

            });

        });

        router.delete(Constants.DISCOVERY_API_WITH_PARAMS).handler(ctx -> {

            LOGGER.info("Delete request has come for {}",Constants.DISCOVERY_API_WITH_PARAMS);

            var id = ctx.pathParam(Constants.ID);

            var result = Discovery.deleteDiscovery(id);

            if (result.getString(Constants.STATUS).equals(Constants.FAIL))
            {
                LOGGER.info("Delete request  has been served for {} with result {}",Constants.DISCOVERY_API_WITH_PARAMS,result);

                ctx.response().setStatusCode(400).end(result.toBuffer());
            }
            else
            {
                LOGGER.info("Delete request  has been served for {} with result {}",Constants.DISCOVERY_API_WITH_PARAMS,result);

                ctx.json(result);
            }

        });

        router.post(Constants.DISCOVERY_RUN_API).handler(ctx -> ctx.request().bodyHandler(buffer -> {

            LOGGER.info("Post request has come for {}",Constants.DISCOVERY_RUN_API);

            if (buffer.length() <= 0) {

                var result= errorHandler("Empty Body","Request body is empty",Constants.EMPTY_BODY);

                LOGGER.info("Post request  has been served for {} with result {}",Constants.DISCOVERY_RUN_API,result);

                ctx.response().setStatusCode(400).end(result.toBuffer());


            } else {

                try {
                    var dataToSend = buffer.toJsonObject();

                  eb.request(Constants.DISCOVERY_ADDRESS,dataToSend, handler->{

                      if (handler.succeeded())
                      {
                          ctx.json(handler.result().body());

                          LOGGER.info("Post request  has been served  for {} with result {}", Constants.DISCOVERY_RUN_API,handler.result().body());

                      }
                  });
                }catch (Exception exception) {

                    var result = errorHandler("JSON ERROR","Invalid JSON Format",Constants.INVALID_JSON);

                    ctx.response().setStatusCode(400).end(result.toBuffer());

                    LOGGER.info("Post request  has been served for {} with result {}",Constants.DISCOVERY_RUN_API,result);

                    LOGGER.error(exception.getCause().toString());
                }


            }
        }));

        router.post(Constants.PROVISION).handler(ctx -> {

            LOGGER.info("Post request has come for {}",Constants.PROVISION);

            ctx.request().bodyHandler(buffer -> {


                if (buffer.length() <= 0) {

                    var result = errorHandler("Empty Body", "Request body is empty", Constants.EMPTY_BODY);

                    LOGGER.info("Post request  has been served for {} with result {}",Constants.PROVISION,result);

                    ctx.response().setStatusCode(400).end(result.toBuffer());

                } else {

                    try {
                        var dataToSend = buffer.toJsonObject();

                     var result=   Provision.provisionDevice(dataToSend);

                     if (result.getString(Constants.STATUS).equals(Constants.FAIL))
                     {
                         LOGGER.info("Post request  has been served for {} with result {}",Constants.PROVISION,result);

                         ctx.response().setStatusCode(400).end(result.toBuffer());

                     }
                     else
                     {
                         LOGGER.info("Post request  has been served for {} with result {}",Constants.PROVISION,result);

                         ctx.json(result);

                     }


                    } catch (Exception exception) {

                        var result = errorHandler("JSON ERROR", "Invalid JSON Format", Constants.INVALID_JSON);

                        ctx.response().setStatusCode(400).end(result.toBuffer());

                        LOGGER.info("Post request  has been served for {} with result {}",Constants.PROVISION,result);

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

         result.put(Constants.ERROR_MESSAGE,errorMessage);

         result.put(Constants.ERROR_CODE,errorCode);

         result.put(Constants.STATUS,Constants.FAIL);


         return result;
     }
}
