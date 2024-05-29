import io.vertx.core.Vertx;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
public class APITest
{
    @Test
    void testCredentialAPI(Vertx vertx, VertxTestContext testContext)
    {
        var client = vertx.createHttpClient();

        var obj = new JsonObject();

        obj.put("user.name","dhvani");
        obj.put("password","Mind@123");
        obj.put("credential.profile.name","dhvani");

        client.request(HttpMethod.GET, 8000, "localhost","/credential/").compose(HttpClientRequest ->
                        HttpClientRequest.send().compose(HttpClientResponse::body)
                )
                .onComplete(testContext.succeeding(buffer -> testContext.verify(() ->
                {
                    assertFalse(buffer.toJsonObject().isEmpty());

                })));

        client.request(HttpMethod.POST,8000,"localhost","/credential/").compose(httpClientRequest ->

                httpClientRequest.send(obj.encodePrettily()).compose(HttpClientResponse::body)

                ).onComplete(testContext.succeeding(buffer -> testContext.verify(()->{

                    assertFalse(buffer.toJsonObject().isEmpty());

            testContext.completeNow();
        })));
    }


}

