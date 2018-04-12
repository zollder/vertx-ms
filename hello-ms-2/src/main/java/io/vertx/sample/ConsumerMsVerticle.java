package io.vertx.sample;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.Status;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.healthchecks.HealthCheckHandler;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.client.HttpRequest;
import io.vertx.rxjava.ext.web.client.HttpResponse;
import io.vertx.rxjava.ext.web.client.WebClient;
import io.vertx.rxjava.ext.web.codec.BodyCodec;
import io.vertx.rxjava.servicediscovery.ServiceDiscovery;
import io.vertx.rxjava.servicediscovery.types.HttpEndpoint;
import rx.Single;

public class ConsumerMsVerticle extends AbstractVerticle {

    private boolean started;
    private WebClient webClient;

    @Override
    public void start() {
        Router router = Router.router(vertx);
        router.get("/").handler(this::invokeProducer);
        router.get("/health").handler(handleHealthCheck());

        // find vertx-ms-1 microservice with service discovery
        ServiceDiscovery.create(vertx, discovery -> {
            Single<WebClient> single = HttpEndpoint.rxGetWebClient(discovery,
                                                                   rec -> rec.getName().equalsIgnoreCase("vertx-ms-1"),
                                                                   new JsonObject().put("keepAlive", false));
            // configure client and start HTTP server
            single.subscribe(
                    client -> {
                        this.webClient = client;
                        vertx.createHttpServer()
                                .requestHandler(router::accept)
                                .listen(8080);
                        started = true;
                    },
                    err -> {
                        System.out.println("Oh no, no service");
                        started = false;
                    }
            );
        });
    }

    private void invokeProducer( RoutingContext ctx) {
        HttpRequest<JsonObject> request1 = webClient.get("/test1").as( BodyCodec.jsonObject());
        HttpRequest<JsonObject> request2 = webClient.get("/test2").as( BodyCodec.jsonObject());

        // assemble results, handle errors
        Single<HttpResponse<JsonObject>> s1 = request1.rxSend();
        Single<HttpResponse<JsonObject>> s2 = request2.rxSend();
        Single.zip(s1, s2, (test1, test2) -> new JsonObject()
                .put("test1", test1.body().getString("message") + " " + test1.body().getString("served-by"))
                .put("test2", test2.body().getString("message") + " " + test2.body().getString("served-by")))
                .subscribe(
                        result -> ctx.response().end(result.encodePrettily()),
                        error -> {
                            error.printStackTrace();
                            ctx.response().setStatusCode(500).end(error.getMessage());
                        });
    }

    private HealthCheckHandler handleHealthCheck() {
        HealthCheckHandler handler = HealthCheckHandler.create(vertx);
        handler.register("vertx-ms-1-running", future -> {
            future.complete(started ? Status.OK() : Status.KO());
        });
        return handler;
    }
}
