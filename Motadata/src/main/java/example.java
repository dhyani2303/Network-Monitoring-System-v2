import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

public class example
{
    public static void main(String[] args)
    {

        anotherMethod().onComplete(i->{

            System.out.println("success");

        }).onFailure(h->{
            System.out.println("Failure");
        });

    }


    public static Future<Boolean> anotherMethod(){

         Promise<Boolean> promise = Promise.promise();

        if (1>0)
        {
            promise.complete(true);

        }

        promise.fail("fail");

        return  promise.future();
    }
}
