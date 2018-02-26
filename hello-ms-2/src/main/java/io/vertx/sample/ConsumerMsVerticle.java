package io.vertx.sample;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.client.HttpRequest;
import io.vertx.rxjava.ext.web.client.HttpResponse;
import io.vertx.rxjava.ext.web.client.WebClient;
import io.vertx.rxjava.ext.web.codec.BodyCodec;
import rx.Single;

public class ConsumerMsVerticle extends AbstractVerticle {

    private WebClient client;

    @Override
    public void start() {
        client = WebClient.create(vertx);

        Router router = Router.router(vertx);
        router.get("/").handler(this::invokeProducer);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(8081);
    }

    private void invokeProducer( RoutingContext ctx) {
        HttpRequest<JsonObject> request1 = client.get(8080, "localhost", "/test1").as( BodyCodec.jsonObject());
        HttpRequest<JsonObject> request2 = client.get(8080, "localhost", "/test2").as( BodyCodec.jsonObject());

        // assemble results, handle errors
        Single<JsonObject> s1 = request1.rxSend().map( HttpResponse::body);
        Single<JsonObject> s2 = request2.rxSend().map( HttpResponse::body);
        Single.zip(s1, s2, (test1, test2) -> new JsonObject()
                .put("test1", test1.getString("message"))
                .put("test2", test2.getString("message")) )
                .subscribe(
                        result -> ctx.response().end(result.encodePrettily()),
                        error -> {
                            error.printStackTrace();
                            ctx.response().setStatusCode(500).end(error.getMessage());
                        });
    }

}
