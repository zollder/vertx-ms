package io.vertx.sample;

import java.util.concurrent.TimeUnit;

import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.RxHelper;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.eventbus.Message;
import rx.Single;

public class InvokerBusMsVerticle extends AbstractVerticle {

    @Override
    public void start() {
        vertx.createHttpServer()
                .requestHandler(
                        request -> {
                            EventBus bus = vertx.eventBus();
                            Single<JsonObject> obs1 = bus.<JsonObject>rxSend("hello", "test1")
                                    .subscribeOn(RxHelper.scheduler(vertx))
                                    .timeout(3, TimeUnit.SECONDS)
                                    .retry()
                                    .map( Message::body);
                            Single<JsonObject> obs2 = bus.<JsonObject>rxSend("hello", "test2")
                                    .subscribeOn(RxHelper.scheduler(vertx))
                                    .timeout( 3, TimeUnit.SECONDS)
                                    .retry()
                                    .map( Message::body);
                            Single.zip(obs1, obs2, (test1, test2) -> new JsonObject()
                                    .put("test1", test1.getString("message"))
                                    .put("test2", test2.getString("message")))
                                    .subscribe(
                                            success -> request.response().end(success.encodePrettily()),
                                            error -> {
                                                error.printStackTrace();
                                                request.response().setStatusCode(500).end(error.getMessage());
                                            });
                        })
                .listen(8082);
    }
}
